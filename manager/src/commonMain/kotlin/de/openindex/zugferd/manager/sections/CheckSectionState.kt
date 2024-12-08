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
import de.openindex.zugferd.manager.utils.Preferences
import de.openindex.zugferd.manager.utils.SectionState
import de.openindex.zugferd.manager.utils.Validation
import de.openindex.zugferd.manager.utils.getHtmlVisualizationFromPdf
import de.openindex.zugferd.manager.utils.getPrettyPrintedXml
import de.openindex.zugferd.manager.utils.getXmlFromPdf
import de.openindex.zugferd.manager.utils.trimToNull
import de.openindex.zugferd.manager.utils.validatePdf
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

    suspend fun selectPdf(preferences: Preferences) {
        val pdf = FileKit.pickFile(
            type = PickerType.File(extensions = listOf("pdf")),
            mode = PickerMode.Single,
            title = "Wähle eine E-Rechnung",
            initialDirectory = preferences.previousPdfLocation,
        ) ?: return

        _selectedPdf.value = pdf
        _selectedPdfXml.value = getXmlFromPdf(pdf)?.let { getPrettyPrintedXml(it) }?.trimToNull()

        _selectedPdfHtml.value = null
        coroutineScope {
            launch(Dispatchers.IO) {
                //delay(2000)
                _selectedPdfHtml.value = getHtmlVisualizationFromPdf(pdf)
            }
        }

        _selectedPdfValidation.value = null
        coroutineScope {
            launch(Dispatchers.IO) {
                //delay(2000)
                _selectedPdfValidation.value = validatePdf(pdf)
            }
        }
    }
}
