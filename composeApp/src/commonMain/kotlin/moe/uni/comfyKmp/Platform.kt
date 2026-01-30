package moe.uni.comfyKmp

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform