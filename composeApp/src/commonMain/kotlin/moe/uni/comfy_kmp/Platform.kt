package moe.uni.comfy_kmp

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform