package moe.uni.comfy_kmp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import moe.uni.comfy_kmp.ui.theme.ComfySpacing
import moe.uni.comfy_kmp.ui.theme.comfyColors

@Composable
fun ComfyDialog(
    onDismissRequest: () -> Unit,
    title: String,
    confirmText: String = "确认",
    dismissText: String = "取消",
    onConfirm: () -> Unit,
    confirmEnabled: Boolean = true,
    showDismiss: Boolean = true,
    properties: DialogProperties = DialogProperties(),
    content: @Composable ColumnScope.() -> Unit
) {
    Dialog(
        onDismissRequest = onDismissRequest,
        properties = properties
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(28.dp)),
            color = MaterialTheme.comfyColors.cardBackground,
            tonalElevation = 6.dp,
            shadowElevation = 8.dp
        ) {
            Column(
                modifier = Modifier.padding(ComfySpacing.xl)
            ) {
                // Title
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Spacer(modifier = Modifier.height(ComfySpacing.lg))
                
                // Content
                content()
                
                Spacer(modifier = Modifier.height(ComfySpacing.xl))
                
                // Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (showDismiss) {
                        TextButton(onClick = onDismissRequest) {
                            Text(dismissText)
                        }
                        Spacer(modifier = Modifier.width(ComfySpacing.sm))
                    }
                    Button(
                        onClick = onConfirm,
                        enabled = confirmEnabled,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(confirmText)
                    }
                }
            }
        }
    }
}

@Composable
fun ComfyAlertDialog(
    onDismissRequest: () -> Unit,
    title: String,
    message: String,
    confirmText: String = "确认",
    dismissText: String = "取消",
    onConfirm: () -> Unit,
    showDismiss: Boolean = true,
    isDestructive: Boolean = false
) {
    Dialog(onDismissRequest = onDismissRequest) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(28.dp)),
            color = MaterialTheme.comfyColors.cardBackground,
            tonalElevation = 6.dp,
            shadowElevation = 8.dp
        ) {
            Column(
                modifier = Modifier.padding(ComfySpacing.xl)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Spacer(modifier = Modifier.height(ComfySpacing.md))
                
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Spacer(modifier = Modifier.height(ComfySpacing.xl))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (showDismiss) {
                        TextButton(onClick = onDismissRequest) {
                            Text(dismissText)
                        }
                        Spacer(modifier = Modifier.width(ComfySpacing.sm))
                    }
                    Button(
                        onClick = onConfirm,
                        shape = RoundedCornerShape(12.dp),
                        colors = if (isDestructive) {
                            androidx.compose.material3.ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error,
                                contentColor = MaterialTheme.colorScheme.onError
                            )
                        } else {
                            androidx.compose.material3.ButtonDefaults.buttonColors()
                        }
                    ) {
                        Text(confirmText)
                    }
                }
            }
        }
    }
}

@Composable
fun ComfyInputDialog(
    onDismissRequest: () -> Unit,
    title: String,
    confirmText: String = "确认",
    dismissText: String = "取消",
    onConfirm: () -> Unit,
    confirmEnabled: Boolean = true,
    content: @Composable ColumnScope.() -> Unit
) {
    ComfyDialog(
        onDismissRequest = onDismissRequest,
        title = title,
        confirmText = confirmText,
        dismissText = dismissText,
        onConfirm = onConfirm,
        confirmEnabled = confirmEnabled,
        content = content
    )
}
