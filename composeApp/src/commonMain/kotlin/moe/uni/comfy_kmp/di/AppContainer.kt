package moe.uni.comfy_kmp.di

import moe.uni.comfy_kmp.network.ComfyApiClient
import moe.uni.comfy_kmp.network.ComfyWebSocketClient
import moe.uni.comfy_kmp.network.createHttpClient
import moe.uni.comfy_kmp.storage.ServerRepository
import moe.uni.comfy_kmp.storage.SettingsStorage
import moe.uni.comfy_kmp.storage.WorkflowRepository

class AppContainer(
    private val storage: SettingsStorage = SettingsStorage()
) {
    private val httpClient = createHttpClient()

    val serverRepository = ServerRepository(storage)
    val workflowRepository = WorkflowRepository(storage)
    val apiClient = ComfyApiClient(httpClient)
    val wsClient = ComfyWebSocketClient(httpClient)
}
