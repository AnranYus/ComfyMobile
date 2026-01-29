package moe.uni.comfy_kmp.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

actual suspend fun saveImageToGallery(bytes: ByteArray, filenameHint: String): String {
    return withContext(Dispatchers.IO) {
        saveToTemp(bytes, filenameHint)
    }
}
