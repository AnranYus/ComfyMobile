package moe.uni.comfyKmp.data

expect suspend fun saveImageToGallery(bytes: ByteArray, filenameHint: String): String
