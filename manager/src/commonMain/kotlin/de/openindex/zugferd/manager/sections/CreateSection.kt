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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.QuestionMark
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import de.openindex.zugferd.manager.gui.ActionButtonWithTooltip
import de.openindex.zugferd.manager.gui.CurrencyField
import de.openindex.zugferd.manager.gui.DateField
import de.openindex.zugferd.manager.gui.DecimalField
import de.openindex.zugferd.manager.gui.Label
import de.openindex.zugferd.manager.gui.NotificationBar
import de.openindex.zugferd.manager.gui.PaymentMethodField
import de.openindex.zugferd.manager.gui.PdfViewer
import de.openindex.zugferd.manager.gui.ProductFieldWithAdd
import de.openindex.zugferd.manager.gui.SectionSubTitle
import de.openindex.zugferd.manager.gui.SectionTitle
import de.openindex.zugferd.manager.gui.TextField
import de.openindex.zugferd.manager.gui.Tooltip
import de.openindex.zugferd.manager.gui.TradePartyFieldWithAdd
import de.openindex.zugferd.manager.gui.VerticalScrollBox
import de.openindex.zugferd.manager.gui.XmlViewer
import de.openindex.zugferd.manager.model.Item
import de.openindex.zugferd.manager.model.UnitOfMeasurement
import de.openindex.zugferd.manager.utils.LocalPreferences
import de.openindex.zugferd.manager.utils.LocalProducts
import de.openindex.zugferd.manager.utils.LocalRecipients
import de.openindex.zugferd.manager.utils.LocalSenders
import de.openindex.zugferd.manager.utils.MAX_PDF_ARCHIVE_VERSION
import de.openindex.zugferd.manager.utils.formatAsPercentage
import de.openindex.zugferd.manager.utils.formatAsPrice
import de.openindex.zugferd.manager.utils.getCurrencySymbol
import de.openindex.zugferd.manager.utils.pluralStringResource
import de.openindex.zugferd.manager.utils.stringResource
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
import de.openindex.zugferd.zugferd_manager.generated.resources.AppCreateGeneralDueDate
import de.openindex.zugferd.zugferd_manager.generated.resources.AppCreateGeneralInvoiceNumber
import de.openindex.zugferd.zugferd_manager.generated.resources.AppCreateGeneralIssueDate
import de.openindex.zugferd.zugferd_manager.generated.resources.AppCreateGeneralRecipient
import de.openindex.zugferd.zugferd_manager.generated.resources.AppCreateGeneralRecipientAdd
import de.openindex.zugferd.zugferd_manager.generated.resources.AppCreateGeneralRecipientEdit
import de.openindex.zugferd.zugferd_manager.generated.resources.AppCreateGeneralSender
import de.openindex.zugferd.zugferd_manager.generated.resources.AppCreateGeneralSenderAdd
import de.openindex.zugferd.zugferd_manager.generated.resources.AppCreateGeneralSenderEdit
import de.openindex.zugferd.zugferd_manager.generated.resources.AppCreateGenerate
import de.openindex.zugferd.zugferd_manager.generated.resources.AppCreateGenerateInfo
import de.openindex.zugferd.zugferd_manager.generated.resources.AppCreateItems
import de.openindex.zugferd.zugferd_manager.generated.resources.AppCreateItemsAdd
import de.openindex.zugferd.zugferd_manager.generated.resources.AppCreateItemsEmpty
import de.openindex.zugferd.zugferd_manager.generated.resources.AppCreateItemsItem
import de.openindex.zugferd.zugferd_manager.generated.resources.AppCreateItemsItemAdd
import de.openindex.zugferd.zugferd_manager.generated.resources.AppCreateItemsItemDescription
import de.openindex.zugferd.zugferd_manager.generated.resources.AppCreateItemsItemEdit
import de.openindex.zugferd.zugferd_manager.generated.resources.AppCreateItemsItemPrice
import de.openindex.zugferd.zugferd_manager.generated.resources.AppCreateItemsItemQuantity
import de.openindex.zugferd.zugferd_manager.generated.resources.AppCreateItemsItemRemove
import de.openindex.zugferd.zugferd_manager.generated.resources.AppCreateItemsItemSummaryGross
import de.openindex.zugferd.zugferd_manager.generated.resources.AppCreateItemsItemSummaryNet
import de.openindex.zugferd.zugferd_manager.generated.resources.AppCreateItemsItemSummaryTax
import de.openindex.zugferd.zugferd_manager.generated.resources.AppCreateSelect
import de.openindex.zugferd.zugferd_manager.generated.resources.AppCreateSelectInfo
import de.openindex.zugferd.zugferd_manager.generated.resources.AppCreateSelectMessage
import de.openindex.zugferd.zugferd_manager.generated.resources.AppCreateSummary
import de.openindex.zugferd.zugferd_manager.generated.resources.AppCreateSummaryGross
import de.openindex.zugferd.zugferd_manager.generated.resources.AppCreateSummaryNet
import de.openindex.zugferd.zugferd_manager.generated.resources.AppCreateSummaryTax
import de.openindex.zugferd.zugferd_manager.generated.resources.Res
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Main view of the create section.
 */
@Composable
@OptIn(ExperimentalFoundationApi::class)
fun CreateSection(state: CreateSectionState) {
    val scope = rememberCoroutineScope()
    val isValid = state.invoiceValid
    val selectedPdf = state.selectedPdf
    val selectedPdfArchiveVersion = state.selectedPdfArchiveVersion
    val selectedPdfArchiveError = state.selectedPdfArchiveError
    val isSelectedPdfArchiveUsable = state.isSelectedPdfArchiveUsable

    // Show an empty view, if no PDF file was selected.
    if (selectedPdf == null) {
        EmptyView(state)
        return
    }

    Row(
        modifier = Modifier
            .fillMaxSize(),
    ) {
        // Left column with e-invoice creation form.
        Column(
            modifier = Modifier
                .weight(0.6f, fill = true),
        ) {
            // Show form to create an e-invoice from the selected PDF file.
            VerticalScrollBox(
                modifier = Modifier
                    .weight(1f, fill = true),
            ) {
                CreateView(state)
            }

            // Show validation error for invalid e-invoice inputs.
            AnimatedVisibility(visible = !isValid && isSelectedPdfArchiveUsable) {
                NotificationBar(
                    text = Res.string.AppCreateErrorInvalid,
                )
            }

            // Show error message, if selected PDF has an unsupported / unusable PDF/A version.
            AnimatedVisibility(visible = !isSelectedPdfArchiveUsable && selectedPdfArchiveVersion > MAX_PDF_ARCHIVE_VERSION) {
                NotificationBar(
                    text = stringResource(
                        Res.string.AppCreateErrorIncompatible,
                        "PDF/A-${selectedPdfArchiveVersion}",
                    ),
                )
            }

            // Show error message, if selected PDF was not convertable to PDF/A-3.
            AnimatedVisibility(visible = !isSelectedPdfArchiveUsable && selectedPdfArchiveError != null) {
                NotificationBar(
                    text = stringResource(Res.string.AppCreateErrorConversion)
                        .plus("\n").plus(selectedPdfArchiveError),
                )
            }

            // Show warning message, if selected PDF has an unsupported PDF/A version but maybe convertable to PDF/A-3.
            AnimatedVisibility(visible = !isSelectedPdfArchiveUsable && selectedPdfArchiveVersion < MAX_PDF_ARCHIVE_VERSION && selectedPdfArchiveError == null) {
                NotificationBar(
                    text = Res.string.AppCreateConvertWarning,
                ) {
                    // Information about possible conversion problems as tooltip.
                    Tooltip(
                        text = Res.string.AppCreateConvertInfo,
                        tooltipPlacement = TooltipPlacement.CursorPoint(
                            alignment = Alignment.TopCenter,
                            offset = DpOffset(8.dp, (-16).dp)
                        ),
                    ) {
                        // Button to start manual conversion to PDF/A-3.
                        Button(
                            onClick = {
                                scope.launch {
                                    state.convertToPdfArchive()
                                }
                            },
                            modifier = Modifier
                                .padding(all = 8.dp),
                        ) {
                            Label(
                                text = stringResource(Res.string.AppCreateConvert).title(),
                            )
                        }
                    }
                }
            }
        }

        // Right column with further details about the selected PDF.
        Column(
            modifier = Modifier
                .weight(0.4f, fill = true),
        ) {
            DetailsView(state)
        }
    }
}

/**
 * Action buttons of the create section, shown on the top right.
 */
@Composable
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
        // Add button to select a PDF file to create an e-invoice from.
        ActionButtonWithTooltip(
            label = Res.string.AppCreateSelect,
            tooltip = Res.string.AppCreateSelectInfo,
            onClick = {
                scope.launch(Dispatchers.IO) {
                    state.selectPdf(
                        preferences = preferences,
                        senders = senders,
                        products = products
                    )
                }
            },
        )

        // Add button to create an e-invoice from the selected PDF with the user-provided inputs.
        AnimatedVisibility(visible = selectedPdf != null && isSelectedPdfArchiveUsable && isValid) {
            ActionButtonWithTooltip(
                label = Res.string.AppCreateGenerate,
                tooltip = Res.string.AppCreateGenerateInfo,
                onClick = {
                    scope.launch(Dispatchers.IO) {
                        state.exportPdf(
                            preferences = preferences,
                        )
                    }
                },
            )
        }
    }
}

/**
 * Empty view of the create section.
 * This is shown, if no PDF file was selected by the user.
 */
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
        ) {
            // Request user to select a PDF file.
            Text(
                text = stringResource(Res.string.AppCreateSelectMessage),
                softWrap = true,
            )
        }
    }
}

/**
 * Left side view of the create section.
 * This provides the form for invoice data.
 */
@Composable
private fun CreateView(state: CreateSectionState) {
    val selectedPdfName = state.originalSelectedPdf?.name ?: "???"

    Column(
        verticalArrangement = Arrangement.spacedBy(24.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp, horizontal = 20.dp)
    ) {
        // Section title.
        SectionTitle(
            text = stringResource(Res.string.AppCreate, selectedPdfName).title(),
        )

        // Subsection with form for general information.
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier
                .fillMaxWidth()
        ) {
            SectionSubTitle(
                text = Res.string.AppCreateGeneral,
            )

            GeneralForm(state)
        }

        // Subsection with form for line items.
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier
                .fillMaxWidth()
        ) {
            SectionSubTitle(
                text = Res.string.AppCreateItems,
            ) {
                Button(
                    onClick = {
                        state.invoiceItems = state.invoiceItems.plus(Item())
                    },
                ) {
                    Label(
                        text = Res.string.AppCreateItemsAdd,
                    )
                }
            }

            ItemsForm(state)
        }

        // Subsection with calculated summary of line items.
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier
                .fillMaxWidth()
        ) {

            SectionSubTitle(
                text = Res.string.AppCreateSummary,
            )

            AmountSummary(state)
        }
    }
}

/**
 * Right side view of the create section.
 * This provides the PDF viewer.
 * Also, the XML viewer is shown, if the selected PDF file contains XML metadata.
 */
@Composable
private fun DetailsView(state: CreateSectionState) {
    val isInvoiceValid = state.invoiceValid
    var tabState by remember { mutableStateOf(0) }
    val isPdfTabSelected by derivedStateOf { !isInvoiceValid || tabState == 0 }
    val isXmlTabSelected by derivedStateOf { isInvoiceValid && tabState == 1 }

    TabRow(
        selectedTabIndex = if (isInvoiceValid) tabState else 0,
    ) {
        // Add tab for PDF viewer.
        Tab(
            selected = isPdfTabSelected,
            onClick = { tabState = 0 },
            text = {
                Label(
                    text = Res.string.AppCreateDetailsPdf,
                )
            },
        )

        // Add tab for XML viewer.
        AnimatedVisibility(visible = isInvoiceValid) {
            Tab(
                selected = isXmlTabSelected,
                enabled = isInvoiceValid,
                onClick = { tabState = 1 },
                text = {
                    Label(
                        text = Res.string.AppCreateDetailsXml,
                    )
                },
            )
        }
    }

    // Show PDF viewer.
    if (isPdfTabSelected) {
        PdfViewer(
            pdf = state.selectedPdf!!,
            modifier = Modifier.fillMaxSize(),
        )
    }

    // Show XML viewer.
    if (isXmlTabSelected) {
        XmlViewer(
            xml = state.invoiceXml,
            modifier = Modifier.fillMaxSize(),
        )
    }
}

/**
 * Form for editing general information about the e-invoice.
 */
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
        // Left side of the form.
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .weight(1f, fill = true),
        ) {
            // Field for the invoice sender / issuer.
            TradePartyFieldWithAdd(
                label = Res.string.AppCreateGeneralSender,
                addLabel = Res.string.AppCreateGeneralSenderAdd,
                editLabel = Res.string.AppCreateGeneralSenderEdit,
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

            // Field for the invoice recipient.
            TradePartyFieldWithAdd(
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
                // Field for the invoice number.
                TextField(
                    label = Res.string.AppCreateGeneralInvoiceNumber,
                    value = state.invoiceNumber,
                    requiredIndicator = true,
                    onValueChange = { state.invoiceNumber = it },
                    modifier = Modifier
                        .weight(0.5f, true),
                )

                // Field for the payment method.
                PaymentMethodField(
                    value = state.invoicePaymentMethod,
                    requiredIndicator = true,
                    onSelect = { state.invoicePaymentMethod = it },
                    modifier = Modifier
                        .weight(0.5f, true),
                )

                // HACK: Show hidden button to ensure equal width of input fields.
                InvisibleButton()
            }

            Tooltip(
                text = Res.string.AppCreateGeneralDeliveryDateInfo,
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .fillMaxWidth(),
                ) {
                    // Field for the delivery date.
                    DateField(
                        label = stringResource(Res.string.AppCreateGeneralDeliveryDate).title(),
                        value = state.deliveryDate,
                        requiredIndicator = state.deliveryStartDate == null || state.deliveryEndDate == null,
                        onValueChange = { state.deliveryDate = it },
                        modifier = Modifier
                            .weight(0.3f, true),
                    )

                    // Field for the start of delivery period.
                    DateField(
                        label = stringResource(Res.string.AppCreateGeneralDeliveryDateStart).title(),
                        value = state.deliveryStartDate,
                        requiredIndicator = state.deliveryDate == null,
                        onValueChange = { state.deliveryStartDate = it },
                        modifier = Modifier
                            .weight(0.3f, true),
                    )

                    // Field for the end of delivery period.
                    DateField(
                        label = stringResource(Res.string.AppCreateGeneralDeliveryDateEnd).title(),
                        value = state.deliveryEndDate,
                        requiredIndicator = state.deliveryDate == null,
                        onValueChange = { state.deliveryEndDate = it },
                        modifier = Modifier
                            .weight(0.3f, true),
                    )

                    // HACK: Show hidden button to ensure equal width of input fields.
                    InvisibleButton()
                }
            }
        }

        // Right side of the form.
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .width(200.dp),
        ) {
            // Field for the currency.
            CurrencyField(
                currency = state.invoiceCurrency,
                requiredIndicator = true,
                onSelect = { currency ->
                    state.invoiceCurrency = currency
                    if (currency != null) {
                        preferences.setPreviousCurrency(currency)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth(),
            )

            // Field for the issue date.
            DateField(
                label = Res.string.AppCreateGeneralIssueDate,
                value = state.invoiceIssueDate,
                requiredIndicator = true,
                onValueChange = { state.invoiceIssueDate = it ?: state.invoiceIssueDate },
                modifier = Modifier
                    .fillMaxWidth(),
            )

            // Field for the due date.
            DateField(
                label = Res.string.AppCreateGeneralDueDate,
                value = state.invoiceDueDate,
                requiredIndicator = true,
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
            label = "XML",
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

/**
 * Form for editing one or more line items about the e-invoice.
 */
@Composable
private fun ColumnScope.ItemsForm(state: CreateSectionState) {
    val preferences = LocalPreferences.current
    val invoiceItems = state.invoiceItems

    // No invoice items selected.
    if (invoiceItems.isEmpty()) {
        Text(
            text = stringResource(Res.string.AppCreateItemsEmpty),
        )
        return
    }

    // Render a form for each available invoice item.
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

/**
 * Form for editing a single line item about the e-invoice.
 */
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

    val unit = remember(item.product?.unit) {
        UnitOfMeasurement.getByCode(item.product?.unit)
            ?: UnitOfMeasurement.UNIT
    }

    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
            .fillMaxWidth(),
    ) {
        // Item form on the left side.
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .weight(1f, fill = true),
        ) {
            // Field for the invoice item.
            ProductFieldWithAdd(
                label = Res.string.AppCreateItemsItem,
                addLabel = Res.string.AppCreateItemsItemAdd,
                editLabel = Res.string.AppCreateItemsItemEdit,
                product = item.product,
                products = productsList.value,
                requiredIndicator = true,
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
                // Field for the item price.
                DecimalField(
                    label = stringResource(
                        Res.string.AppCreateItemsItemPrice,
                        unit.symbol ?: pluralStringResource(unit.value, 1)
                    ),
                    value = item.price,
                    minPrecision = 2,
                    maxPrecision = 2,
                    requiredIndicator = true,
                    onValueChange = { newPrice ->
                        if (newPrice != null) {
                            onUpdate(
                                item.copy(
                                    price = newPrice,
                                )
                            )
                        }
                    },
                    modifier = Modifier
                        .weight(0.5f, fill = true),
                )

                // Field for the item quantity.
                DecimalField(
                    label = stringResource(
                        Res.string.AppCreateItemsItemQuantity,
                        unit.symbol ?: stringResource(unit.title)
                    ),
                    value = item.quantity,
                    minPrecision = unit.minPrecision,
                    maxPrecision = unit.maxPrecision,
                    requiredIndicator = true,
                    onValueChange = { newQuantity ->
                        if (newQuantity != null) {
                            onUpdate(
                                item.copy(
                                    quantity = newQuantity,
                                )
                            )
                        }
                    },
                    modifier = Modifier
                        .weight(0.5f, fill = true),
                )

                // Button for item removal.
                Tooltip(
                    text = Res.string.AppCreateItemsItemRemove,
                ) {
                    IconButton(
                        onClick = { onRemove() },
                    ) {
                        Icon(
                            imageVector = Icons.Default.Remove,
                            contentDescription = stringResource(Res.string.AppCreateItemsItemRemove),
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
                // Field for the item description.
                TextField(
                    label = Res.string.AppCreateItemsItemDescription,
                    value = item.notes ?: "",
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
                InvisibleButton()
            }
        }

        // Item summary on the right side.
        ItemSummary(
            item = item,
            state = state,
            modifier = Modifier
                .width(200.dp),
        )
    }
}

/**
 * Summary of a single line item.
 */
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
            // Item net amount.
            Text(
                text = buildAnnotatedString {
                    withStyle(style = SpanStyle(fontSize = 0.9.em)) {
                        append(stringResource(Res.string.AppCreateItemsItemSummaryNet).title())
                        append("\n")
                    }
                    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                        append("${item.totalNetPrice.formatAsPrice} $currency")
                    }
                },
                style = MaterialTheme.typography.bodyLarge,
            )

            // Calculated item tax.
            Text(
                text = buildAnnotatedString {
                    withStyle(style = SpanStyle(fontSize = 0.9.em)) {
                        if (item.product != null) {
                            append("${item.product.vatPercent.formatAsPercentage}% ")
                        }
                        append(stringResource(Res.string.AppCreateItemsItemSummaryTax).title())
                        append("\n")
                    }
                    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                        append("${item.tax.formatAsPrice} $currency")
                    }
                },
                style = MaterialTheme.typography.bodyLarge,
            )

            // Calculated item gross amount.
            Text(
                text = buildAnnotatedString {
                    withStyle(style = SpanStyle(fontSize = 0.9.em)) {
                        append(stringResource(Res.string.AppCreateItemsItemSummaryGross).title())
                        append("\n")
                    }
                    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                        append("${item.totalGrossPrice.formatAsPrice} $currency")
                    }
                },
                style = MaterialTheme.typography.bodyLarge,
            )
        }
    }
}

/**
 * Calculated summary of all line items.
 */
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
            // Calculated total net amount.
            Text(
                text = buildAnnotatedString {
                    withStyle(style = SpanStyle(fontSize = 0.9.em)) {
                        append(stringResource(Res.string.AppCreateSummaryNet).title())
                        append("\n")
                    }
                    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                        append("${netSum.value.formatAsPrice} $currency")
                    }
                },
                style = MaterialTheme.typography.bodyLarge,
            )

            // Calculated total tax.
            Text(
                text = buildAnnotatedString {
                    withStyle(style = SpanStyle(fontSize = 0.9.em)) {
                        append(stringResource(Res.string.AppCreateSummaryTax).title())
                        append("\n")
                    }
                    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                        append("${taxSum.value.formatAsPrice} $currency")
                    }
                },
                style = MaterialTheme.typography.bodyLarge,
            )

            // Calculated total gross amount.
            Text(
                text = buildAnnotatedString {
                    withStyle(style = SpanStyle(fontSize = 0.9.em)) {
                        append(stringResource(Res.string.AppCreateSummaryGross).title())
                        append("\n")
                    }
                    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                        append("${grossSum.value.formatAsPrice} $currency")
                    }
                },
                style = MaterialTheme.typography.bodyLarge,
            )
        }
    }
}

/**
 * Invisible button component.
 *
 * It takes the regular space of a button with icon, but has 100% transparency.
 * This method is used to ensure proper layout.
 */
@Composable
private fun InvisibleButton() = IconButton(
    onClick = {},
    modifier = Modifier
        .alpha(0f)
) {
    Icon(
        imageVector = Icons.Default.QuestionMark,
        contentDescription = "",
    )
}
