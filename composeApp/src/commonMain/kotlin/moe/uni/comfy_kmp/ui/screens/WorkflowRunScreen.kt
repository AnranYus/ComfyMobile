package moe.uni.comfy_kmp.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonPrimitive
import moe.uni.comfy_kmp.data.ComfyJson
import moe.uni.comfy_kmp.data.ImageRef
import moe.uni.comfy_kmp.data.NodeExecutionState
import moe.uni.comfy_kmp.data.NodeStatus
import moe.uni.comfy_kmp.data.PromptRequest
import moe.uni.comfy_kmp.data.extractImagesFromHistory
import moe.uni.comfy_kmp.data.extractPromptObject
import moe.uni.comfy_kmp.data.parsePromptNodes
import moe.uni.comfy_kmp.data.saveCoverImage
import moe.uni.comfy_kmp.data.saveToTemp
import moe.uni.comfy_kmp.data.updateNodeInput
import moe.uni.comfy_kmp.di.LocalAppContainer
import moe.uni.comfy_kmp.network.ComfyApiClient
import moe.uni.comfy_kmp.network.ComfyWebSocketClient
import moe.uni.comfy_kmp.storage.ServerRepository
import moe.uni.comfy_kmp.storage.WorkflowRepository
import moe.uni.comfy_kmp.storage.generateId
import moe.uni.comfy_kmp.ui.components.EditableNodeCard
import moe.uni.comfy_kmp.ui.components.ExecutionStatus
import moe.uni.comfy_kmp.ui.components.ExecutionStatusBar
import moe.uni.comfy_kmp.ui.components.GalleryImage
import moe.uni.comfy_kmp.ui.components.ImageGallery
import moe.uni.comfy_kmp.ui.components.detectNodeType
import moe.uni.comfy_kmp.ui.components.getModelFolder
import moe.uni.comfy_kmp.ui.theme.ComfySpacing
import moe.uni.comfy_kmp.ui.theme.comfyColors
import org.jetbrains.compose.resources.decodeToImageBitmap
import kotlin.random.Random
import kotlin.time.Clock

data class WorkflowRunScreen(val workflowId: String) : Screen {
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val container = LocalAppContainer.current
        val model = rememberScreenModel {
            WorkflowRunScreenModel(
                workflowId = workflowId,
                workflowRepository = container.workflowRepository,
                apiClient = container.apiClient,
                wsClient = container.wsClient,
                serverRepository = container.serverRepository
            )
        }

        LaunchedEffect(Unit) {
            model.connect()
        }

        val scaffoldState = rememberBottomSheetScaffoldState(
            bottomSheetState = rememberStandardBottomSheetState(
                initialValue = SheetValue.PartiallyExpanded,
                skipHiddenState = false
            )
        )

        val comfyColors = MaterialTheme.comfyColors

        BottomSheetScaffold(
            scaffoldState = scaffoldState,
            sheetPeekHeight = 120.dp,
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
                    statusText = model.statusText,
                    progress = model.progress,
                    progressText = model.progressText,
                    isRunning = model.running,
                    onStart = { model.runWorkflow() },
                    onInterrupt = { model.interrupt() }
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
                ImageGallery(
                    images = model.galleryImages,
                    onImageClick = { index ->
                        model.getPreviewImage(index)?.let { preview ->
                            navigator.push(
                                ImagePreviewScreen(
                                    preview.filename,
                                    preview.bytes,
                                    onSave = { model.saveImage(index) }
                                )
                            )
                        }
                    },
                    onSaveClick = { index -> model.saveImage(index) },
                    onSetCoverClick = { index -> model.setCoverImage(index) },
                    modifier = Modifier.fillMaxSize()
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
    statusText: String,
    progress: Float?,
    progressText: String?,
    isRunning: Boolean,
    onStart: () -> Unit,
    onInterrupt: () -> Unit
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
            actions = {
                AnimatedContent(
                    targetState = isRunning,
                    transitionSpec = { fadeIn() togetherWith fadeOut() }
                ) { running ->
                    if (running) {
                        OutlinedButton(
                            onClick = onInterrupt,
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.error
                            ),
                            modifier = Modifier.padding(end = ComfySpacing.sm)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Stop,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(ComfySpacing.xs))
                            Text("中断")
                        }
                    } else {
                        FilledTonalButton(
                            onClick = onStart,
                            modifier = Modifier.padding(end = ComfySpacing.sm)
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(ComfySpacing.xs))
                            Text("开始执行")
                        }
                    }
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        )
        
        ExecutionStatusBar(
            status = status,
            statusText = statusText,
            progress = progress,
            progressText = progressText
        )
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
    private val serverRepository: ServerRepository
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

        screenModelScope.launch {
            wsClient.disconnect()
            wsClient.connect(baseUrl, clientId)
            wsClient.events.collectLatest { msg ->
                handleWebSocketMessage(msg.type, msg.data)
            }
        }
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
                val path = saveToTemp(bytes, ref.filename)
                statusText = "已保存: $path"
            } catch (e: Exception) {
                statusText = "保存失败: ${e.message}"
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
        statusText = "已设置为封面"
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
