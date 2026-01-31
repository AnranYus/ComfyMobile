package moe.uni.comfyKmp.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Material 3 Window Width Size Classes
 * 
 * - Compact: < 600dp (手机竖屏)
 * - Medium: 600dp - 840dp (小平板、折叠屏、手机横屏)
 * - Expanded: > 840dp (大平板、桌面)
 */
enum class WindowWidthSizeClass {
    Compact,
    Medium,
    Expanded
}

/**
 * Material 3 Window Height Size Classes
 */
enum class WindowHeightSizeClass {
    Compact,
    Medium,
    Expanded
}

/**
 * 窗口尺寸信息
 */
data class WindowSizeInfo(
    val widthSizeClass: WindowWidthSizeClass,
    val heightSizeClass: WindowHeightSizeClass,
    val widthDp: Dp,
    val heightDp: Dp
) {
    val isCompact: Boolean get() = widthSizeClass == WindowWidthSizeClass.Compact
    val isMedium: Boolean get() = widthSizeClass == WindowWidthSizeClass.Medium
    val isExpanded: Boolean get() = widthSizeClass == WindowWidthSizeClass.Expanded
    
    /** 是否应该使用双面板布局 */
    val shouldUseTwoPane: Boolean get() = widthSizeClass == WindowWidthSizeClass.Expanded
    
    /** 是否应该使用网格布局 */
    val shouldUseGrid: Boolean get() = widthSizeClass != WindowWidthSizeClass.Compact
}

/**
 * 根据宽度计算 Window Width Size Class
 */
fun calculateWidthSizeClass(widthDp: Dp): WindowWidthSizeClass {
    return when {
        widthDp < 600.dp -> WindowWidthSizeClass.Compact
        widthDp < 840.dp -> WindowWidthSizeClass.Medium
        else -> WindowWidthSizeClass.Expanded
    }
}

/**
 * 根据高度计算 Window Height Size Class
 */
fun calculateHeightSizeClass(heightDp: Dp): WindowHeightSizeClass {
    return when {
        heightDp < 480.dp -> WindowHeightSizeClass.Compact
        heightDp < 900.dp -> WindowHeightSizeClass.Medium
        else -> WindowHeightSizeClass.Expanded
    }
}

/**
 * 计算窗口尺寸信息
 * 
 * 需要在 BoxWithConstraints 内部使用，传入 maxWidth 和 maxHeight
 */
@Composable
fun rememberWindowSizeInfo(maxWidth: Dp, maxHeight: Dp): WindowSizeInfo {
    return remember(maxWidth, maxHeight) {
        WindowSizeInfo(
            widthSizeClass = calculateWidthSizeClass(maxWidth),
            heightSizeClass = calculateHeightSizeClass(maxHeight),
            widthDp = maxWidth,
            heightDp = maxHeight
        )
    }
}

/**
 * 响应式布局常量
 */
object AdaptiveLayoutConstants {
    /** 列表面板最小宽度 */
    val listPaneMinWidth = 300.dp
    
    /** 列表面板最大宽度 */
    val listPaneMaxWidth = 400.dp
    
    /** 列表面板推荐宽度 */
    val listPanePreferredWidth = 360.dp
    
    /** 详情面板最小宽度 */
    val detailPaneMinWidth = 400.dp
    
    /** 卡片最大宽度 */
    val cardMaxWidth = 480.dp
    
    /** 对话框最大宽度 */
    val dialogMaxWidth = 560.dp
    
    /** 网格项最小宽度 */
    val gridItemMinWidth = 280.dp
    
    /** 网格项最大宽度 */
    val gridItemMaxWidth = 400.dp
}
