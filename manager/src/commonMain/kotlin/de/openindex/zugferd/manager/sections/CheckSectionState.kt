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
import de.openindex.zugferd.manager.AppState
import de.openindex.zugferd.manager.model.ValidationSeverity
import de.openindex.zugferd.manager.model.ValidationType
import de.openindex.zugferd.manager.utils.SectionState
import de.openindex.zugferd.manager.utils.Validation
import de.openindex.zugferd.manager.utils.getHtmlVisualizationFromPdf
import de.openindex.zugferd.manager.utils.getPrettyPrintedXml
import de.openindex.zugferd.manager.utils.getString
import de.openindex.zugferd.manager.utils.getXmlFromPdf
import de.openindex.zugferd.manager.utils.title
import de.openindex.zugferd.manager.utils.trimToNull
import de.openindex.zugferd.manager.utils.validatePdf
import de.openindex.zugferd.manager.utils.writeJson
import de.openindex.zugferd.quba.generated.resources.AppCheckSelectFile
import de.openindex.zugferd.quba.generated.resources.Res
import io.github.vinceglb.filekit.core.FileKit
import io.github.vinceglb.filekit.core.PickerMode
import io.github.vinceglb.filekit.core.PickerType
import io.github.vinceglb.filekit.core.PlatformFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

class CheckSectionState : SectionState() {
    private var _selectedPdf = mutableStateOf<PlatformFile?>(null)
    val selectedPdf: PlatformFile?
        get() = _selectedPdf.value

    private var _selectedPdfXml = mutableStateOf<String?>(null)
    val selectedPdfXml: String?
        get() = _selectedPdfXml.value

    private var _selectedPdfHtml = mutableStateOf<String?>(null)
    val selectedPdfHtml: String?
        get() = _selectedPdfHtml.value

    private var _selectedPdfValidation = mutableStateOf<Validation?>(null)
    val selectedPdfValidation: Validation?
        get() = _selectedPdfValidation.value

    suspend fun selectPdf(appState: AppState) {
        val pdf = FileKit.pickFile(
            type = PickerType.File(extensions = listOf("pdf")),
            mode = PickerMode.Single,
            title = getString(Res.string.AppCheckSelectFile).title(),
            initialDirectory = appState.preferences.previousPdfLocation,
        ) ?: return

        selectPdf(
            pdf = pdf,
            appState = appState,
        )
    }

    @Suppress("UNUSED_PARAMETER")
    suspend fun selectPdf(pdf: PlatformFile, appState: AppState) {
        _selectedPdfHtml.value = null
        _selectedPdfValidation.value = null
        _selectedPdf.value = pdf
        _selectedPdfXml.value = getXmlFromPdf(pdf)?.let { getPrettyPrintedXml(it) }?.trimToNull()

        coroutineScope {
            launch(Dispatchers.IO) {
                //delay(2000)
                _selectedPdfHtml.value = getHtmlVisualizationFromPdf(pdf)
            }
        }

        coroutineScope {
            launch(Dispatchers.IO) {
                //delay(2000)
                _selectedPdfValidation.value = validatePdf(pdf)
                _filterType.value = ValidationType.entries.toList()
                _filterSeverity.value = ValidationSeverity.entries.toList()
            }
        }
    }

    suspend fun exportValidation(validation: Validation) {
        val sourceFile = selectedPdf ?: return
        val targetFile = FileKit.saveFile(
            bytes = null,
            baseName = sourceFile.name.substringBeforeLast(".")
                .plus(".validation"),
            extension = "json",
            initialDirectory = sourceFile.path,
        ) ?: return

        targetFile.writeJson(validation)
    }

    private var _filterType = mutableStateOf(ValidationType.entries.toList())
    val filterType: List<ValidationType>
        get() = _filterType.value

    fun setFilterType(type: List<ValidationType>) {
        _filterType.value = type.toList()
    }

    private var _filterSeverity = mutableStateOf(ValidationSeverity.entries.toList())
    val filterSeverity: List<ValidationSeverity>
        get() = _filterSeverity.value

    fun setFilterSeverity(severity: List<ValidationSeverity>) {
        _filterSeverity.value = severity.toList()
    }
}
