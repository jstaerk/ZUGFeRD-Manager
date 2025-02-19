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

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import org.jetbrains.compose.resources.PluralStringResource
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource

const val REQUIRED_INDICATOR = "*"

@Composable
fun Label(
    text: String,
    requiredIndicator: Boolean = false,
) {
    Text(
        text = if (requiredIndicator)
            "${text}${REQUIRED_INDICATOR}"
        else
            text,

        softWrap = false,
    )
}

@Composable
fun Label(
    text: StringResource,
    requiredIndicator: Boolean = false,
) {
    Label(
        text = stringResource(text),
        requiredIndicator = requiredIndicator,
    )
}

@Composable
fun Label(
    text: PluralStringResource,
    quantity: Int = 1,
    requiredIndicator: Boolean = false,
) {
    Label(
        text = pluralStringResource(text, quantity),
        requiredIndicator = requiredIndicator,
    )
}
