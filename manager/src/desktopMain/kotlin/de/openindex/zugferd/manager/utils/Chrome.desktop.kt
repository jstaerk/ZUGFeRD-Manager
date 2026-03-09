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

package de.openindex.zugferd.manager.utils

import com.jetbrains.cef.JCefAppConfig
import de.openindex.zugferd.manager.APP_LOGGER
import de.openindex.zugferd.manager.AppInfo
import de.openindex.zugferd.manager.AppSection
import de.openindex.zugferd.manager._APP_STATE
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.cef.CefApp
import org.cef.CefClient
import org.cef.CefSettings
import org.cef.browser.CefBrowser
import org.cef.browser.CefRendering
import org.cef.handler.CefAppHandlerAdapter
import java.nio.file.Path
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.absolutePathString
import kotlin.io.path.createDirectories
import kotlin.io.path.deleteIfExists
import kotlin.io.path.deleteRecursively
import kotlin.io.path.exists

private const val CEF_WINDOWLESS_RENDERING_ENABLED = false
private const val CEF_OFFSCREEN_RENDERING_ENABLED = false
private const val CEF_TRANSPARENCY_ENABLED = false

@Suppress("KotlinConstantConditions")
private const val CEF_DEBUG = AppInfo.Git.GIT_BRANCH != "master"

private val CEF_CACHE_DIR: Path by lazy {
    CACHE_DIR
        .resolve("jcef")
        .createDirectories()
}

private val CEF_LOG_FILE: Path by lazy {
    LOGS_DIR
        .resolve("jcef.log")
}

private val OLD_CEF_CACHE_DIR: Path by lazy {
    CACHE_DIR
        .resolve("chrome")
        .createDirectories()
}

private val OLD_CEF_INSTALLATION_DIR: Path by lazy {
    APP_WORK_DIR
        .resolve("chrome")
        .createDirectories()
}

private val OLD_CEF_LOG_FILE: Path by lazy {
    LOGS_DIR
        .resolve("chrome.log")
}

private var CEF_APP: CefApp? = null

private var CEF_CLIENT: CefClient? = null

private var CEF_BROWSER: CefBrowser? = null

/**
 * As we never show multiple browser instances at once,
 * we keep one browser instance permanently in memory and only
 * switch the loaded url.
 */
fun getCefBrowser(url: String): CefBrowser {
    if (CEF_CLIENT == null) {
        try {
            CEF_CLIENT = CEF_APP!!.createClient()
                // Disable Drag & Drop on the browser component,
                // as we don't want external files to be dragged directly into the browser.
                .also {
                    it.removeDragHandler()
                    it.addDragHandler { _, _, _ -> true }
                }
                .also { client ->
                    // Block popups (prevents blank windows from original downloadData/window.open calls).
                    client.addLifeSpanHandler(object : org.cef.handler.CefLifeSpanHandlerAdapter() {
                        override fun onBeforePopup(
                            browser: org.cef.browser.CefBrowser?,
                            frame: org.cef.browser.CefFrame?,
                            targetUrl: String?,
                            targetFrameName: String?,
                        ): Boolean = true
                    })
                }
                .also { client ->
                    // Handle data: URLs (attachments) manually if the browser tries to navigate to them
                    client.addRequestHandler(object : org.cef.handler.CefRequestHandlerAdapter() {
                        override fun onBeforeBrowse(
                            browser: org.cef.browser.CefBrowser?,
                            frame: org.cef.browser.CefFrame?,
                            request: org.cef.network.CefRequest?,
                            user_gesture: Boolean,
                            is_redirect: Boolean
                        ): Boolean {
                            val url = request?.url
                            
                            // Allow initial load of HTML visualization (data:text/html without user gesture)
                            if (url != null && url.startsWith("data:text/html") && !user_gesture) {
                                return false
                            }

                            if (url != null && url.startsWith("data:")) {
                                APP_LOGGER.info("Intercepted data URL navigation. Gesture: $user_gesture")
                                handleDataUrl(url)
                                return true // Cancel navigation
                            }

                            // Intercept file:// links to our attachment temp files → show save dialog.
                            if (url != null && url.startsWith("file:") && url.contains("zugferd_attachments_")) {
                                try {
                                    val file = java.io.File(java.net.URI(url))
                                    if (file.exists()) {
                                        javax.swing.SwingUtilities.invokeLater {
                                            val chooser = javax.swing.JFileChooser()
                                            chooser.selectedFile = java.io.File(
                                                javax.swing.filechooser.FileSystemView
                                                    .getFileSystemView().defaultDirectory,
                                                file.name,
                                            )
                                            chooser.dialogTitle = "Anhang speichern"
                                            if (chooser.showSaveDialog(null) == javax.swing.JFileChooser.APPROVE_OPTION) {
                                                try {
                                                    file.copyTo(chooser.selectedFile, overwrite = true)
                                                } catch (e: Exception) {
                                                    APP_LOGGER.error("Anhang konnte nicht gespeichert werden.", e)
                                                }
                                            }
                                        }
                                    }
                                } catch (e: Exception) {
                                    APP_LOGGER.error("Fehler beim Öffnen des Anhangs.", e)
                                }
                                return true // Cancel navigation
                            }

                            return super.onBeforeBrowse(browser, frame, request, user_gesture, is_redirect)
                        }
                    })
                }
                .also { client ->
                    // Handle downloads from <a href="data:...;base64,..." download="filename"> links
                    // that postProcessHtmlForAttachments() injects in place of "Öffnen" links.
                    client.addDownloadHandler(object : org.cef.handler.CefDownloadHandlerAdapter() {
                        override fun onBeforeDownload(
                            browser: org.cef.browser.CefBrowser?,
                            downloadItem: org.cef.callback.CefDownloadItem?,
                            suggestedName: String?,
                            callback: org.cef.callback.CefBeforeDownloadCallback?,
                        ) {
                            javax.swing.SwingUtilities.invokeLater {
                                val chooser = javax.swing.JFileChooser()
                                chooser.selectedFile = java.io.File(
                                    javax.swing.filechooser.FileSystemView
                                        .getFileSystemView().defaultDirectory,
                                    suggestedName ?: "anhang",
                                )
                                chooser.dialogTitle = "Anhang speichern"
                                if (chooser.showSaveDialog(null) == javax.swing.JFileChooser.APPROVE_OPTION) {
                                    callback?.Continue(chooser.selectedFile.absolutePath, false)
                                }
                            }
                        }
                    })
                }
                .also { client ->
                    client.addKeyboardHandler(object : org.cef.handler.CefKeyboardHandlerAdapter() {
                        override fun onKeyEvent(
                            browser: org.cef.browser.CefBrowser?,
                            event: org.cef.handler.CefKeyboardHandler.CefKeyEvent?
                        ): Boolean {
                            if (event == null) return false
                            if (event.type == org.cef.handler.CefKeyboardHandler.CefKeyEvent.EventType.KEYEVENT_RAWKEYDOWN ||
                                event.type == org.cef.handler.CefKeyboardHandler.CefKeyEvent.EventType.KEYEVENT_KEYDOWN) {
                                
                                // Flags from CefKeyboardHandler
                                // EVENTFLAG_CONTROL_DOWN = 1 << 2
                                // EVENTFLAG_COMMAND_DOWN = 1 << 7 (Meta/Cmd)
                                val controlPressed = (event.modifiers and 4 != 0)
                                val metaPressed = (event.modifiers and 128 != 0)
                                
                                if (event.windows_key_code == 70 && (controlPressed || metaPressed)) { // 70 is 'F'
                                    val visualsState = AppSection.VISUALISATION.state as? de.openindex.zugferd.manager.sections.VisualsSectionState
                                    if (visualsState != null && visualsState.documents.isNotEmpty()) {
                                        visualsState.isSearchOpen = true
                                        return true // Consume event
                                    }
                                }
                            }
                            return false
                        }
                    })
                }
        } catch (e: Exception) {
            APP_LOGGER.error("Browser is not properly initialized!", e)
            throw RuntimeException("Browser is not properly initialized!", e)
        }
    }

    return if (CEF_BROWSER == null) {
        CEF_CLIENT!!.createBrowser(
            url,
            CefRendering.OFFSCREEN
                .takeIf { CEF_OFFSCREEN_RENDERING_ENABLED }
                ?: CefRendering.DEFAULT,
            CEF_TRANSPARENCY_ENABLED,
        )
    } else {
        CEF_BROWSER!!.let {
            it.stopLoad()
            it.loadURL(url)
            it
        }
    }
}

fun uninstallWebView() {
    CEF_BROWSER?.close(true)
    CEF_CLIENT?.dispose()
    CEF_APP?.dispose()
}

@OptIn(ExperimentalPathApi::class)
suspend fun installWebView(gpuEnabled: Boolean = true) {
    if (CEF_APP != null) {
        return
    }

    //
    // Cleanup some old JCEF files from user directory.
    //
    if (OLD_CEF_CACHE_DIR.exists()) {
        OLD_CEF_CACHE_DIR.deleteRecursively()
    }
    if (OLD_CEF_INSTALLATION_DIR.exists()) {
        OLD_CEF_INSTALLATION_DIR.deleteRecursively()
    }
    if (OLD_CEF_LOG_FILE.exists()) {
        OLD_CEF_LOG_FILE.deleteRecursively()
    }

    //
    // Setup paths to bundled JCEF binaries.
    //
    // see org.cef.CefApp#startupAsync
    // see org.cef.Startup
    //
    //if (getPlatform().isRunningFromInstallation) {
    //    if (SystemUtils.IS_OS_MAC) {
    //        val frameworksPath = withContext(Dispatchers.IO) {
    //            APP_LAUNCHER!!.parent.parent.resolve("Frameworks").toRealPath()
    //        }
    //
    //        val cefFrameworkPath = frameworksPath.resolve("Chromium Embedded Framework.framework").absolute()
    //        if (!cefFrameworkPath.isDirectory()) {
    //            APP_LOGGER.error("Can't find bundled CEF Framework: $cefFrameworkPath")
    //        } else {
    //            APP_LOGGER.info("Using bundled CEF Framework: $cefFrameworkPath")
    //            System.setProperty(
    //                "ALT_CEF_FRAMEWORK_DIR",
    //                cefFrameworkPath.pathString,
    //            )
    //        }
    //
    //        val cefHelperPath = frameworksPath.resolve("jcef Helper.app").absolute()
    //        if (!cefHelperPath.isDirectory()) {
    //            APP_LOGGER.error("Can't find bundled CEF Helper: $cefHelperPath")
    //        } else {
    //            APP_LOGGER.info("Using bundled CEF Helper: $cefHelperPath")
    //            System.setProperty(
    //                "ALT_CEF_HELPER_APP_DIR",
    //                cefHelperPath.pathString,
    //            )
    //        }
    //
    //        //System.setProperty(
    //        //    "ALT_JCEF_LIB_DIR",
    //        //    "",
    //        //)
    //    }
    //    //else {
    //    //    System.setProperty(
    //    //        "ALT_CEF_FRAMEWORK_DIR",
    //    //        Path.of(SystemUtils.JAVA_HOME, "lib").absolutePathString(),
    //    //    )
    //    //}
    //}

    CEF_APP = withContext(Dispatchers.IO) {
        try {
            CEF_LOG_FILE.deleteIfExists()
        } catch (e: Exception) {
            APP_LOGGER.warn("Could not delete CEF log file: ${e.message}")
        }

        if (CEF_DEBUG) {
            enableVerboseLogging()
        }

        //if (CEF_DEBUG) {
        //    CefLog.init(
        //        CEF_LOG_FILE.absolutePathString(),
        //        CefSettings.LogSeverity.LOGSEVERITY_VERBOSE,
        //    )
        //} else {
        //    CefLog.init(
        //        CEF_LOG_FILE.absolutePathString(),
        //        CefSettings.LogSeverity.LOGSEVERITY_WARNING,
        //    )
        //}

        // Perform startup initialization on platforms that require it.
        APP_LOGGER.debug("Starting up CEF application...")
        CefApp.startup(emptyArray())

        val config = JCefAppConfig.getInstance()

        //
        // Chrome command line arguments.
        // https://peter.sh/experiments/chromium-command-line-switches/
        //

        val appArgs = config.appArgsAsList

        // Disable DRM / Widevine
        // https://magpcss.org/ceforum/viewtopic.php?f=6&t=19093
        appArgs.add("--disable-component-update")
        
        // Allow local file access and disable web security to support data URLs and local resources
        appArgs.add("--allow-file-access-from-files")
        appArgs.add("--disable-web-security")

        // Disable Hardware Acceleration
        if (!gpuEnabled) {
            appArgs.addAll(
                listOf(
                    "--disable-gpu",
                    "--disable-gpu-compositing",
                    "--disable-gpu-vsync",
                    "--disable-software-rasterizer",
                    //"--disable-extensions",
                )
            )
        }

        val settings = config.cefSettings
        //settings.no_sandbox = true
        settings.locale = "de"
        settings.cache_path = CEF_CACHE_DIR.absolutePathString()
        //settings.root_cache_path = CEF_CACHE_DIR.absolutePathString()
        settings.windowless_rendering_enabled = CEF_WINDOWLESS_RENDERING_ENABLED
        settings.log_file = CEF_LOG_FILE.absolutePathString()
        settings.log_severity = CefSettings.LogSeverity.LOGSEVERITY_VERBOSE
            .takeIf { CEF_DEBUG }
            ?: CefSettings.LogSeverity.LOGSEVERITY_WARNING

        CefApp.addAppHandler(object : CefAppHandlerAdapter(appArgs.toTypedArray()) {
            override fun stateHasChanged(state: CefApp.CefAppState) {
                // Shutdown the app if the native CEF part is terminated
                //if (state == CefAppState.TERMINATED) System.exit(0)

                @Suppress("LoggingStringTemplateAsArgument")
                APP_LOGGER.debug("Changing CEF state to $state")
                super.stateHasChanged(state)
            }

            override fun onBeforeChildProcessLaunch(commandLine: String) {
                APP_LOGGER.info("Launching CEF process: $commandLine")
                super.onBeforeChildProcessLaunch(commandLine)
            }
        })

        APP_LOGGER.debug("Getting CEF application instance...")
        CefApp.getInstance(appArgs.toTypedArray(), settings)
    }
}

@Suppress("SpellCheckingInspection")
private fun enableVerboseLogging() {
    System.setProperty("jcef.tests.verbose", "true")
    System.setProperty("jcef.trace.cefbrowser_n.lifespan", "true")
    System.setProperty("jcef.trace.cefclient.lifespan", "true")
    System.setProperty("jcef.trace.cefapp.lifespan", "true")
    System.setProperty("jcef.trace.cefbrowserwr.addnotify", "true")
    System.setProperty("jcef.log.trace_thread", "true")
}

private fun handleDataUrl(url: String) {
    try {
        val commaIndex = url.indexOf(',')
        if (commaIndex == -1) return

        val metadata = url.substring(5, commaIndex)
        val base64Data = url.substring(commaIndex + 1)
        
        // Extract mime type (e.g. "application/pdf;base64")
        val mimeType = metadata.substringBefore(';')
        
        // Determine extension
        val extension = when {
            mimeType.contains("pdf") -> ".pdf"
            mimeType.contains("png") -> ".png"
            mimeType.contains("jpeg") -> ".jpg"
            mimeType.contains("jpg") -> ".jpg"
            mimeType.contains("gif") -> ".gif"
            mimeType.contains("xml") -> ".xml"
            mimeType.contains("html") -> ".html"
            else -> ".bin"
        }

        val bytes = java.util.Base64.getDecoder().decode(base64Data)
        
        javax.swing.SwingUtilities.invokeLater {
            val chooser = javax.swing.JFileChooser()
            chooser.selectedFile = java.io.File(
                javax.swing.filechooser.FileSystemView.getFileSystemView().defaultDirectory,
                "anhang$extension"
            )
            chooser.dialogTitle = "Anhang speichern"
            if (chooser.showSaveDialog(null) == javax.swing.JFileChooser.APPROVE_OPTION) {
                try {
                    java.nio.file.Files.write(chooser.selectedFile.toPath(), bytes)
                    APP_LOGGER.info("Saved attachment to: ${chooser.selectedFile}")
                } catch (e: Exception) {
                    APP_LOGGER.error("Failed to save attachment", e)
                    javax.swing.JOptionPane.showMessageDialog(null, "Fehler beim Speichern: ${e.message}")
                }
            }
        }
    } catch (e: Exception) {
        APP_LOGGER.error("Failed to handle data URL", e)
    }
}
