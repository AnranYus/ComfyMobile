package moe.uni.comfy_kmp.network

import io.ktor.client.HttpClient
import io.ktor.client.engine.darwin.Darwin

actual fun createHttpClient(): HttpClient = HttpClient(Darwin) {
    installComfyPlugins()
}
