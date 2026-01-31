package moe.uni.comfyKmp.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import moe.uni.comfyKmp.ui.theme.AdaptiveLayoutConstants
import moe.uni.comfyKmp.ui.theme.WindowSizeInfo
import moe.uni.comfyKmp.ui.theme.rememberWindowSizeInfo

/**
 * 自适应双面板布局
 * 
 * - Compact: 仅显示 listPane 或 detailPane（根据 showDetail 决定）
 * - Medium/Expanded: 左右并排显示两个面板
 * 
 * @param showDetail 在 Compact 模式下是否显示详情面板
 * @param listPaneWidth 列表面板宽度（仅在非 Compact 模式下生效）
 * @param listPane 列表面板内容
 * @param detailPane 详情面板内容
 * @param emptyDetailPane 当没有选中项时显示的占位内容
 */
@Composable
fun AdaptiveTwoPaneLayout(
    modifier: Modifier = Modifier,
    showDetail: Boolean = false,
    listPaneWidth: Dp = AdaptiveLayoutConstants.listPanePreferredWidth,
    listPane: @Composable (WindowSizeInfo) -> Unit,
    detailPane: @Composable (WindowSizeInfo) -> Unit,
    emptyDetailPane: @Composable (WindowSizeInfo) -> Unit = {}
) {
    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val windowSizeInfo = rememberWindowSizeInfo(maxWidth, maxHeight)
        
        if (windowSizeInfo.shouldUseTwoPane) {
            // Expanded: 双面板布局
            Row(modifier = Modifier.fillMaxSize()) {
                // 左侧列表面板
                Box(
                    modifier = Modifier
                        .width(listPaneWidth.coerceIn(
                            AdaptiveLayoutConstants.listPaneMinWidth,
                            AdaptiveLayoutConstants.listPaneMaxWidth
                        ))
                        .fillMaxHeight()
                ) {
                    listPane(windowSizeInfo)
                }
                
                // 右侧详情面板
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                ) {
                    if (showDetail) {
                        detailPane(windowSizeInfo)
                    } else {
                        emptyDetailPane(windowSizeInfo)
                    }
                }
            }
        } else {
            // Compact/Medium: 单面板布局
            if (showDetail) {
                detailPane(windowSizeInfo)
            } else {
                listPane(windowSizeInfo)
            }
        }
    }
}

/**
 * 自适应内容容器
 * 
 * 在大屏幕上限制内容最大宽度并居中显示
 */
@Composable
fun AdaptiveContentContainer(
    modifier: Modifier = Modifier,
    maxWidth: Dp = AdaptiveLayoutConstants.cardMaxWidth,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.TopCenter
    ) {
        Box(modifier = Modifier.widthIn(max = maxWidth)) {
            content()
        }
    }
}

/**
 * 计算网格列数
 * 
 * @param availableWidth 可用宽度
 * @param minItemWidth 每项最小宽度
 * @param maxColumns 最大列数
 */
fun calculateGridColumns(
    availableWidth: Dp,
    minItemWidth: Dp = AdaptiveLayoutConstants.gridItemMinWidth,
    maxColumns: Int = 3
): Int {
    val columns = (availableWidth / minItemWidth).toInt()
    return columns.coerceIn(1, maxColumns)
}
