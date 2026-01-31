@file:OptIn(ExperimentalMaterial3Api::class)

package moe.uni.comfyKmp.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
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
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FabPosition
import androidx.compose.material3.Icon
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
import moe.uni.comfyKmp.data.ServerConfig
import moe.uni.comfyKmp.di.LocalAppContainer
import moe.uni.comfyKmp.storage.generateId
import moe.uni.comfyKmp.ui.components.ComfyDialog
import moe.uni.comfyKmp.ui.components.GlassCard
import moe.uni.comfyKmp.ui.theme.AdaptiveLayoutConstants
import moe.uni.comfyKmp.ui.theme.ComfySpacing
import moe.uni.comfyKmp.ui.theme.comfyColors
import moe.uni.comfyKmp.ui.theme.rememberWindowSizeInfo

class ServerScreen : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val container = LocalAppContainer.current
        val model = rememberScreenModel { ServerScreenModel(container.serverRepository) }

        var showDialog by remember { mutableStateOf(false) }
        var editingServer by remember { mutableStateOf<ServerConfig?>(null) }
        var selectedServerId by remember { mutableStateOf<String?>(null) }

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

        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val windowSizeInfo = rememberWindowSizeInfo(maxWidth, maxHeight)
            
            if (windowSizeInfo.shouldUseTwoPane) {
                // 大屏幕：双面板布局
                ExpandedServerLayout(
                    model = model,
                    navigator = navigator,
                    comfyColors = comfyColors,
                    selectedServerId = selectedServerId,
                    onServerSelect = { selectedServerId = it },
                    onAddServer = {
                        editingServer = null
                        showDialog = true
                    },
                    onEditServer = { server ->
                        editingServer = server
                        showDialog = true
                    }
                )
            } else {
                // 小屏幕：单面板布局
                CompactServerLayout(
                    model = model,
                    navigator = navigator,
                    comfyColors = comfyColors,
                    onAddServer = {
                        editingServer = null
                        showDialog = true
                    },
                    onEditServer = { server ->
                        editingServer = server
                        showDialog = true
                    }
                )
            }
        }
    }
}

/**
 * 小屏幕布局：单面板
 */
@Composable
private fun CompactServerLayout(
    model: ServerScreenModel,
    navigator: cafe.adriel.voyager.navigator.Navigator,
    comfyColors: moe.uni.comfyKmp.ui.theme.ComfyExtendedColors,
    onAddServer: () -> Unit,
    onEditServer: (ServerConfig) -> Unit
) {
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
                        onClick = onAddServer,
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
        ServerListContent(
            model = model,
            comfyColors = comfyColors,
            onAddServer = onAddServer,
            onEditServer = onEditServer,
            modifier = Modifier.padding(padding)
        )
    }
}

/**
 * 大屏幕布局：双面板
 */
@Composable
private fun ExpandedServerLayout(
    model: ServerScreenModel,
    navigator: cafe.adriel.voyager.navigator.Navigator,
    comfyColors: moe.uni.comfyKmp.ui.theme.ComfyExtendedColors,
    selectedServerId: String?,
    onServerSelect: (String?) -> Unit,
    onAddServer: () -> Unit,
    onEditServer: (ServerConfig) -> Unit
) {
    val selectedServer = model.servers.find { it.id == selectedServerId }
    
    Row(modifier = Modifier.fillMaxSize()) {
        // 左侧：服务器列表
        Column(
            modifier = Modifier
                .widthIn(
                    min = AdaptiveLayoutConstants.listPaneMinWidth,
                    max = AdaptiveLayoutConstants.listPaneMaxWidth
                )
                .fillMaxHeight()
        ) {
            // 顶部栏
            TopAppBar(
                title = { 
                    Text("Comfy KMP", style = MaterialTheme.typography.headlineSmall)
                },
                actions = {
                    FilledTonalButton(
                        onClick = onAddServer,
                        modifier = Modifier.padding(end = ComfySpacing.sm)
                    ) {
                        Icon(Icons.Default.Add, null, Modifier.size(18.dp))
                        Spacer(Modifier.width(ComfySpacing.xs))
                        Text("添加")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
            
            // 服务器列表
            ServerListContent(
                model = model,
                comfyColors = comfyColors,
                onAddServer = onAddServer,
                onEditServer = onEditServer,
                selectedServerId = selectedServerId,
                onServerClick = { onServerSelect(it) },
                modifier = Modifier.weight(1f)
            )
        }
        
        // 右侧：详情面板
        ServerDetailPanel(
            server = selectedServer,
            isActive = selectedServer?.id == model.activeServerId,
            comfyColors = comfyColors,
            onSelect = { selectedServer?.let { model.setActive(it.id) } },
            onEdit = { selectedServer?.let { onEditServer(it) } },
            onDelete = { 
                selectedServer?.let { 
                    model.delete(it.id)
                    onServerSelect(null)
                }
            },
            onEnterWorkflow = {
                val activeId = model.activeServerId ?: return@ServerDetailPanel
                navigator.push(WorkflowListScreen(activeId))
            },
            canEnterWorkflow = model.activeServerId != null,
            modifier = Modifier.weight(1f).fillMaxHeight()
        )
    }
}

private class ServerScreenModel(
    private val repository: moe.uni.comfyKmp.storage.ServerRepository
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

/**
 * 服务器列表内容（可复用）
 */
@Composable
private fun ServerListContent(
    model: ServerScreenModel,
    comfyColors: moe.uni.comfyKmp.ui.theme.ComfyExtendedColors,
    onAddServer: () -> Unit,
    onEditServer: (ServerConfig) -> Unit,
    modifier: Modifier = Modifier,
    selectedServerId: String? = null,
    onServerClick: ((String) -> Unit)? = null
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        comfyColors.gradientStart,
                        comfyColors.gradientEnd
                    )
                )
            )
    ) {
        if (model.servers.isEmpty()) {
            EmptyServerState(
                onAdd = onAddServer,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            LazyColumn(
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
                            isSelected = server.id == selectedServerId,
                            onSelect = { 
                                model.setActive(server.id)
                                onServerClick?.invoke(server.id)
                            },
                            onEdit = { onEditServer(server) },
                            onDelete = { model.delete(server.id) },
                            showActions = onServerClick == null
                        )
                    }
                }
            }
        }
    }
}

/**
 * 大屏幕右侧详情面板
 */
@Composable
private fun ServerDetailPanel(
    server: ServerConfig?,
    isActive: Boolean,
    comfyColors: moe.uni.comfyKmp.ui.theme.ComfyExtendedColors,
    onSelect: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onEnterWorkflow: () -> Unit,
    canEnterWorkflow: Boolean,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(comfyColors.cardBackground)
            .padding(ComfySpacing.xl),
        contentAlignment = Alignment.Center
    ) {
        if (server == null) {
            // 未选择服务器时的占位内容
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Dns,
                    contentDescription = null,
                    modifier = Modifier.size(72.dp),
                    tint = MaterialTheme.colorScheme.outline
                )
                Spacer(Modifier.height(ComfySpacing.lg))
                Text(
                    "选择一个服务器查看详情",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            // 显示服务器详情
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(Modifier.weight(1f))
                
                // 服务器图标
                Box(
                    modifier = Modifier
                        .size(96.dp)
                        .clip(CircleShape)
                        .background(
                            if (isActive) MaterialTheme.colorScheme.primaryContainer
                            else MaterialTheme.colorScheme.surfaceVariant
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Dns,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = if (isActive) MaterialTheme.colorScheme.onPrimaryContainer
                               else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Spacer(Modifier.height(ComfySpacing.xl))
                
                // 服务器名称
                Text(
                    text = server.name,
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Spacer(Modifier.height(ComfySpacing.sm))
                
                // 服务器地址
                Text(
                    text = server.baseUrl,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                if (server.isDefault) {
                    Spacer(Modifier.height(ComfySpacing.md))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer)
                            .padding(horizontal = ComfySpacing.md, vertical = ComfySpacing.sm)
                    ) {
                        Text(
                            "默认服务器",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
                
                Spacer(Modifier.height(ComfySpacing.xxl))
                
                // 操作按钮
                Row(horizontalArrangement = Arrangement.spacedBy(ComfySpacing.md)) {
                    OutlinedButton(
                        onClick = onSelect,
                        enabled = !isActive
                    ) {
                        Icon(
                            if (isActive) Icons.Default.Check else Icons.Default.RadioButtonUnchecked,
                            null, Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(ComfySpacing.sm))
                        Text(if (isActive) "已选择" else "选择")
                    }
                    
                    OutlinedButton(onClick = onEdit) {
                        Icon(Icons.Default.Edit, null, Modifier.size(18.dp))
                        Spacer(Modifier.width(ComfySpacing.sm))
                        Text("编辑")
                    }
                    
                    OutlinedButton(
                        onClick = onDelete,
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Icon(Icons.Default.Delete, null, Modifier.size(18.dp))
                        Spacer(Modifier.width(ComfySpacing.sm))
                        Text("删除")
                    }
                }
                
                Spacer(Modifier.weight(1f))
                
                // 进入工作流按钮
                ExtendedFloatingActionButton(
                    onClick = onEnterWorkflow,
                    icon = {
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, null, Modifier.size(20.dp))
                    },
                    text = { Text("进入工作流") },
                    containerColor = if (canEnterWorkflow) {
                        MaterialTheme.colorScheme.primaryContainer
                    } else {
                        MaterialTheme.colorScheme.surfaceVariant
                    },
                    contentColor = if (canEnterWorkflow) {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }
        }
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
    onDelete: () -> Unit,
    isSelected: Boolean = false,
    showActions: Boolean = true
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
            
            // Actions - 仅在小屏幕模式下显示
            if (showActions) {
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
