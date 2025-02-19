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

package de.openindex.zugferd.manager.sections

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.TooltipPlacement
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.QuestionMark
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import de.openindex.zugferd.manager.gui.CurrencySelectField
import de.openindex.zugferd.manager.gui.DateField
import de.openindex.zugferd.manager.gui.Label
import de.openindex.zugferd.manager.gui.PaymentMethodDropDown
import de.openindex.zugferd.manager.gui.PdfViewer
import de.openindex.zugferd.manager.gui.ProductSelectFieldWithAdd
import de.openindex.zugferd.manager.gui.SectionSubTitle
import de.openindex.zugferd.manager.gui.SectionTitle
import de.openindex.zugferd.manager.gui.Tooltip
import de.openindex.zugferd.manager.gui.TradePartySelectFieldWithAdd
import de.openindex.zugferd.manager.gui.VerticalScrollBox
import de.openindex.zugferd.manager.model.Item
import de.openindex.zugferd.manager.model.UnitOfMeasurement
import de.openindex.zugferd.manager.utils.LocalPreferences
import de.openindex.zugferd.manager.utils.LocalProducts
import de.openindex.zugferd.manager.utils.LocalRecipients
import de.openindex.zugferd.manager.utils.LocalSenders
import de.openindex.zugferd.manager.utils.MAX_PDF_ARCHIVE_VERSION
import de.openindex.zugferd.manager.utils.XmlVisualTransformation
import de.openindex.zugferd.manager.utils.formatAsPercentage
import de.openindex.zugferd.manager.utils.formatAsPrice
import de.openindex.zugferd.manager.utils.formatAsQuantity
import de.openindex.zugferd.manager.utils.getCurrencySymbol
import de.openindex.zugferd.manager.utils.parsePrice
import de.openindex.zugferd.manager.utils.parseQuantity
import de.openindex.zugferd.manager.utils.title
import de.openindex.zugferd.zugferd_manager.generated.resources.AppCreate
import de.openindex.zugferd.zugferd_manager.generated.resources.AppCreateConvert
import de.openindex.zugferd.zugferd_manager.generated.resources.AppCreateConvertInfo
import de.openindex.zugferd.zugferd_manager.generated.resources.AppCreateConvertWarning
import de.openindex.zugferd.zugferd_manager.generated.resources.AppCreateDetailsPdf
import de.openindex.zugferd.zugferd_manager.generated.resources.AppCreateDetailsXml
import de.openindex.zugferd.zugferd_manager.generated.resources.AppCreateErrorConversion
import de.openindex.zugferd.zugferd_manager.generated.resources.AppCreateErrorIncompatible
import de.openindex.zugferd.zugferd_manager.generated.resources.AppCreateErrorInvalid
import de.openindex.zugferd.zugferd_manager.generated.resources.AppCreateGeneral
import de.openindex.zugferd.zugferd_manager.generated.resources.AppCreateGeneralDeliveryDate
import de.openindex.zugferd.zugferd_manager.generated.resources.AppCreateGeneralDeliveryDateEnd
import de.openindex.zugferd.zugferd_manager.generated.resources.AppCreateGeneralDeliveryDateInfo
import de.openindex.zugferd.zugferd_manager.generated.resources.AppCreateGeneralDeliveryDateStart
import de.openindex.zugferd.zugferd_manager.generated.resources.AppCreateGeneralInvoiceNumber
import de.openindex.zugferd.zugferd_manager.generated.resources.AppCreateGeneralIssuer
import de.openindex.zugferd.zugferd_manager.generated.resources.AppCreateGeneralIssuerAdd
import de.openindex.zugferd.zugferd_manager.generated.resources.AppCreateGeneralIssuerEdit
import de.openindex.zugferd.zugferd_manager.generated.resources.AppCreateGeneralRecipient
import de.openindex.zugferd.zugferd_manager.generated.resources.AppCreateGeneralRecipientAdd
import de.openindex.zugferd.zugferd_manager.generated.resources.AppCreateGeneralRecipientEdit
import de.openindex.zugferd.zugferd_manager.generated.resources.AppCreateGenerate
import de.openindex.zugferd.zugferd_manager.generated.resources.AppCreateGenerateInfo
import de.openindex.zugferd.zugferd_manager.generated.resources.AppCreateItems
import de.openindex.zugferd.zugferd_manager.generated.resources.AppCreateItemsAdd
import de.openindex.zugferd.zugferd_manager.generated.resources.AppCreateSelect
import de.openindex.zugferd.zugferd_manager.generated.resources.AppCreateSelectInfo
import de.openindex.zugferd.zugferd_manager.generated.resources.AppCreateSelectMessage
import de.openindex.zugferd.zugferd_manager.generated.resources.AppCreateTotals
import de.openindex.zugferd.zugferd_manager.generated.resources.Res
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource

@Composable
@OptIn(ExperimentalFoundationApi::class)
fun CreateSection(state: CreateSectionState) {
    val scope = rememberCoroutineScope()
    val isValid = state.invoiceValid
    val selectedPdf = state.selectedPdf
    val selectedPdfArchiveVersion = state.selectedPdfArchiveVersion
    val selectedPdfArchiveError = state.selectedPdfArchiveError
    val isSelectedPdfArchiveUsable = state.isSelectedPdfArchiveUsable

    if (selectedPdf == null) {
        EmptyView(state)
    } else {
        Column {

        }
        Row(
            modifier = Modifier
                .fillMaxSize(),
        ) {
            Column(
                modifier = Modifier
                    .weight(0.6f, fill = true),
            ) {
                VerticalScrollBox(
                    modifier = Modifier
                        .weight(1f, fill = true),
                ) {
                    CreateView(state)
                }

                AnimatedVisibility(visible = !isValid && isSelectedPdfArchiveUsable) {
                    Notification(
                        text = stringResource(Res.string.AppCreateErrorInvalid).trim(),
                    )
                }

                AnimatedVisibility(visible = !isSelectedPdfArchiveUsable && selectedPdfArchiveVersion > MAX_PDF_ARCHIVE_VERSION) {
                    Notification(
                        text = stringResource(
                            Res.string.AppCreateErrorIncompatible,
                            "PDF/A-${selectedPdfArchiveVersion}",
                        ).trim(),
                    )
                }

                AnimatedVisibility(visible = !isSelectedPdfArchiveUsable && selectedPdfArchiveError != null) {
                    Notification(
                        text = stringResource(Res.string.AppCreateErrorConversion).trim()
                            .plus(" ").plus(selectedPdfArchiveError),
                    )
                }

                AnimatedVisibility(visible = !isSelectedPdfArchiveUsable && selectedPdfArchiveVersion < MAX_PDF_ARCHIVE_VERSION && selectedPdfArchiveError == null) {
                    Notification(
                        text = stringResource(Res.string.AppCreateConvertWarning).trim()
                    ) {
                        Tooltip(
                            text = stringResource(Res.string.AppCreateConvertInfo).trim(),
                            tooltipPlacement = TooltipPlacement.CursorPoint(
                                alignment = Alignment.TopCenter,
                                offset = DpOffset(8.dp, (-16).dp)
                            ),
                        ) {
                            Button(
                                onClick = {
                                    scope.launch {
                                        state.convertToPdfArchive()
                                    }
                                },
                                modifier = Modifier
                                    .padding(all = 8.dp),
                            ) {
                                Text(
                                    text = stringResource(Res.string.AppCreateConvert).title(),
                                    softWrap = false,
                                )
                            }
                        }
                    }
                }
            }

            Column(
                modifier = Modifier
                    .weight(0.4f, fill = true),
            ) {
                DetailsView(state)
            }
        }
    }
}

@Composable
@OptIn(ExperimentalFoundationApi::class)
fun CreateSectionActions(state: CreateSectionState) {
    val scope = rememberCoroutineScope()
    val preferences = LocalPreferences.current
    val senders = LocalSenders.current
    val products = LocalProducts.current
    val selectedPdf = state.selectedPdf
    val isSelectedPdfArchiveUsable = state.isSelectedPdfArchiveUsable
    val isValid = state.invoiceValid

    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
            .padding(end = 8.dp),
    ) {
        Tooltip(
            text = stringResource(Res.string.AppCreateSelectInfo).trim(),
        ) {
            TextButton(
                onClick = {
                    scope.launch(Dispatchers.IO) {
                        state.selectPdf(
                            preferences = preferences,
                            senders = senders,
                            products = products
                        )
                    }
                },
                colors = ButtonDefaults.textButtonColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                )
            ) {
                Text(
                    text = stringResource(Res.string.AppCreateSelect).title(),
                    softWrap = false,
                )
            }
        }

        AnimatedVisibility(visible = selectedPdf != null && isSelectedPdfArchiveUsable && isValid) {
            Tooltip(
                text = stringResource(Res.string.AppCreateGenerateInfo).trim(),
            ) {
                TextButton(
                    onClick = {
                        scope.launch(Dispatchers.IO) {
                            state.exportPdf(
                                preferences = preferences,
                            )
                        }
                    },
                    colors = ButtonDefaults.textButtonColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                    )
                ) {
                    Text(
                        text = stringResource(Res.string.AppCreateGenerate).title(),
                        softWrap = false,
                    )
                }
            }
        }
    }
}

@Composable
@Suppress("UNUSED_PARAMETER")
private fun EmptyView(state: CreateSectionState) {
    Row(
        modifier = Modifier
            .fillMaxSize(),
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxSize(),
        )
        {
            Text(
                text = stringResource(Res.string.AppCreateSelectMessage).trim(),
                softWrap = false,
            )
        }
    }
}

@Composable
private fun CreateView(state: CreateSectionState) {
    val selectedPdfName = state.originalSelectedPdf?.name ?: "???"

    Column(
        verticalArrangement = Arrangement.spacedBy(24.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp, horizontal = 20.dp)
    ) {
        SectionTitle(
            text = stringResource(Res.string.AppCreate, selectedPdfName).title(),
        )

        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier
                .fillMaxWidth()
        ) {

            SectionSubTitle(
                text = stringResource(Res.string.AppCreateGeneral).title(),
            )

            GeneralForm(state)
        }

        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier
                .fillMaxWidth()
        ) {

            SectionSubTitle(
                text = stringResource(Res.string.AppCreateItems).title(),
            ) {
                Button(
                    onClick = {
                        state.invoiceItems = state.invoiceItems
                            .plus(Item())
                    },
                    modifier = Modifier,
                ) {
                    Text(
                        text = stringResource(Res.string.AppCreateItemsAdd).title(),
                        softWrap = false,
                    )
                }
            }

            ItemsForm(state)
        }

        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier
                .fillMaxWidth()
        ) {

            SectionSubTitle(
                text = stringResource(Res.string.AppCreateTotals).title(),
            )

            AmountSummary(state)
        }
    }
}

@Composable
private fun DetailsView(state: CreateSectionState) {
    val preferences = LocalPreferences.current
    val selectedPdf = state.selectedPdf
    val isInvoiceValid = state.invoiceValid
    var tabState by remember { mutableStateOf(0) }
    val isPdfTab by derivedStateOf { !isInvoiceValid || tabState == 0 }
    val isXmlTab by derivedStateOf { isInvoiceValid && tabState == 1 }

    val systemIsDark = isSystemInDarkTheme()
    val xmlVisualTransformation = remember(preferences.isThemeDark, systemIsDark) {
        XmlVisualTransformation(darkMode = preferences.darkMode ?: systemIsDark)
    }
    val xmlTextStyle = MaterialTheme.typography.bodyMedium.copy(
        fontFamily = FontFamily.Monospace,
    )

    TabRow(
        selectedTabIndex = if (isInvoiceValid) tabState else 0,
    ) {
        Tab(
            selected = isPdfTab,
            onClick = { tabState = 0 },
            text = {
                Text(
                    text = stringResource(Res.string.AppCreateDetailsPdf).title(),
                    softWrap = false,
                )
            },
        )

        AnimatedVisibility(visible = isInvoiceValid) {
            Tab(
                selected = isXmlTab,
                enabled = isInvoiceValid,
                onClick = { tabState = 1 },
                text = {
                    Text(
                        text = stringResource(Res.string.AppCreateDetailsXml).title(),
                        softWrap = false,
                    )
                },
            )
        }
    }

    if (isPdfTab) {
        PdfViewer(pdf = selectedPdf!!)
    }

    if (isXmlTab) {
        TextField(
            value = state.invoiceXml,
            onValueChange = {},
            readOnly = true,
            singleLine = false,
            shape = RectangleShape,
            textStyle = xmlTextStyle,
            visualTransformation = xmlVisualTransformation,
            modifier = Modifier.fillMaxSize(),
        )
    }
}

@Composable
@OptIn(ExperimentalFoundationApi::class)
@Suppress("UnusedReceiverParameter")
private fun ColumnScope.GeneralForm(state: CreateSectionState) {
    val scope = rememberCoroutineScope()
    val preferences = LocalPreferences.current

    val senders = LocalSenders.current
    val sendersList = derivedStateOf {
        if (state.invoiceSender?.isSaved == false) {
            listOf(state.invoiceSender!!, *senders.senders.toTypedArray())
        } else {
            senders.senders
        }
    }

    val recipients = LocalRecipients.current
    val recipientsList = derivedStateOf {
        if (state.invoiceRecipient?.isSaved == false) {
            listOf(state.invoiceRecipient!!, *recipients.recipients.toTypedArray())
        } else {
            recipients.recipients
        }
    }

    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
            .fillMaxWidth(),
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .weight(1f, fill = true),
        ) {
            TradePartySelectFieldWithAdd(
                label = Res.string.AppCreateGeneralIssuer,
                addLabel = Res.string.AppCreateGeneralIssuerAdd,
                editLabel = Res.string.AppCreateGeneralIssuerEdit,
                tradeParty = state.invoiceSender,
                tradeParties = sendersList.value,
                requiredIndicator = true,
                onSelect = { sender, savePermanently ->
                    state.invoiceSender = sender
                    if (sender != null && savePermanently) {
                        scope.launch {
                            senders.put(sender, preferences)
                        }
                    } else if (sender?.isSaved == true) {
                        preferences.setPreviousSenderKey(sender._key)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth(),
            )

            TradePartySelectFieldWithAdd(
                label = Res.string.AppCreateGeneralRecipient,
                addLabel = Res.string.AppCreateGeneralRecipientAdd,
                editLabel = Res.string.AppCreateGeneralRecipientEdit,
                tradeParty = state.invoiceRecipient,
                tradeParties = recipientsList.value,
                requiredIndicator = true,
                onSelect = { recipient, savePermanently ->
                    state.invoiceRecipient = recipient
                    state.invoicePaymentMethod = recipient?._defaultPaymentMethod ?: state.invoicePaymentMethod
                    if (recipient != null && savePermanently) {
                        scope.launch {
                            recipients.put(recipient)
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth(),
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth(),
            ) {
                TextField(
                    label = {
                        Label(
                            text = stringResource(Res.string.AppCreateGeneralInvoiceNumber).title(),
                            requiredIndicator = true,
                        )
                    },
                    value = state.invoiceNumber,
                    onValueChange = { state.invoiceNumber = it },
                    modifier = Modifier
                        .weight(0.5f, true),
                )

                PaymentMethodDropDown(
                    value = state.invoicePaymentMethod,
                    onSelect = { state.invoicePaymentMethod = it },
                    modifier = Modifier
                        .weight(0.5f, true),
                )

                // HACK: Show hidden button to ensure equal width of input fields.
                IconButton(
                    onClick = {},
                    modifier = Modifier
                        .alpha(0f)
                ) {
                    Icon(
                        imageVector = Icons.Default.QuestionMark,
                        contentDescription = "",
                    )
                }
            }

            Tooltip(
                text = stringResource(Res.string.AppCreateGeneralDeliveryDateInfo).trim()
            )
            {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .fillMaxWidth(),
                ) {
                    DateField(
                        label = stringResource(Res.string.AppCreateGeneralDeliveryDate).title(),
                        value = state.deliveryDate,
                        requiredIndicator = state.deliveryStartDate == null || state.deliveryEndDate == null,
                        onValueChange = { state.deliveryDate = it },
                        modifier = Modifier
                            .weight(0.3f, true),
                    )

                    DateField(
                        label = stringResource(Res.string.AppCreateGeneralDeliveryDateStart).title(),
                        value = state.deliveryStartDate,
                        requiredIndicator = state.deliveryDate == null,
                        onValueChange = { state.deliveryStartDate = it },
                        modifier = Modifier
                            .weight(0.3f, true),
                    )

                    DateField(
                        label = stringResource(Res.string.AppCreateGeneralDeliveryDateEnd).title(),
                        value = state.deliveryEndDate,
                        requiredIndicator = state.deliveryDate == null,
                        onValueChange = { state.deliveryEndDate = it },
                        modifier = Modifier
                            .weight(0.3f, true),
                    )

                    // HACK: Show hidden button to ensure equal width of input fields.
                    IconButton(
                        onClick = {},
                        modifier = Modifier
                            .alpha(0f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.QuestionMark,
                            contentDescription = "",
                        )
                    }
                }
            }
        }

        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .width(200.dp),
        ) {
            CurrencySelectField(
                label = "Währung*",
                currency = state.invoiceCurrency,
                onSelect = { currency ->
                    state.invoiceCurrency = currency
                    if (currency != null) {
                        preferences.setPreviousCurrency(currency)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth(),
            )

            DateField(
                label = "erstellt am*",
                value = state.invoiceIssueDate,
                onValueChange = { state.invoiceIssueDate = it ?: state.invoiceIssueDate },
                modifier = Modifier
                    .fillMaxWidth(),
            )

            DateField(
                label = "fällig am*",
                value = state.invoiceDueDate,
                onValueChange = { state.invoiceDueDate = it ?: state.invoiceDueDate },
                modifier = Modifier
                    .fillMaxWidth(),
            )
        }
    }

    /*
    if (!state.invoiceValid) {
        Text(text = "Die Rechnungsangaben sind ungültig bzw. unvollständig.")
    } else {
        TextField(
            value = state.invoiceXml,
            label = {
                Text(
                    text = "XML",
                    softWrap = false,
                )
            },
            readOnly = true,
            minLines = 10,
            maxLines = 30,
            onValueChange = {},
            modifier = Modifier
                //.height(50.dp)
                .fillMaxWidth(),
        )
    }
    */
}

@Composable
private fun ColumnScope.ItemsForm(state: CreateSectionState) {
    val preferences = LocalPreferences.current
    val invoiceItems = state.invoiceItems

    if (invoiceItems.isEmpty()) {
        Text(
            text = "Es sind noch keine Rechnungsposten hinterlegt.",
        )
    } else {
        invoiceItems.forEachIndexed { index, item ->
            if (index > 0) {
                HorizontalDivider(
                    modifier = Modifier
                        .fillMaxWidth(),
                )
            }

            ItemForm(
                item = item,
                state = state,
                onUpdate = { updatedItem ->
                    state.invoiceItems = state.invoiceItems
                        .filter { it._uid != item._uid }
                        .plus(updatedItem)

                    if (index == 0 && updatedItem.product?.isSaved == true) {
                        preferences.setPreviousProductKey(
                            updatedItem.product._key
                        )
                    }
                },
                onRemove = {
                    state.invoiceItems = state.invoiceItems
                        .filter { it._uid != item._uid }
                },
            )
        }
    }
}

@Composable
@OptIn(ExperimentalFoundationApi::class)
@Suppress("UnusedReceiverParameter")
private fun ColumnScope.ItemForm(
    item: Item,
    state: CreateSectionState,
    onUpdate: (item: Item) -> Unit,
    onRemove: () -> Unit,
) {
    val scope = rememberCoroutineScope()

    val products = LocalProducts.current
    val productsList = derivedStateOf {
        if (item.product?.isSaved == false) {
            listOf(item.product, *products.products.toTypedArray())
        } else {
            products.products
        }
    }

    val unitSingularName = remember(item.product?.unit) {
        UnitOfMeasurement.getByCode(item.product?.unit)
            ?.description
            ?: item.product?.unit
            ?: UnitOfMeasurement.UNIT.description
    }
    val unitPluralName = remember(item.product?.unit) {
        UnitOfMeasurement.getByCode(item.product?.unit)
            ?.pluralDescription
            ?: item.product?.unit
            ?: UnitOfMeasurement.UNIT.pluralDescription
    }

    Row(
        //verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
            .fillMaxWidth(),
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .weight(1f, fill = true),
        ) {
            ProductSelectFieldWithAdd(
                label = "Rechnungsposten*",
                addLabel = "Neuer Rechnungsposten",
                editLabel = "Rechnungsposten bearbeiten",
                product = item.product,
                products = productsList.value,
                onSelect = { product, savePermanently ->
                    onUpdate(
                        item.copy(
                            product = product,
                            price = product?._defaultPricePerUnit ?: 0.0,
                        )
                    )
                    if (product != null && savePermanently) {
                        scope.launch {
                            products.put(product)
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth(),
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth(),
            ) {
                TextField(
                    value = item.price.formatAsPrice,
                    label = {
                        Text(
                            text = "Preis pro ${unitSingularName}*",
                            softWrap = false,
                        )
                    },
                    singleLine = true,
                    onValueChange = {
                        val price = parsePrice(it)
                        if (price != null) {
                            onUpdate(
                                item.copy(
                                    price = price,
                                )
                            )
                        }
                    },
                    keyboardOptions = KeyboardOptions.Default
                        .copy(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier
                        .weight(0.5f, fill = true),
                )

                TextField(
                    value = item.quantity.formatAsQuantity,
                    label = {
                        Text(
                            text = "Anzahl ${unitPluralName}*",
                            softWrap = false,
                        )
                    },
                    singleLine = true,
                    onValueChange = {
                        val quantity = parseQuantity(it)
                        if (quantity != null) {
                            onUpdate(
                                item.copy(
                                    quantity = quantity,
                                )
                            )
                        }
                    },
                    keyboardOptions = KeyboardOptions.Default
                        .copy(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier
                        .weight(0.5f, fill = true),
                )

                Tooltip(
                    text = "Posten aus der Rechnung entfernen."
                ) {
                    IconButton(
                        onClick = { onRemove() },
                    ) {
                        Icon(
                            imageVector = Icons.Default.Remove,
                            contentDescription = "Rechnungsposten entfernen.",
                        )
                    }
                }
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth(),
            ) {
                TextField(
                    value = item.notes ?: "",
                    label = {
                        Text(
                            text = "Anmerkungen",
                            softWrap = false,
                        )
                    },
                    singleLine = true,
                    onValueChange = {
                        onUpdate(
                            item.copy(
                                notes = it,
                            )
                        )
                    },
                    modifier = Modifier
                        .weight(1f, fill = true),
                )

                // HACK: Show hidden button to ensure equal width of input fields.
                IconButton(
                    onClick = {},
                    modifier = Modifier
                        .alpha(0f)
                ) {
                    Icon(
                        imageVector = Icons.Default.QuestionMark,
                        contentDescription = "",
                    )
                }
            }
        }

        ItemSummary(
            item = item,
            state = state,
            modifier = Modifier
                .width(200.dp),
        )
    }
}

@Composable
@Suppress("UnusedReceiverParameter")
private fun RowScope.ItemSummary(
    item: Item,
    state: CreateSectionState,
    modifier: Modifier = Modifier,
) {
    val currency = remember(state.invoiceCurrency) {
        val c = state.invoiceCurrency
        if (c != null)
            getCurrencySymbol(c) ?: c
        else
            ""
    }

    Card(
        modifier = modifier,
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .padding(vertical = 8.dp, horizontal = 16.dp)
        ) {
            Text(
                text = buildAnnotatedString {
                    withStyle(style = SpanStyle(fontSize = 0.9.em)) {
                        append("Netto-Betrag\n")
                    }
                    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                        append(
                            "${item.totalNetPrice.formatAsPrice} $currency"
                        )
                    }
                },
                //textAlign = TextAlign.End,
                style = MaterialTheme.typography.bodyLarge,
            )

            Text(
                text = buildAnnotatedString {
                    withStyle(style = SpanStyle(fontSize = 0.9.em)) {
                        if (item.product != null) {
                            append("${item.product.vatPercent.formatAsPercentage}% ")
                        }
                        append("Steuer\n")
                    }
                    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                        append(
                            "${item.tax.formatAsPrice} $currency"
                        )
                    }
                },
                //textAlign = TextAlign.End,
                style = MaterialTheme.typography.bodyLarge,
            )

            Text(
                text = buildAnnotatedString {
                    withStyle(style = SpanStyle(fontSize = 0.9.em)) {
                        append("Brutto-Betrag\n")
                    }
                    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                        append(
                            "${item.totalGrossPrice.formatAsPrice} $currency"
                        )
                    }
                },
                //textAlign = TextAlign.End,
                style = MaterialTheme.typography.bodyLarge,
            )
        }
    }
}

@Composable
@Suppress("UnusedReceiverParameter")
private fun ColumnScope.AmountSummary(
    state: CreateSectionState,
) {
    val currency = remember(state.invoiceCurrency) {
        val c = state.invoiceCurrency
        if (c != null)
            getCurrencySymbol(c) ?: c
        else
            ""
    }
    val netSum = derivedStateOf {
        state.invoiceItems.sumOf { it.totalNetPrice }
    }
    val taxSum = derivedStateOf {
        state.invoiceItems.sumOf { it.tax }
    }
    val grossSum = derivedStateOf {
        state.invoiceItems.sumOf { it.totalGrossPrice }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth(),
    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .padding(vertical = 8.dp, horizontal = 16.dp)
                .fillMaxWidth(),
        ) {
            Text(
                text = buildAnnotatedString {
                    withStyle(style = SpanStyle(fontSize = 0.9.em)) {
                        append("Netto-Summe\n")
                    }
                    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                        append(
                            "${netSum.value.formatAsPrice} $currency"
                        )
                    }
                },
                style = MaterialTheme.typography.bodyLarge,
            )

            Text(
                text = buildAnnotatedString {
                    withStyle(style = SpanStyle(fontSize = 0.9.em)) {
                        append("Steuer-Summe\n")
                    }
                    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                        append(
                            "${taxSum.value.formatAsPrice} $currency"
                        )
                    }
                },
                style = MaterialTheme.typography.bodyLarge,
            )

            Text(
                text = buildAnnotatedString {
                    withStyle(style = SpanStyle(fontSize = 0.9.em)) {
                        append("Brutto-Summe\n")
                    }
                    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                        append(
                            "${grossSum.value.formatAsPrice} $currency"
                        )
                    }
                },
                style = MaterialTheme.typography.bodyLarge,
            )
        }
    }
}

@Composable
private fun Notification(
    text: String,
    action: @Composable () -> Unit = {},
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .background(color = MaterialTheme.colorScheme.secondaryContainer),
    ) {
        action()

        Text(
            text = text,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSecondaryContainer,
            style = MaterialTheme.typography.bodySmall,
            softWrap = true,
            modifier = Modifier
                .padding(16.dp)
        )
    }
}
