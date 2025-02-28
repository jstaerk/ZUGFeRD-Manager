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

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import de.openindex.zugferd.manager.model.Product

@Composable
fun ProductSelectField(
    label: String = "Rechnungsposten",
    product: Product? = null,
    products: List<Product>,
    onSelect: (Product?) -> Unit,
    modifier: Modifier = Modifier,
) {
    val options = remember(products) {
        buildMap {
            products.forEach { party ->
                put(party, party.summary)
            }
        }
    }

    AutoCompleteField(
        label = label,
        entry = product,
        entries = options,
        onSelect = onSelect,
        modifier = modifier,
    )
}

@Composable
@OptIn(ExperimentalFoundationApi::class)
fun ProductSelectFieldWithAdd(
    label: String = "Rechnungsposten",
    addLabel: String = "Neuer Rechnungsposten",
    editLabel: String = "Rechnungsposten bearbeiten",
    product: Product? = null,
    products: List<Product>,
    onSelect: (product: Product?, savePermanently: Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    var newProduct by remember { mutableStateOf<Product?>(null) }
    val unsavedProduct by derivedStateOf {
        products.find { !it.isSaved }
    }

    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier,
    ) {
        ProductSelectField(
            label = label,
            product = product,
            products = products,
            onSelect = { selection ->
                onSelect(selection, false)
            },
            modifier = Modifier.weight(1f, fill = true),
        )

        Tooltip(
            text = addLabel.takeIf { unsavedProduct == null } ?: editLabel,
        ) {
            IconButton(
                onClick = {
                    newProduct = unsavedProduct ?: Product()
                },
                modifier = Modifier,
            ) {
                if (unsavedProduct == null) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = addLabel,
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = editLabel,
                    )
                }
            }
        }
    }

    if (newProduct != null) {
        ProductDialog(
            title = addLabel.takeIf { unsavedProduct == null } ?: editLabel,
            value = newProduct!!,
            permanentSaveOption = true,
            onDismissRequest = { newProduct = null },
            onSubmitRequest = { selection, savePermanently ->
                onSelect(selection, savePermanently)
                newProduct = null
            },
        )
    }
}
