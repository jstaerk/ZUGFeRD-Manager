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
import de.openindex.zugferd.manager.model.DocumentTab
import de.openindex.zugferd.manager.sections.VisualsSectionState
import de.openindex.zugferd.manager.utils.createDragAndDropTarget
import de.openindex.zugferd.manager.utils.stringResource
import de.openindex.zugferd.quba.generated.resources.AppCreateSelectMessage
import de.openindex.zugferd.quba.generated.resources.Res
import kotlinx.coroutines.launch
import java.awt.Cursor

/*
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

 */




// Versuch aus nur xml datei Visiualiserung machen
/*
@Composable
@OptIn(ExperimentalFoundationApi::class)
fun VisualsSection(state: VisualsSectionState) {
    val scope = rememberCoroutineScope()
    val appState = LocalAppState.current
    var tabState by remember { mutableStateOf(0) }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        // Tabs oben
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

        if (state.documents.isNotEmpty()) {
            val currentTab = state.documents[state.selectedIndex]
            val hasPdf = currentTab.pdf != null
            val hasXmlOrHtml = currentTab.xml != null || currentTab.html != null

            // Tabs automatisch initialisieren
            LaunchedEffect(currentTab.html, currentTab.xml) {
                tabState = when {
                    currentTab.html != null -> 0
                    currentTab.xml != null -> 1
                    else -> 0
                }
            }

            val dragAndDropCallback = remember(currentTab) {
                createDragAndDropTarget { file ->
                    scope.launch {
                        if (file.name.endsWith(".pdf", ignoreCase = true)) {
                            state.loadPdfInTab(currentTab, file, appState)
                        } else if (file.name.endsWith(".xml", ignoreCase = true)) {
                            state.loadXmlInTab(currentTab, file, appState)
                        }
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
                if (hasPdf && hasXmlOrHtml) {
                    // Fall 1: SplitPane
                    ResizableSplitPane(
                        modifier = Modifier.fillMaxSize(),
                        leftContent = {
                            Column(modifier = Modifier.fillMaxSize()) {
                                TabRow(selectedTabIndex = tabState) {
                                    if (currentTab.html != null) {
                                        Tab(
                                            selected = tabState == 0,
                                            onClick = { tabState = 0 },
                                            text = { Text("HTML Vorschau") }
                                        )
                                    }
                                    if (currentTab.xml != null) {
                                        Tab(
                                            selected = tabState == 1,
                                            onClick = { tabState = 1 },
                                            text = { Text("XML") }
                                        )
                                    }
                                }

                                Box(modifier = Modifier.fillMaxSize()) {
                                    when {
                                        tabState == 0 && currentTab.html != null -> WebViewer(
                                            html = currentTab.html!!,
                                            modifier = Modifier.fillMaxSize()
                                        )

                                        tabState == 1 && currentTab.xml != null -> XmlViewer(
                                            xml = currentTab.xml!!,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    }
                                }
                            }
                        },
                        rightContent = {
                            PdfViewer(
                                pdf = currentTab.pdf!!,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    )
                } else if (hasPdf) {
                    // Fall 2: Nur PDF
                    PdfViewer(
                        pdf = currentTab.pdf!!,
                        modifier = Modifier.fillMaxSize()
                    )
                } else if (hasXmlOrHtml) {
                    // Fall 3: Nur XML/HTML
                    Column(modifier = Modifier.fillMaxSize()) {
                        TabRow(selectedTabIndex = tabState) {
                            if (currentTab.html != null) {
                                Tab(
                                    selected = tabState == 0,
                                    onClick = { tabState = 0 },
                                    text = { Text("HTML Vorschau") }
                                )
                            }
                            if (currentTab.xml != null) {
                                Tab(
                                    selected = tabState == 1,
                                    onClick = { tabState = 1 },
                                    text = { Text("XML") }
                                )
                            }
                        }

                        Box(modifier = Modifier.fillMaxSize()) {
                            when {
                                tabState == 0 && currentTab.html != null -> WebViewer(
                                    html = currentTab.html!!,
                                    modifier = Modifier.fillMaxSize()
                                )

                                tabState == 1 && currentTab.xml != null -> XmlViewer(
                                    xml = currentTab.xml!!,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }
                    }
                } else {
                    // Kein Inhalt
                    EmptyVisualsView()
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

 */

/*
@Composable
@OptIn(ExperimentalFoundationApi::class)
fun VisualsSection(state: VisualsSectionState) {
    val scope = rememberCoroutineScope()
    val appState = LocalAppState.current
    var tabState by remember { mutableStateOf(0) }
    var showPdfOnly by remember { mutableStateOf(true) }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
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
                            .clickable { state.removeTab(index) }
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

        if (state.documents.isNotEmpty()) {
            val currentTab = state.documents[state.selectedIndex]
            val hasHtmlOrXml = currentTab.html != null || currentTab.xml != null

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
                    if (!hasHtmlOrXml) {
                        // Nur PDF → Vollbild
                        PdfViewer(
                            pdf = currentTab.pdf!!,
                            modifier = Modifier
                                .fillMaxSize()
                                .border(
                                    width = 2.dp,
                                    color = MaterialTheme.colorScheme.outline,
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .padding(8.dp)
                        )
                    } else {
                        // PDF + HTML/XML → Umschaltbar
                        Column(modifier = Modifier.fillMaxSize()) {

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                TextButton(onClick = { showPdfOnly = !showPdfOnly }) {
                                    Text(if (showPdfOnly) "HTML/XML anzeigen" else "Nur PDF anzeigen")
                                }
                            }

                            if (showPdfOnly) {
                                PdfViewer(
                                    pdf = currentTab.pdf!!,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .border(
                                            width = 2.dp,
                                            color = MaterialTheme.colorScheme.outline,
                                            shape = RoundedCornerShape(8.dp)
                                        )
                                        .padding(8.dp)
                                )
                            } else {
                                ResizableSplitPane(
                                    modifier = Modifier.fillMaxSize(),
                                    leftContent = {
                                        Column(modifier = Modifier.fillMaxSize()) {
                                            TabRow(selectedTabIndex = tabState) {
                                                if (currentTab.html != null) {
                                                    Tab(
                                                        selected = tabState == 0,
                                                        onClick = { tabState = 0 },
                                                        text = { Text("HTML Vorschau") }
                                                    )
                                                }
                                                if (currentTab.xml != null) {
                                                    Tab(
                                                        selected = tabState == 1,
                                                        onClick = { tabState = 1 },
                                                        text = { Text("XML") }
                                                    )
                                                }
                                            }

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
                                                when (tabState) {
                                                    0 -> currentTab.html?.let {
                                                        WebViewer(
                                                            html = it,
                                                            modifier = Modifier.fillMaxSize()
                                                        )
                                                    }
                                                    1 -> currentTab.xml?.let {
                                                        XmlViewer(
                                                            xml = it,
                                                            modifier = Modifier.fillMaxSize()
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    },
                                    rightContent = {
                                        PdfViewer(
                                            pdf = currentTab.pdf!!,
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .border(
                                                    width = 2.dp,
                                                    color = MaterialTheme.colorScheme.outline,
                                                    shape = RoundedCornerShape(8.dp)
                                                )
                                                .padding(8.dp)
                                        )
                                    }
                                )
                            }
                        }
                    }
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

            Box(
                modifier = Modifier
                    .width(2.dp)
                    .fillMaxHeight()
                    .background(Color.Gray)
                    .pointerHoverIcon(PointerIcon(Cursor(Cursor.E_RESIZE_CURSOR)))
                    .draggable(
                        orientation = Orientation.Horizontal,
                        state = rememberDraggableState { delta ->
                            ratio = (ratio * totalWidthPx + delta).coerceIn(100f, totalWidthPx - 100f) / totalWidthPx
                        }
                    )
            )

            Box(modifier = Modifier.fillMaxSize()) {
                rightContent()
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
    var viewMode by remember { mutableStateOf(ViewMode.SPLIT) }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        val scrollState = rememberScrollState()

        // Tabs
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
                            .clickable { state.removeTab(index) }
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

        if (state.documents.isNotEmpty()) {
            val currentTab = state.documents[state.selectedIndex]
            val hasHtmlOrXml = currentTab.html != null || currentTab.xml != null

            LaunchedEffect(hasHtmlOrXml) {
                if (!hasHtmlOrXml) {
                    viewMode = ViewMode.PDF_ONLY
                } else if (viewMode == ViewMode.PDF_ONLY) {
                    viewMode = ViewMode.SPLIT
                }
            }

            // Toggle außerhalb des View-Bereichs
            if (hasHtmlOrXml) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ViewModeToggle(
                        currentMode = viewMode,
                        onModeChanged = { viewMode = it }
                    )
                }
            }

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
                    when {
                        !hasHtmlOrXml || viewMode == ViewMode.PDF_ONLY -> {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(top = 16.dp)
                            ) {
                                PdfViewer(
                                    pdf = currentTab.pdf!!,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }

                        viewMode == ViewMode.CODE_ONLY -> {
                            Column(modifier = Modifier.fillMaxSize()) {
                                TabRow(selectedTabIndex = tabState) {
                                    if (currentTab.html != null) {
                                        Tab(
                                            selected = tabState == 0,
                                            onClick = { tabState = 0 },
                                            text = { Text("HTML Vorschau") }
                                        )
                                    }

                                    if (currentTab.xml != null) {
                                        Tab(
                                            selected = tabState == 1,
                                            onClick = { tabState = 1 },
                                            text = { Text("XML") }
                                        )
                                    }
                                }

                                Box(modifier = Modifier.fillMaxSize().padding(top = 16.dp)) {
                                    when {
                                        tabState == 0 && currentTab.html != null -> {
                                            WebViewer(
                                                html = currentTab.html!!,
                                                modifier = Modifier.fillMaxSize()
                                            )
                                        }

                                        tabState == 1 && currentTab.xml != null -> {
                                            XmlViewer(
                                                xml = currentTab.xml!!,
                                                modifier = Modifier.fillMaxSize()
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        else -> {
                            ResizableSplitPane(
                                modifier = Modifier.fillMaxSize(),
                                leftContent = {
                                    Column(modifier = Modifier.fillMaxSize()) {
                                        TabRow(selectedTabIndex = tabState) {
                                            if (currentTab.html != null) {
                                                Tab(
                                                    selected = tabState == 0,
                                                    onClick = { tabState = 0 },
                                                    text = { Text("HTML Vorschau") }
                                                )
                                            }

                                            if (currentTab.xml != null) {
                                                Tab(
                                                    selected = tabState == 1,
                                                    onClick = { tabState = 1 },
                                                    text = { Text("XML") }
                                                )
                                            }
                                        }

                                        Box(modifier = Modifier.fillMaxSize().padding(top = 16.dp)) {
                                            when {
                                                tabState == 0 && currentTab.html != null -> {
                                                    WebViewer(
                                                        html = currentTab.html!!,
                                                        modifier = Modifier.fillMaxSize()
                                                    )
                                                }

                                                tabState == 1 && currentTab.xml != null -> {
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
                                            .padding(top = 16.dp)
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
    }
}


// Ansichtsmodi
enum class ViewMode {
    PDF_ONLY,
    CODE_ONLY,
    SPLIT
}

// Toggle-Buttons für die Ansichtsauswahl
@Composable
private fun ViewModeToggle(
    currentMode: ViewMode,
    onModeChanged: (ViewMode) -> Unit
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        listOf(
            ViewMode.PDF_ONLY to "PDF",
            ViewMode.SPLIT to "Geteilt",
            ViewMode.CODE_ONLY to "Html/Xml"
        ).forEach { (mode, label) ->
            Text(
                text = label,
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { onModeChanged(mode) }
                    .background(
                        if (currentMode == mode) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                        else Color.Transparent
                    )
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                color = if (currentMode == mode) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.labelMedium
            )

            if (mode != ViewMode.CODE_ONLY) {
                Spacer(modifier = Modifier.width(4.dp))
            }
        }
    }
}

// Leere Ansicht wenn kein PDF
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

// Resizable Split Pane (Links HTML/XML, Rechts PDF)
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

            Box(
                modifier = Modifier
                    .width(2.dp)
                    .fillMaxHeight()
                    .background(Color.Gray)
                    .pointerHoverIcon(PointerIcon(Cursor(Cursor.E_RESIZE_CURSOR)))
                    .draggable(
                        orientation = Orientation.Horizontal,
                        state = rememberDraggableState { delta ->
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
