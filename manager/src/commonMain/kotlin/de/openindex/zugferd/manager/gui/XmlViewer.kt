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

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.font.FontFamily
import de.openindex.zugferd.manager.utils.LocalPreferences
import de.openindex.zugferd.manager.utils.XmlVisualTransformation

@Composable
fun XmlViewer(
    xml: String,
    modifier: Modifier = Modifier,
) {
    val preferences = LocalPreferences.current
    val systemIsDark = isSystemInDarkTheme()
    val xmlVisualTransformation = remember(preferences.isThemeDark, systemIsDark) {
        XmlVisualTransformation(darkMode = preferences.darkMode ?: systemIsDark)
    }
    val xmlTextStyle = MaterialTheme.typography.bodyMedium.copy(
        fontFamily = FontFamily.Monospace,
    )

    TextField(
        value = xml,
        onValueChange = {},
        readOnly = true,
        singleLine = false,
        shape = RectangleShape,
        textStyle = xmlTextStyle,
        visualTransformation = xmlVisualTransformation,
        modifier = modifier,
    )
}