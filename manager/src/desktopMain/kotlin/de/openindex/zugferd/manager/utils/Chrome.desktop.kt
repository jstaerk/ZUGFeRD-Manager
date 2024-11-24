package de.openindex.zugferd.manager.utils

import de.openindex.zugferd.manager.APP_LOGGER
import de.openindex.zugferd.manager.AppInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import me.friwi.jcefmaven.CefAppBuilder
import me.friwi.jcefmaven.MavenCefAppHandlerAdapter
import org.apache.commons.lang3.SystemUtils
import org.cef.CefApp
import org.cef.CefClient
import java.nio.file.Path
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.absolutePathString
import kotlin.io.path.createDirectories
import kotlin.io.path.deleteIfExists
import kotlin.io.path.deleteRecursively
import kotlin.io.path.exists
import kotlin.io.path.isRegularFile
import kotlin.io.path.reader
import kotlin.io.path.writer
import kotlin.system.exitProcess

private val CEF_INSTALL_DIR: Path by lazy {
    APP_DIR
        .resolve("chrome")
        .createDirectories()
}

private val CEF_CACHE_DIR: Path by lazy {
    CACHE_DIR
        .resolve("chrome")
        .createDirectories()
}

private val CEF_LOG_FILE: Path by lazy {
    LOGS_DIR
        .resolve("chrome.log")
}

private val CEF_VERSION_FILE: Path by lazy {
    CACHE_DIR
        .resolve("chrome.version")
}

/*@OptIn(ExperimentalPathApi::class)
val CEF_APP: CefApp by lazy {
    CEF_LOG_FILE.deleteIfExists()

    // Delete CEF installation directory,
    // if a newer version is available.
    if (CEF_INSTALL_DIR.exists()) {
        val installedCefVersion = if (CEF_VERSION_FILE.isRegularFile()) {
            CEF_VERSION_FILE
                .reader(charset = Charsets.UTF_8)
                .use { reader ->
                    reader.readText().trim()
                }
        } else {
            null
        }

        if (installedCefVersion != AppInfo.Custom.CHROME_VERSION) {
            APP_LOGGER.info("Delete outdated CEF version \"${installedCefVersion}\".")
            CEF_INSTALL_DIR.deleteRecursively()
        }
    }

    // Remember currently installed CEF version.
    CEF_VERSION_FILE
        .writer(charset = Charsets.UTF_8)
        .use { writer ->
            writer.write(AppInfo.Custom.CHROME_VERSION)
        }

    val builder = CefAppBuilder()
    builder.setInstallDir(CEF_INSTALL_DIR.toFile())
    builder.setProgressHandler { state, percent ->
        APP_LOGGER.info(
            "Chrome-Setup | {} | {}",
            state,
            if (percent >= 0f) "${percent}%" else "in progress",
        )
    }

    builder.cefSettings.locale = "de-DE"
    builder.cefSettings.windowless_rendering_enabled = false
    builder.cefSettings.log_file = CEF_LOG_FILE.absolutePathString()
    builder.cefSettings.root_cache_path = CEF_CACHE_DIR.absolutePathString()

    //builder.cefSettings.background_color = builder.cefSettings.ColorType(1, 0, 0, 0)
    //builder.addJcefArgs("--disable-gpu")

    builder.setAppHandler(
        object : MavenCefAppHandlerAdapter() {
            override fun onBeforeTerminate(): Boolean {
                if (SystemUtils.IS_OS_MAC) {
                    exitProcess(0)
                    @Suppress("UNREACHABLE_CODE")
                    return true
                }
                return super.onBeforeTerminate()
            }
        }
    )

    builder.build()
}*/

val CEF_CLIENT: CefClient by lazy {
    try {
        CEF_APP!!.createClient()
    } catch (e: Exception) {
        APP_LOGGER.error("Browser is not properly initialized!", e)
        throw RuntimeException("Browser is not properly initialized!", e)
    }
}

private var CEF_APP: CefApp? = null

@OptIn(ExperimentalPathApi::class)
suspend fun installWebView() {
    CEF_APP = withContext(Dispatchers.IO) {
        CEF_LOG_FILE.deleteIfExists()

        // Delete CEF installation directory,
        // if a newer version is available.
        if (CEF_INSTALL_DIR.exists()) {
            val installedCefVersion = if (CEF_VERSION_FILE.isRegularFile()) {
                CEF_VERSION_FILE
                    .reader(charset = Charsets.UTF_8)
                    .use { reader ->
                        reader.readText().trim()
                    }
            } else {
                null
            }

            if (installedCefVersion != AppInfo.Custom.CHROME_VERSION) {
                APP_LOGGER.info("Delete outdated CEF version \"${installedCefVersion}\".")
                CEF_INSTALL_DIR.deleteRecursively()
            }
        }

        // Remember currently installed CEF version.
        CEF_VERSION_FILE
            .writer(charset = Charsets.UTF_8)
            .use { writer ->
                writer.write(AppInfo.Custom.CHROME_VERSION)
            }

        val builder = CefAppBuilder()
        builder.setInstallDir(CEF_INSTALL_DIR.toFile())
        builder.setProgressHandler { state, percent ->
            APP_LOGGER.info(
                "Chrome-Setup | {} | {}",
                state,
                if (percent >= 0f) "${percent}%" else "in progress",
            )
        }

        builder.cefSettings.locale = "de-DE"
        builder.cefSettings.windowless_rendering_enabled = false
        builder.cefSettings.log_file = CEF_LOG_FILE.absolutePathString()
        builder.cefSettings.root_cache_path = CEF_CACHE_DIR.absolutePathString()

        //builder.cefSettings.background_color = builder.cefSettings.ColorType(1, 0, 0, 0)
        //builder.addJcefArgs("--disable-gpu")

        builder.setAppHandler(
            object : MavenCefAppHandlerAdapter() {
                override fun onBeforeTerminate(): Boolean {
                    if (SystemUtils.IS_OS_MAC) {
                        exitProcess(0)
                        @Suppress("UNREACHABLE_CODE")
                        return true
                    }
                    return super.onBeforeTerminate()
                }
            }
        )

        builder.build()
    }
}
