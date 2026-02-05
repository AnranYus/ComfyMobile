package moe.uni.comfyKmp.data

import okio.FileSystem
import okio.Path.Companion.toPath
import okio.SYSTEM
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

expect fun saveCoverImage(bytes: ByteArray, filenameHint: String): String

@OptIn(ExperimentalEncodingApi::class)
fun loadCoverImageBytes(coverImage: String): ByteArray? {
    val cleaned = coverImage.removePrefix("file://")
    val path = cleaned.toPath()
    if (FileSystem.SYSTEM.exists(path)) {
        return FileSystem.SYSTEM.read(path) {
            readByteArray()
        }
    }
    return try {
        Base64.decode(coverImage)
    } catch (_: IllegalArgumentException) {
        null
    }
}
