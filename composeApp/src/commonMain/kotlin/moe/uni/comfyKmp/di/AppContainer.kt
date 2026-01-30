package moe.uni.comfyKmp.di

import moe.uni.comfyKmp.network.ComfyApiClient
import moe.uni.comfyKmp.network.ComfyWebSocketClient
import moe.uni.comfyKmp.network.createHttpClient
import moe.uni.comfyKmp.storage.ServerRepository
import moe.uni.comfyKmp.storage.SettingsStorage
import moe.uni.comfyKmp.storage.WorkflowRepository

class AppContainer(
    private val storage: SettingsStorage = SettingsStorage()
) {
    private val httpClient = createHttpClient()

    val serverRepository = ServerRepository(storage)
    val workflowRepository = WorkflowRepository(storage)
    val apiClient = ComfyApiClient(httpClient)
    val wsClient = ComfyWebSocketClient(httpClient)
}
