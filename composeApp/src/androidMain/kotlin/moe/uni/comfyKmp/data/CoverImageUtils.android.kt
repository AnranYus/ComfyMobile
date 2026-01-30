package moe.uni.comfyKmp.data

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.File
import kotlin.math.max
import moe.uni.comfyKmp.AndroidAppContext

actual fun saveCoverImage(bytes: ByteArray, filenameHint: String): String {
    val safeName = filenameHint.replace(Regex("[^A-Za-z0-9._-]"), "_")
    val file = File(AndroidAppContext.appContext.cacheDir, safeName)
    val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        ?: throw IllegalArgumentException("无法解码封面图片")
    val scaled = scaleDownBitmap(bitmap, 1024)
    file.outputStream().use { stream ->
        scaled.compress(Bitmap.CompressFormat.JPEG, 80, stream)
    }
    if (scaled !== bitmap) {
        scaled.recycle()
    }
    return file.absolutePath
}

private fun scaleDownBitmap(bitmap: Bitmap, maxSize: Int): Bitmap {
    val width = bitmap.width
    val height = bitmap.height
    val largest = max(width, height)
    if (largest <= maxSize) return bitmap
    val scale = maxSize.toFloat() / largest.toFloat()
    val targetWidth = (width * scale).toInt().coerceAtLeast(1)
    val targetHeight = (height * scale).toInt().coerceAtLeast(1)
    return Bitmap.createScaledBitmap(bitmap, targetWidth, targetHeight, true)
}
