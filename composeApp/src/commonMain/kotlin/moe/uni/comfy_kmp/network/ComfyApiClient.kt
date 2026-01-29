package moe.uni.comfy_kmp.network

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import moe.uni.comfy_kmp.data.PromptRequest
import moe.uni.comfy_kmp.data.PromptResponse

class ComfyApiClient(
    private val httpClient: HttpClient
) {
    suspend fun getObjectInfo(baseUrl: String): JsonObject {
        return httpClient.get("${baseUrl.trimEnd('/')}/object_info").body()
    }

    suspend fun getObjectInfo(baseUrl: String, nodeClass: String): JsonObject {
        return httpClient.get("${baseUrl.trimEnd('/')}/object_info/$nodeClass").body()
    }

    suspend fun getModels(baseUrl: String): JsonElement {
        return httpClient.get("${baseUrl.trimEnd('/')}/models").body()
    }

    suspend fun getModels(baseUrl: String, folder: String): JsonElement {
        return httpClient.get("${baseUrl.trimEnd('/')}/models/$folder").body()
    }

    suspend fun getQueue(baseUrl: String): JsonElement {
        return httpClient.get("${baseUrl.trimEnd('/')}/queue").body()
    }

    suspend fun getHistory(baseUrl: String): JsonElement {
        return httpClient.get("${baseUrl.trimEnd('/')}/history").body()
    }

    suspend fun getHistory(baseUrl: String, promptId: String): JsonElement {
        return httpClient.get("${baseUrl.trimEnd('/')}/history/$promptId").body()
    }

    suspend fun postPrompt(baseUrl: String, request: PromptRequest): PromptResponse {
        return httpClient.post("${baseUrl.trimEnd('/')}/prompt") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }

    suspend fun interrupt(baseUrl: String): JsonElement {
        return httpClient.post("${baseUrl.trimEnd('/')}/interrupt").body()
    }

    suspend fun clearHistory(baseUrl: String): JsonElement {
        return httpClient.post("${baseUrl.trimEnd('/')}/history") {
            contentType(ContentType.Application.Json)
            setBody(mapOf("clear" to true))
        }.body()
    }

    suspend fun deleteHistoryItem(baseUrl: String, promptId: String): JsonElement {
        return httpClient.delete("${baseUrl.trimEnd('/')}/history/$promptId").body()
    }

    suspend fun getBytes(url: String): ByteArray {
        return httpClient.get(url).body()
    }
}
