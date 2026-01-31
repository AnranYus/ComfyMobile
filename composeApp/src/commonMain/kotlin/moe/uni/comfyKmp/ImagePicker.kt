package moe.uni.comfyKmp

import androidx.compose.runtime.Composable

/**
 * Represents a picked image with its data
 */
data class PickedImage(
    val filename: String,
    val bytes: ByteArray,
    val mimeType: String
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false
        other as PickedImage
        return filename == other.filename &&
                bytes.contentEquals(other.bytes) &&
                mimeType == other.mimeType
    }

    override fun hashCode(): Int {
        var result = filename.hashCode()
        result = 31 * result + bytes.contentHashCode()
        result = 31 * result + mimeType.hashCode()
        return result
    }
}

/**
 * Remembers an image picker launcher for selecting images from gallery
 * @param onResult Callback with the picked image, or null if cancelled
 * @return A function to launch the image picker
 */
@Composable
expect fun rememberImagePickerLauncher(
    onResult: (PickedImage?) -> Unit
): () -> Unit
