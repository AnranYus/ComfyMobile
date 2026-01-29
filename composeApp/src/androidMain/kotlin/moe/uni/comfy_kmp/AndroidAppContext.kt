package moe.uni.comfy_kmp

import android.content.Context

object AndroidAppContext {
    lateinit var appContext: Context
        private set

    fun init(context: Context) {
        appContext = context.applicationContext
    }
}
