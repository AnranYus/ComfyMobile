package moe.uni.comfy_kmp.data

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject

@Serializable
data class ServerConfig(
    val id: String,
    val name: String,
    val baseUrl: String,
    val isDefault: Boolean = false
)

@Serializable
data class WorkflowEntity(
    val id: String,
    val name: String,
    val json: String,
    val serverId: String,
    val updatedAt: Long,
    val coverImage: String? = null
)

@Serializable
data class PromptNode(
    val class_type: String,
    val inputs: JsonObject = JsonObject(emptyMap())
)

@Serializable
data class PromptRequest(
    val prompt: JsonObject,
    val client_id: String? = null
)

@Serializable
data class PromptResponse(
    val prompt_id: String? = null,
    val number: Int? = null,
    val error: String? = null,
    val node_errors: JsonObject? = null
)

@Serializable
data class ImageRef(
    val filename: String,
    val subfolder: String? = null,
    val type: String? = null
)

@Serializable
data class WsMessage(
    val type: String,
    val data: JsonObject? = null
)

@Serializable
data class SimpleStatus(
    val status: String,
    val message: String? = null
)

@Serializable
data class QueueSnapshot(
    val queue_running: JsonArray? = null,
    val queue_pending: JsonArray? = null
)

data class PromptNodeField(
    val name: String,
    val type: String,
    val defaultValue: JsonElement? = null,
    val optional: Boolean = false
)

enum class NodeStatus {
    PENDING,
    RUNNING,
    COMPLETED,
    ERROR
}

data class NodeExecutionState(
    val nodeId: String,
    val classType: String,
    val status: NodeStatus = NodeStatus.PENDING,
    val inputs: Map<String, JsonElement> = emptyMap()
)
