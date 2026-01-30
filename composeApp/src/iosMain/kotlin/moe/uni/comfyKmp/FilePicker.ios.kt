package moe.uni.comfyKmp

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import platform.Foundation.NSURL
import platform.UIKit.UIApplication
import platform.UIKit.UIDocumentPickerDelegateProtocol
import platform.UIKit.UIDocumentPickerMode
import platform.UIKit.UIDocumentPickerViewController
import platform.UIKit.UIViewController
import platform.UIKit.UIWindow
import platform.darwin.NSObject
import okio.FileSystem
import okio.Path.Companion.toPath

@Composable
actual fun rememberFilePickerLauncher(
    onResult: (String?) -> Unit
): () -> Unit {
    val onResultState = rememberUpdatedState(onResult)
    val controller = remember {
        IosFilePickerController { content ->
            onResultState.value(content)
        }
    }
    return { controller.launch() }
}

actual fun isFilePickerSupported(): Boolean = true

private class IosFilePickerController(
    private val onResult: (String?) -> Unit
) : NSObject(), UIDocumentPickerDelegateProtocol {
    private var activePicker: UIDocumentPickerViewController? = null

    fun launch() {
        val presenter = currentPresenter()
        if (presenter == null) {
            onResult(null)
            return
        }

        val picker = UIDocumentPickerViewController(
            documentTypes = listOf("public.json", "public.plain-text", "public.data"),
            inMode = UIDocumentPickerMode.UIDocumentPickerModeImport
        )
        picker.delegate = this
        picker.allowsMultipleSelection = false
        activePicker = picker
        presenter.presentViewController(picker, animated = true, completion = null)
    }

    override fun documentPicker(
        controller: UIDocumentPickerViewController,
        didPickDocumentsAtURLs: List<*>
    ) {
        val url = didPickDocumentsAtURLs.firstOrNull() as? NSURL
        onResult(readText(url))
        activePicker = null
    }

    override fun documentPickerWasCancelled(controller: UIDocumentPickerViewController) {
        onResult(null)
        activePicker = null
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

private fun readText(url: NSURL?): String? {
    if (url == null) return null
    val path = url.path ?: return null
    return try {
        FileSystem.SYSTEM.read(path.toPath()) {
            readUtf8()
        }
    } catch (_: Throwable) {
        null
    }
}
