package moe.uni.comfyKmp.network

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.forms.formData
import io.ktor.client.request.forms.submitFormWithBinaryData
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import moe.uni.comfyKmp.data.ImageUploadResponse
import moe.uni.comfyKmp.data.PromptRequest
import moe.uni.comfyKmp.data.PromptResponse

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

    suspend fun uploadImage(
        baseUrl: String,
        filename: String,
        bytes: ByteArray,
        mimeType: String = "image/png",
        overwrite: Boolean = true
    ): ImageUploadResponse {
        return httpClient.submitFormWithBinaryData(
            url = "${baseUrl.trimEnd('/')}/upload/image",
            formData = formData {
                append("image", bytes, Headers.build {
                    append(HttpHeaders.ContentType, mimeType)
                    append(HttpHeaders.ContentDisposition, "filename=\"$filename\"")
                })
                append("overwrite", overwrite.toString())
            }
        ).body()
    }
}
