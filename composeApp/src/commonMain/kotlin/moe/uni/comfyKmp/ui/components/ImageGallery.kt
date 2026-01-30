package moe.uni.comfyKmp.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import moe.uni.comfyKmp.ui.theme.ComfySpacing
import moe.uni.comfyKmp.ui.theme.comfyColors

data class GalleryImage(
    val filename: String,
    val bitmap: ImageBitmap? = null,
    val isLoading: Boolean = true
)

@Composable
fun ImageGallery(
    images: List<GalleryImage>,
    modifier: Modifier = Modifier,
    onImageClick: ((Int) -> Unit)? = null,
    onSaveClick: ((Int) -> Unit)? = null
) {
    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val availableWidth = maxWidth
        val availableHeight = maxHeight
        
        if (images.isEmpty()) {
            EmptyGalleryState(modifier = Modifier.align(Alignment.Center))
        } else {
            LazyColumn(
                contentPadding = PaddingValues(ComfySpacing.lg),
                verticalArrangement = Arrangement.spacedBy(ComfySpacing.md),
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxSize()
            ) {
                itemsIndexed(images) { index, image ->
                    GalleryImageItem(
                        image = image,
                        availableWidth = availableWidth,
                        availableHeight = availableHeight,
                        onClick = { 
                            onImageClick?.invoke(index) 
                        },
                        onSave = onSaveClick?.let { { it(index) } }
                    )
                }
            }
        }
    }
}

@Composable
private fun GalleryImageItem(
    image: GalleryImage,
    availableWidth: androidx.compose.ui.unit.Dp,
    availableHeight: androidx.compose.ui.unit.Dp,
    onClick: () -> Unit,
    onSave: (() -> Unit)?
) {
    val shape = RoundedCornerShape(12.dp)
    val bitmap = image.bitmap
    
    // Calculate max dimensions (leave padding space)
    val padding = ComfySpacing.lg * 2
    val maxWidth = availableWidth - padding
    val maxHeight = availableHeight - padding
    
    // Calculate fitted size maintaining aspect ratio
    val (fittedWidth, fittedHeight) = if (bitmap != null) {
        val imageAspect = bitmap.width.toFloat() / bitmap.height.toFloat()
        val containerAspect = maxWidth / maxHeight
        
        if (imageAspect > containerAspect) {
            // Image is wider - fit to width
            maxWidth to (maxWidth / imageAspect)
        } else {
            // Image is taller - fit to height
            (maxHeight * imageAspect) to maxHeight
        }
    } else {
        maxWidth to (maxWidth * 0.75f) // Default 4:3 for placeholder
    }
    
    Column(
        verticalArrangement = Arrangement.spacedBy(ComfySpacing.sm),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .widthIn(max = fittedWidth)
                .heightIn(max = fittedHeight)
                .shadow(4.dp, shape)
                .clip(shape)
                .background(MaterialTheme.comfyColors.cardBackground)
                .clickable(onClick = onClick)
        ) {
            when {
                bitmap != null -> {
                    Image(
                        bitmap = bitmap,
                        contentDescription = image.filename,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .widthIn(max = fittedWidth)
                            .heightIn(max = fittedHeight)
                    )
                    
                    // Filename badge at bottom
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .fillMaxWidth()
                            .background(Color.Black.copy(alpha = 0.5f))
                            .padding(ComfySpacing.sm)
                    ) {
                        Text(
                            text = image.filename,
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White,
                            maxLines = 1
                        )
                    }
                }
                image.isLoading -> {
                    ShimmerPlaceholder(modifier = Modifier.fillMaxSize())
                }
                else -> {
                    WaitingPlaceholder(modifier = Modifier.fillMaxSize())
                }
            }
        }
        
        // Action buttons (reserved)
    }
}

@Composable
private fun EmptyGalleryState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(ComfySpacing.xl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(ComfySpacing.md)
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Image,
                contentDescription = null,
                modifier = Modifier.size(36.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Text(
            text = "暂无生成结果",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = "点击「开始执行」运行工作流",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun ShimmerPlaceholder(modifier: Modifier = Modifier) {
    val comfyColors = MaterialTheme.comfyColors
    
    Box(
        modifier = modifier.background(comfyColors.shimmerBase),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(ComfySpacing.sm)
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                strokeWidth = 2.dp,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "加载中...",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun WaitingPlaceholder(modifier: Modifier = Modifier) {
    val comfyColors = MaterialTheme.comfyColors
    
    Box(
        modifier = modifier.background(comfyColors.shimmerBase),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.HourglassEmpty,
            contentDescription = "等待中",
            modifier = Modifier.size(32.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun ErrorPlaceholder(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.background(MaterialTheme.colorScheme.errorContainer),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.ErrorOutline,
            contentDescription = "加载失败",
            modifier = Modifier.size(32.dp),
            tint = MaterialTheme.colorScheme.error
        )
    }
}
