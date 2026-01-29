package moe.uni.comfy_kmp.network

import io.ktor.client.HttpClient
import io.ktor.client.plugins.websocket.webSocket
import io.ktor.websocket.Frame
import io.ktor.websocket.WebSocketSession
import io.ktor.websocket.close
import io.ktor.websocket.readText
import io.ktor.utils.io.errors.IOException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import moe.uni.comfy_kmp.data.ComfyJson
import moe.uni.comfy_kmp.data.WsMessage

class ComfyWebSocketClient(
    private val httpClient: HttpClient
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val _events = MutableSharedFlow<WsMessage>(extraBufferCapacity = 64)
    val events: SharedFlow<WsMessage> = _events

    private var session: WebSocketSession? = null

    fun connect(baseUrl: String, clientId: String) {
        if (session != null) return
        val wsUrl = buildWsUrl(baseUrl, clientId)
        scope.launch {
            try {
                httpClient.webSocket(wsUrl) {
                    session = this
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
            } catch (_: IOException) {
                session = null
            }
        }
    }

    suspend fun disconnect() {
        session?.close()
        session = null
    }

    fun close() {
        scope.cancel()
    }

    private fun buildWsUrl(baseUrl: String, clientId: String): String {
        val trimmed = baseUrl.trimEnd('/')
        val scheme = if (trimmed.startsWith("https://")) "wss://" else "ws://"
        val host = trimmed.removePrefix("https://").removePrefix("http://")
        return "${scheme}${host}/ws?clientId=$clientId"
    }
}
