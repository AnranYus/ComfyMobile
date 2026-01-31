package moe.uni.comfyKmp.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import moe.uni.comfyKmp.ui.theme.ComfySpacing

/**
 * Toast 消息数据
 */
data class ToastMessage(
    val message: String,
    val duration: Long = 2000L
)

/**
 * Toast 状态管理
 */
class ToastState {
    var currentMessage by mutableStateOf<ToastMessage?>(null)
        private set
    
    fun show(message: String, duration: Long = 2000L) {
        currentMessage = ToastMessage(message, duration)
    }
    
    fun dismiss() {
        currentMessage = null
    }
}

@Composable
fun rememberToastState(): ToastState {
    return remember { ToastState() }
}

/**
 * Toast 容器 - 包裹内容并在底部显示 Toast
 */
@Composable
fun ToastHost(
    state: ToastState,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(modifier = modifier.fillMaxSize()) {
        content()
        
        val message = state.currentMessage
        
        LaunchedEffect(message) {
            if (message != null) {
                delay(message.duration)
                state.dismiss()
            }
        }
        
        AnimatedVisibility(
            visible = message != null,
            enter = fadeIn() + slideInVertically { it },
            exit = fadeOut() + slideOutVertically { it },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 100.dp)
        ) {
            message?.let {
                ToastContent(message = it.message)
            }
        }
    }
}

@Composable
private fun ToastContent(message: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(24.dp))
            .background(MaterialTheme.colorScheme.inverseSurface)
            .padding(horizontal = ComfySpacing.lg, vertical = ComfySpacing.md)
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.inverseOnSurface
        )
    }
}
