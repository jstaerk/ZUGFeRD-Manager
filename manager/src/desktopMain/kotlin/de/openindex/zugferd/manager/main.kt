/*
 * Copyright (c) 2024-2025 Andreas Rudolph <andy@openindex.de>.
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package de.openindex.zugferd.manager

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.window.ApplicationScope
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import ch.qos.logback.classic.ClassicConstants
import de.openindex.zugferd.manager.utils.APP_LAUNCHER
import de.openindex.zugferd.manager.utils.LOGS_DIR
import de.openindex.zugferd.manager.utils.LocalPreferences
import de.openindex.zugferd.manager.utils.LocalProducts
import de.openindex.zugferd.manager.utils.LocalRecipients
import de.openindex.zugferd.manager.utils.LocalSenders
import de.openindex.zugferd.manager.utils.SHUTDOWN_HANDLER
import de.openindex.zugferd.manager.utils.getPlatform
import de.openindex.zugferd.manager.utils.loadPreferencesData
import kotlinx.coroutines.runBlocking
import org.apache.commons.lang3.SystemUtils
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.slf4j.bridge.SLF4JBridgeHandler
import kotlin.io.path.absolutePathString

val APP_LOGGER: Logger by lazy {
    LoggerFactory.getLogger("de.openindex.zugferd.manager")
}

//val LocalDesktopWindow = staticCompositionLocalOf<ComposeWindow?> { null }
//val LocalWindowState = staticCompositionLocalOf<WindowState?> { null }
val LocalApplicationScope = staticCompositionLocalOf<ApplicationScope?> { null }

fun main() {
    //
    // Setup logging.
    //

    System.setProperty(
        "app.log.dir",
        LOGS_DIR.absolutePathString(),
    )
    val logbackXml = object {}.javaClass.getResource("logback.xml")
    if (logbackXml != null) {
        System.setProperty(
            ClassicConstants.CONFIG_FILE_PROPERTY,
            logbackXml.toExternalForm()
        )
    }

    //
    // Bridge between Java Logging and SLF4J.
    // https://www.slf4j.org/legacy.html#jul-to-slf4j
    // https://www.slf4j.org/api/org/slf4j/bridge/SLF4JBridgeHandler.html
    //

    SLF4JBridgeHandler.removeHandlersForRootLogger()
    SLF4JBridgeHandler.install()


    APP_LOGGER.info(
        """
            Launching $APP_TITLE_FULL
            - version  : $APP_VERSION
            - platform : ${getPlatform().name}
            - launcher : $APP_LAUNCHER
        """.trimIndent(),
    )


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


    //
    // Dump System Properties
    //

    //System.getProperties().keys()
    //    .asSequence()
    //    .map { it.toString() }
    //    .sortedBy { it }
    //    .forEach {
    //        APP_LOGGER.info("${it}: ${System.getProperty(it)}")
    //    }


    //
    // Start desktop application.
    //

    application {
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

        Runtime.getRuntime().addShutdownHook(Thread {
            APP_LOGGER.info("Shutdown hook was triggered...")
            SHUTDOWN_HANDLER.saveSettingsOnShutdown(
                preferences = preferences,
                windowState = windowState,
            )
        })

        Window(
            title = "$APP_TITLE_FULL $APP_VERSION",
            state = windowState,
            onCloseRequest = {
                APP_LOGGER.info("Closing window...")
                exitApplication()
            },
        ) {
            CompositionLocalProvider(LocalApplicationScope provides this@application) {
                //CompositionLocalProvider(LocalWindowState provides windowState) {
                App()
                //}
            }
        }
    }
}
