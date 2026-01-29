package moe.uni.comfy_kmp

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import java.io.BufferedReader
import java.io.InputStreamReader

@Composable
actual fun rememberFilePickerLauncher(
    onResult: (String?) -> Unit
): () -> Unit {
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            val content = readTextFromUri(context, uri)
            onResult(content)
        } else {
            onResult(null)
        }
    }
    
    return {
        launcher.launch(arrayOf("application/json", "text/plain", "*/*"))
    }
}

actual fun isFilePickerSupported(): Boolean = true

private fun readTextFromUri(context: Context, uri: Uri): String? {
    return try {
        context.contentResolver.openInputStream(uri)?.use { inputStream ->
            BufferedReader(InputStreamReader(inputStream)).use { reader ->
                reader.readText()
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}
