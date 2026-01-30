package moe.uni.comfyKmp.storage

import moe.uni.comfyKmp.data.ServerConfig
import moe.uni.comfyKmp.data.WorkflowEntity
import kotlin.random.Random
import kotlin.time.Clock

class ServerRepository(
    private val storage: SettingsStorage
) {
    fun getServers(): List<ServerConfig> = storage.getServers()

    fun getActiveServer(): ServerConfig? {
        val servers = storage.getServers()
        val activeId = storage.getActiveServerId()
        return servers.firstOrNull { it.id == activeId } ?: servers.firstOrNull { it.isDefault }
    }

    fun setActiveServer(serverId: String?) {
        storage.setActiveServerId(serverId)
    }

    fun upsertServer(server: ServerConfig) {
        val servers = storage.getServers().toMutableList()
        val index = servers.indexOfFirst { it.id == server.id }
        if (index >= 0) {
            servers[index] = server
        } else {
            servers.add(server)
        }
        storage.saveServers(servers)
    }

    fun deleteServer(serverId: String) {
        val servers = storage.getServers().filterNot { it.id == serverId }
        storage.saveServers(servers)
        val active = storage.getActiveServerId()
        if (active == serverId) {
            storage.setActiveServerId(servers.firstOrNull()?.id)
        }
    }
}

class WorkflowRepository(
    private val storage: SettingsStorage
) {
    fun getWorkflows(serverId: String): List<WorkflowEntity> {
        return storage.getWorkflows().filter { it.serverId == serverId }
    }

    fun getWorkflow(workflowId: String): WorkflowEntity? {
        return storage.getWorkflows().firstOrNull { it.id == workflowId }
    }

    fun upsertWorkflow(workflow: WorkflowEntity) {
        val items = storage.getWorkflows().toMutableList()
        val index = items.indexOfFirst { it.id == workflow.id }
        if (index >= 0) {
            items[index] = workflow
        } else {
            items.add(workflow)
        }
        storage.saveWorkflows(items)
    }

    fun deleteWorkflow(workflowId: String) {
        val items = storage.getWorkflows().filterNot { it.id == workflowId }
        storage.saveWorkflows(items)
    }
}

fun generateId(prefix: String): String {
    val seed = Clock.System.now().epochSeconds
    val rand = Random.nextInt(1000, 9999)
    return "${prefix}_${seed}_$rand"
}
