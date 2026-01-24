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
import de.openindex.zugferd.manager.utils.getString
import de.openindex.zugferd.manager.utils.getXmlFromPdf
import de.openindex.zugferd.manager.utils.trimToNull
import de.openindex.zugferd.quba.generated.resources.AppCheckSelectFile
import de.openindex.zugferd.quba.generated.resources.Res
import io.github.vinceglb.filekit.core.FileKit
import io.github.vinceglb.filekit.core.PickerMode
import io.github.vinceglb.filekit.core.PickerType
import io.github.vinceglb.filekit.core.PlatformFile
import kotlinx.coroutines.delay
import java.io.File
import java.io.IOException

//Orginal
/*
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


    suspend fun loadFileInTab(tab: DocumentTab, file: PlatformFile, appState: AppState) {
        val tabIndex = documents.indexOf(tab)
        if (tabIndex == -1) return

        // Tab-Loading aktivieren
        updateTabLoadingState(tabIndex, true)

        try {
            // Arbeitskopie erstellen
            var updatedTab = tab.copy(isLoading = true)

            updatedTab.name = file.name
            updatedTab.pdf = null
            updatedTab.html = null
            updatedTab.xml = null
            updatedTab.tags = listOf()

            val fileText = file.file.readText()
            val filePath = file.file.toPath()

            val isXml = file.name.lowercase().endsWith(".xml") || fileText.trimStart().startsWith("<")

            if (isXml) {
                updatedTab.xml = fileText.trimToNull()
                updatedTab.html = getHtmlVisualizationFromXML(filePath)?.trimToNull()
            } else {
                val isPdf = fileText.trimStart().startsWith("%PDF")
                if (isPdf) {
                    updatedTab.pdf = file
                    updatedTab.html = getHtmlVisualizationFromPdf(file)
                    updatedTab.xml = getXmlFromPdf(file)?.let { getPrettyPrintedXml(it) }?.trimToNull()

                    file.directory?.let {
                        appState.preferences.setPreviousPdfLocation(it)
                    }
                }
            }

            // Aktualisierte Tab-Daten in die Liste schreiben
            documents[tabIndex] = updatedTab
        } finally {
            // Tab-Loading beenden
            updateTabLoadingState(tabIndex, false)
        }
    }

    fun updateTabLoadingState(index: Int, loading: Boolean) {
        if (index in documents.indices) {
            val tab = documents[index]
            documents[index] = tab.copy(isLoading = loading)
        }
    }



    private var _selectedPdfXml = mutableStateOf<String?>(null)
    val selectedPdfXml: String?
        get() = _selectedPdfXml.value


    private var _selectedPdfHtml = mutableStateOf<String?>(null)
    val selectedPdfHtml: String?
        get() = _selectedPdfHtml.value


}

 */

class VisualsSectionState : SectionState() {
    val documents = mutableStateListOf<DocumentTab>()
    var selectedIndex by mutableStateOf(0)

    // Search State
    var isSearchOpen by mutableStateOf(false)
    var searchQuery by mutableStateOf("")
    var searchSequence by mutableStateOf(0)

    fun addNewTab() {
        documents.add(DocumentTab(name = "Neuer Tab", pdf = null, isLoading = false))
        selectedIndex = documents.lastIndex
    }

    /*
    fun removeTab(index: Int) {
        if (documents.size > 1) {
            documents.removeAt(index)
            selectedIndex = (documents.size - 1).coerceAtLeast(0)
        }
    }

     */
    fun removeTab(index: Int) {
        if (index !in documents.indices) return
        documents.removeAt(index)
        selectedIndex = when {
            documents.isEmpty() -> 0
            index <= selectedIndex -> (selectedIndex - 1).coerceAtLeast(0)
            else -> selectedIndex
        }
    }



    suspend fun selectFile(appState: AppState) {
        val file = FileKit.pickFile(
            type = PickerType.File(extensions = listOf("pdf", "xml")),
            mode = PickerMode.Single,
            title = "Datei auswählen",
            initialDirectory = appState.preferences.previousPdfLocation
        ) ?: return

        if (documents.isEmpty()) addNewTab()
        loadFileInCurrentTab(file, appState)
    }



suspend fun loadFileInCurrentTab(file: PlatformFile, appState: AppState) {
    if (documents.isEmpty()) {
        addTabWithFile(file, appState)
    } else {
        val currentTab = documents[selectedIndex]
        loadFileInTab(currentTab, file, appState)
    }
}


    suspend fun addTabWithFile(file: PlatformFile, appState: AppState) {
        val newTab = DocumentTab(
            name = file.name,
            pdf = null,
            isLoading = true
        )

        documents.add(newTab)
        selectedIndex = documents.lastIndex

        // Verzögerung nur beim allerersten Tab, damit Datei von OS freigegeben wird
        if (documents.size == 1) {
            delay(200L) // 200ms sollten reichen
        }

        loadFileInTab(newTab, file, appState)
    }



    suspend fun readFileSafely(file: File): String {
        repeat(5) { attempt ->
            try {
                return file.inputStream().bufferedReader().use { it.readText() }
            } catch (e: IOException) {
                if (attempt == 4) throw e
                delay(100L) // 100ms warten und nochmal versuchen
            }
        }
        return "" // wird nie erreicht
    }


    suspend fun loadFileInTab(tab: DocumentTab, file: PlatformFile, appState: AppState) {
        tab.isLoading = true

        try {
            // Wenn es der erste Tab ist, kurz warten
            if (tab.name.isEmpty()) {
                delay(150L)
            }

            // Datei mehrfach versuchen zu lesen
            val fileText = readFileWithRetry(file.file)

            val filePath = file.file.toPath()
            val isXml = file.name.lowercase().endsWith(".xml") || fileText.trimStart().startsWith("<")

            tab.name = file.name
            tab.pdf = if (!isXml) file else null
            tab.html = if (isXml) getHtmlVisualizationFromXML(filePath)?.trimToNull()
            else getHtmlVisualizationFromPdf(file)?.trimToNull()
            tab.xml = if (isXml) fileText.trimToNull()
            else getXmlFromPdf(file)?.let { getPrettyPrintedXml(it) }?.trimToNull()

            file.directory?.let { appState.preferences.setPreviousPdfLocation(it) }

        } catch (e: IOException) {
            println("Fehler beim Lesen der Datei, erneut versuchen: ${e.message}")
            throw e
        } finally {
            tab.isLoading = false
        }
    }

    suspend fun readFileWithRetry(file: File, maxAttempts: Int = 5, initialDelay: Long = 100L): String {
        var currentDelay = initialDelay
        repeat(maxAttempts) { attempt ->
            try {
                return file.inputStream().bufferedReader().use { it.readText() }
            } catch (e: IOException) {
                if (attempt == maxAttempts - 1) throw e
                delay(currentDelay)
                currentDelay *= 2 // Exponentielles Backoff
            }
        }
        throw IOException("Datei konnte nach $maxAttempts Versuchen nicht gelesen werden")
    }




    private fun updateTabLoadingState(index: Int, loading: Boolean) {
        if (index in documents.indices) {
            val tab = documents[index]
            documents[index] = tab.copy(isLoading = loading)
        }
    }
}



