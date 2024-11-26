package de.openindex.zugferd.manager.utils

interface Platform {
    val name: String
    val type: PlatformType
    val os: OsType
    val isRunningInMacAppBundle: Boolean
}

enum class PlatformType {
    DESKTOP,
}

enum class OsType {
    LINUX,
    MAC,
    WINDOWS,
}

expect fun getPlatform(): Platform
