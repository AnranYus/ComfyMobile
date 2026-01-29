package moe.uni.comfy_kmp.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import moe.uni.comfy_kmp.ui.theme.ComfySpacing
import moe.uni.comfy_kmp.ui.theme.comfyColors

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 16.dp,
    onClick: (() -> Unit)? = null,
    isSelected: Boolean = false,
    content: @Composable BoxScope.() -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()
    val isPressed by interactionSource.collectIsPressedAsState()
    
    val elevation by animateDpAsState(
        targetValue = when {
            isPressed -> 2.dp
            isHovered -> 8.dp
            isSelected -> 6.dp
            else -> 4.dp
        },
        animationSpec = tween(150)
    )
    
    val borderColor by animateColorAsState(
        targetValue = when {
            isSelected -> MaterialTheme.colorScheme.primary
            isHovered -> MaterialTheme.colorScheme.outline
            else -> MaterialTheme.comfyColors.cardBorder
        },
        animationSpec = tween(150)
    )
    
    val shape = RoundedCornerShape(cornerRadius)
    
    Box(
        modifier = modifier
            .shadow(
                elevation = elevation,
                shape = shape,
                ambientColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
            )
            .clip(shape)
            .background(MaterialTheme.comfyColors.cardBackground)
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = borderColor,
                shape = shape
            )
            .then(
                if (onClick != null) {
                    Modifier.clickable(
                        interactionSource = interactionSource,
                        indication = null,
                        onClick = onClick
                    )
                } else Modifier
            )
            .padding(ComfySpacing.lg),
        content = content
    )
}

@Composable
fun GlassCardCompact(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 12.dp,
    backgroundColor: Color = MaterialTheme.comfyColors.cardBackground,
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit
) {
    val shape = RoundedCornerShape(cornerRadius)
    
    Box(
        modifier = modifier
            .shadow(
                elevation = 2.dp,
                shape = shape
            )
            .clip(shape)
            .background(backgroundColor)
            .border(
                width = 1.dp,
                color = MaterialTheme.comfyColors.cardBorder,
                shape = shape
            )
            .then(
                if (onClick != null) {
                    Modifier.clickable(onClick = onClick)
                } else Modifier
            )
            .padding(ComfySpacing.md),
        content = content
    )
}
