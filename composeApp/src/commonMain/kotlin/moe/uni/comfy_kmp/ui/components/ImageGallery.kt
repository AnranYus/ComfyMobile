package moe.uni.comfy_kmp.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import moe.uni.comfy_kmp.ui.theme.ComfySpacing
import moe.uni.comfy_kmp.ui.theme.comfyColors

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
    Box(modifier = modifier.fillMaxSize()) {
        if (images.isEmpty()) {
            EmptyGalleryState(modifier = Modifier.align(Alignment.Center))
        } else {
            LazyColumn(
                contentPadding = PaddingValues(ComfySpacing.lg),
                verticalArrangement = Arrangement.spacedBy(ComfySpacing.md),
                modifier = Modifier.fillMaxSize()
            ) {
                itemsIndexed(images) { index, image ->
                    GalleryImageItem(
                        image = image,
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
    onClick: () -> Unit,
    onSave: (() -> Unit)?
) {
    val shape = RoundedCornerShape(12.dp)
    val aspectRatio = image.bitmap?.let { it.width.toFloat() / it.height.toFloat() } ?: 1f
    
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(ComfySpacing.sm)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(4.dp, shape)
                .clip(shape)
                .background(MaterialTheme.comfyColors.cardBackground)
                .clickable(onClick = onClick)
        ) {
            when {
                image.bitmap != null -> {
                    Image(
                        bitmap = image.bitmap,
                        contentDescription = image.filename,
                        contentScale = ContentScale.FillWidth,
                        modifier = Modifier
                            .fillMaxWidth()
                            .then(
                                if (aspectRatio.isFinite() && aspectRatio > 0f) {
                                    Modifier.aspectRatio(aspectRatio)
                                } else Modifier
                            )
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
