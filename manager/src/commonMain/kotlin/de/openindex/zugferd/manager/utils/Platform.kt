package de.openindex.zugferd.manager.utils

interface Platform {
    val name: String
    val type: PlatformType
}

enum class PlatformType {
    DESKTOP,
}

expect fun getPlatform(): Platform
