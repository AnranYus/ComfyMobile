package moe.uni.comfy_kmp

import androidx.compose.runtime.Composable

/**
 * Remembers a file picker launcher that can be used to select JSON files.
 * @param onResult Callback with the file content as String, or null if cancelled/failed
 * @return A function to launch the file picker
 */
@Composable
expect fun rememberFilePickerLauncher(
    onResult: (String?) -> Unit
): () -> Unit

/**
 * Returns whether file picking is supported on the current platform
 */
expect fun isFilePickerSupported(): Boolean
