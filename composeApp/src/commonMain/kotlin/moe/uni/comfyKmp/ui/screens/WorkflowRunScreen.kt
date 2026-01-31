package moe.uni.comfyKmp.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberStandardBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive
import moe.uni.comfyKmp.data.ComfyJson
import moe.uni.comfyKmp.data.ImageRef
import moe.uni.comfyKmp.data.NodeExecutionState
import moe.uni.comfyKmp.data.NodeStatus
import moe.uni.comfyKmp.data.PromptRequest
import moe.uni.comfyKmp.data.extractImagesFromHistory
import moe.uni.comfyKmp.data.extractPromptObject
import moe.uni.comfyKmp.data.parsePromptNodes
import moe.uni.comfyKmp.data.saveCoverImage
import moe.uni.comfyKmp.data.saveImageToGallery
import moe.uni.comfyKmp.data.updateNodeInput
import moe.uni.comfyKmp.di.LocalAppContainer
import moe.uni.comfyKmp.network.ComfyApiClient
import moe.uni.comfyKmp.network.ComfyWebSocketClient
import moe.uni.comfyKmp.network.WsConnectionStatus
import moe.uni.comfyKmp.storage.ServerRepository
import moe.uni.comfyKmp.storage.WorkflowRepository
import moe.uni.comfyKmp.storage.generateId
import moe.uni.comfyKmp.ui.components.EditableNodeType
import moe.uni.comfyKmp.ui.components.EditableNodeCard
import moe.uni.comfyKmp.ui.components.ExecutionStatus
import moe.uni.comfyKmp.ui.components.ExecutionStatusBar
import moe.uni.comfyKmp.ui.components.GalleryImage
import moe.uni.comfyKmp.ui.components.ImageGallery
import moe.uni.comfyKmp.ui.components.ToastHost
import moe.uni.comfyKmp.ui.components.ToastState
import moe.uni.comfyKmp.ui.components.detectNodeType
import moe.uni.comfyKmp.ui.components.getModelFolder
import moe.uni.comfyKmp.ui.components.rememberToastState
import moe.uni.comfyKmp.ui.theme.ComfySpacing
import moe.uni.comfyKmp.ui.theme.WindowSizeInfo
import moe.uni.comfyKmp.ui.theme.comfyColors
import moe.uni.comfyKmp.ui.theme.rememberWindowSizeInfo
import org.jetbrains.compose.resources.decodeToImageBitmap
import kotlin.random.Random
import kotlin.time.Clock

data class WorkflowRunScreen(val workflowId: String) : Screen {
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val container = LocalAppContainer.current
        val toastState = rememberToastState()
        val model = rememberScreenModel {
            WorkflowRunScreenModel(
                workflowId = workflowId,
                workflowRepository = container.workflowRepository,
                apiClient = container.apiClient,
                wsClient = container.wsClient,
                serverRepository = container.serverRepository,
                onToast = { toastState.show(it) }
            )
        }

        LaunchedEffect(Unit) {
            model.connect()
        }

        val comfyColors = MaterialTheme.comfyColors

        ToastHost(state = toastState) {
            BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                val windowSizeInfo = rememberWindowSizeInfo(maxWidth, maxHeight)
                
                if (windowSizeInfo.shouldUseTwoPane) {
                    ExpandedWorkflowRunLayout(
                        model = model,
                        navigator = navigator,
                        comfyColors = comfyColors
                    )
                } else {
                    CompactWorkflowRunLayout(
                        model = model,
                        navigator = navigator,
                        comfyColors = comfyColors
                    )
                }
            }
        }
    }
}

/**
 * 小屏幕布局：底部面板
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CompactWorkflowRunLayout(
    model: WorkflowRunScreenModel,
    navigator: cafe.adriel.voyager.navigator.Navigator,
    comfyColors: moe.uni.comfyKmp.ui.theme.ComfyExtendedColors
) {
    val scaffoldState = rememberBottomSheetScaffoldState(
        bottomSheetState = rememberStandardBottomSheetState(
            initialValue = SheetValue.PartiallyExpanded,
            skipHiddenState = false
        )
    )
    val sheetPeekHeight = 120.dp

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val density = LocalDensity.current
        val layoutHeightPx = with(density) { maxHeight.toPx() }
        val peekHeightPx = with(density) { sheetPeekHeight.toPx() }

        BottomSheetScaffold(
            scaffoldState = scaffoldState,
            sheetPeekHeight = sheetPeekHeight,
            sheetContainerColor = comfyColors.cardBackground,
            sheetContentColor = MaterialTheme.colorScheme.onSurface,
            sheetShape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            sheetDragHandle = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Box(
                        modifier = Modifier
                            .width(40.dp)
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "节点状态 (${model.nodeStates.size})",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            },
            sheetContent = {
                NodeListSheet(
                    nodes = model.orderedNodeStates,
                    randomSeedNodes = model.randomSeedNodes,
                    modelChoices = model.modelChoices,
                    loadingModels = model.loadingModels,
                    onInputChange = { nodeId, field, value -> model.updateNodeInput(nodeId, field, value) },
                    onRandomSeedToggle = { nodeId, enabled -> model.setRandomSeedMode(nodeId, enabled) },
                    onRandomizeSeed = { nodeId -> model.randomizeSeed(nodeId) },
                    onLoadModels = { nodeId -> model.loadModelsForNode(nodeId) },
                    modifier = Modifier.navigationBarsPadding()
                )
            },
            topBar = {
                WorkflowRunTopBar(
                    onBack = { navigator.pop() },
                    status = model.executionStatus,
                    statusText = model.statusText
                )
            },
            containerColor = MaterialTheme.colorScheme.background
        ) { padding ->
            val layoutDirection = LocalLayoutDirection.current
            val startPadding = padding.calculateLeftPadding(layoutDirection)
            val endPadding = padding.calculateRightPadding(layoutDirection)
            val contentPadding = PaddingValues(
                start = startPadding,
                top = padding.calculateTopPadding(),
                end = endPadding,
                bottom = 0.dp
            )
            val sheetOffsetPx = runCatching {
                scaffoldState.bottomSheetState.requireOffset()
            }.getOrElse { (layoutHeightPx - peekHeightPx).coerceAtLeast(0f) }
            val sheetHeightPx = (layoutHeightPx - sheetOffsetPx).coerceAtLeast(0f)
            val sheetHeightOffset = with(density) { sheetHeightPx.toDp() }

            Box(
                modifier = Modifier
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
                ImageGallery(
                    images = model.galleryImages,
                    onImageClick = { index ->
                        model.getPreviewImage(index)?.let { preview ->
                            navigator.push(
                                ImagePreviewScreen(
                                    preview.filename,
                                    preview.bytes,
                                    onSave = { model.saveImage(index) },
                                    onSetCover = { model.setCoverImage(index) }
                                )
                            )
                        }
                    },
                    onSaveClick = { index -> model.saveImage(index) },
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(contentPadding)
                )

                WorkflowRunFab(
                    isRunning = model.running,
                    onStart = { model.runWorkflow() },
                    onInterrupt = { model.interrupt() },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .navigationBarsPadding()
                        .padding(end = ComfySpacing.lg, bottom = ComfySpacing.lg)
                        .offset(y = -sheetHeightOffset)
                )
            }
        }
    }
}

/**
 * 大屏幕布局：左右分栏
 * 左侧：图片预览区域
 * 右侧：节点列表
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ExpandedWorkflowRunLayout(
    model: WorkflowRunScreenModel,
    navigator: cafe.adriel.voyager.navigator.Navigator,
    comfyColors: moe.uni.comfyKmp.ui.theme.ComfyExtendedColors
) {
    Column(modifier = Modifier.fillMaxSize()) {
        // 顶部栏
        WorkflowRunTopBar(
            onBack = { navigator.pop() },
            status = model.executionStatus,
            statusText = model.statusText
        )
        
        // 主内容区：左右分栏
        Row(
            modifier = Modifier
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
            // 左侧：图片预览区域
            Box(
                modifier = Modifier
                    .weight(0.6f)
                    .fillMaxHeight()
            ) {
                ImageGallery(
                    images = model.galleryImages,
                    onImageClick = { index ->
                        model.getPreviewImage(index)?.let { preview ->
                            navigator.push(
                                ImagePreviewScreen(
                                    preview.filename,
                                    preview.bytes,
                                    onSave = { model.saveImage(index) },
                                    onSetCover = { model.setCoverImage(index) }
                                )
                            )
                        }
                    },
                    onSaveClick = { index -> model.saveImage(index) },
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(ComfySpacing.lg)
                )
                
                // FAB 放在左侧区域的右下角
                WorkflowRunFab(
                    isRunning = model.running,
                    onStart = { model.runWorkflow() },
                    onInterrupt = { model.interrupt() },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(ComfySpacing.lg)
                )
            }
            
            // 右侧：节点列表面板
            ExpandedNodeListPanel(
                model = model,
                comfyColors = comfyColors,
                modifier = Modifier
                    .widthIn(min = 320.dp, max = 420.dp)
                    .fillMaxHeight()
            )
        }
    }
}

/**
 * 大屏幕右侧节点列表面板
 */
@Composable
private fun ExpandedNodeListPanel(
    model: WorkflowRunScreenModel,
    comfyColors: moe.uni.comfyKmp.ui.theme.ComfyExtendedColors,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .background(comfyColors.cardBackground)
    ) {
        // 面板标题
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = ComfySpacing.lg, vertical = ComfySpacing.md),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "节点状态 (${model.nodeStates.size})",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        
        // 节点列表
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = ComfySpacing.lg,
                end = ComfySpacing.lg,
                bottom = ComfySpacing.xl
            ),
            verticalArrangement = Arrangement.spacedBy(ComfySpacing.md)
        ) {
            items(model.orderedNodeStates, key = { it.nodeId }) { node ->
                EditableNodeCard(
                    nodeId = node.nodeId,
                    classType = node.classType,
                    status = node.status,
                    inputs = node.inputs,
                    isRandomSeedEnabled = model.randomSeedNodes.contains(node.nodeId),
                    modelOptions = model.modelChoices[node.nodeId] ?: emptyList(),
                    isLoadingModels = model.loadingModels.contains(node.nodeId),
                    onInputChange = { field, value -> 
                        model.updateNodeInput(node.nodeId, field, value) 
                    },
                    onRandomSeedToggle = { enabled -> 
                        model.setRandomSeedMode(node.nodeId, enabled) 
                    },
                    onRandomizeSeed = { model.randomizeSeed(node.nodeId) },
                    onLoadModels = { model.loadModelsForNode(node.nodeId) }
                )
            }
        }
    }
}

private data class PreviewImage(
    val filename: String,
    val bytes: ByteArray
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WorkflowRunTopBar(
    onBack: () -> Unit,
    status: ExecutionStatus,
    statusText: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
    ) {
        TopAppBar(
            title = { Text("工作流执行") },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "返回"
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        )
        
        ExecutionStatusBar(
            status = status,
            statusText = statusText
        )
    }
}

@Composable
private fun WorkflowRunFab(
    isRunning: Boolean,
    onStart: () -> Unit,
    onInterrupt: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier) {
        AnimatedContent(
            targetState = isRunning,
            transitionSpec = { fadeIn() togetherWith fadeOut() }
        ) { running ->
            if (running) {
                ExtendedFloatingActionButton(
                    onClick = onInterrupt,
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Stop,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    text = { Text("中断") },
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer
                )
            } else {
                ExtendedFloatingActionButton(
                    onClick = onStart,
                    icon = {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    text = { Text("开始执行") },
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}

@Composable
private fun NodeListSheet(
    nodes: List<NodeExecutionState>,
    randomSeedNodes: Set<String>,
    modelChoices: Map<String, List<String>>,
    loadingModels: Set<String>,
    onInputChange: (nodeId: String, field: String, value: JsonElement) -> Unit,
    onRandomSeedToggle: (nodeId: String, enabled: Boolean) -> Unit,
    onRandomizeSeed: (nodeId: String) -> Unit,
    onLoadModels: (nodeId: String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(
            start = ComfySpacing.lg,
            end = ComfySpacing.lg,
            bottom = ComfySpacing.xl
        ),
        verticalArrangement = Arrangement.spacedBy(ComfySpacing.md)
    ) {
        items(nodes, key = { it.nodeId }) { node ->
            EditableNodeCard(
                nodeId = node.nodeId,
                classType = node.classType,
                status = node.status,
                inputs = node.inputs,
                isRandomSeedEnabled = randomSeedNodes.contains(node.nodeId),
                modelOptions = modelChoices[node.nodeId] ?: emptyList(),
                isLoadingModels = loadingModels.contains(node.nodeId),
                onInputChange = { field, value -> onInputChange(node.nodeId, field, value) },
                onRandomSeedToggle = { enabled -> onRandomSeedToggle(node.nodeId, enabled) },
                onRandomizeSeed = { onRandomizeSeed(node.nodeId) },
                onLoadModels = { onLoadModels(node.nodeId) }
            )
        }
    }
}

private class WorkflowRunScreenModel(
    private val workflowId: String,
    private val workflowRepository: WorkflowRepository,
    private val apiClient: ComfyApiClient,
    private val wsClient: ComfyWebSocketClient,
    private val serverRepository: ServerRepository,
    private val onToast: (String) -> Unit = {}
) : ScreenModel {
    private var workflow = workflowRepository.getWorkflow(workflowId)
    private val server = workflow?.let { serverRepository.getServers().firstOrNull { s -> s.id == it.serverId } }
    private val baseUrl: String = server?.baseUrl.orEmpty()
    private val clientId = generateId("client")
    
    // Editable prompt object
    private var promptObject by mutableStateOf(
        workflow?.json?.let { extractPromptObject(it) } ?: JsonObject(emptyMap())
    )

    var executionStatus by mutableStateOf(ExecutionStatus.IDLE)
        private set
    var statusText by mutableStateOf("准备就绪")
        private set
    var progress by mutableStateOf<Float?>(null)
        private set
    var progressText by mutableStateOf<String?>(null)
        private set
    var running by mutableStateOf(false)
        private set
    var nodeStates by mutableStateOf(buildNodeStates())
        private set
    private val initialNodeOrder = nodeStates.map { it.nodeId }
    var executionOrder by mutableStateOf<List<String>>(emptyList())
        private set
    
    // Editing state
    var randomSeedNodes by mutableStateOf<Set<String>>(emptySet())
        private set
    var modelChoices by mutableStateOf<Map<String, List<String>>>(emptyMap())
        private set
    var loadingModels by mutableStateOf<Set<String>>(emptySet())
        private set
    
    private var promptId by mutableStateOf<String?>(null)
    private var imageRefs by mutableStateOf<List<ImageRef>>(emptyList())
    private var imageBitmaps by mutableStateOf<Map<String, ImageBitmap>>(emptyMap())
    private var imageBytes by mutableStateOf<Map<String, ByteArray>>(emptyMap())
    private var isLoadingImages by mutableStateOf(false)
    private var saveJob: Job? = null
    private var wsEventsJob: Job? = null
    private var wsStateJob: Job? = null
    
    val galleryImages: List<GalleryImage>
        get() = imageRefs.map { ref ->
            GalleryImage(
                filename = ref.filename,
                bitmap = imageBitmaps[ref.filename],
                isLoading = isLoadingImages && !imageBitmaps.containsKey(ref.filename)
            )
        }

    val orderedNodeStates: List<NodeExecutionState>
        get() {
            val orderIndex = executionOrder.withIndex().associate { it.value to it.index }
            val fallbackIndex = initialNodeOrder.withIndex().associate { it.value to it.index }
            return nodeStates.sortedWith(
                compareBy<NodeExecutionState> { orderIndex[it.nodeId] ?: Int.MAX_VALUE }
                    .thenBy { fallbackIndex[it.nodeId] ?: Int.MAX_VALUE }
            )
        }

    init {
        randomSeedNodes = nodeStates
            .filter { detectNodeType(it.classType) == EditableNodeType.KSAMPLER }
            .map { it.nodeId }
            .toSet()
        preloadModelLists()
    }

    fun getPreviewImage(index: Int): PreviewImage? {
        val ref = imageRefs.getOrNull(index) ?: return null
        val bytes = imageBytes[ref.filename] ?: return null
        return PreviewImage(ref.filename, bytes)
    }
    
    private fun buildNodeStates(): List<NodeExecutionState> {
        val parsedNodes = parsePromptNodes(promptObject)
        return parsedNodes.map { node ->
            NodeExecutionState(
                nodeId = node.id,
                classType = node.classType,
                status = NodeStatus.PENDING,
                inputs = node.inputs.toMap()
            )
        }
    }
    
    fun updateNodeInput(nodeId: String, field: String, value: JsonElement) {
        promptObject = updateNodeInput(promptObject, nodeId, field, value)
        
        // Update node states
        nodeStates = nodeStates.map { state ->
            if (state.nodeId == nodeId) {
                state.copy(inputs = state.inputs.toMutableMap().apply { put(field, value) })
            } else state
        }
        
        scheduleSave()
    }
    
    fun setRandomSeedMode(nodeId: String, enabled: Boolean) {
        randomSeedNodes = if (enabled) {
            randomSeedNodes + nodeId
        } else {
            randomSeedNodes - nodeId
        }
    }
    
    fun randomizeSeed(nodeId: String) {
        val newSeed = Random.nextLong(0, Long.MAX_VALUE)
        updateNodeInput(nodeId, "seed", JsonPrimitive(newSeed))
    }
    
    fun loadModelsForNode(nodeId: String) {
        val node = nodeStates.find { it.nodeId == nodeId } ?: return
        val nodeType = detectNodeType(node.classType)
        val folder = getModelFolder(nodeType) ?: return
        
        if (modelChoices.containsKey(nodeId) || loadingModels.contains(nodeId)) return
        if (baseUrl.isBlank()) return
        
        loadingModels = loadingModels + nodeId
        
        screenModelScope.launch {
            try {
                val result = apiClient.getModels(baseUrl, folder)
                val list = result as? JsonArray
                val names = list?.mapNotNull { it.jsonPrimitive.contentOrNull } ?: emptyList()
                modelChoices = modelChoices + (nodeId to names)
            } catch (e: Exception) {
                statusText = "加载模型失败: ${e.message}"
            } finally {
                loadingModels = loadingModels - nodeId
            }
        }
    }

    private fun preloadModelLists() {
        val modelNodeIds = nodeStates.filter { node ->
            getModelFolder(detectNodeType(node.classType)) != null
        }.map { it.nodeId }
        modelNodeIds.forEach { loadModelsForNode(it) }
    }
    
    private fun scheduleSave() {
        saveJob?.cancel()
        saveJob = screenModelScope.launch {
            delay(350)
            saveWorkflow()
        }
    }

    private suspend fun saveWorkflow() {
        val current = workflow ?: return
        val snapshot = promptObject
        val newJson = withContext(Dispatchers.Default) {
            ComfyJson.encodeToString(JsonObject.serializer(), snapshot)
        }
        val updated = current.copy(
            json = newJson,
            updatedAt = Clock.System.now().toEpochMilliseconds()
        )
        workflowRepository.upsertWorkflow(updated)
        workflow = updated
    }

    fun connect() {
        if (baseUrl.isBlank()) {
            executionStatus = ExecutionStatus.ERROR
            statusText = "未设置服务器"
            return
        }
        
        executionStatus = ExecutionStatus.CONNECTING
        statusText = "正在连接..."

        wsEventsJob?.cancel()
        wsStateJob?.cancel()

        wsEventsJob = screenModelScope.launch {
            wsClient.events.collectLatest { msg ->
                handleWebSocketMessage(msg.type, msg.data)
            }
        }

        wsStateJob = screenModelScope.launch {
            wsClient.connectionState.collectLatest { state ->
                when (state.status) {
                    WsConnectionStatus.CONNECTING -> {
                        executionStatus = ExecutionStatus.CONNECTING
                        statusText = "正在连接..."
                    }
                    WsConnectionStatus.CONNECTED -> {
                        if (!running) {
                            executionStatus = ExecutionStatus.IDLE
                            statusText = "已连接"
                        }
                    }
                    WsConnectionStatus.RECONNECTING -> {
                        executionStatus = ExecutionStatus.CONNECTING
                        statusText = "连接断开，正在重连(${state.attempt}/${state.maxAttempts})"
                    }
                    WsConnectionStatus.FAILED -> {
                        executionStatus = ExecutionStatus.ERROR
                        statusText = "重连失败（已重试${state.maxAttempts}次）"
                    }
                    WsConnectionStatus.DISCONNECTED -> {
                        if (!running) {
                            executionStatus = ExecutionStatus.IDLE
                            statusText = "已断开"
                        }
                    }
                    WsConnectionStatus.IDLE -> Unit
                }
            }
        }

        screenModelScope.launch {
            wsClient.disconnect()
            wsClient.connect(baseUrl, clientId)
        }
    }

    fun disconnect() {
        wsEventsJob?.cancel()
        wsEventsJob = null
        wsStateJob?.cancel()
        wsStateJob = null
        wsClient.disconnectAsync()
    }

    override fun onDispose() {
        disconnect()
    }
    
    private fun handleWebSocketMessage(type: String, data: JsonObject?) {
        when (type) {
            "status" -> {
                if (!running) {
                    executionStatus = ExecutionStatus.IDLE
                    statusText = "已连接"
                }
            }
            "execution_start" -> {
                running = true
                executionStatus = ExecutionStatus.RUNNING
                statusText = "开始执行"
                executionOrder = emptyList()
                resetNodeStatuses()
            }
            "execution_cached" -> {
                val nodeIds = extractCachedNodes(data)
                if (nodeIds.isNotEmpty()) {
                    val newIds = nodeIds.filterNot { executionOrder.contains(it) }
                    if (newIds.isNotEmpty()) {
                        executionOrder = executionOrder + newIds
                    }
                }
                nodeIds.forEach { nodeId ->
                    updateNodeStatus(nodeId, NodeStatus.COMPLETED)
                }
            }
            "executing" -> {
                val nodeId = data?.get("node")?.jsonPrimitive?.content
                    ?.takeIf { it.isNotBlank() && it != "null" }
                if (nodeId != null) {
                    if (!executionOrder.contains(nodeId)) {
                        executionOrder = executionOrder + nodeId
                    }
                    // Mark previous running nodes as completed
                    nodeStates = nodeStates.map { state ->
                        if (state.status == NodeStatus.RUNNING) {
                            state.copy(status = NodeStatus.COMPLETED)
                        } else state
                    }
                    updateNodeStatus(nodeId, NodeStatus.RUNNING)
                    statusText = "执行: ${getNodeClassType(nodeId)}"
                } else {
                    // nodeId is null means execution finished
                    running = false
                    executionStatus = ExecutionStatus.COMPLETED
                    statusText = "执行完成"
                    markAllCompleted()
                    // Delay a bit to ensure history is available
                    screenModelScope.launch {
                        delay(500)
                        fetchResults()
                    }
                }
            }
            "progress" -> {
                val value = data?.get("value")?.jsonPrimitive?.content?.toFloatOrNull() ?: 0f
                val max = data?.get("max")?.jsonPrimitive?.content?.toFloatOrNull() ?: 1f
                progress = if (max > 0) value / max else null
                progressText = "${value.toInt()} / ${max.toInt()}"
            }
            "executed" -> {
                val nodeId = data?.get("node")?.jsonPrimitive?.content
                if (nodeId != null) {
                    updateNodeStatus(nodeId, NodeStatus.COMPLETED)
                }
                // Try to fetch results on each executed event (for intermediate results)
                screenModelScope.launch {
                    fetchResults()
                }
            }
            "execution_error" -> {
                running = false
                executionStatus = ExecutionStatus.ERROR
                val errorMsg = data?.get("exception_message")?.jsonPrimitive?.content
                statusText = "执行出错: ${errorMsg ?: "未知错误"}"
                val nodeId = data?.get("node_id")?.jsonPrimitive?.content
                if (nodeId != null) {
                    updateNodeStatus(nodeId, NodeStatus.ERROR)
                }
            }
        }
    }

    fun runWorkflow() {
        // Check if prompt is empty
        if (promptObject.isEmpty()) {
            executionStatus = ExecutionStatus.ERROR
            statusText = "工作流为空或格式错误"
            return
        }
        
        // Generate random seeds for nodes with random mode enabled
        randomSeedNodes.forEach { nodeId ->
            randomizeSeed(nodeId)
        }
        
        resetNodeStatuses()
        progress = null
        progressText = null
        
        screenModelScope.launch {
            executionStatus = ExecutionStatus.RUNNING
            statusText = "提交中..."
            
            try {
                val response = apiClient.postPrompt(baseUrl, PromptRequest(promptObject, clientId))
                if (response.error != null) {
                    executionStatus = ExecutionStatus.ERROR
                    statusText = "错误: ${response.error}"
                    running = false
                } else if (response.node_errors != null && response.node_errors.isNotEmpty()) {
                    executionStatus = ExecutionStatus.ERROR
                    val firstError = response.node_errors.entries.firstOrNull()
                    statusText = "节点错误: ${firstError?.key}"
                    running = false
                } else {
                    promptId = response.prompt_id
                    running = true
                    statusText = "已提交: ${response.prompt_id?.take(8)}..."
                }
            } catch (e: Exception) {
                executionStatus = ExecutionStatus.ERROR
                statusText = "提交失败: ${e.message}"
                running = false
            }
        }
    }

    fun interrupt() {
        if (baseUrl.isBlank()) return
        screenModelScope.launch {
            try {
                apiClient.interrupt(baseUrl)
                running = false
                executionStatus = ExecutionStatus.IDLE
                statusText = "已中断"
            } catch (e: Exception) {
                statusText = "中断失败: ${e.message}"
            }
        }
    }

    private fun fetchResults() {
        val id = promptId ?: return
        screenModelScope.launch {
            try {
                val history = apiClient.getHistory(baseUrl, id)
                val newRefs = extractImagesFromHistory(history)
                if (newRefs.isNotEmpty() && newRefs != imageRefs) {
                    imageRefs = newRefs
                    loadImages()
                }
            } catch (e: Exception) {
                // Silently ignore - history might not be ready yet
            }
        }
    }
    
    private fun loadImages() {
        isLoadingImages = true
        imageRefs.forEach { ref ->
            if (!imageBitmaps.containsKey(ref.filename)) {
                screenModelScope.launch {
                    try {
                        val url = buildViewUrl(ref)
                        val bytes = apiClient.getBytes(url)
                        imageBytes = imageBytes + (ref.filename to bytes)
                        val bitmap = bytes.decodeToImageBitmap()
                        imageBitmaps = imageBitmaps + (ref.filename to bitmap)
                    } catch (e: Exception) {
                        // Image loading failed
                    } finally {
                        // Check if all images are loaded
                        if (imageBitmaps.size >= imageRefs.size) {
                            isLoadingImages = false
                        }
                    }
                }
            }
        }
        // If all images are already cached
        if (imageRefs.all { imageBitmaps.containsKey(it.filename) }) {
            isLoadingImages = false
        }
    }
    
    fun saveImage(index: Int) {
        val ref = imageRefs.getOrNull(index) ?: return
        val bytes = imageBytes[ref.filename] ?: return
        screenModelScope.launch {
            try {
                val location = saveImageToGallery(bytes, ref.filename)
                onToast("已保存: $location")
            } catch (e: Exception) {
                onToast("保存失败: ${e.message}")
            }
        }
    }
    
    fun setCoverImage(index: Int) {
        val ref = imageRefs.getOrNull(index) ?: return
        val bytes = imageBytes[ref.filename] ?: return
        val current = workflow ?: return
        
        val filename = "cover_${current.id}_${Clock.System.now().toEpochMilliseconds()}.jpg"
        val path = saveCoverImage(bytes, filename)
        val updated = current.copy(
            coverImage = path,
            updatedAt = Clock.System.now().toEpochMilliseconds()
        )
        workflowRepository.upsertWorkflow(updated)
        workflow = updated
        onToast("已设置为封面")
    }

    private fun updateNodeStatus(nodeId: String, status: NodeStatus) {
        nodeStates = nodeStates.map { state ->
            if (state.nodeId == nodeId) state.copy(status = status)
            else state
        }
    }
    
    private fun resetNodeStatuses() {
        nodeStates = nodeStates.map { it.copy(status = NodeStatus.PENDING) }
    }
    
    private fun markAllCompleted() {
        nodeStates = nodeStates.map { state ->
            if (state.status == NodeStatus.RUNNING || state.status == NodeStatus.PENDING) {
                state.copy(status = NodeStatus.COMPLETED)
            } else state
        }
    }
    
    private fun getNodeClassType(nodeId: String): String {
        return nodeStates.find { it.nodeId == nodeId }?.classType ?: nodeId
    }
    
    private fun extractCachedNodes(data: JsonObject?): List<String> {
        // Extract node IDs from execution_cached event
        val nodes = data?.get("nodes")
        return if (nodes is JsonArray) {
            nodes.mapNotNull { it.jsonPrimitive.content }
        } else {
            emptyList()
        }
    }

    private fun buildViewUrl(ref: ImageRef): String {
        val params = buildString {
            append("filename=").append(ref.filename)
            if (!ref.subfolder.isNullOrBlank()) append("&subfolder=").append(ref.subfolder)
            if (!ref.type.isNullOrBlank()) append("&type=").append(ref.type)
        }
        return "${baseUrl.trimEnd('/')}/view?$params"
    }
}
