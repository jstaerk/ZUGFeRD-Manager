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

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import de.openindex.zugferd.manager.utils.pluralStringResource
import de.openindex.zugferd.manager.utils.stringResource
import de.openindex.zugferd.manager.utils.title
import org.jetbrains.compose.resources.PluralStringResource
import org.jetbrains.compose.resources.StringResource

@Composable
fun SectionTitle(
    text: String,
    color: Color = MaterialTheme.colorScheme.onSurface,
    modifier: Modifier = Modifier,
) = Text(
    text = text,
    color = color,
    style = MaterialTheme.typography.titleLarge,
    softWrap = true,
    modifier = modifier,
)

@Composable
fun SectionTitle(
    text: StringResource,
    color: Color = MaterialTheme.colorScheme.onSurface,
    modifier: Modifier = Modifier,
) = SectionTitle(
    text = stringResource(text).title(),
    color = color,
    modifier = modifier,
)

@Composable
@Suppress("unused")
fun SectionTitle(
    text: PluralStringResource,
    quantity: Int = 1,
    color: Color = MaterialTheme.colorScheme.onSurface,
    modifier: Modifier = Modifier,
) = SectionTitle(
    text = pluralStringResource(text, quantity).title(),
    color = color,
    modifier = modifier,
)

@Composable
fun SectionSubTitle(
    text: String,
    modifier: Modifier = Modifier,
    actions: @Composable RowScope.() -> Unit = {},
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth(),
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.titleMedium,
            lineHeight = 1.em,
            softWrap = false,
            modifier = Modifier,
        )

        HorizontalDivider(
            //color = MaterialTheme.colorScheme.onSurface
            //    .copy(alpha = 0.2f),
            modifier = Modifier
                .weight(1f, fill = true),
        )

        actions()
    }
}

@Composable
fun SectionSubTitle(
    text: StringResource,
    modifier: Modifier = Modifier,
    actions: @Composable RowScope.() -> Unit = {},
) = SectionSubTitle(
    text = stringResource(text).title(),
    modifier = modifier,
    actions = actions,
)

@Composable
@Suppress("unused")
fun SectionSubTitle(
    text: PluralStringResource,
    quantity: Int = 1,
    modifier: Modifier = Modifier,
    actions: @Composable RowScope.() -> Unit = {},
) = SectionSubTitle(
    text = pluralStringResource(text, quantity).title(),
    modifier = modifier,
    actions = actions,
)

@Composable
fun SectionInfo(
    text: String,
    color: Color = MaterialTheme.colorScheme.onSurface,
    modifier: Modifier = Modifier,
) = Text(
    text = text,
    color = color,
    modifier = modifier,
    style = MaterialTheme.typography.bodyMedium,
    softWrap = true,
)

@Composable
fun SectionInfo(
    text: StringResource,
    color: Color = MaterialTheme.colorScheme.onSurface,
    modifier: Modifier = Modifier,
) = SectionInfo(
    text = stringResource(text),
    color = color,
    modifier = modifier,
)

@Composable
@Suppress("unused")
fun SectionInfo(
    text: PluralStringResource,
    quantity: Int = 1,
    color: Color = MaterialTheme.colorScheme.onSurface,
    modifier: Modifier = Modifier,
) = SectionInfo(
    text = pluralStringResource(text, quantity),
    color = color,
    modifier = modifier,
)
