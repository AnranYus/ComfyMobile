@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)

package moe.uni.comfyKmp.data

import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import kotlinx.coroutines.suspendCancellableCoroutine
import platform.Foundation.NSData
import platform.Foundation.dataWithBytes
import platform.Photos.PHAccessLevelAddOnly
import platform.Photos.PHAssetChangeRequest
import platform.Photos.PHAuthorizationStatusAuthorized
import platform.Photos.PHAuthorizationStatusLimited
import platform.Photos.PHAuthorizationStatusNotDetermined
import platform.Photos.PHPhotoLibrary
import platform.UIKit.UIImage
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

actual suspend fun saveImageToGallery(bytes: ByteArray, filenameHint: String): String {
    return suspendCancellableCoroutine { continuation ->
        val image: UIImage? = UIImage(data = bytes.toNSData())
        if (image == null) {
            continuation.resumeWithException(IllegalArgumentException("Unable to decode image"))
            return@suspendCancellableCoroutine
        }

        fun finishSuccess() {
            if (continuation.isActive) {
                continuation.resume("Photos")
            }
        }

        fun finishError(message: String) {
            if (continuation.isActive) {
                continuation.resumeWithException(IllegalStateException(message))
            }
        }

        fun saveToAlbum() {
            PHPhotoLibrary.sharedPhotoLibrary().performChanges({
                PHAssetChangeRequest.creationRequestForAssetFromImage(image)
            }, completionHandler = { success, error ->
                if (success) {
                    finishSuccess()
                } else {
                    val details = error?.localizedDescription?.let { ": $it" }.orEmpty()
                    finishError("Failed to save to Photos$details")
                }
            })
        }

        val status = PHPhotoLibrary.authorizationStatusForAccessLevel(PHAccessLevelAddOnly)
        when (status) {
            PHAuthorizationStatusAuthorized, PHAuthorizationStatusLimited -> saveToAlbum()
            PHAuthorizationStatusNotDetermined -> {
                PHPhotoLibrary.requestAuthorizationForAccessLevel(PHAccessLevelAddOnly) { newStatus ->
                    if (newStatus == PHAuthorizationStatusAuthorized || newStatus == PHAuthorizationStatusLimited) {
                        saveToAlbum()
                    } else {
                        finishError("Photo Library access not granted")
                    }
                }
            }
            else -> finishError("Photo Library access not granted")
        }
    }
}

private fun ByteArray.toNSData(): NSData {
    return usePinned { pinned ->
        NSData.dataWithBytes(pinned.addressOf(0), size.toULong())
    }
}
