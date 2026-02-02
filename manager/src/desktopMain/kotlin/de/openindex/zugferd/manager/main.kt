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
import de.openindex.zugferd.manager.utils.SHUTDOWN_HANDLER
import de.openindex.zugferd.manager.utils.getPlatform
import de.openindex.zugferd.manager.utils.installWebView
import org.apache.commons.lang3.SystemUtils
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.slf4j.bridge.SLF4JBridgeHandler
import kotlinx.coroutines.runBlocking
import kotlin.io.path.absolutePathString
import java.awt.KeyboardFocusManager
import java.awt.event.KeyEvent

val APP_LOGGER: Logger by lazy {
    LoggerFactory.getLogger("de.openindex.zugferd.manager")
}

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
            - runtime  : ${SystemUtils.JAVA_HOME}
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
    // Create and init application state.
    //

    val appState = _APP_STATE

    /*sorgen dafür, dass CEF beim Programmstart vorbereitet wird, 
     *damit der allererste Klick auf „Select e‑Invoice“ keinen CEF‑Initialisierungsfehler mehr auslöst.
    */
    runBlocking {
        installWebView()
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
    // Global Keyboard Interaction (handles Swing/PDF viewer focus too)
    //
    KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher { e ->
        if (e.id == KeyEvent.KEY_PRESSED &&
            e.keyCode == KeyEvent.VK_F &&
            (e.isControlDown || e.isMetaDown)
        ) {
            // Check if Visualisation Section is active
            if (appState.section == AppSection.VISUALISATION) {
                val visualsState = AppSection.VISUALISATION.state as? de.openindex.zugferd.manager.sections.VisualsSectionState
                if (visualsState != null && visualsState.documents.isNotEmpty()) {
                    // Open search (UI update)
                    visualsState.isSearchOpen = true
                    return@addKeyEventDispatcher true // Consume event
                }
            }
        }
        false
    }


    //
    // Start desktop application.
    //

    application {
        val windowState = rememberWindowState(
            size = appState.preferences.windowSize,
            position = appState.preferences.windowPosition,
        )

        Runtime.getRuntime().addShutdownHook(Thread {
            APP_LOGGER.info("Shutdown hook was triggered...")
            SHUTDOWN_HANDLER.saveSettingsOnShutdown(
                preferences = appState.preferences,
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
                App()
            }
        }
    }
}
