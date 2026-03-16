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
import de.openindex.zugferd.manager.utils.getHtmlVisualizationFromXML
import de.openindex.zugferd.manager.utils.getHtmlWithAttachments
import de.openindex.zugferd.manager.utils.getPrettyPrintedXml
import de.openindex.zugferd.manager.utils.getXmlFromPdf
import de.openindex.zugferd.manager.utils.trimToNull
import io.github.vinceglb.filekit.core.FileKit
import io.github.vinceglb.filekit.core.PickerMode
import io.github.vinceglb.filekit.core.PickerType
import io.github.vinceglb.filekit.core.PlatformFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException

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



    suspend fun loadFileInTab(tab: DocumentTab, file: PlatformFile, appState: AppState) {
        tab.isLoading = true
        tab.isHtmlLoading = false

        // Phase 1: XML sofort lesen und anzeigen (schnell)
        val filePath: java.nio.file.Path
        val isXml: Boolean
        try {
            if (tab.name.isEmpty()) delay(150L)

            // Datei auf IO-Dispatcher lesen (blockierende Operation)
            val fileText = withContext(Dispatchers.IO) { readFileWithRetry(file.file) }

            filePath = file.file.toPath()
            isXml = file.name.lowercase().endsWith(".xml") || fileText.trimStart().startsWith("<")

            tab.name = file.name
            tab.pdf = if (!isXml) file else null

            // XML-Inhalt: bei XML-Datei bereits im Speicher, bei PDF auf IO-Dispatcher
            tab.xml = if (isXml) {
                fileText.trimToNull()
            } else {
                withContext(Dispatchers.IO) {
                    getXmlFromPdf(file)?.let { getPrettyPrintedXml(it) }?.trimToNull()
                }
            }

            file.directory?.let { appState.preferences.setPreviousPdfLocation(it) }

        } catch (e: IOException) {
            println("Fehler beim Lesen der Datei: ${e.message}")
            throw e
        } finally {
            tab.isLoading = false  // XML ist bereit → Spinner ausblenden
        }

        // Phase 2: HTML im Hintergrund generieren (kann länger dauern)
        // XML-View ist bereits sichtbar, HTML-Tab erscheint sobald fertig
        try {
            tab.isHtmlLoading = true
            tab.html = withContext(Dispatchers.IO) {
                if (isXml) getHtmlVisualizationFromXML(filePath)?.trimToNull()
                else getHtmlWithAttachments(file)?.trimToNull()
            }
        } finally {
            tab.isHtmlLoading = false
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




}



