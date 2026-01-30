package moe.uni.comfyKmp.data

import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

fun extractImagesFromHistory(history: JsonElement): List<ImageRef> {
    val result = mutableListOf<ImageRef>()
    val root = history as? JsonObject ?: return emptyList()
    root.values.forEach { promptEntry ->
        val outputs = promptEntry.jsonObject["outputs"]?.jsonObject ?: return@forEach
        outputs.values.forEach { nodeOutput ->
            val images = nodeOutput.jsonObject["images"] as? JsonArray ?: return@forEach
            images.forEach { img ->
                val obj = img.jsonObject
                val filename = obj["filename"]?.jsonPrimitive?.content ?: return@forEach
                val subfolder = obj["subfolder"]?.jsonPrimitive?.content
                val type = obj["type"]?.jsonPrimitive?.content
                result.add(ImageRef(filename, subfolder, type))
            }
        }
    }
    return result
}
