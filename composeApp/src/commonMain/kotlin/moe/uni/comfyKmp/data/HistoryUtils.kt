package moe.uni.comfyKmp.data

import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

/**
 * Extract images from history with their associated node IDs
 */
fun extractImagesFromHistoryWithNodeId(history: JsonElement): List<NodeImageRef> {
    val result = mutableListOf<NodeImageRef>()
    val root = history as? JsonObject ?: return emptyList()
    root.values.forEach { promptEntry ->
        val outputs = promptEntry.jsonObject["outputs"]?.jsonObject ?: return@forEach
        outputs.forEach { (nodeId, nodeOutput) ->
            val images = nodeOutput.jsonObject["images"] as? JsonArray ?: return@forEach
            images.forEach { img ->
                val obj = img.jsonObject
                val filename = obj["filename"]?.jsonPrimitive?.content ?: return@forEach
                val subfolder = obj["subfolder"]?.jsonPrimitive?.content
                val type = obj["type"]?.jsonPrimitive?.content
                result.add(NodeImageRef(nodeId, ImageRef(filename, subfolder, type)))
            }
        }
    }
    return result
}

fun extractImagesFromHistory(history: JsonElement): List<ImageRef> {
    return extractImagesFromHistoryWithNodeId(history).map { it.imageRef }
}
