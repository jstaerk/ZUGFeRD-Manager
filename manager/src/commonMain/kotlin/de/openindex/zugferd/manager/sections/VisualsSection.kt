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

import androidx.compose.foundation.*
import androidx.compose.foundation.draganddrop.dragAndDropTarget
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import de.openindex.zugferd.manager.LocalAppState
import de.openindex.zugferd.manager.gui.PdfViewer
import de.openindex.zugferd.manager.gui.WebViewer
import de.openindex.zugferd.manager.gui.XmlViewer
import de.openindex.zugferd.manager.sections.VisualsSectionState
import de.openindex.zugferd.manager.utils.createDragAndDropTarget
import de.openindex.zugferd.manager.utils.stringResource
import de.openindex.zugferd.zugferd_manager.generated.resources.AppCreateSelectMessage
import de.openindex.zugferd.zugferd_manager.generated.resources.Res
import kotlinx.coroutines.launch
import java.awt.Cursor


/*
@Composable
fun VisualsSection(state: VisualsSectionState) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {

        // Top Row: Datei öffnen, Tabs, Plus-Button
        Row(verticalAlignment = Alignment.CenterVertically) {
            Button(onClick = {
                val file = chooseFile()
                file?.let {
                    val content = it.readText()
                    state.documents.add(DokumentTab(it.name, content))
                    state.selectedIndex = state.documents.lastIndex
                }
            }) {
                Text("Datei öffnen")
            }

            Spacer(modifier = Modifier.width(16.dp))

            val scrollState = rememberScrollState()
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .horizontalScroll(scrollState)
                    .weight(1f)
            ) {
                state.documents.forEachIndexed { index, doc ->
                    val isSelected = index == state.selectedIndex

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .padding(horizontal = 4.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(
                                if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                                else MaterialTheme.colorScheme.surface
                            )
                            .border(
                                width = if (isSelected) 2.dp else 1.dp,
                                color = if (isSelected) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.outline,
                                shape = RoundedCornerShape(6.dp)
                            )
                            .clickable { state.selectedIndex = index }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = doc.name,
                            color = if (isSelected) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurface,
                            style = MaterialTheme.typography.bodyMedium
                        )

                        Spacer(modifier = Modifier.width(4.dp))

                        Text(
                            text = "✕",
                            modifier = Modifier
                                .clickable {
                                    state.documents.removeAt(index)
                                    if (state.selectedIndex >= state.documents.size) {
                                        state.selectedIndex = (state.documents.size - 1).coerceAtLeast(0)
                                    }
                                }
                                .padding(start = 4.dp),
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // + Button
            Button(onClick = {
                val file = chooseFile()
                file?.let {
                    val content = it.readText()
                    state.documents.add(DokumentTab(it.name, content))
                    state.selectedIndex = state.documents.lastIndex
                }
            }) {
                Text("+")
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Inhalt anzeigen
        if (state.documents.isNotEmpty()) {
            val currentDoc = state.documents[state.selectedIndex]
            Text(
                text = currentDoc.content,
                style = MaterialTheme.typography.bodyLarge
            )
        } else {
            Text("Kein Dokument geöffnet.")
        }
    }
}

// Desktop File Picker
fun chooseFile(): File? {
    val chooser = JFileChooser()
    val result = chooser.showOpenDialog(null)
    return if (result == JFileChooser.APPROVE_OPTION) {
        chooser.selectedFile
    } else null
}
*/


/*
Diese Code Variente 1 = Nur tabs und + button
@Composable
fun VisualsSection(state: VisualsSectionState) {


    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {

        // Top Row: Datei öffnen, Tabs, Plus-Button
        Row(verticalAlignment = Alignment.CenterVertically) {
            Button(onClick = {
                val file = chooseFile()
                file?.let {
                    val content = it.readText()
                    state.documents.add(DocumentTab(it.name, content))
                    state.selectedIndex = state.documents.lastIndex
                }
            }) {
                Text("Datei öffnen")
            }

            Spacer(modifier = Modifier.width(16.dp))

            val scrollState = rememberScrollState()
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .horizontalScroll(scrollState)
                    .weight(1f)
            ) {
                state.documents.forEachIndexed { index, doc ->
                    val isSelected = index == state.selectedIndex

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .padding(horizontal = 4.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(
                                if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                                else MaterialTheme.colorScheme.surface
                            )
                            .border(
                                width = if (isSelected) 2.dp else 1.dp,
                                color = if (isSelected) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.outline,
                                shape = RoundedCornerShape(6.dp)
                            )
                            .clickable { state.selectedIndex = index }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = doc.name,
                            color = if (isSelected) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurface,
                            style = MaterialTheme.typography.bodyMedium
                        )

                        Spacer(modifier = Modifier.width(4.dp))

                        Text(
                            text = "✕",
                            modifier = Modifier
                                .clickable {
                                    state.documents.removeAt(index)
                                    if (state.selectedIndex >= state.documents.size) {
                                        state.selectedIndex = (state.documents.size - 1).coerceAtLeast(0)
                                    }
                                }
                                .padding(start = 4.dp),
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // + Button
            Button(onClick = {
                val file = chooseFile()
                file?.let {
                    val content = it.readText()
                    state.documents.add(DocumentTab(it.name, content))
                    state.selectedIndex = state.documents.lastIndex
                }
            }) {
                Text("+")
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Inhalt anzeigen
        if (state.documents.isNotEmpty()) {
            val currentDoc = state.documents[state.selectedIndex]
            Text(
                text = currentDoc.content,
                style = MaterialTheme.typography.bodyLarge
            )
        } else {
            Text("Kein Dokument geöffnet.")
        }
    }
}

// Desktop File Picker
fun chooseFile(): File? {
    val chooser = JFileChooser()
    val result = chooser.showOpenDialog(null)
    return if (result == JFileChooser.APPROVE_OPTION) {
        chooser.selectedFile
    } else null
}


 */

// diese Code für Dragdrop und PDF viewer
/*
@Composable
@OptIn(ExperimentalFoundationApi::class)
fun VisualsSection(state: VisualsSectionState) {
    val scope = rememberCoroutineScope()
    val appState = LocalAppState.current

    val dragAndDropCallback = remember {
        createDragAndDropTarget(
            onDrop = { pdfFile ->
                scope.launch {
                    state.selectVisualPdf(
                        pdf = pdfFile,
                        appState = appState,
                    )
                }
            }
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .dragAndDropTarget(
                target = dragAndDropCallback,
                shouldStartDragAndDrop = { true },
            )
    ) {
        if (state.selectedPdf == null) {
            EmptyVisualsView()
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Tags and Add button
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    state.visualTags.forEach { tag ->
                        TagView(text = tag)
                    }

                    Button(
                        onClick = {
                            state.visualTags = state.visualTags + "Tag ${state.visualTags.size + 1}"
                        }
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add Tag")
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add Tag")
                    }
                }

                // PDF Viewer
                PdfViewer(
                    pdf = state.selectedPdf!!,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}


@Composable
private fun EmptyVisualsView() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = stringResource(Res.string.AppCreateSelectMessage),
            textAlign = TextAlign.Center
        )
    }
}
@Composable
fun TagView(text: String) {
    Box(
        modifier = Modifier
            .background(color = Color.LightGray, shape = RoundedCornerShape(12.dp))
            .padding(horizontal = 12.dp, vertical = 6.dp),
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = Color.Black
        )
    }
}

 */


/*
@Composable
@OptIn(ExperimentalFoundationApi::class)
fun VisualsSection(state: VisualsSectionState) {
    val scope = rememberCoroutineScope()
    val appState = LocalAppState.current

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {

        // Tabs mit + Button
        val scrollState = rememberScrollState()
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .horizontalScroll(scrollState)
                .fillMaxWidth()
        ) {
            state.documents.forEachIndexed { index, doc ->
                val isSelected = index == state.selectedIndex

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .padding(horizontal = 4.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(
                            if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                            else MaterialTheme.colorScheme.surface
                        )
                        .border(
                            width = if (isSelected) 2.dp else 1.dp,
                            color = if (isSelected) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.outline,
                            shape = RoundedCornerShape(6.dp)
                        )
                        .clickable { state.selectedIndex = index }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = doc.name.ifBlank { "Neuer Tab" },
                        color = if (isSelected) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Spacer(modifier = Modifier.width(4.dp))

                    Text(
                        text = "✕",
                        modifier = Modifier
                            .clickable {
                                state.removeTab(index)
                            }
                            .padding(start = 4.dp),
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }

            // + Button für neuen Tab
            IconButton(onClick = { state.addNewTab() }) {
                Icon(Icons.Default.Add, contentDescription = "Neuer Tab")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Aktueller Tab mit Drag & Drop
        if (state.documents.isNotEmpty()) {
            val currentTab = state.documents[state.selectedIndex]

            // dragAndDropCallback NEU: kein plain remember, sondern abhängig vom currentTab
            val dragAndDropCallback = remember(currentTab) {
                createDragAndDropTarget { pdfFile ->
                    scope.launch {
                        state.loadPdfInTab(currentTab, pdfFile, appState)
                    }
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .dragAndDropTarget(
                        target = dragAndDropCallback,
                        shouldStartDragAndDrop = { true }
                    )
            ) {
                if (currentTab.pdf == null) {
                    EmptyVisualsView()
                } else {
                    // ✅ Nur PDF anzeigen
                    PdfViewer(
                        pdf = currentTab.pdf!!,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}

 */


@Composable
@OptIn(ExperimentalFoundationApi::class)
fun VisualsSection(state: VisualsSectionState) {
    val scope = rememberCoroutineScope()
    val appState = LocalAppState.current
    var tabState by remember { mutableStateOf(0) }
    val isHtmlTabSelected by derivedStateOf { tabState == 1 && state.selectedPdfHtml != null }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        // Tabs mit + Button
        val scrollState = rememberScrollState()
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .horizontalScroll(scrollState)
                .fillMaxWidth()
        ) {
            state.documents.forEachIndexed { index, doc ->
                val isSelected = index == state.selectedIndex

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .padding(horizontal = 4.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(
                            if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                            else MaterialTheme.colorScheme.surface
                        )
                        .border(
                            width = if (isSelected) 2.dp else 1.dp,
                            color = if (isSelected) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.outline,
                            shape = RoundedCornerShape(6.dp)
                        )
                        .clickable { state.selectedIndex = index }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = doc.name.ifBlank { "Neuer Tab" },
                        color = if (isSelected) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Spacer(modifier = Modifier.width(4.dp))

                    Text(
                        text = "✕",
                        modifier = Modifier
                            .clickable {
                                state.removeTab(index)
                            }
                            .padding(start = 4.dp),
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }

            IconButton(onClick = { state.addNewTab() }) {
                Icon(Icons.Default.Add, contentDescription = "Neuer Tab")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        /*
        if (state.documents.isNotEmpty()) {
            val currentTab = state.documents[state.selectedIndex]

            val dragAndDropCallback = remember(currentTab) {
                createDragAndDropTarget { pdfFile ->
                    scope.launch {
                        state.loadPdfInTab(currentTab, pdfFile, appState)
                    }
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .dragAndDropTarget(
                        target = dragAndDropCallback,
                        shouldStartDragAndDrop = { true }
                    )
            ) {
                if (currentTab.pdf == null) {
                    EmptyVisualsView()
                } else {
                    ResizableSplitPane(
                        modifier = Modifier.fillMaxSize(),
                        leftContent = {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .border(
                                        width = 2.dp,
                                        color = MaterialTheme.colorScheme.outline,
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .padding(8.dp)
                            ) {
//                                Text(
//                                    "XML Vorschau oder andere Infos...",
//                                    modifier = Modifier.padding(16.dp)
//                                )
//                                XmlViewer(
//                                    xml = state.selectedPdfXml ?: "",
//                                    modifier = Modifier.fillMaxSize(),
//                                )

                                if (state.selectedPdfHtml != null) {
                                    Tab(
                                        selected = isHtmlTabSelected,
                                        onClick = { tabState = 1 },
                                        text = {
                                            de.openindex.zugferd.manager.gui.Label(
                                                text = Res.string.AppCheckDetailsHtml,
                                            )
                                        },
                                    )
                                }
                            }
                        },
                        rightContent = {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .border(
                                        width = 2.dp,
                                        color = MaterialTheme.colorScheme.outline,
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .padding(8.dp)
                            ) {
                                PdfViewer(
                                    pdf = currentTab.pdf!!,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }
                    )
                }

            }
        }

         */

        if (state.documents.isNotEmpty()) {
            val currentTab = state.documents[state.selectedIndex]
            val isHtmlTabSelected by derivedStateOf { tabState == 0 }
            val isXmlTabSelected by derivedStateOf { tabState == 1 && currentTab.xml != null }

            val dragAndDropCallback = remember(currentTab) {
                createDragAndDropTarget { pdfFile ->
                    scope.launch {
                        state.loadPdfInTab(currentTab, pdfFile, appState)
                    }
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .dragAndDropTarget(
                        target = dragAndDropCallback,
                        shouldStartDragAndDrop = { true }
                    )
            ) {
                if (currentTab.pdf == null) {
                    EmptyVisualsView()
                } else {
                    ResizableSplitPane(
                        modifier = Modifier.fillMaxSize(),
                        leftContent = {
                            Column(modifier = Modifier.fillMaxSize()) {
                                // Tab-Auswahl für HTML/XML
                                TabRow(selectedTabIndex = tabState) {
                                    if (currentTab.html != null) {
                                        Tab(
                                            selected = isHtmlTabSelected,
                                            onClick = { tabState = 0 },
                                            text = { Text("HTML Vorschau") }
                                        )
                                    }

                                    if (currentTab.xml != null) {
                                        Tab(
                                            selected = isXmlTabSelected,
                                            onClick = { tabState = 1 },
                                            text = { Text("XML") }
                                        )
                                    }
                                }

                                // Inhalt je nach ausgewähltem Tab
                                Box(modifier = Modifier.fillMaxSize()) {
                                    when {
                                        isHtmlTabSelected && currentTab.html != null -> {
                                            WebViewer(
                                                html = currentTab.html!!,
                                                modifier = Modifier.fillMaxSize()
                                            )
                                        }

                                        isXmlTabSelected && currentTab.xml != null -> {
                                            XmlViewer(
                                                xml = currentTab.xml!!,
                                                modifier = Modifier.fillMaxSize()
                                            )
                                        }
                                    }
                                }
                            }
                        },
                        rightContent = {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .border(
                                        width = 2.dp,
                                        color = MaterialTheme.colorScheme.outline,
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .padding(8.dp)
                            ) {
                                PdfViewer(
                                    pdf = currentTab.pdf!!,
                                    modifier = Modifier.fillMaxSize()
                                )

                            }

                        }
                    )

                }
            }
        }

    }
}

@Composable
private fun EmptyVisualsView() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = stringResource(Res.string.AppCreateSelectMessage),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun ResizableSplitPane(


    modifier: Modifier = Modifier,
    initialRatio: Float = 0.5f,
    minRatio: Float = 0.1f,
    maxRatio: Float = 0.9f,
    leftContent: @Composable () -> Unit,
    rightContent: @Composable () -> Unit
) {
    var dragOffset by remember { mutableStateOf(0f) }
    val density = LocalDensity.current
    var ratio by remember { mutableStateOf(initialRatio) }


    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val totalWidthPx = with(density) { maxWidth.toPx() }
        val leftWidthPx = (totalWidthPx * ratio + dragOffset).coerceIn(
            totalWidthPx * minRatio,
            totalWidthPx * maxRatio
        )

        Row(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .width(with(density) { leftWidthPx.toDp() })
                    .fillMaxHeight()
            ) {
                leftContent()
            }

            //val totalWidthPx = with(density) { constraints.maxWidth.toFloat() }

            Box(
                modifier = Modifier
                    .width(2.dp)
                    .fillMaxHeight()
                    .background(Color.Gray)
                    .pointerHoverIcon(PointerIcon(Cursor(Cursor.E_RESIZE_CURSOR)))
                    .draggable(
                        orientation = Orientation.Horizontal,
                        state = rememberDraggableState { delta ->
                            // Update ratio direkt während Drag
                            ratio = (ratio * totalWidthPx + delta).coerceIn(100f, totalWidthPx - 100f) / totalWidthPx
                        }
                    )
            )


            Box(
                modifier = Modifier.fillMaxSize()
            ) {
                rightContent()
            }
        }
    }
}
