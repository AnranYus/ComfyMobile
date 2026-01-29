@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)

package moe.uni.comfy_kmp.data

import kotlinx.cinterop.addressOf
import kotlinx.cinterop.convert
import kotlinx.cinterop.usePinned
import okio.FileSystem
import okio.Path.Companion.toPath
import platform.Foundation.NSCachesDirectory
import platform.Foundation.NSData
import platform.Foundation.NSFileManager
import platform.Foundation.NSUserDomainMask
import platform.Foundation.URLByAppendingPathComponent
import platform.Foundation.dataWithBytes
import platform.UIKit.UIImage
import platform.UIKit.UIImageJPEGRepresentation
import platform.posix.memcpy

actual fun saveCoverImage(bytes: ByteArray, filenameHint: String): String {
    val safeName = filenameHint.replace(Regex("[^A-Za-z0-9._-]"), "_")
    val cacheDir = NSFileManager.defaultManager.URLForDirectory(
        directory = NSCachesDirectory,
        inDomain = NSUserDomainMask,
        appropriateForURL = null,
        create = true,
        error = null
    ) ?: throw IllegalStateException("无法获取缓存目录")
    val fileUrl = cacheDir.URLByAppendingPathComponent(safeName)
        ?: throw IllegalStateException("无法获取封面路径")
    val data = bytes.toNSData()
    val image = UIImage(data = data) ?: throw IllegalArgumentException("无法解码封面图片")
    val jpegData = UIImageJPEGRepresentation(image, 0.8) ?: throw IllegalArgumentException("无法压缩封面图片")
    val outputPath = fileUrl.path ?: throw IllegalStateException("无法获取封面路径")
    val bytesToWrite = jpegData.toByteArray()
    FileSystem.SYSTEM.write(outputPath.toPath()) {
        write(bytesToWrite)
    }
    return outputPath
}

private fun ByteArray.toNSData(): NSData {
    return usePinned { pinned ->
        NSData.dataWithBytes(pinned.addressOf(0), size.toULong())
    }
}

private fun NSData.toByteArray(): ByteArray {
    val size = length.toInt()
    if (size == 0) return ByteArray(0)
    val result = ByteArray(size)
    result.usePinned { pinned ->
        memcpy(pinned.addressOf(0), bytes, size.convert())
    }
    return result
}
