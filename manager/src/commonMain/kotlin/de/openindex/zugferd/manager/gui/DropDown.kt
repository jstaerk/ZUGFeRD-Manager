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
import de.openindex.zugferd.manager.model.TaxCategoryCode
import de.openindex.zugferd.manager.model.UnitOfMeasurement

/**
 * based on https://composables.com/material/exposeddropdownmenubox
 */
@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun <T> DropDown(
    label: String,
    value: T,
    options: Map<T, String>,
    onSelect: (T) -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }
    var textFieldValue by remember(value) { mutableStateOf(options[value] ?: "") }
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
                Text(
                    text = label,
                    softWrap = false,
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
fun TaxCategoryCodeDropDown(
    label: String = "Besteuerung",
    value: TaxCategoryCode,
    options: Map<TaxCategoryCode, String> = buildMap {
        TaxCategoryCode.entries.forEach { e -> put(e, e.description) }
    },
    onSelect: (TaxCategoryCode) -> Unit,
    modifier: Modifier = Modifier,
) = DropDown(
    label = label,
    value = value,
    options = options,
    onSelect = onSelect,
    modifier = modifier,
)

@Composable
fun UnitOfMeasurementDropDown(
    label: String = "Maßeinheit",
    value: UnitOfMeasurement,
    options: Map<UnitOfMeasurement, String> = buildMap {
        UnitOfMeasurement.entries.forEach { e -> put(e, e.description) }
    },
    onSelect: (UnitOfMeasurement) -> Unit,
    modifier: Modifier = Modifier,
) = DropDown(
    label = label,
    value = value,
    options = options,
    onSelect = onSelect,
    modifier = modifier,
)

@Composable
fun PaymentMethodDropDown(
    label: String = "Zahlungsart",
    value: PaymentMethod,
    options: Map<PaymentMethod, String> = buildMap {
        PaymentMethod.entries.forEach { e -> put(e, e.description) }
    },
    onSelect: (PaymentMethod) -> Unit,
    modifier: Modifier = Modifier,
) = DropDown(
    label = label,
    value = value,
    options = options,
    onSelect = onSelect,
    modifier = modifier,
)
