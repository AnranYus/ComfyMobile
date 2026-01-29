package moe.uni.comfy_kmp.di

import androidx.compose.runtime.compositionLocalOf

val LocalAppContainer = compositionLocalOf<AppContainer> {
    error("AppContainer not provided")
}
