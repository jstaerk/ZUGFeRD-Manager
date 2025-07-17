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
import de.openindex.zugferd.manager.utils.getHtmlVisualizationFromPdf
import de.openindex.zugferd.manager.utils.getHtmlVisualizationFromXML
import de.openindex.zugferd.manager.utils.getPrettyPrintedXml
import de.openindex.zugferd.manager.utils.getXmlFromPdf
import de.openindex.zugferd.manager.utils.trimToNull
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


/*
      fun loadPdfInTab(tab: DocumentTab, pdfFile: PlatformFile, appState: AppState) {
         tab.name = pdfFile.name
         tab.pdf = pdfFile
         tab.tags = listOf()

         pdfFile.directory?.let {
             appState.preferences.setPreviousPdfLocation(it)
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

    /*
    suspend fun loadPdfInTab(tab: DocumentTab, pdfFile: PlatformFile, appState: AppState) {
        tab.name = pdfFile.name
        tab.pdf = pdfFile
        tab.tags = listOf()

        // HTML und XML für die Vorschau extrahieren
        tab.html = getHtmlVisualizationFromPdf(pdfFile)
        tab.xml = getXmlFromPdf(pdfFile)?.let { getPrettyPrintedXml(it) }?.trimToNull()

        pdfFile.directory?.let {
            appState.preferences.setPreviousPdfLocation(it)
        }
    }

     */


    suspend fun loadFileInTab(tab: DocumentTab, file: PlatformFile, appState: AppState) {
        tab.name = file.name
        tab.pdf = null
        tab.html = null
        tab.xml = null
        tab.tags = listOf()

        val fileText = file.file.readText()
        val filePath = file.file.toPath()

        val isXml = file.name.lowercase().endsWith(".xml") || fileText.trimStart().startsWith("<")

        if (isXml) {
            tab.xml = fileText.trimToNull()
            tab.html = getHtmlVisualizationFromXML(filePath)?.trimToNull()
        }

        else {
            val isPdf = fileText.trimStart().startsWith("%PDF")
            if (isPdf) {
                tab.pdf = file
                tab.html = getHtmlVisualizationFromPdf(file)
                tab.xml = getXmlFromPdf(file)?.let { getPrettyPrintedXml(it) }?.trimToNull()
                file.directory?.let {
                    appState.preferences.setPreviousPdfLocation(it)
                }
            }
        }
    }





    private var _selectedPdfXml = mutableStateOf<String?>(null)
    val selectedPdfXml: String?
        get() = _selectedPdfXml.value


    private var _selectedPdfHtml = mutableStateOf<String?>(null)
    val selectedPdfHtml: String?
        get() = _selectedPdfHtml.value
}
