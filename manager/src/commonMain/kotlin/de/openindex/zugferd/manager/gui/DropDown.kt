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
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
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
import de.openindex.zugferd.manager.model.PaymentMethod
import de.openindex.zugferd.manager.model.TaxCategory
import de.openindex.zugferd.manager.model.UnitOfMeasurement
import de.openindex.zugferd.zugferd_manager.generated.resources.PaymentMethod
import de.openindex.zugferd.zugferd_manager.generated.resources.Res
import de.openindex.zugferd.zugferd_manager.generated.resources.TaxCategory
import de.openindex.zugferd.zugferd_manager.generated.resources.UnitOfMeasurement
import org.jetbrains.compose.resources.PluralStringResource
import org.jetbrains.compose.resources.Resource
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource

/**
 * based on https://composables.com/material/exposeddropdownmenubox
 */
@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun <T> DropDown(
    label: String,
    value: T? = null,
    options: Map<T, String>,
    onSelect: (T) -> Unit,
    requiredIndicator: Boolean = false,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }
    var textFieldValue by remember(value) {
        mutableStateOf(
            if (value != null) options[value] ?: "" else ""
        )
    }
    //val textFieldState = rememberTextFieldState(options.get(value) ?: "")

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = modifier,
    ) {
        TextField(
            value = textFieldValue,
            onValueChange = { textFieldValue = it },
            readOnly = true,
            label = {
                InputLabel(
                    text = label,
                    requiredIndicator = requiredIndicator,
                )
            },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(
                    expanded = expanded,
                )
            },
            colors = ExposedDropdownMenuDefaults.textFieldColors(),
            modifier = Modifier
                .pointerHoverIcon(PointerIcon.Default, true)
                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                .fillMaxWidth(),
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            options
                .forEach { option ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = option.value,
                                softWrap = false,
                            )
                        },
                        onClick = {
                            onSelect(option.key)
                            textFieldValue = option.value
                            //textFieldState.setTextAndPlaceCursorAtEnd(option.value)
                            expanded = false
                        }
                    )
                }
        }
    }
}

@Composable
fun <T> DropDown(
    label: Resource,
    value: T? = null,
    options: Map<T, StringResource>,
    onSelect: (T) -> Unit,
    requiredIndicator: Boolean = false,
    modifier: Modifier = Modifier,
) {
    DropDown(
        label = when (label) {
            is PluralStringResource -> pluralStringResource(label, 1)
            is StringResource -> stringResource(label)
            else -> "???"
        },
        value = value,
        options = buildMap<T, String> {
            options.entries.forEach { e -> put(e.key, stringResource(e.value)) }
        },
        onSelect = onSelect,
        requiredIndicator = requiredIndicator,
        modifier = modifier,
    )
}

@Composable
fun TaxCategoryDropDown(
    label: PluralStringResource = Res.plurals.TaxCategory,
    value: TaxCategory? = null,
    options: Map<TaxCategory, StringResource> = buildMap {
        TaxCategory.entries.forEach { e -> put(e, e.title) }
    },
    onSelect: (TaxCategory) -> Unit,
    requiredIndicator: Boolean = false,
    modifier: Modifier = Modifier,
) = DropDown(
    label = label,
    value = value,
    options = options,
    onSelect = onSelect,
    requiredIndicator = requiredIndicator,
    modifier = modifier,
)

@Composable
fun UnitOfMeasurementDropDown(
    label: PluralStringResource = Res.plurals.UnitOfMeasurement,
    value: UnitOfMeasurement? = null,
    options: Map<UnitOfMeasurement, StringResource> = buildMap {
        UnitOfMeasurement.entries.forEach { e -> put(e, e.title) }
    },
    onSelect: (UnitOfMeasurement) -> Unit,
    requiredIndicator: Boolean = false,
    modifier: Modifier = Modifier,
) = DropDown(
    label = label,
    value = value,
    options = options,
    onSelect = onSelect,
    requiredIndicator = requiredIndicator,
    modifier = modifier,
)

@Composable
fun PaymentMethodDropDown(
    label: PluralStringResource = Res.plurals.PaymentMethod,
    value: PaymentMethod? = null,
    options: Map<PaymentMethod, StringResource> = buildMap {
        PaymentMethod.entries.forEach { e -> put(e, e.title) }
    },
    onSelect: (PaymentMethod) -> Unit,
    requiredIndicator: Boolean = false,
    modifier: Modifier = Modifier,
) = DropDown(
    label = label,
    value = value,
    options = options,
    onSelect = onSelect,
    requiredIndicator = requiredIndicator,
    modifier = modifier,
)
