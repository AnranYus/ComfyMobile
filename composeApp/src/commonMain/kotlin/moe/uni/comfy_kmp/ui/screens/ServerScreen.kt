@file:OptIn(ExperimentalMaterial3Api::class)

package moe.uni.comfy_kmp.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.RadioButtonChecked
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FabPosition
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import moe.uni.comfy_kmp.data.ServerConfig
import moe.uni.comfy_kmp.di.LocalAppContainer
import moe.uni.comfy_kmp.storage.generateId
import moe.uni.comfy_kmp.ui.components.ComfyDialog
import moe.uni.comfy_kmp.ui.components.GlassCard
import moe.uni.comfy_kmp.ui.theme.ComfySpacing
import moe.uni.comfy_kmp.ui.theme.comfyColors

class ServerScreen : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val container = LocalAppContainer.current
        val model = rememberScreenModel { ServerScreenModel(container.serverRepository) }

        var showDialog by remember { mutableStateOf(false) }
        var editingServer by remember { mutableStateOf<ServerConfig?>(null) }

        if (showDialog) {
            ServerEditDialog(
                initial = editingServer,
                onDismiss = { showDialog = false },
                onSave = { server ->
                    model.upsert(server)
                    showDialog = false
                    editingServer = null
                }
            )
        }

        val comfyColors = MaterialTheme.comfyColors

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { 
                        Column {
                            Text(
                                "Comfy KMP",
                                style = MaterialTheme.typography.headlineSmall
                            )
                        }
                    },
                    actions = {
                        FilledTonalButton(
                            onClick = {
                                editingServer = null
                                showDialog = true
                            },
                            modifier = Modifier.padding(end = ComfySpacing.sm)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(ComfySpacing.xs))
                            Text("添加服务器")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )
            },
            floatingActionButton = {
                val canEnter = model.activeServerId != null
                ExtendedFloatingActionButton(
                    onClick = {
                        val activeId = model.activeServerId ?: return@ExtendedFloatingActionButton
                        navigator.push(WorkflowListScreen(activeId))
                    },
                    icon = {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    text = { Text("进入工作流") },
                    containerColor = if (canEnter) {
                        MaterialTheme.colorScheme.primaryContainer
                    } else {
                        MaterialTheme.colorScheme.surfaceVariant
                    },
                    contentColor = if (canEnter) {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            },
            floatingActionButtonPosition = FabPosition.End,
            containerColor = MaterialTheme.colorScheme.background
        ) { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                comfyColors.gradientStart,
                                comfyColors.gradientEnd
                            )
                        )
                    )
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    if (model.servers.isEmpty()) {
                        EmptyServerState(
                            onAdd = {
                                editingServer = null
                                showDialog = true
                            },
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(ComfySpacing.lg),
                            verticalArrangement = Arrangement.spacedBy(ComfySpacing.md)
                        ) {
                            itemsIndexed(
                                items = model.servers,
                                key = { _, server -> server.id }
                            ) { index, server ->
                                AnimatedVisibility(
                                    visible = true,
                                    enter = fadeIn(tween(300, delayMillis = index * 50)) +
                                            slideInVertically(
                                                initialOffsetY = { it / 2 },
                                                animationSpec = tween(300, delayMillis = index * 50)
                                            )
                                ) {
                                    ServerCard(
                                        server = server,
                                        isActive = server.id == model.activeServerId,
                                        onSelect = { model.setActive(server.id) },
                                        onEdit = {
                                            editingServer = server
                                            showDialog = true
                                        },
                                        onDelete = { model.delete(server.id) }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private class ServerScreenModel(
    private val repository: moe.uni.comfy_kmp.storage.ServerRepository
) : ScreenModel {
    var servers by mutableStateOf(repository.getServers())
        private set
    var activeServerId by mutableStateOf(repository.getActiveServer()?.id)
        private set

    fun upsert(server: ServerConfig) {
        repository.upsertServer(server)
        servers = repository.getServers()
        if (server.isDefault) {
            repository.setActiveServer(server.id)
            activeServerId = server.id
        }
    }

    fun delete(serverId: String) {
        repository.deleteServer(serverId)
        servers = repository.getServers()
        activeServerId = repository.getActiveServer()?.id
    }

    fun setActive(serverId: String) {
        repository.setActiveServer(serverId)
        activeServerId = serverId
    }
}

@Composable
private fun ServerEditDialog(
    initial: ServerConfig?,
    onDismiss: () -> Unit,
    onSave: (ServerConfig) -> Unit
) {
    var name by remember { mutableStateOf(initial?.name ?: "") }
    var baseUrl by remember { mutableStateOf(initial?.baseUrl ?: "") }
    var isDefault by remember { mutableStateOf(initial?.isDefault ?: false) }

    ComfyDialog(
        onDismissRequest = onDismiss,
        title = if (initial == null) "添加服务器" else "编辑服务器",
        confirmText = "保存",
        dismissText = "取消",
        confirmEnabled = baseUrl.isNotBlank(),
        onConfirm = {
            val id = initial?.id ?: generateId("server")
            onSave(
                ServerConfig(
                    id = id,
                    name = name.ifBlank { "未命名服务器" },
                    baseUrl = baseUrl.trim(),
                    isDefault = isDefault
                )
            )
        }
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(ComfySpacing.lg)) {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("名称") },
                placeholder = { Text("我的服务器") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )
            
            OutlinedTextField(
                value = baseUrl,
                onValueChange = { baseUrl = it },
                label = { Text("服务器地址") },
                placeholder = { Text("http://192.168.1.100:8188") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )
            
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(ComfySpacing.sm)
            ) {
                Checkbox(
                    checked = isDefault,
                    onCheckedChange = { isDefault = it }
                )
                Text(
                    "设为默认服务器",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
private fun ServerCard(
    server: ServerConfig,
    isActive: Boolean,
    onSelect: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        isSelected = isActive,
        onClick = onSelect
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(ComfySpacing.md)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(ComfySpacing.md),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Status indicator
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(CircleShape)
                            .background(
                                if (isActive) MaterialTheme.comfyColors.success
                                else MaterialTheme.colorScheme.outline
                            )
                    )
                    
                    Column {
                        Text(
                            text = server.name,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = server.baseUrl,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                
                if (server.isDefault) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer)
                            .padding(horizontal = ComfySpacing.sm, vertical = ComfySpacing.xs)
                    ) {
                        Text(
                            "默认",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
            
            // Actions
            Row(
                horizontalArrangement = Arrangement.spacedBy(ComfySpacing.sm)
            ) {
                OutlinedButton(
                    onClick = onSelect,
                    enabled = !isActive,
                    contentPadding = PaddingValues(
                        horizontal = ComfySpacing.md,
                        vertical = ComfySpacing.sm
                    )
                ) {
                    Icon(
                        imageVector = if (isActive) Icons.Default.Check else Icons.Default.RadioButtonUnchecked,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(ComfySpacing.xs))
                    Text(if (isActive) "已选择" else "选择")
                }
                
                OutlinedButton(
                    onClick = onEdit,
                    contentPadding = PaddingValues(
                        horizontal = ComfySpacing.md,
                        vertical = ComfySpacing.sm
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(ComfySpacing.xs))
                    Text("编辑")
                }
                
                Spacer(modifier = Modifier.weight(1f))
                
                TextButton(
                    onClick = onDelete,
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(ComfySpacing.xs))
                    Text("删除")
                }
            }
        }
    }
}

@Composable
private fun EmptyServerState(
    onAdd: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Dns,
                contentDescription = null,
                modifier = Modifier.size(40.dp),
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
        
        Spacer(modifier = Modifier.height(ComfySpacing.xl))
        
        Text(
            text = "欢迎使用 Comfy KMP",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface
        )
        
        Spacer(modifier = Modifier.height(ComfySpacing.sm))
        
        Text(
            text = "添加一个 ComfyUI 服务器开始使用",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(ComfySpacing.xl))
        
        Button(
            onClick = onAdd,
            contentPadding = PaddingValues(
                horizontal = ComfySpacing.xl,
                vertical = ComfySpacing.md
            )
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(ComfySpacing.sm))
            Text("添加服务器")
        }
    }
}
