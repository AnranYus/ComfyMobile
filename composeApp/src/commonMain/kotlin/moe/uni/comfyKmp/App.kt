package moe.uni.comfyKmp

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.tooling.preview.Preview
import cafe.adriel.voyager.navigator.Navigator
import moe.uni.comfyKmp.di.AppContainer
import moe.uni.comfyKmp.di.LocalAppContainer
import moe.uni.comfyKmp.ui.screens.ServerScreen
import moe.uni.comfyKmp.ui.theme.ComfyTheme

@Composable
@Preview
fun App() {
    val container = remember { AppContainer() }
    CompositionLocalProvider(LocalAppContainer provides container) {
        ComfyTheme(darkTheme = isSystemInDarkTheme()) {
            Navigator(ServerScreen())
        }
    }
}