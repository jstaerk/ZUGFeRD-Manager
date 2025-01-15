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

package de.openindex.zugferd.manager.gui

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.SwingPanel
import de.openindex.zugferd.manager.utils.LocalPreferences
import de.openindex.zugferd.manager.utils.getCefBrowser
import de.openindex.zugferd.manager.utils.installWebView
import kotlinx.coroutines.launch
import org.cef.browser.CefBrowser
import java.awt.BorderLayout
import javax.swing.JPanel
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

@Composable
@OptIn(ExperimentalEncodingApi::class)
@Suppress("DuplicatedCode")
actual fun WebViewer(html: String, modifier: Modifier) {
    val scope = rememberCoroutineScope()
    var isInstalled by remember { mutableStateOf(false) }
    val chromeGpuEnabled = LocalPreferences.current.chromeGpuEnabled

    LaunchedEffect(Unit) {
        scope.launch {
            installWebView(
                gpuEnabled = chromeGpuEnabled,
            )
        }.invokeOnCompletion {
            isInstalled = true
        }
    }

    var browserState by remember { mutableStateOf<CefBrowser?>(null) }
    val dataUrl = remember(html) {
        "data:text/html;charset=utf-8;base64,".plus(
            Base64.Default.encode(html.encodeToByteArray())
        )
    }

    DisposableEffect(Unit) {
        onDispose {
            //APP_LOGGER.debug("CLOSE WEB-BROWSER")
            browserState?.close(true)
        }
    }

    if (isInstalled) {
        SwingPanel(
            background = MaterialTheme.colorScheme.surface,
            modifier = modifier,
            factory = {
                val browser = getCefBrowser(dataUrl)
                browserState = browser
                WebPanel(browser)
            },
            update = { panel: WebPanel ->
                panel.browser.stopLoad()
                panel.browser.loadURL(dataUrl)
            }
        )
    }
}

private class WebPanel(
    val browser: CefBrowser
) : JPanel(BorderLayout()) {
    init {
        add(browser.uiComponent, BorderLayout.CENTER)
    }
}
