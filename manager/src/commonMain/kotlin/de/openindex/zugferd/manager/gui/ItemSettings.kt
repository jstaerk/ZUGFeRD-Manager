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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.awtEventOrNull
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.unit.dp
import de.openindex.zugferd.manager.model.Product
import de.openindex.zugferd.manager.model.TradeParty
import de.openindex.zugferd.manager.utils.trimToNull

interface ItemSettingsContext {
    fun closeDialog()
}

@Composable
@OptIn(ExperimentalLayoutApi::class, ExperimentalComposeUiApi::class)
fun <T, K> ItemSettings(
    items: List<T>,
    itemKey: (T) -> K,
    itemText: (T) -> String,
    itemCreate: () -> T,
    onRemove: (T) -> Unit,
    dialog: @Composable ItemSettingsContext.(item: T) -> Unit,
) {
    val selectedItem = remember { mutableStateOf<T?>(null) }
    val editedItem = remember { mutableStateOf<T?>(null) }

    fun getItemKey(item: T?): K? {
        return if (item != null) {
            itemKey(item)
        } else {
            null
        }
    }

    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier,
    ) {
        if (items.isNotEmpty()) {
            Card(
                modifier = Modifier,
            ) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(0.dp, 400.dp)
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .verticalScroll(rememberScrollState()),
                ) {
                    items.forEach { item ->
                        TextButton(
                            onClick = {
                                //tradePartySelected.value = item
                                //    .takeIf { tradePartySelected.value != item }
                                selectedItem.value = item
                            },
                            shape = MaterialTheme.shapes.medium,
                            colors = if (getItemKey(selectedItem.value) != getItemKey(item))
                                ButtonDefaults.textButtonColors()
                            else
                                ButtonDefaults.textButtonColors(
                                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                    contentColor = Color.White,
                                ),
                            modifier = Modifier
                                .onPointerEvent(PointerEventType.Press) {
                                    if (it.awtEventOrNull?.clickCount != 2) {
                                        return@onPointerEvent
                                    }
                                    editedItem.value = selectedItem.value
                                },
                        ) {
                            Text(
                                text = itemText(item).trimToNull() ?: "???",
                                softWrap = false,
                            )
                        }
                    }
                }
            }
        }

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier,
        ) {
            Button(
                onClick = {
                    editedItem.value = itemCreate()
                },
            ) {
                Text(
                    text = "Hinzufügen",
                    softWrap = false,
                )
            }

            AnimatedVisibility(visible = selectedItem.value != null) {
                Button(
                    onClick = {
                        editedItem.value = selectedItem.value
                        //tradePartySelected.value = null
                    },
                ) {
                    Text(
                        text = "Bearbeiten",
                        softWrap = false,
                    )
                }
            }

            AnimatedVisibility(visible = selectedItem.value != null) {
                Button(
                    onClick = {
                        val tradeParty = selectedItem.value
                        if (tradeParty != null) {
                            onRemove(tradeParty)
                            editedItem.value = null
                            selectedItem.value = null
                        }
                    },
                ) {
                    Text(
                        text = "Löschen",
                        softWrap = false,
                    )
                }
            }
        }
    }

    if (editedItem.value != null) {
        val item = editedItem.value!!
        val context = object : ItemSettingsContext {
            override fun closeDialog() {
                editedItem.value = null
                selectedItem.value = null
            }
        }
        context.dialog(item)
    }
}

@Composable
fun TradePartyItemSettings(
    tradeParties: List<TradeParty>,
    isCustomer: Boolean = false,
    onSave: (TradeParty) -> Unit,
    onRemove: (TradeParty) -> Unit,
    dialogTitle: (TradeParty?) -> String
) {
    ItemSettings(
        items = tradeParties,
        itemKey = { it._key },
        itemText = { it.summary },
        itemCreate = { TradeParty() },
        onRemove = onRemove,
    ) { selectedItem ->
        TradePartyDialog(
            title = dialogTitle(selectedItem),
            value = selectedItem,
            isCustomer = isCustomer,
            onSubmitRequest = { updatedItem, _ ->
                onSave(updatedItem)
                closeDialog()
            },
            onDismissRequest = {
                closeDialog()
            },
        )
    }
}

@Composable
fun ProductItemSettings(
    products: List<Product>,
    onSave: (Product) -> Unit,
    onRemove: (Product) -> Unit,
    dialogTitle: (Product?) -> String
) {
    ItemSettings(
        items = products,
        itemKey = { it._key },
        itemText = { it.summary },
        itemCreate = { Product() },
        onRemove = onRemove,
    ) { selectedItem ->
        ProductDialog(
            title = dialogTitle(selectedItem),
            value = selectedItem,
            onSubmitRequest = { updatedItem, _ ->
                onSave(updatedItem)
                closeDialog()
            },
            onDismissRequest = {
                closeDialog()
            },
        )
    }
}
