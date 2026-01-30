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
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
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
import moe.uni.comfyKmp.data.loadCoverImageBytes
import moe.uni.comfyKmp.di.LocalAppContainer
import moe.uni.comfyKmp.isFilePickerSupported
import moe.uni.comfyKmp.rememberFilePickerLauncher
import moe.uni.comfyKmp.storage.generateId
import moe.uni.comfyKmp.ui.components.ComfyDialog
import moe.uni.comfyKmp.ui.theme.ComfySpacing
import moe.uni.comfyKmp.ui.theme.comfyColors
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
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "返回"
                            )
                        }
                    },
                    actions = {
                        FilledTonalButton(
                            onClick = { showImport = true },
                            modifier = Modifier.padding(end = ComfySpacing.sm)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(ComfySpacing.xs))
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
                if (model.workflows.isEmpty()) {
                    EmptyWorkflowState(
                        onImport = { showImport = true },
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
                                    onRun = { navigator.push(WorkflowRunScreen(workflow.id)) },
                                    onDelete = { model.deleteWorkflow(workflow.id) }
                                )
                            }
                        }
                    }
                }
            }
        }
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
    onDelete: () -> Unit
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
            .shadow(4.dp, shape)
            .clip(shape)
            .background(comfyColors.cardBackground)
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
                    Column {
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
