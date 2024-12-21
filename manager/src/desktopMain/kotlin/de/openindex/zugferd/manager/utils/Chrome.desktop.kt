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

import de.openindex.zugferd.manager.APP_LOGGER
import de.openindex.zugferd.manager.AppInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import me.friwi.jcefmaven.CefAppBuilder
import org.cef.CefApp
import org.cef.CefClient
import org.cef.browser.CefBrowser
import java.nio.file.Path
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.absolutePathString
import kotlin.io.path.createDirectories
import kotlin.io.path.deleteIfExists
import kotlin.io.path.deleteRecursively
import kotlin.io.path.exists
import kotlin.io.path.isDirectory
import kotlin.io.path.isRegularFile
import kotlin.io.path.reader
import kotlin.io.path.writer

const val CEF_WINDOWLESS_RENDERING_ENABLED = true
const val CEF_OFFSCREEN_RENDERING_ENABLED = true
const val CEF_TRANSPARENCY_ENABLED = false
const val CEF_DISABLE_GPU = false

private val CEF_IS_BUNDLED: Boolean by lazy {
    CEF_BUNDLED_INSTALL_DIR.isDirectory()
}

private val CEF_BUNDLED_INSTALL_DIR: Path by lazy {
    if (getPlatform().isRunningInMacAppBundle && APP_LAUNCHER != null) {
        APP_LAUNCHER!!.parent.parent.resolve("chrome")
    } else {
        RESOURCES_DIR.resolve("chrome")
    }
}

private val CEF_INSTALL_DIR: Path by lazy {
    if (CEF_IS_BUNDLED) {
        APP_LOGGER.info("Using bundled Chrome libraries.")
        CEF_BUNDLED_INSTALL_DIR
    } else {
        APP_LOGGER.info("Installing Chrome libraries into user directory.")
        APP_WORK_DIR
            .resolve("chrome")
            .createDirectories()
    }
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
        } catch (e: Exception) {
            APP_LOGGER.error("Browser is not properly initialized!", e)
            throw RuntimeException("Browser is not properly initialized!", e)
        }
    }

    return if (CEF_BROWSER == null) {
        CEF_CLIENT!!.createBrowser(
            url,
            CEF_OFFSCREEN_RENDERING_ENABLED,
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

    runBlocking {
        delay(1000)
    }
}

@OptIn(ExperimentalPathApi::class)
suspend fun installWebView() {
    if (CEF_APP != null) {
        return
    }

    CEF_APP = withContext(Dispatchers.IO) {
        CEF_LOG_FILE.deleteIfExists()

        // Delete CEF installation directory,
        // if a newer version is available.
        if (!CEF_IS_BUNDLED && CEF_INSTALL_DIR.exists()) {
            val installedCefVersion = if (CEF_VERSION_FILE.isRegularFile()) {
                CEF_VERSION_FILE
                    .reader(charset = Charsets.UTF_8)
                    .use { it.readText().trimToNull() }
            } else {
                null
            }

            if (installedCefVersion != AppInfo.Custom.CHROME_VERSION) {
                APP_LOGGER.info("Delete outdated Chrome version \"${installedCefVersion}\".")
                CEF_INSTALL_DIR.deleteRecursively()
            }
        }

        // Remember currently installed CEF version.
        if (!CEF_IS_BUNDLED) {
            CEF_VERSION_FILE
                .writer(charset = Charsets.UTF_8)
                .use { it.write(AppInfo.Custom.CHROME_VERSION) }
        }

        val builder = CefAppBuilder()
        builder.setInstallDir(CEF_INSTALL_DIR.toFile())
        builder.skipInstallation = CEF_IS_BUNDLED
        builder.cefSettings.locale = "de-DE"
        builder.cefSettings.log_file = CEF_LOG_FILE.absolutePathString()
        builder.cefSettings.root_cache_path = CEF_CACHE_DIR.absolutePathString()
        builder.cefSettings.cache_path = CEF_CACHE_DIR.resolve("client").absolutePathString()
        builder.cefSettings.windowless_rendering_enabled = CEF_WINDOWLESS_RENDERING_ENABLED

        if (CEF_DISABLE_GPU) {
            builder.addJcefArgs("--disable-gpu")
        }

        builder.build()
    }
}
