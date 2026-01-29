package moe.uni.comfy_kmp.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.dp
import moe.uni.comfy_kmp.ui.theme.ComfySpacing
import moe.uni.comfy_kmp.ui.theme.comfyColors

enum class ExecutionStatus {
    IDLE,
    CONNECTING,
    RUNNING,
    COMPLETED,
    ERROR
}

@Composable
fun ExecutionStatusBar(
    status: ExecutionStatus,
    statusText: String,
    progress: Float? = null,
    progressText: String? = null,
    modifier: Modifier = Modifier
) {
    val comfyColors = MaterialTheme.comfyColors
    
    val statusColor by animateColorAsState(
        targetValue = when (status) {
            ExecutionStatus.IDLE -> comfyColors.nodePending
            ExecutionStatus.CONNECTING -> MaterialTheme.colorScheme.primary
            ExecutionStatus.RUNNING -> comfyColors.nodeRunning
            ExecutionStatus.COMPLETED -> comfyColors.nodeCompleted
            ExecutionStatus.ERROR -> MaterialTheme.colorScheme.error
        },
        animationSpec = tween(300)
    )
    
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = ComfySpacing.lg, vertical = ComfySpacing.md),
        verticalArrangement = Arrangement.spacedBy(ComfySpacing.sm)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(ComfySpacing.sm)
        ) {
            // Animated status indicator
            StatusIndicator(status = status, color = statusColor)
            
            // Status text with animation
            AnimatedContent(
                targetState = statusText,
                transitionSpec = {
                    fadeIn(tween(200)) togetherWith fadeOut(tween(200))
                }
            ) { text ->
                Text(
                    text = text,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            // Progress text
            progressText?.let { text ->
                Text(
                    text = text,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        
        // Progress bar
        if (status == ExecutionStatus.RUNNING) {
            if (progress != null && progress > 0f) {
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp)),
                    color = statusColor,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                    strokeCap = StrokeCap.Round
                )
            } else {
                LinearProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp)),
                    color = statusColor,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                    strokeCap = StrokeCap.Round
                )
            }
        }
    }
}

@Composable
private fun StatusIndicator(
    status: ExecutionStatus,
    color: androidx.compose.ui.graphics.Color
) {
    val infiniteTransition = rememberInfiniteTransition()
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0.4f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )
    
    Box(
        modifier = Modifier
            .size(10.dp)
            .alpha(if (status == ExecutionStatus.RUNNING) pulseAlpha else 1f)
            .clip(CircleShape)
            .background(color)
    )
}

@Composable
fun CompactStatusChip(
    status: ExecutionStatus,
    text: String,
    modifier: Modifier = Modifier
) {
    val comfyColors = MaterialTheme.comfyColors
    
    val backgroundColor by animateColorAsState(
        targetValue = when (status) {
            ExecutionStatus.IDLE -> MaterialTheme.colorScheme.surfaceVariant
            ExecutionStatus.CONNECTING -> MaterialTheme.colorScheme.primaryContainer
            ExecutionStatus.RUNNING -> comfyColors.warningContainer
            ExecutionStatus.COMPLETED -> comfyColors.successContainer
            ExecutionStatus.ERROR -> MaterialTheme.colorScheme.errorContainer
        },
        animationSpec = tween(200)
    )
    
    val textColor by animateColorAsState(
        targetValue = when (status) {
            ExecutionStatus.IDLE -> MaterialTheme.colorScheme.onSurfaceVariant
            ExecutionStatus.CONNECTING -> MaterialTheme.colorScheme.onPrimaryContainer
            ExecutionStatus.RUNNING -> comfyColors.onWarning
            ExecutionStatus.COMPLETED -> comfyColors.onSuccess
            ExecutionStatus.ERROR -> MaterialTheme.colorScheme.onErrorContainer
        },
        animationSpec = tween(200)
    )
    
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(backgroundColor)
            .padding(horizontal = ComfySpacing.md, vertical = ComfySpacing.sm)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            color = textColor
        )
    }
}
