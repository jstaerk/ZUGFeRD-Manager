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

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import de.openindex.zugferd.manager.LocalAppState
import de.openindex.zugferd.manager.model.Product
import de.openindex.zugferd.manager.model.TaxCategory
import de.openindex.zugferd.manager.model.UnitOfMeasurement
import de.openindex.zugferd.manager.utils.formatAsPercentage
import de.openindex.zugferd.manager.utils.formatAsPrice
import de.openindex.zugferd.manager.utils.parsePercentage
import de.openindex.zugferd.manager.utils.parsePrice
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.getString

private enum class ProductForm {
    GENERAL,
    DESCRIPTION;

    fun title(): String = when (this) {
        GENERAL -> "Allgemein"
        DESCRIPTION -> "Beschreibung"
    }
}

@Composable
fun ProductDialog(
    title: String,
    value: Product,
    permanentSaveOption: Boolean = false,
    onDismissRequest: () -> Unit,
    onSubmitRequest: (product: Product, savePermanently: Boolean) -> Unit,
) {
    val appState = LocalAppState.current
    appState.setLocked(true)

    DialogWindow(
        onCloseRequest = {
            onDismissRequest()
            appState.setLocked(false)
        },
        title = title,
        width = 700.dp,
        height = 600.dp,
    ) {
        ProductDialogContent(
            //title = title,
            title = null,
            value = value,
            permanentSaveOption = permanentSaveOption,
            onDismissRequest = {
                onDismissRequest()
                appState.setLocked(false)
            },
            onSubmitRequest = { product, savePermanently ->
                onSubmitRequest(product, savePermanently)
                appState.setLocked(false)
            },
        )
    }

    /*
    Dialog(
        onDismissRequest = onDismissRequest,
    ) {
        ProductDialogContent(
            title = title,
            value = value,
            onDismissRequest = onDismissRequest,
            onSubmitRequest = onSubmitRequest,
        )
    }
    */
}

@Composable
@Suppress("SameParameterValue", "DuplicatedCode")
private fun ProductDialogContent(
    title: String?,
    value: Product,
    permanentSaveOption: Boolean,
    onDismissRequest: () -> Unit,
    onSubmitRequest: (product: Product, savePermanently: Boolean) -> Unit,
) {
    var product by remember { mutableStateOf(value.copy()) }
    var savePermanently by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .border(border = BorderStroke(Dp.Hairline, MaterialTheme.colorScheme.primaryContainer))
            .shadow(elevation = 12.dp)
            //.fillMaxWidth(),
            .fillMaxWidth(),
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize(),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize(),
            ) {
                if (title != null) {
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .background(color = MaterialTheme.colorScheme.primaryContainer)
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                    ) {
                        Text(
                            text = title,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            style = MaterialTheme.typography.titleLarge,
                            softWrap = false,
                        )
                    }
                }

                ProductForm(
                    value = product,
                    onUpdate = { product = it },
                )

                Spacer(Modifier.weight(1f, true))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.Start),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                ) {
                    Button(
                        onClick = {
                            onSubmitRequest(
                                product,
                                savePermanently,
                            )
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        )
                    ) {
                        Text(
                            text = "Übernehmen",
                            softWrap = false,
                        )
                    }

                    if (permanentSaveOption) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier
                                .clickable { savePermanently = !savePermanently },
                        ) {
                            Switch(
                                checked = savePermanently,
                                onCheckedChange = { savePermanently = it },
                            )
                            Text(
                                text = "dauerhaft speichern",
                                softWrap = false,
                            )
                        }
                    }

                    Spacer(
                        modifier = Modifier
                            .weight(1f, true),
                    )

                    Button(
                        onClick = { onDismissRequest() },
                    ) {
                        Text(
                            text = "Abbrechen",
                            softWrap = false,
                        )
                    }
                }
            }
        }
    }
}


@Composable
private fun ProductForm(
    value: Product,
    onUpdate: (product: Product) -> Unit,
) {
    var state by remember { mutableStateOf(0) }

    TabRow(
        selectedTabIndex = state,
    ) {
        ProductForm.entries.forEachIndexed { index, form ->
            Tab(
                selected = state == index,
                onClick = { state = index },
                text = {
                    Text(
                        text = form.title(),
                        softWrap = false,
                    )
                },
            )
        }
    }

    Column(
        modifier = Modifier
            .padding(horizontal = 16.dp, vertical = 16.dp)
            .fillMaxWidth()
    ) {
        AnimatedVisibility(visible = state == 0) {
            ProductFormGeneral(
                value = value,
                onUpdate = onUpdate,
            )
        }
        AnimatedVisibility(visible = state == 1) {
            ProductFormDescription(
                value = value,
                onUpdate = onUpdate,
            )
        }
    }
}

@Composable
private fun ProductFormGeneral(
    value: Product,
    onUpdate: (product: Product) -> Unit,
) {
    val scope = rememberCoroutineScope()

    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
            .fillMaxWidth(),
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .fillMaxWidth(),
        ) {
            TextField(
                value = value.name,
                label = {
                    InputLabel(
                        text = "Name",
                        requiredIndicator = true,
                    )
                },
                singleLine = true,
                onValueChange = {
                    onUpdate(
                        value.copy(name = it)
                    )
                },
                modifier = Modifier
                    .weight(1f, fill = true),
            )

            UnitOfMeasurementDropDown(
                value = UnitOfMeasurement.getByCode(value.unit) ?: UnitOfMeasurement.UNIT,
                requiredIndicator = true,
                onSelect = {
                    onUpdate(
                        value.copy(unit = it.code)
                    )
                },
                modifier = Modifier
                    .width(150.dp),
            )

            TextField(
                value = value._defaultPricePerUnit.formatAsPrice,
                label = {
                    InputLabel(
                        text = "Preis pro Einheit",
                        requiredIndicator = true,
                    )
                },
                singleLine = true,
                onValueChange = {
                    val defaultPrice = parsePrice(it)
                    if (defaultPrice != null) {
                        onUpdate(
                            value.copy(_defaultPricePerUnit = defaultPrice)
                        )
                    }
                },
                keyboardOptions = KeyboardOptions.Default
                    .copy(keyboardType = KeyboardType.Decimal),
                modifier = Modifier
                    .width(150.dp),
            )
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .fillMaxWidth(),
        ) {
            TaxCategoryDropDown(
                value = TaxCategory.getByCode(value.taxCategoryCode) ?: TaxCategory.NORMAL_TAX,
                requiredIndicator = true,
                onSelect = {
                    scope.launch {
                        val taxExemptionReason = if (it.defaultExemptionReason != null)
                            getString(it.defaultExemptionReason)
                        else
                            null

                        onUpdate(
                            value.copy(
                                taxCategoryCode = it.code,
                                vatPercent = it.defaultPercentage,
                                taxExemptionReason = taxExemptionReason,
                            )
                        )
                    }
                },
                modifier = Modifier
                    .weight(1f, fill = true),
            )

            TextField(
                value = value.vatPercent.formatAsPercentage,
                label = {
                    InputLabel(
                        text = "Steuersatz in %",
                        requiredIndicator = true,
                    )
                },
                singleLine = true,
                onValueChange = {
                    val vatPercent = parsePercentage(it)
                    if (vatPercent != null) {
                        onUpdate(
                            value.copy(
                                vatPercent = vatPercent,
                                taxExemptionReason = if (vatPercent > 0) "" else value.taxExemptionReason,
                            )
                        )
                    }
                },
                keyboardOptions = KeyboardOptions.Default
                    .copy(keyboardType = KeyboardType.Decimal),
                modifier = Modifier
                    .width(150.dp),
            )
        }

        AnimatedVisibility(visible = value.vatPercent <= 0.0) {
            TextField(
                value = value.taxExemptionReason ?: "",
                label = {
                    InputLabel(
                        text = "Begründung für fehlende Besteuerung",
                        requiredIndicator = true,
                    )
                },
                onValueChange = {
                    onUpdate(
                        value.copy(taxExemptionReason = it)
                    )
                },
                singleLine = false,
                modifier = Modifier
                    .heightIn(min = 150.dp, max = 300.dp)
                    .fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun ProductFormDescription(
    value: Product,
    onUpdate: (product: Product) -> Unit,
) {
    TextField(
        value = value.description ?: "",
        label = {
            Text(
                text = "Beschreibung",
                softWrap = false,
            )
        },
        onValueChange = {
            onUpdate(
                value.copy(
                    description = it
                )
            )
        },
        singleLine = false,
        modifier = Modifier
            .heightIn(min = 150.dp, max = 300.dp)
            .fillMaxWidth(),
    )
}
