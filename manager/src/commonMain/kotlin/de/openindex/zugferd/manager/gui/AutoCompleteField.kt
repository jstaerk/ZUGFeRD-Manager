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

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> AutoCompleteField(
    label: String,
    entry: T? = null,
    entries: Map<T, String>,
    onSelect: (T) -> Unit,
    //actions: @Composable RowScope.() -> Unit = {},
    modifier: Modifier = Modifier,
) {
    var value by remember(entry) {
        mutableStateOf(
            (if (entry != null) entries[entry] else null) ?: ""
        )
    }
    //var valueIsTouched by mutableStateOf(entry == null)

    // The text that the user inputs into the text field can be used to filter the options.
    // This sample uses string subsequence matching.
    //val filteredOptions = options.filteredBy(textFieldState.text)
    val filteredOptions = entries
        .filter {
            //if (entry != null && valueIsTouched) {
            //    return@filter true
            //}

            val query = value.trim()
            query.isBlank() || it.value.lowercase().contains(query.lowercase())
        }
        .toList()
    //.sortedBy { e -> e.second.lowercase() }

    val (allowExpanded, setExpanded) = remember { mutableStateOf(false) }
    val expanded = allowExpanded && filteredOptions.isNotEmpty()

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = setExpanded,
        modifier = modifier,
    ) {
        TextField(
            value = value,
            onValueChange = {
                value = it
                //valueIsTouched = true
                //setExpanded(!expanded)
            },
            // The `menuAnchor` modifier must be passed to the text field to handle
            // expanding/collapsing the menu on click. An editable text field has
            // the anchor type `PrimaryEditable`.
            modifier = Modifier
                //.clickable {
                //    if (valueIsTouched) {
                //        setExpanded(!expanded)
                //    }
                //}
                .fillMaxWidth()
                //.weight(1f, true)
                .menuAnchor(MenuAnchorType.PrimaryEditable),
            singleLine = true,
            label = {
                Text(
                    text = label,
                    softWrap = false,
                )
            },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(
                    expanded = expanded,
                    // If the text field is editable, it is recommended to make the
                    // trailing icon a `menuAnchor` of type `SecondaryEditable`. This
                    // provides a better experience for certain accessibility services
                    // to choose a menu option without typing.
                    modifier = Modifier
                        .menuAnchor(MenuAnchorType.SecondaryEditable)
                        .pointerHoverIcon(PointerIcon.Default, true),
                )
            },
            colors = ExposedDropdownMenuDefaults.textFieldColors(),
        )

        ExposedDropdownMenu(
            modifier = Modifier.heightIn(max = 280.dp),
            expanded = expanded,
            onDismissRequest = { setExpanded(false) },
        ) {
            filteredOptions.forEach { option ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = option.second,
                            style = MaterialTheme.typography.bodyLarge,
                            softWrap = false,
                        )
                    },
                    onClick = {
                        value = option.second
                        //textFieldState.setTextAndPlaceCursorAtEnd(option.second)
                        onSelect(option.first)
                        setExpanded(false)
                    },
                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                )
            }
        }
    }
}
