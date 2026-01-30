package moe.uni.comfy_kmp.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.longOrNull
import moe.uni.comfy_kmp.data.NodeStatus
import moe.uni.comfy_kmp.ui.theme.ComfySpacing
import moe.uni.comfy_kmp.ui.theme.comfyColors
import kotlin.math.roundToInt

@Composable
fun NodeCard(
    nodeId: String,
    classType: String,
    status: NodeStatus,
    inputs: Map<String, JsonElement> = emptyMap(),
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    val comfyColors = MaterialTheme.comfyColors
    
    val statusColor by animateColorAsState(
        targetValue = when (status) {
            NodeStatus.RUNNING -> comfyColors.nodeRunning
            NodeStatus.COMPLETED -> comfyColors.nodeCompleted
            NodeStatus.PENDING -> comfyColors.nodePending
            NodeStatus.ERROR -> MaterialTheme.colorScheme.error
        },
        animationSpec = tween(300)
    )
    
    val cardAlpha by animateFloatAsState(
        targetValue = if (status == NodeStatus.PENDING) 0.7f else 1f,
        animationSpec = tween(200)
    )
    
    val shape = RoundedCornerShape(12.dp)
    
    Box(
        modifier = modifier
            .fillMaxWidth()
            .alpha(cardAlpha)
            .shadow(
                elevation = if (status == NodeStatus.RUNNING) 6.dp else 2.dp,
                shape = shape,
                ambientColor = statusColor.copy(alpha = 0.2f),
                spotColor = statusColor.copy(alpha = 0.25f)
            )
            .clip(shape)
            .background(comfyColors.cardBackground)
            .border(
                width = if (status == NodeStatus.RUNNING) 2.dp else 1.dp,
                color = if (status == NodeStatus.RUNNING) statusColor else comfyColors.cardBorder,
                shape = shape
            )
            .then(
                if (onClick != null) Modifier.clickable(onClick = onClick)
                else Modifier
            )
            .padding(ComfySpacing.md)
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(ComfySpacing.sm)
        ) {
            // Header row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(ComfySpacing.sm),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Status indicator
                    StatusDot(status = status, color = statusColor)
                    
                    // Class type
                    Text(
                        text = classType,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                
                // Node ID badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "#$nodeId",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            // Key inputs preview
            val previewInputs = inputs.entries
                .filter { (_, value) -> value is JsonPrimitive }
                .take(3)
            
            if (previewInputs.isNotEmpty()) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(ComfySpacing.xs)
                ) {
                    previewInputs.forEach { (key, value) ->
                        InputPreviewRow(key = key, value = value)
                    }
                }
            }
        }
    }
}

@Composable
private fun StatusDot(
    status: NodeStatus,
    color: Color
) {
    val dotSize = when (status) {
        NodeStatus.RUNNING -> 10.dp
        else -> 8.dp
    }
    
    Box(
        modifier = Modifier
            .size(dotSize)
            .clip(CircleShape)
            .background(color)
            .then(
                if (status == NodeStatus.RUNNING) {
                    Modifier.border(2.dp, color.copy(alpha = 0.3f), CircleShape)
                } else Modifier
            )
    )
}

@Composable
private fun InputPreviewRow(
    key: String,
    value: JsonElement
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(ComfySpacing.sm),
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = "$key:",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = formatValue(value),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
    }
}

private fun formatValue(value: JsonElement): String {
    return when (value) {
        is JsonPrimitive -> {
            val content = value.contentOrNull ?: value.toString()
            if (content.length > 50) content.take(47) + "..." else content
        }
        else -> value.toString().take(50)
    }
}

// Node type detection helpers
enum class EditableNodeType {
    KSAMPLER,
    CHECKPOINT_LOADER,
    VAE_LOADER,
    LORA_LOADER,
    CLIP_TEXT_ENCODE,
    EMPTY_LATENT_IMAGE,
    OTHER
}

fun detectNodeType(classType: String): EditableNodeType {
    val normalized = classType.lowercase()
    return when {
        normalized.contains("ksampler") -> EditableNodeType.KSAMPLER
        normalized.contains("checkpointloader") || 
        normalized.contains("checkpoint_loader") ||
        normalized == "load checkpoint" -> EditableNodeType.CHECKPOINT_LOADER
        normalized.contains("vaeloader") || 
        normalized.contains("vae_loader") ||
        normalized == "load vae" -> EditableNodeType.VAE_LOADER
        normalized.contains("loraloader") || 
        normalized.contains("lora_loader") ||
        normalized == "load lora" -> EditableNodeType.LORA_LOADER
        normalized.contains("cliptextencode") || 
        normalized.contains("clip_text_encode") ||
        normalized == "clip text encode" -> EditableNodeType.CLIP_TEXT_ENCODE
        normalized.contains("emptylatentimage") ||
        normalized.contains("empty_latent_image") ||
        normalized == "empty latent image" -> EditableNodeType.EMPTY_LATENT_IMAGE
        else -> EditableNodeType.OTHER
    }
}

fun getModelFolder(nodeType: EditableNodeType): String? {
    return when (nodeType) {
        EditableNodeType.CHECKPOINT_LOADER -> "checkpoints"
        EditableNodeType.VAE_LOADER -> "vae"
        EditableNodeType.LORA_LOADER -> "loras"
        else -> null
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditableNodeCard(
    nodeId: String,
    classType: String,
    status: NodeStatus,
    inputs: Map<String, JsonElement>,
    isRandomSeedEnabled: Boolean = false,
    modelOptions: List<String> = emptyList(),
    isLoadingModels: Boolean = false,
    modifier: Modifier = Modifier,
    onInputChange: (field: String, value: JsonElement) -> Unit = { _, _ -> },
    onRandomSeedToggle: (enabled: Boolean) -> Unit = {},
    onRandomizeSeed: () -> Unit = {},
    onLoadModels: () -> Unit = {}
) {
    val comfyColors = MaterialTheme.comfyColors
    val nodeType = remember(classType) { detectNodeType(classType) }
    var isExpanded by remember { mutableStateOf(nodeType != EditableNodeType.OTHER) }
    
    val statusColor by animateColorAsState(
        targetValue = when (status) {
            NodeStatus.RUNNING -> comfyColors.nodeRunning
            NodeStatus.COMPLETED -> comfyColors.nodeCompleted
            NodeStatus.PENDING -> comfyColors.nodePending
            NodeStatus.ERROR -> MaterialTheme.colorScheme.error
        },
        animationSpec = tween(300)
    )
    
    val cardAlpha by animateFloatAsState(
        targetValue = if (status == NodeStatus.PENDING) 0.85f else 1f,
        animationSpec = tween(200)
    )
    
    val shape = RoundedCornerShape(16.dp)
    
    Box(
        modifier = modifier
            .fillMaxWidth()
            .alpha(cardAlpha)
            .shadow(
                elevation = if (status == NodeStatus.RUNNING) 8.dp else 3.dp,
                shape = shape,
                ambientColor = statusColor.copy(alpha = 0.15f),
                spotColor = statusColor.copy(alpha = 0.2f)
            )
            .clip(shape)
            .background(comfyColors.cardBackground)
            .border(
                width = if (status == NodeStatus.RUNNING) 2.dp else 1.dp,
                color = if (status == NodeStatus.RUNNING) statusColor else comfyColors.cardBorder,
                shape = shape
            )
            .clickable { isExpanded = !isExpanded }
            .padding(ComfySpacing.lg)
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(ComfySpacing.md)
        ) {
            // Header row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(ComfySpacing.sm),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    StatusDot(status = status, color = statusColor)
                    Column {
                        Text(
                            text = classType,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (nodeType != EditableNodeType.OTHER) {
                            Text(
                                text = when (nodeType) {
                                    EditableNodeType.KSAMPLER -> "采样器"
                                    EditableNodeType.CHECKPOINT_LOADER -> "模型"
                                    EditableNodeType.VAE_LOADER -> "VAE"
                                    EditableNodeType.LORA_LOADER -> "LoRA"
                                    EditableNodeType.CLIP_TEXT_ENCODE -> "提示词"
                                    EditableNodeType.EMPTY_LATENT_IMAGE -> "空潜图"
                                    else -> ""
                                },
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(ComfySpacing.sm),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "#$nodeId",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = if (isExpanded) "收起" else "展开",
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            // Editable content
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically(tween(200)),
                exit = shrinkVertically(tween(200))
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(ComfySpacing.md),
                    modifier = Modifier.padding(top = ComfySpacing.sm)
                ) {
                    when (nodeType) {
                        EditableNodeType.KSAMPLER -> {
                            KSamplerEditor(
                                inputs = inputs,
                                isRandomSeedEnabled = isRandomSeedEnabled,
                                onInputChange = onInputChange,
                                onRandomSeedToggle = onRandomSeedToggle,
                                onRandomizeSeed = onRandomizeSeed
                            )
                        }
                        EditableNodeType.CHECKPOINT_LOADER,
                        EditableNodeType.VAE_LOADER,
                        EditableNodeType.LORA_LOADER -> {
                            ModelSelector(
                                nodeType = nodeType,
                                inputs = inputs,
                                modelOptions = modelOptions,
                                isLoading = isLoadingModels,
                                onInputChange = onInputChange,
                                onLoadModels = onLoadModels
                            )
                        }
                        EditableNodeType.CLIP_TEXT_ENCODE -> {
                            PromptEditor(
                                inputs = inputs,
                                onInputChange = onInputChange
                            )
                        }
                        EditableNodeType.EMPTY_LATENT_IMAGE -> {
                            EmptyLatentImageEditor(
                                inputs = inputs,
                                onInputChange = onInputChange
                            )
                        }
                        EditableNodeType.OTHER -> {
                            // Show read-only inputs for other node types
                            val previewInputs = inputs.entries
                                .filter { (_, value) -> value is JsonPrimitive }
                                .take(5)
                            previewInputs.forEach { (key, value) ->
                                InputPreviewRow(key = key, value = value)
                            }
                        }
                    }
                }
            }
            
            // Collapsed preview for non-expanded state
            if (!isExpanded && nodeType == EditableNodeType.OTHER) {
                val previewInputs = inputs.entries
                    .filter { (_, value) -> value is JsonPrimitive }
                    .take(2)
                if (previewInputs.isNotEmpty()) {
                    Column(verticalArrangement = Arrangement.spacedBy(ComfySpacing.xs)) {
                        previewInputs.forEach { (key, value) ->
                            InputPreviewRow(key = key, value = value)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun KSamplerEditor(
    inputs: Map<String, JsonElement>,
    isRandomSeedEnabled: Boolean,
    onInputChange: (String, JsonElement) -> Unit,
    onRandomSeedToggle: (Boolean) -> Unit,
    onRandomizeSeed: () -> Unit
) {
    val seedValue = inputs["seed"]?.let { 
        (it as? JsonPrimitive)?.longOrNull?.toString() 
    } ?: ""
    val stepsValue = inputs["steps"]?.let { 
        (it as? JsonPrimitive)?.contentOrNull 
    } ?: ""
    val cfgValue = inputs["cfg"]?.let { 
        (it as? JsonPrimitive)?.contentOrNull 
    } ?: ""
    
    Column(verticalArrangement = Arrangement.spacedBy(ComfySpacing.md)) {
        // Seed with random mode
        Column(verticalArrangement = Arrangement.spacedBy(ComfySpacing.sm)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Seed",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(ComfySpacing.sm),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "随机",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Switch(
                        checked = isRandomSeedEnabled,
                        onCheckedChange = onRandomSeedToggle
                    )
                }
            }
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(ComfySpacing.sm),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = seedValue,
                    onValueChange = { newValue ->
                        newValue.toLongOrNull()?.let {
                            onInputChange("seed", JsonPrimitive(it))
                        }
                    },
                    modifier = Modifier.weight(1f),
                    enabled = !isRandomSeedEnabled,
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    placeholder = { Text("输入 seed") }
                )
                
                FilledTonalIconButton(
                    onClick = onRandomizeSeed
                ) {
                    Icon(
                        imageVector = Icons.Default.Casino,
                        contentDescription = "随机生成",
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
        
        // Steps
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(ComfySpacing.md)
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(ComfySpacing.xs)
            ) {
                Text(
                    text = "Steps",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                OutlinedTextField(
                    value = stepsValue,
                    onValueChange = { newValue ->
                        newValue.toIntOrNull()?.let {
                            onInputChange("steps", JsonPrimitive(it))
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )
            }
            
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(ComfySpacing.xs)
            ) {
                Text(
                    text = "CFG",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                OutlinedTextField(
                    value = cfgValue,
                    onValueChange = { newValue ->
                        newValue.toDoubleOrNull()?.let {
                            onInputChange("cfg", JsonPrimitive(it))
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ModelSelector(
    nodeType: EditableNodeType,
    inputs: Map<String, JsonElement>,
    modelOptions: List<String>,
    isLoading: Boolean,
    onInputChange: (String, JsonElement) -> Unit,
    onLoadModels: () -> Unit
) {
    val fieldName = when (nodeType) {
        EditableNodeType.CHECKPOINT_LOADER -> "ckpt_name"
        EditableNodeType.VAE_LOADER -> "vae_name"
        EditableNodeType.LORA_LOADER -> "lora_name"
        else -> return
    }
    
    val currentValue = inputs[fieldName]?.let { 
        (it as? JsonPrimitive)?.contentOrNull 
    } ?: ""
    
    var expanded by remember { mutableStateOf(false) }
    
    Column(verticalArrangement = Arrangement.spacedBy(ComfySpacing.md)) {
        Column(verticalArrangement = Arrangement.spacedBy(ComfySpacing.sm)) {
            Text(
                text = when (nodeType) {
                    EditableNodeType.CHECKPOINT_LOADER -> "模型"
                    EditableNodeType.VAE_LOADER -> "VAE"
                    EditableNodeType.LORA_LOADER -> "LoRA"
                    else -> "选择"
                },
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            if (modelOptions.isEmpty() && !isLoading) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(ComfySpacing.sm),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = currentValue.ifEmpty { "未选择" },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    FilledTonalButton(
                        onClick = onLoadModels,
                        contentPadding = PaddingValues(horizontal = ComfySpacing.md)
                    ) {
                        Text("加载列表")
                    }
                }
            } else if (isLoading) {
                Text(
                    text = "加载中...",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = it }
                ) {
                    OutlinedTextField(
                        value = currentValue,
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )
                    
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        modelOptions.forEach { option ->
                            DropdownMenuItem(
                                text = { 
                                    Text(
                                        option,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    ) 
                                },
                                onClick = {
                                    onInputChange(fieldName, JsonPrimitive(option))
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }
        }
        
        if (nodeType == EditableNodeType.LORA_LOADER) {
            val strengthModel = inputs["strength_model"]?.let { 
                (it as? JsonPrimitive)?.contentOrNull?.toFloatOrNull() 
            } ?: 1.0f
            
            val strengthClip = inputs["strength_clip"]?.let { 
                (it as? JsonPrimitive)?.contentOrNull?.toFloatOrNull() 
            } ?: 1.0f
            
            Column(verticalArrangement = Arrangement.spacedBy(ComfySpacing.sm)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Model 强度",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = formatTwoDecimals(strengthModel),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                
                Slider(
                    value = strengthModel,
                    onValueChange = { newValue ->
                        onInputChange("strength_model", JsonPrimitive(newValue.toDouble()))
                    },
                    valueRange = 0f..2f,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            
            Column(verticalArrangement = Arrangement.spacedBy(ComfySpacing.sm)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "CLIP 强度",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = formatTwoDecimals(strengthClip),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                
                Slider(
                    value = strengthClip,
                    onValueChange = { newValue ->
                        onInputChange("strength_clip", JsonPrimitive(newValue.toDouble()))
                    },
                    valueRange = 0f..2f,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

private fun formatTwoDecimals(value: Float): String {
    val scaled = (value * 100).roundToInt()
    val whole = scaled / 100
    val fraction = kotlin.math.abs(scaled % 100)
    return "$whole.${fraction.toString().padStart(2, '0')}"
}

@Composable
private fun EmptyLatentImageEditor(
    inputs: Map<String, JsonElement>,
    onInputChange: (String, JsonElement) -> Unit
) {
    val widthValue = inputs["width"]?.let { (it as? JsonPrimitive)?.contentOrNull } ?: ""
    val heightValue = inputs["height"]?.let { (it as? JsonPrimitive)?.contentOrNull } ?: ""
    val batchValue = inputs["batch_size"]?.let { (it as? JsonPrimitive)?.contentOrNull } ?: ""

    Column(verticalArrangement = Arrangement.spacedBy(ComfySpacing.md)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(ComfySpacing.md)
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(ComfySpacing.xs)
            ) {
                Text(
                    text = "宽度",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                OutlinedTextField(
                    value = widthValue,
                    onValueChange = { newValue ->
                        newValue.toIntOrNull()?.let { onInputChange("width", JsonPrimitive(it)) }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    placeholder = { Text("如 512") }
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(ComfySpacing.xs)
            ) {
                Text(
                    text = "高度",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                OutlinedTextField(
                    value = heightValue,
                    onValueChange = { newValue ->
                        newValue.toIntOrNull()?.let { onInputChange("height", JsonPrimitive(it)) }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    placeholder = { Text("如 512") }
                )
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(ComfySpacing.xs)) {
            Text(
                text = "批量",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            OutlinedTextField(
                value = batchValue,
                onValueChange = { newValue ->
                    newValue.toIntOrNull()?.let { onInputChange("batch_size", JsonPrimitive(it)) }
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                placeholder = { Text("如 1") }
            )
        }
    }
}

@Composable
private fun PromptEditor(
    inputs: Map<String, JsonElement>,
    onInputChange: (String, JsonElement) -> Unit
) {
    val textValue = inputs["text"]?.let { 
        (it as? JsonPrimitive)?.contentOrNull 
    } ?: ""
    
    Column(verticalArrangement = Arrangement.spacedBy(ComfySpacing.sm)) {
        Text(
            text = "提示词",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        OutlinedTextField(
            value = textValue,
            onValueChange = { newValue ->
                onInputChange("text", JsonPrimitive(newValue))
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp),
            shape = RoundedCornerShape(12.dp),
            placeholder = { Text("输入提示词...") }
        )
    }
}
