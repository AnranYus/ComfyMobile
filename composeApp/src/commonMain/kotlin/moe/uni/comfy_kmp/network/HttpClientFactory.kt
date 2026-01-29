package moe.uni.comfy_kmp.network

import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.serialization.kotlinx.json.json
import moe.uni.comfy_kmp.data.ComfyJson

internal fun HttpClientConfig<*>.installComfyPlugins() {
    install(ContentNegotiation) {
        json(ComfyJson)
    }
    install(WebSockets)
}

expect fun createHttpClient(): HttpClient
