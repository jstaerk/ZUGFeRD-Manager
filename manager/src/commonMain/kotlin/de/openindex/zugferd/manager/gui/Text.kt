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

/*
@Composable
fun SingleLineText(
    text: String,
    color: Color = Color.Unspecified,
    style: TextStyle = LocalTextStyle.current,
    overflow: TextOverflow = TextOverflow.Ellipsis,
    modifier: Modifier = Modifier,
) {
    /*
    val localContentColor = LocalContentColor.current
    val localContentAlpha = LocalContentAlpha.current
    val overrideColorOrUnspecified: Color = if (color.isSpecified) {
        color
    } else if (style.color.isSpecified) {
        style.color
    } else {
        localContentColor.copy(localContentAlpha)
    }
    */

    Text(
        text = text,
        color = color,
        //color = overrideColorOrUnspecified,
        style = style,
        overflow = overflow,
        softWrap = false,
        modifier = modifier,
    )
}
*/

@Composable
fun SectionTitle(
    text: String,
    color: Color = MaterialTheme.colorScheme.onSurface,
    modifier: Modifier = Modifier,
) = Text(
    text = text,
    color = color,
    modifier = modifier,
    style = MaterialTheme.typography.titleLarge,
    softWrap = true,
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
fun SectionSubTitle(
    text: String,
    modifier: Modifier = Modifier,
    actions: @Composable RowScope.() -> Unit = {},
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
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
