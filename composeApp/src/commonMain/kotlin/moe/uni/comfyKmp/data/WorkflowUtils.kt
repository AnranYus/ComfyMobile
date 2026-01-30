package moe.uni.comfyKmp.data

import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.longOrNull

data class PromptNodeEntry(
    val id: String,
    val classType: String,
    val inputs: JsonObject
)

/**
 * Extracts the prompt object from various JSON formats:
 * 1. ComfyUI web export format: {"prompt": {...}, "workflow": {...}}
 * 2. Direct prompt object: {"1": {"class_type": "...", "inputs": {...}}, ...}
 * 3. API format: {"prompt": {...}}
 */
fun extractPromptObject(raw: String): JsonObject {
    if (raw.isBlank()) return JsonObject(emptyMap())
    
    return try {
        val root = parseJsonObject(raw)
        
        // Check if it's a web export format with "prompt" key
        val prompt = root["prompt"]?.jsonObject
        if (prompt != null) {
            return prompt
        }
        
        // Check if root looks like a prompt object (has nodes with class_type)
        val hasNodes = root.any { (_, value) ->
            try {
                val obj = value.jsonObject
                obj.containsKey("class_type") || obj.containsKey("inputs")
            } catch (e: Exception) {
                false
            }
        }
        
        if (hasNodes) {
            return root
        }
        
        // Return empty if nothing valid found
        JsonObject(emptyMap())
    } catch (e: Exception) {
        JsonObject(emptyMap())
    }
}

fun parsePromptNodes(prompt: JsonObject): List<PromptNodeEntry> {
    return prompt.mapNotNull { (id, nodeElement) ->
        try {
            val nodeObj = nodeElement.jsonObject
            val classType = nodeObj["class_type"]?.jsonPrimitive?.content
            if (classType == null) {
                // Skip entries that don't look like nodes
                null
            } else {
                val inputs = nodeObj["inputs"]?.jsonObject ?: JsonObject(emptyMap())
                PromptNodeEntry(id = id, classType = classType, inputs = inputs)
            }
        } catch (e: Exception) {
            null
        }
    }.sortedBy { it.id.toIntOrNull() ?: Int.MAX_VALUE }
}

fun updateNodeInput(
    prompt: JsonObject,
    nodeId: String,
    field: String,
    value: JsonElement
): JsonObject {
    val nodeObj = prompt[nodeId]?.jsonObject ?: return prompt
    val currentInputs = nodeObj["inputs"]?.jsonObject ?: JsonObject(emptyMap())
    val newInputs = JsonObject(currentInputs.toMutableMap().apply { put(field, value) })
    val newNode = JsonObject(nodeObj.toMutableMap().apply { put("inputs", newInputs) })
    return JsonObject(prompt.toMutableMap().apply { put(nodeId, newNode) })
}

fun guessInputType(element: JsonElement?): String {
    return when (element) {
        null -> "string"
        is JsonPrimitive -> {
            when {
                element.isString -> "string"
                element.booleanOrNull != null -> "boolean"
                element.longOrNull != null || element.doubleOrNull != null -> "number"
                else -> "string"
            }
        }
        is JsonArray -> "array"
        is JsonObject -> "object"
        else -> "string"
    }
}

/**
 * Validates if a JSON string contains a valid ComfyUI workflow/prompt
 */
fun isValidWorkflowJson(raw: String): Boolean {
    if (raw.isBlank()) return false
    return try {
        val prompt = extractPromptObject(raw)
        prompt.isNotEmpty() && parsePromptNodes(prompt).isNotEmpty()
    } catch (e: Exception) {
        false
    }
}
