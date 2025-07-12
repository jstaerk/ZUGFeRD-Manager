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


import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import de.openindex.zugferd.manager.AppState

import de.openindex.zugferd.manager.model.DocumentTab
import de.openindex.zugferd.manager.utils.SectionState
import de.openindex.zugferd.manager.utils.directory
import io.github.vinceglb.filekit.core.PlatformFile

/*
class VisualsSectionState: SectionState() {
    val documents = mutableStateListOf<DocumentTab>()
    var selectedIndex by mutableStateOf(0)
}
*/

/*
// Diese Code nur für Dragdrop und pdfviewer
class VisualsSectionState : SectionState() {
    private var _selectedPdf = mutableStateOf<PlatformFile?>(null)
    val selectedPdf: PlatformFile?
        get() = _selectedPdf.value

    var visualTags by mutableStateOf(listOf<String>())

    suspend fun selectVisualPdf(appState: AppState) {
        val pdf = FileKit.pickFile(
            type = PickerType.File(extensions = listOf("pdf")),
            mode = PickerMode.Single,
            title = getString(Res.string.AppCreateSelectFile).title(),
            initialDirectory = appState.preferences.previousPdfLocation,
        ) ?: return

        selectVisualPdf(pdf, appState)
    }

    suspend fun selectVisualPdf(pdf: PlatformFile, appState: AppState) {
        _selectedPdf.value = pdf

        pdf.directory?.let {
            appState.preferences.setPreviousPdfLocation(it)
        }

        visualTags = listOf() // Reset tags when loading new file
    }
}

 */



class VisualsSectionState : SectionState() {
    val documents = mutableStateListOf<DocumentTab>()
    var selectedIndex by mutableStateOf(0)



    fun addNewTab() {
        documents.add(DocumentTab(name = "", pdf = null))
        selectedIndex = documents.lastIndex
    }

    fun removeTab(index: Int) {
        documents.removeAt(index)
        if (selectedIndex >= documents.size) {
            selectedIndex = (documents.size - 1).coerceAtLeast(0)
        }
    }

     fun loadPdfInTab(tab: DocumentTab, pdfFile: PlatformFile, appState: AppState) {
        tab.name = pdfFile.name
        tab.pdf = pdfFile
        tab.tags = listOf()

        pdfFile.directory?.let {
            appState.preferences.setPreviousPdfLocation(it)
        }
    }


    private var _selectedPdfXml = mutableStateOf<String?>(null)
    val selectedPdfXml: String?
        get() = _selectedPdfXml.value

}
