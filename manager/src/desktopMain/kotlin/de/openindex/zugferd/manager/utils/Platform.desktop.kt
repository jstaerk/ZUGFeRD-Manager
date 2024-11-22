package de.openindex.zugferd.manager.utils

import de.openindex.zugferd.manager.APP_NAME
import de.openindex.zugferd.manager.APP_VENDOR
import net.harawata.appdirs.AppDirsFactory
import org.apache.commons.lang3.SystemUtils
import java.nio.file.Path
import kotlin.io.path.Path
import kotlin.io.path.createDirectories

val APP_DIR: Path by lazy {
    Path(
        AppDirsFactory
            .getInstance()
            .getUserDataDir(APP_NAME, null, APP_VENDOR)
    ).createDirectories()
}

val CACHE_DIR: Path by lazy {
    APP_DIR
        .resolve("cache")
        .createDirectories()
}

val DATA_DIR: Path by lazy {
    APP_DIR
        .resolve("data")
        .createDirectories()
}

val LOGS_DIR: Path by lazy {
    APP_DIR
        .resolve("logs")
        .createDirectories()
}

val BACKUPS_DIR: Path by lazy {
    APP_DIR
        .resolve("backups")
        .createDirectories()
}


private class JvmPlatform(
    override val name: String = "Java ${SystemUtils.JAVA_VERSION}",
    override val type: PlatformType = PlatformType.DESKTOP,
) : Platform

actual fun getPlatform(): Platform = JvmPlatform()
