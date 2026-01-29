package moe.uni.comfy_kmp.storage

import com.russhwolf.settings.Settings
import moe.uni.comfy_kmp.data.ComfyJson
import moe.uni.comfy_kmp.data.ServerConfig
import moe.uni.comfy_kmp.data.WorkflowEntity
import moe.uni.comfy_kmp.data.saveCoverImage
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import okio.FileSystem
import okio.Path.Companion.toPath
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi
import kotlin.time.Clock

class SettingsStorage(
    private val settings: Settings = Settings()
) {
    private val serversKey = "servers"
    private val workflowsKey = "workflows"
    private val activeServerKey = "active_server_id"

    fun getServers(): List<ServerConfig> {
        val raw = settings.getStringOrNull(serversKey) ?: return emptyList()
        return ComfyJson.decodeFromString(ListSerializer(ServerConfig.serializer()), raw)
    }

    fun saveServers(servers: List<ServerConfig>) {
        settings.putString(serversKey, ComfyJson.encodeToString(servers))
    }

    fun getActiveServerId(): String? = settings.getStringOrNull(activeServerKey)

    fun setActiveServerId(serverId: String?) {
        if (serverId == null) {
            settings.remove(activeServerKey)
        } else {
            settings.putString(activeServerKey, serverId)
        }
    }

    fun getWorkflows(): List<WorkflowEntity> {
        val raw = settings.getStringOrNull(workflowsKey) ?: return emptyList()
        val items = ComfyJson.decodeFromString(ListSerializer(WorkflowEntity.serializer()), raw)
        val migrated = migrateCoverImages(items)
        if (migrated !== items) {
            saveWorkflows(migrated)
        }
        return migrated
    }

    fun saveWorkflows(workflows: List<WorkflowEntity>) {
        settings.putString(workflowsKey, ComfyJson.encodeToString(workflows))
    }

    @OptIn(ExperimentalEncodingApi::class)
    private fun migrateCoverImages(items: List<WorkflowEntity>): List<WorkflowEntity> {
        var changed = false
        val updated = items.map { workflow ->
            val cover = workflow.coverImage ?: return@map workflow
            val path = cover.removePrefix("file://").toPath()
            if (FileSystem.SYSTEM.exists(path)) return@map workflow
            val bytes = try {
                Base64.decode(cover)
            } catch (_: IllegalArgumentException) {
                return@map workflow
            }
            val filename = "cover_${workflow.id}_${Clock.System.now().toEpochMilliseconds()}.jpg"
            val savedPath = saveCoverImage(bytes, filename)
            changed = true
            workflow.copy(coverImage = savedPath)
        }
        return if (changed) updated else items
    }
}
