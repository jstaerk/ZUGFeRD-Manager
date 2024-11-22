package de.openindex.zugferd.manager

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.awt.ComposeWindow
import androidx.compose.ui.unit.isSpecified
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import ch.qos.logback.classic.ClassicConstants
import de.openindex.zugferd.manager.utils.LOGS_DIR
import de.openindex.zugferd.manager.utils.LocalPreferences
import de.openindex.zugferd.manager.utils.LocalProducts
import de.openindex.zugferd.manager.utils.LocalRecipients
import de.openindex.zugferd.manager.utils.LocalSenders
import de.openindex.zugferd.manager.utils.loadPreferencesData
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.apache.commons.lang3.SystemUtils
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import kotlin.io.path.absolutePathString

val APP_LOGGER: Logger by lazy {
    LoggerFactory.getLogger("de.openindex.zugferd.manager")
}

val LocalDesktopWindow = staticCompositionLocalOf<ComposeWindow?> { null }

fun main() {
    //
    // Setup logging.
    //

    System.setProperty(
        "app.log.dir",
        LOGS_DIR.absolutePathString(),
    )
    val logbackXml = object {}.javaClass
        .getResource("/de/openindex/zugferd/ui/logback.xml")
    if (logbackXml != null) {
        System.setProperty(
            ClassicConstants.CONFIG_FILE_PROPERTY,
            logbackXml.toExternalForm()
        )
    }

    //
    // Enable interop blending.
    // Experimental feature with certain drawbacks:
    // https://github.com/JetBrains/compose-multiplatform/issues/4941
    //

    //System.setProperty("compose.interop.blending", "true")


    //
    // Setup system properties for MacOSX.
    //

    if (SystemUtils.IS_OS_MAC) {
        val preferencesData = try {
            runBlocking { loadPreferencesData() }
        } catch (e: Exception) {
            APP_LOGGER.warn("Can't load preferences.", e)
            null
        }

        //System.setProperty("apple.awt.application.name", "ZUGFeRD-UI")
        if (preferencesData?.darkMode == true) {
            System.setProperty("apple.awt.application.appearance", "NSAppearanceNameDarkAqua")
        } else if (preferencesData?.darkMode == null) {
            System.setProperty("apple.awt.application.appearance", "system")
        }
    }

    application {
        APP_LOGGER.info("Launching $APP_TITLE_FULL $APP_VERSION")

        val scope = rememberCoroutineScope()

        // Initially load preferences within composable application.
        val preferences = LocalPreferences.current

        // Initially load senders within composable application.
        @Suppress("UNUSED_VARIABLE")
        val senders = LocalSenders.current

        // Initially load recipients within composable application.
        @Suppress("UNUSED_VARIABLE")
        val recipients = LocalRecipients.current

        // Initially load products within composable application.
        @Suppress("UNUSED_VARIABLE")
        val products = LocalProducts.current

        val windowState = rememberWindowState(
            size = preferences.windowSize,
            position = preferences.windowPosition,
        )

        Window(
            title = "$APP_TITLE_FULL $APP_VERSION",
            state = windowState,
            onCloseRequest = {
                val pos = windowState.position
                preferences.setWindowPosition(
                    x = if (pos.isSpecified) pos.x.value.toInt() else null,
                    y = if (pos.isSpecified) pos.y.value.toInt() else null,
                )

                val size = windowState.size
                preferences.setWindowSize(
                    width = if (size.isSpecified) size.width.value.toInt() else 1000,
                    height = if (size.isSpecified) size.height.value.toInt() else 800,
                )

                scope.launch {
                    preferences.save()
                }

                exitApplication()
            },
        ) {
            CompositionLocalProvider(LocalDesktopWindow provides window) {
                App()
            }
        }
    }
}
