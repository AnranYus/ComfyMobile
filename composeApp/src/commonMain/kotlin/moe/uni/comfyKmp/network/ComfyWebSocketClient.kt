package moe.uni.comfyKmp.network

import io.ktor.client.HttpClient
import io.ktor.client.plugins.websocket.webSocket
import io.ktor.websocket.Frame
import io.ktor.websocket.WebSocketSession
import io.ktor.websocket.close
import io.ktor.websocket.readText
import io.ktor.utils.io.errors.IOException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import moe.uni.comfyKmp.data.ComfyJson
import moe.uni.comfyKmp.data.WsMessage
import kotlin.math.min
import kotlin.random.Random

data class ReconnectPolicy(
    val maxAttempts: Int,
    val initialDelayMs: Long,
    val maxDelayMs: Long,
    val jitterMs: Long
)

enum class WsConnectionStatus {
    IDLE,
    CONNECTING,
    CONNECTED,
    RECONNECTING,
    FAILED,
    DISCONNECTED
}

data class WsConnectionState(
    val status: WsConnectionStatus,
    val attempt: Int = 0,
    val maxAttempts: Int = 0
)

class ComfyWebSocketClient(
    private val httpClient: HttpClient
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val _events = MutableSharedFlow<WsMessage>(extraBufferCapacity = 64)
    val events: SharedFlow<WsMessage> = _events
    private val _connectionState = MutableStateFlow(WsConnectionState(WsConnectionStatus.IDLE))
    val connectionState: StateFlow<WsConnectionState> = _connectionState.asStateFlow()

    private var session: WebSocketSession? = null
    private var connectJob: Job? = null
    private var shouldReconnect = false

    fun connect(
        baseUrl: String,
        clientId: String,
        policy: ReconnectPolicy = DEFAULT_RECONNECT_POLICY
    ) {
        if (connectJob?.isActive == true) return
        shouldReconnect = true
        val wsUrl = buildWsUrl(baseUrl, clientId)
        connectJob = scope.launch {
            val maxAttempts = policy.maxAttempts
            var retryCount = 0
            try {
                while (isActive && shouldReconnect) {
                    val status = if (retryCount == 0) {
                        WsConnectionStatus.CONNECTING
                    } else {
                        WsConnectionStatus.RECONNECTING
                    }
                    _connectionState.value = WsConnectionState(
                        status = status,
                        attempt = retryCount,
                        maxAttempts = maxAttempts
                    )
                    try {
                        httpClient.webSocket(wsUrl) {
                            session = this
                            _connectionState.value = WsConnectionState(WsConnectionStatus.CONNECTED)
                            retryCount = 0
                            try {
                                for (frame in incoming) {
                                    if (frame is Frame.Text) {
                                        val element = ComfyJson.parseToJsonElement(frame.readText())
                                        val obj = element.jsonObject
                                        val type = obj["type"]?.jsonPrimitive?.content ?: "unknown"
                                        val data = obj["data"] as? JsonObject
                                        _events.tryEmit(WsMessage(type = type, data = data))
                                    }
                                }
                            } catch (_: IOException) {
                                // 后台/断网时底层 socket 关闭会抛异常，这里忽略即可
                            } finally {
                                session = null
                            }
                        }
                    } catch (e: CancellationException) {
                        throw e
                    } catch (_: Exception) {
                        session = null
                    }

                    if (!shouldReconnect || !isActive) break
                    retryCount += 1
                    if (retryCount > maxAttempts) {
                        _connectionState.value = WsConnectionState(
                            status = WsConnectionStatus.FAILED,
                            attempt = maxAttempts,
                            maxAttempts = maxAttempts
                        )
                        shouldReconnect = false
                        break
                    }

                    _connectionState.value = WsConnectionState(
                        status = WsConnectionStatus.RECONNECTING,
                        attempt = retryCount,
                        maxAttempts = maxAttempts
                    )
                    delay(calculateBackoffDelayMs(policy, retryCount))
                }
            } finally {
                if (!shouldReconnect && _connectionState.value.status != WsConnectionStatus.FAILED) {
                    _connectionState.value = WsConnectionState(WsConnectionStatus.DISCONNECTED)
                }
                connectJob = null
            }
        }
    }

    suspend fun disconnect() {
        shouldReconnect = false
        connectJob?.cancelAndJoin()
        connectJob = null
        try {
            session?.close()
        } catch (_: IOException) {
            // Ignore close errors
        } finally {
            session = null
        }
        _connectionState.value = WsConnectionState(WsConnectionStatus.DISCONNECTED)
    }

    fun disconnectAsync() {
        scope.launch {
            disconnect()
        }
    }

    fun close() {
        shouldReconnect = false
        connectJob?.cancel()
        scope.cancel()
    }

    private fun buildWsUrl(baseUrl: String, clientId: String): String {
        val trimmed = baseUrl.trimEnd('/')
        val scheme = if (trimmed.startsWith("https://")) "wss://" else "ws://"
        val host = trimmed.removePrefix("https://").removePrefix("http://")
        return "${scheme}${host}/ws?clientId=$clientId"
    }

    private fun calculateBackoffDelayMs(policy: ReconnectPolicy, attempt: Int): Long {
        val exponent = (attempt - 1).coerceAtLeast(0).coerceAtMost(30)
        val baseDelay = policy.initialDelayMs * (1L shl exponent)
        val cappedDelay = min(policy.maxDelayMs, baseDelay)
        val jitter = if (policy.jitterMs > 0) {
            Random.nextLong(0, policy.jitterMs + 1)
        } else {
            0L
        }
        return cappedDelay + jitter
    }
}

private val DEFAULT_RECONNECT_POLICY = ReconnectPolicy(
    maxAttempts = 5,
    initialDelayMs = 1_000,
    maxDelayMs = 30_000,
    jitterMs = 250
)
