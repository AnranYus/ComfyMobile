package moe.uni.comfyKmp.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import moe.uni.comfyKmp.data.WorkflowEntity
import moe.uni.comfyKmp.data.extractPromptObject
import moe.uni.comfyKmp.data.loadCoverImageBytes
import moe.uni.comfyKmp.data.parsePromptNodes
import moe.uni.comfyKmp.di.LocalAppContainer
import moe.uni.comfyKmp.isFilePickerSupported
import moe.uni.comfyKmp.rememberFilePickerLauncher
import moe.uni.comfyKmp.storage.generateId
import moe.uni.comfyKmp.ui.components.ComfyDialog
import moe.uni.comfyKmp.ui.theme.AdaptiveLayoutConstants
import moe.uni.comfyKmp.ui.theme.ComfySpacing
import moe.uni.comfyKmp.ui.theme.comfyColors
import moe.uni.comfyKmp.ui.theme.rememberWindowSizeInfo
import org.jetbrains.compose.resources.decodeToImageBitmap
import kotlin.time.Clock

data class WorkflowListScreen(val serverId: String) : Screen {
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val container = LocalAppContainer.current
        val model = rememberScreenModel { WorkflowListScreenModel(serverId, container.workflowRepository) }

        LaunchedEffect(Unit) {
            model.refresh()
        }

        var showImport by remember { mutableStateOf(false) }
        var selectedWorkflowId by remember { mutableStateOf<String?>(null) }
        
        if (showImport) {
            WorkflowImportDialog(
                onDismiss = { showImport = false },
                onSave = { name, json ->
                    model.addWorkflow(name, json)
                    showImport = false
                }
            )
        }

        val comfyColors = MaterialTheme.comfyColors

        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val windowSizeInfo = rememberWindowSizeInfo(maxWidth, maxHeight)
            
            if (windowSizeInfo.shouldUseTwoPane) {
                // 大屏幕：双面板布局
                ExpandedWorkflowListLayout(
                    model = model,
                    navigator = navigator,
                    comfyColors = comfyColors,
                    selectedWorkflowId = selectedWorkflowId,
                    onWorkflowSelect = { selectedWorkflowId = it },
                    onImport = { showImport = true }
                )
            } else {
                // 小屏幕：单面板布局
                CompactWorkflowListLayout(
                    model = model,
                    navigator = navigator,
                    comfyColors = comfyColors,
                    onImport = { showImport = true }
                )
            }
        }
    }
}

/**
 * 小屏幕布局：单面板
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CompactWorkflowListLayout(
    model: WorkflowListScreenModel,
    navigator: cafe.adriel.voyager.navigator.Navigator,
    comfyColors: moe.uni.comfyKmp.ui.theme.ComfyExtendedColors,
    onImport: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Column {
                        Text("工作流")
                        Text(
                            text = "${model.workflows.size} 个工作流",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navigator.pop() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "返回")
                    }
                },
                actions = {
                    FilledTonalButton(
                        onClick = onImport,
                        modifier = Modifier.padding(end = ComfySpacing.sm)
                    ) {
                        Icon(Icons.Default.Add, null, Modifier.size(18.dp))
                        Spacer(Modifier.width(ComfySpacing.xs))
                        Text("导入")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        WorkflowListContent(
            model = model,
            comfyColors = comfyColors,
            onImport = onImport,
            onWorkflowClick = { navigator.push(WorkflowRunScreen(it)) },
            modifier = Modifier.padding(padding)
        )
    }
}

/**
 * 大屏幕布局：双面板
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ExpandedWorkflowListLayout(
    model: WorkflowListScreenModel,
    navigator: cafe.adriel.voyager.navigator.Navigator,
    comfyColors: moe.uni.comfyKmp.ui.theme.ComfyExtendedColors,
    selectedWorkflowId: String?,
    onWorkflowSelect: (String?) -> Unit,
    onImport: () -> Unit
) {
    val selectedWorkflow = model.workflows.find { it.id == selectedWorkflowId }
    
    Row(modifier = Modifier.fillMaxSize()) {
        // 左侧：工作流列表
        Column(
            modifier = Modifier
                .widthIn(
                    min = AdaptiveLayoutConstants.listPaneMinWidth,
                    max = AdaptiveLayoutConstants.listPaneMaxWidth
                )
                .fillMaxHeight()
        ) {
            TopAppBar(
                title = { 
                    Column {
                        Text("工作流")
                        Text(
                            "${model.workflows.size} 个工作流",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navigator.pop() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "返回")
                    }
                },
                actions = {
                    FilledTonalButton(
                        onClick = onImport,
                        modifier = Modifier.padding(end = ComfySpacing.sm)
                    ) {
                        Icon(Icons.Default.Add, null, Modifier.size(18.dp))
                        Spacer(Modifier.width(ComfySpacing.xs))
                        Text("导入")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
            
            WorkflowListContent(
                model = model,
                comfyColors = comfyColors,
                onImport = onImport,
                onWorkflowClick = { onWorkflowSelect(it) },
                selectedWorkflowId = selectedWorkflowId,
                modifier = Modifier.weight(1f)
            )
        }
        
        // 右侧：详情面板
        WorkflowDetailPanel(
            workflow = selectedWorkflow,
            comfyColors = comfyColors,
            onRun = { 
                selectedWorkflow?.let { navigator.push(WorkflowRunScreen(it.id)) }
            },
            onDelete = {
                selectedWorkflow?.let {
                    model.deleteWorkflow(it.id)
                    onWorkflowSelect(null)
                }
            },
            modifier = Modifier.weight(1f).fillMaxHeight()
        )
    }
}

private class WorkflowListScreenModel(
    private val serverId: String,
    private val repository: moe.uni.comfyKmp.storage.WorkflowRepository
) : ScreenModel {
    var workflows by mutableStateOf(repository.getWorkflows(serverId))
        private set

    fun refresh() {
        workflows = repository.getWorkflows(serverId)
    }

    fun addWorkflow(name: String, json: String) {
        val workflow = WorkflowEntity(
            id = generateId("workflow"),
            name = name.ifBlank { "未命名工作流" },
            json = json,
            serverId = serverId,
            updatedAt = Clock.System.now().epochSeconds
        )
        repository.upsertWorkflow(workflow)
        refresh()
    }

    fun deleteWorkflow(id: String) {
        repository.deleteWorkflow(id)
        refresh()
    }
}

/**
 * 工作流列表内容（可复用）
 */
@Composable
private fun WorkflowListContent(
    model: WorkflowListScreenModel,
    comfyColors: moe.uni.comfyKmp.ui.theme.ComfyExtendedColors,
    onImport: () -> Unit,
    onWorkflowClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    selectedWorkflowId: String? = null
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
        if (model.workflows.isEmpty()) {
            EmptyWorkflowState(
                onImport = onImport,
                modifier = Modifier.align(Alignment.Center)
            )
        } else {
            LazyColumn(
                contentPadding = PaddingValues(ComfySpacing.lg),
                verticalArrangement = Arrangement.spacedBy(ComfySpacing.md)
            ) {
                itemsIndexed(
                    items = model.workflows,
                    key = { _, workflow -> workflow.id }
                ) { index, workflow ->
                    AnimatedVisibility(
                        visible = true,
                        enter = fadeIn(tween(300, delayMillis = index * 50)) +
                                slideInVertically(
                                    initialOffsetY = { it / 2 },
                                    animationSpec = tween(300, delayMillis = index * 50)
                                )
                    ) {
                        WorkflowCard(
                            workflow = workflow,
                            isSelected = workflow.id == selectedWorkflowId,
                            onRun = { onWorkflowClick(workflow.id) },
                            onDelete = { model.deleteWorkflow(workflow.id) },
                            showActions = selectedWorkflowId == null
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
private fun WorkflowDetailPanel(
    workflow: WorkflowEntity?,
    comfyColors: moe.uni.comfyKmp.ui.theme.ComfyExtendedColors,
    onRun: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    // 解析节点列表
    val nodes = remember(workflow?.json) {
        workflow?.json?.let { json ->
            try {
                val prompt = extractPromptObject(json)
                parsePromptNodes(prompt)
            } catch (e: Exception) { emptyList() }
        } ?: emptyList()
    }
    
    // 加载封面图片
    val coverBitmap = remember(workflow?.coverImage) {
        workflow?.coverImage?.let { cover ->
            try {
                loadCoverImageBytes(cover)?.decodeToImageBitmap()
            } catch (e: Exception) { null }
        }
    }
    
    Box(
        modifier = modifier.background(comfyColors.cardBackground),
        contentAlignment = Alignment.Center
    ) {
        if (workflow == null) {
            // 未选择工作流时的占位内容
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Assignment,
                    contentDescription = null,
                    modifier = Modifier.size(72.dp),
                    tint = MaterialTheme.colorScheme.outline
                )
                Spacer(Modifier.height(ComfySpacing.lg))
                Text(
                    "选择一个工作流查看详情",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            // 显示工作流详情
            Column(modifier = Modifier.fillMaxSize()) {
                // 顶部信息区域
                WorkflowDetailHeader(
                    workflow = workflow,
                    coverBitmap = coverBitmap,
                    nodeCount = nodes.size,
                    onRun = onRun,
                    onDelete = onDelete
                )
                
                // 节点列表
                WorkflowNodeList(
                    nodes = nodes,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

/**
 * 工作流详情头部
 */
@Composable
private fun WorkflowDetailHeader(
    workflow: WorkflowEntity,
    coverBitmap: androidx.compose.ui.graphics.ImageBitmap?,
    nodeCount: Int,
    onRun: () -> Unit,
    onDelete: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(ComfySpacing.lg),
        horizontalArrangement = Arrangement.spacedBy(ComfySpacing.lg),
        verticalAlignment = Alignment.Top
    ) {
        // 封面图片（小尺寸）
        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            if (coverBitmap != null) {
                Image(
                    bitmap = coverBitmap,
                    contentDescription = workflow.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Icon(
                    Icons.Default.Image, null,
                    Modifier.size(40.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(0.5f)
                )
            }
        }
        
        // 信息区域
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = workflow.name,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            
            Spacer(Modifier.height(ComfySpacing.xs))
            
            Text(
                text = "更新于 ${formatTimestamp(workflow.updatedAt)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(Modifier.height(ComfySpacing.xs))
            
            Text(
                text = "$nodeCount 个节点",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(Modifier.height(ComfySpacing.md))
            
            // 操作按钮
            Row(horizontalArrangement = Arrangement.spacedBy(ComfySpacing.sm)) {
                Button(
                    onClick = onRun,
                    modifier = Modifier.height(36.dp),
                    contentPadding = PaddingValues(horizontal = ComfySpacing.lg)
                ) {
                    Text("运行", style = MaterialTheme.typography.labelMedium)
                }
                
                OutlinedButton(
                    onClick = onDelete,
                    modifier = Modifier.height(36.dp),
                    contentPadding = PaddingValues(horizontal = ComfySpacing.md),
                    colors = androidx.compose.material3.ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Icon(Icons.Default.Delete, null, Modifier.size(16.dp))
                }
            }
        }
    }
}

/**
 * 节点列表
 */
@Composable
private fun WorkflowNodeList(
    nodes: List<moe.uni.comfyKmp.data.PromptNodeEntry>,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        // 标题
        Text(
            text = "节点列表",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = ComfySpacing.lg, vertical = ComfySpacing.sm)
        )
        
        if (nodes.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "无法解析节点",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(
                    start = ComfySpacing.lg,
                    end = ComfySpacing.lg,
                    bottom = ComfySpacing.lg
                ),
                verticalArrangement = Arrangement.spacedBy(ComfySpacing.sm)
            ) {
                items(nodes, key = { it.id }) { node ->
                    NodeListItem(node = node)
                }
            }
        }
    }
}

@Composable
private fun NodeListItem(node: moe.uni.comfyKmp.data.PromptNodeEntry) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .padding(horizontal = ComfySpacing.md, vertical = ComfySpacing.sm),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 节点类型
        Text(
            text = node.classType,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
        
        // 节点 ID
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(4.dp))
                .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                .padding(horizontal = ComfySpacing.sm, vertical = 2.dp)
        ) {
            Text(
                text = "#${node.id}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun WorkflowImportDialog(
    onDismiss: () -> Unit,
    onSave: (String, String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var json by remember { mutableStateOf("") }
    
    // File picker for Android
    val filePickerSupported = isFilePickerSupported()
    val launchFilePicker = rememberFilePickerLauncher { content ->
        if (content != null) {
            json = content
            if (name.isBlank()) {
                name = "导入的工作流"
            }
        }
    }

    ComfyDialog(
        onDismissRequest = onDismiss,
        title = "导入工作流",
        confirmText = "导入",
        dismissText = "取消",
        confirmEnabled = json.isNotBlank(),
        onConfirm = { onSave(name, json) }
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(ComfySpacing.lg)) {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("名称") },
                placeholder = { Text("输入工作流名称") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )
            
            Column(verticalArrangement = Arrangement.spacedBy(ComfySpacing.sm)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "JSON 内容",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    if (filePickerSupported) {
                        OutlinedButton(
                            onClick = { launchFilePicker() },
                            contentPadding = PaddingValues(
                                horizontal = ComfySpacing.md,
                                vertical = ComfySpacing.sm
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.FolderOpen,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(ComfySpacing.xs))
                            Text("选择文件")
                        }
                    }
                }
                
                OutlinedTextField(
                    value = json,
                    onValueChange = { json = it },
                    placeholder = { Text("粘贴 JSON 或选择本地文件...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    minLines = 6,
                    shape = RoundedCornerShape(12.dp)
                )
            }
        }
    }
}

@Composable
private fun WorkflowCard(
    workflow: WorkflowEntity,
    onRun: () -> Unit,
    onDelete: () -> Unit,
    isSelected: Boolean = false,
    showActions: Boolean = true
) {
    val shape = RoundedCornerShape(16.dp)
    val comfyColors = MaterialTheme.comfyColors
    
    // Decode cover image if available
    val coverBitmap = remember(workflow.coverImage) {
        workflow.coverImage?.let { cover ->
            try {
                val bytes = loadCoverImageBytes(cover)
                bytes?.decodeToImageBitmap()
            } catch (e: Exception) {
                null
            }
        }
    }
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(if (isSelected) 8.dp else 4.dp, shape)
            .clip(shape)
            .background(comfyColors.cardBackground)
            .then(
                if (isSelected) {
                    Modifier.background(
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f)
                    )
                } else Modifier
            )
            .clickable(onClick = onRun)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Cover image area (4:3)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(4f / 3f)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                if (coverBitmap != null) {
                    Image(
                        bitmap = coverBitmap,
                        contentDescription = workflow.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    // Placeholder
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(ComfySpacing.sm)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Image,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                            )
                            Text(
                                text = "暂无封面",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                            )
                        }
                    }
                }
            }
            
            // Info and actions area
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(comfyColors.cardBackground)
                    .padding(ComfySpacing.md),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                
                // Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    // Title and timestamp
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = workflow.name,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = formatTimestamp(workflow.updatedAt),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    
                    if (showActions) {
                        FilledTonalIconButton(
                            onClick = onDelete,
                            colors = IconButtonDefaults.filledTonalIconButtonColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer,
                                contentColor = MaterialTheme.colorScheme.error
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "删除",
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyWorkflowState(
    onImport: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(ComfySpacing.xl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(ComfySpacing.lg)
    ) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.secondaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.Assignment,
                contentDescription = null,
                modifier = Modifier.size(40.dp),
                tint = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
        
        Text(
            text = "暂无工作流",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface
        )
        
        Text(
            text = "导入 ComfyUI 工作流 JSON 开始使用",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(ComfySpacing.md))
        
        Button(onClick = onImport) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(ComfySpacing.sm))
            Text("导入工作流")
        }
    }
}

private fun formatTimestamp(timestamp: Long): String {
    val now = Clock.System.now().epochSeconds
    val diff = now - timestamp
    return when {
        diff < 60 -> "刚刚"
        diff < 3600 -> "${diff / 60} 分钟前"
        diff < 86400 -> "${diff / 3600} 小时前"
        diff < 604800 -> "${diff / 86400} 天前"
        else -> "${diff / 604800} 周前"
    }
}
