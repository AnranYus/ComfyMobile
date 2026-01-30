package moe.uni.comfyKmp.data

import okio.FileSystem

fun saveToTemp(bytes: ByteArray, filename: String): String {
    val dir = FileSystem.SYSTEM_TEMPORARY_DIRECTORY
    val safeName = filename.replace(Regex("[^A-Za-z0-9._-]"), "_")
    val path = dir / safeName
    FileSystem.SYSTEM.write(path) {
        write(bytes)
    }
    return path.toString()
}
