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

import androidx.compose.runtime.mutableStateOf
import de.openindex.zugferd.manager.model.DEFAULT_CURRENCY
import de.openindex.zugferd.manager.model.Invoice
import de.openindex.zugferd.manager.model.Item
import de.openindex.zugferd.manager.model.PaymentMethod
import de.openindex.zugferd.manager.model.TradeParty
import de.openindex.zugferd.manager.model.export
import de.openindex.zugferd.manager.model.isValid
import de.openindex.zugferd.manager.model.toXml
import de.openindex.zugferd.manager.utils.Preferences
import de.openindex.zugferd.manager.utils.Products
import de.openindex.zugferd.manager.utils.SectionState
import de.openindex.zugferd.manager.utils.Senders
import de.openindex.zugferd.manager.utils.convertToPdfArchive
import de.openindex.zugferd.manager.utils.directory
import de.openindex.zugferd.manager.utils.isPdfArchive
import de.openindex.zugferd.manager.utils.trimToNull
import io.github.vinceglb.filekit.core.FileKit
import io.github.vinceglb.filekit.core.PickerMode
import io.github.vinceglb.filekit.core.PickerType
import io.github.vinceglb.filekit.core.PlatformFile
import kotlinx.datetime.LocalDate

class CreateSectionState : SectionState() {
    // This file was originally selected by the user.
    private var _originalSelectedPdf = mutableStateOf<PlatformFile?>(null)

    val originalSelectedPdf: PlatformFile?
        get() = _originalSelectedPdf.value

    // This file might contain a converted PDF/A, based on the originally selected file by the user.
    private var _selectedPdf = mutableStateOf<PlatformFile?>(null)

    // This returns the converted PDF/A (if exists) or the originally selected file.
    val selectedPdf: PlatformFile?
        get() = _selectedPdf.value ?: _originalSelectedPdf.value

    private var _selectedPdfIsArchive = mutableStateOf(false)
    val selectedPdfIsArchive: Boolean
        get() = _selectedPdfIsArchive.value

    suspend fun selectPdf(preferences: Preferences, senders: Senders, products: Products) {
        val pdf = FileKit.pickFile(
            type = PickerType.File(extensions = listOf("pdf")),
            mode = PickerMode.Single,
            title = "Wähle eine PDF-Rechnung",
            initialDirectory = preferences.previousPdfLocation,
        ) ?: return

        // Remember directory of selected pdf.
        val directory = pdf.directory
        if (directory != null) {
            preferences.setPreviousPdfLocation(directory)
        }

        // Detect previously selected sender.
        val senderKey = preferences.previousSenderKey
        val sender = if (senderKey != null) {
            senders.senders.find { it._key == senderKey }
        } else {
            null
        }

        // Detect previously selected product.
        val productKey = preferences.previousProductKey
        val product = if (productKey != null) {
            products.products.find { it._key == productKey }
        } else {
            null
        }

        _originalSelectedPdf.value = pdf
        _selectedPdf.value = null

        val pdfIsArchive = isPdfArchive(pdf)
        if (!pdfIsArchive && preferences.autoConvertToPdfA) {
            val convertedPdf = convertToPdfArchive(pdf)
            _selectedPdf.value = convertedPdf
            _selectedPdfIsArchive.value = isPdfArchive(convertedPdf)
        } else {
            _selectedPdfIsArchive.value = pdfIsArchive
        }

        // Create empty invoice instance.
        _invoice.value = Invoice(
            currency = preferences.previousCurrency ?: DEFAULT_CURRENCY,
            sender = sender,
            items = listOf(
                Item(
                    product = product,
                    price = product?._defaultPricePerUnit ?: 0.0,
                )
            ),
        )
    }

    suspend fun setSelectedPdf(pdf: PlatformFile) {
        _selectedPdf.value = pdf
        _selectedPdfIsArchive.value = isPdfArchive(pdf)
    }

    suspend fun exportPdf(preferences: Preferences) {
        val sourceFile = selectedPdf ?: return
        val originalSourceFile = _originalSelectedPdf.value ?: return
        val targetFile = FileKit.saveFile(
            bytes = null,
            baseName = originalSourceFile.name.substringBeforeLast(".").plus(".e-rechnung"),
            extension = "pdf",
            initialDirectory = preferences.previousExportLocation
                ?: preferences.previousPdfLocation
                ?: originalSourceFile.path,
        ) ?: return

        // Remember directory of exported pdf.
        val directory = targetFile.directory
        if (directory != null) {
            preferences.setPreviousExportLocation(directory)
        }

        _invoice.value.export(
            sourceFile = sourceFile,
            targetFile = targetFile,
            method = invoicePaymentMethod,
        )
    }

    private var _invoice = mutableStateOf(Invoice())
    val invoiceValid: Boolean
        get() = _invoice.value.isValid()
    val invoiceXml: String
        get() = _invoice.value.toXml(method = invoicePaymentMethod)
    var invoiceCurrency: String
        get() = _invoice.value.currency
        set(value) {
            _invoice.value = _invoice.value.copy(
                currency = value.trimToNull() ?: DEFAULT_CURRENCY
            )
        }
    var invoiceNumber: String
        get() = _invoice.value.number
        set(value) {
            _invoice.value = _invoice.value.copy(number = value)
        }
    var invoicePaymentMethod: PaymentMethod
        get() = _invoice.value._paymentMethod
        set(value) {
            _invoice.value = _invoice.value.copy(_paymentMethod = value)
        }
    var invoiceIssueDate: LocalDate
        get() = _invoice.value.issueDate
        set(value) {
            _invoice.value = _invoice.value.copy(issueDate = value)
        }
    var invoiceDueDate: LocalDate
        get() = _invoice.value.dueDate
        set(value) {
            _invoice.value = _invoice.value.copy(dueDate = value)
        }
    var invoiceSender: TradeParty?
        get() = _invoice.value.sender
        set(value) {
            _invoice.value = _invoice.value.copy(sender = value)
        }
    var invoiceRecipient: TradeParty?
        get() = _invoice.value.recipient
        set(value) {
            _invoice.value = _invoice.value.copy(recipient = value)
        }
    var invoiceItems: List<Item>
        get() = _invoice.value.items.sortedBy { it._uid }
        set(value) {
            _invoice.value = _invoice.value.copy(items = value)
        }
}
