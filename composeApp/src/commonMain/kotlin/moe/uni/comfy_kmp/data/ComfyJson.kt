package moe.uni.comfy_kmp.data

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject

val ComfyJson = Json {
    ignoreUnknownKeys = true
    isLenient = true
    prettyPrint = false
}

fun parseJsonObject(raw: String): JsonObject = ComfyJson.parseToJsonElement(raw).jsonObject

fun jsonOrNull(raw: String): JsonElement? = runCatching { ComfyJson.parseToJsonElement(raw) }.getOrNull()
