package moe.uni.comfyKmp

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.refTo
import platform.Foundation.NSData
import platform.Foundation.NSDate
import platform.Foundation.timeIntervalSince1970
import platform.PhotosUI.PHPickerConfiguration
import platform.PhotosUI.PHPickerFilter
import platform.PhotosUI.PHPickerResult
import platform.PhotosUI.PHPickerViewController
import platform.PhotosUI.PHPickerViewControllerDelegateProtocol
import platform.UIKit.UIApplication
import platform.UIKit.UIViewController
import platform.UIKit.UIWindow
import platform.UniformTypeIdentifiers.UTTypeImage
import platform.darwin.NSObject
import platform.posix.memcpy

@Composable
actual fun rememberImagePickerLauncher(
    onResult: (PickedImage?) -> Unit
): () -> Unit {
    val onResultState = rememberUpdatedState(onResult)
    val controller = remember {
        IosImagePickerController { image ->
            onResultState.value(image)
        }
    }
    return { controller.launch() }
}

private class IosImagePickerController(
    private val onResult: (PickedImage?) -> Unit
) : NSObject(), PHPickerViewControllerDelegateProtocol {

    fun launch() {
        val presenter = currentPresenter()
        if (presenter == null) {
            onResult(null)
            return
        }

        val config = PHPickerConfiguration().apply {
            filter = PHPickerFilter.imagesFilter
            selectionLimit = 1
        }

        val picker = PHPickerViewController(configuration = config)
        picker.delegate = this
        presenter.presentViewController(picker, animated = true, completion = null)
    }

    override fun picker(picker: PHPickerViewController, didFinishPicking: List<*>) {
        picker.dismissViewControllerAnimated(true, completion = null)

        val result = didFinishPicking.firstOrNull() as? PHPickerResult
        if (result == null) {
            onResult(null)
            return
        }

        result.itemProvider.loadDataRepresentationForTypeIdentifier(
            typeIdentifier = UTTypeImage.identifier
        ) { data, error ->
            if (data != null && error == null) {
                val bytes = data.toByteArray()
                val timestamp = NSDate().timeIntervalSince1970.toLong()
                val filename = "image_$timestamp.png"
                onResult(PickedImage(filename, bytes, "image/png"))
            } else {
                onResult(null)
            }
        }
    }
}

private fun currentPresenter(): UIViewController? {
    val app = UIApplication.sharedApplication
    val window = (app.keyWindow ?: app.windows.firstOrNull()) as? UIWindow
    var controller = window?.rootViewController
    while (controller?.presentedViewController != null) {
        controller = controller.presentedViewController
    }
    return controller
}

@OptIn(ExperimentalForeignApi::class)
private fun NSData.toByteArray(): ByteArray {
    val size = length.toInt()
    val bytes = ByteArray(size)
    if (size > 0) {
        memcpy(bytes.refTo(0), this.bytes, length)
    }
    return bytes
}
