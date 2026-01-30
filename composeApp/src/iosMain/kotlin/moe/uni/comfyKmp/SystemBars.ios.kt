package moe.uni.comfyKmp

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import platform.Foundation.NSNotificationCenter
import platform.Foundation.NSUserDefaults

private const val STATUS_BAR_NOTIFICATION = "ComfyStatusBarHiddenChanged"
private const val STATUS_BAR_KEY = "comfyStatusBarHidden"

@Composable
actual fun SystemBarsEffect(hidden: Boolean) {
    DisposableEffect(hidden) {
        val previous = isStatusBarHidden()
        setStatusBarHidden(hidden)
        onDispose {
            setStatusBarHidden(previous)
        }
    }
}

private fun isStatusBarHidden(): Boolean {
    return NSUserDefaults.standardUserDefaults.boolForKey(STATUS_BAR_KEY)
}

private fun setStatusBarHidden(hidden: Boolean) {
    NSUserDefaults.standardUserDefaults.setBool(hidden, forKey = STATUS_BAR_KEY)
    NSNotificationCenter.defaultCenter.postNotificationName(
        aName = STATUS_BAR_NOTIFICATION,
        `object` = null
    )
}
