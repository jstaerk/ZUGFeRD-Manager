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
import de.openindex.zugferd.manager.utils.getHtmlVisualizationFromXML
import de.openindex.zugferd.manager.utils.stringResource
import de.openindex.zugferd.quba.generated.resources.AppCreateSelectMessage
import de.openindex.zugferd.quba.generated.resources.Res
import kotlinx.coroutines.launch
import java.awt.Cursor



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
            val hasPdfAndHtmlOrXml = currentTab.pdf != null && hasHtmlOrXml


            LaunchedEffect(hasHtmlOrXml) {
                if (!hasHtmlOrXml) {
                    viewMode = ViewMode.PDF_ONLY
                } else if (viewMode == ViewMode.PDF_ONLY && currentTab.pdf != null) {
                    viewMode = ViewMode.SPLIT
                } else if (currentTab.pdf == null) {
                    viewMode = ViewMode.CODE_ONLY
                }
            }

            if (hasPdfAndHtmlOrXml) {
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
                createDragAndDropTarget { file ->
                    scope.launch {
                        // Lade PDF oder XML basierend auf Inhalt
                        if (file.name.endsWith(".xml", ignoreCase = true)) {
                            currentTab.name = file.name
                            currentTab.pdf = null
                            currentTab.tags = listOf()
                            currentTab.xml = file.file.readText().trim()
                            currentTab.html = getHtmlVisualizationFromXML(file.file.toPath())
                        } else {
                            state.loadFileInTab(currentTab, file, appState)
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
                when {
                    currentTab.pdf == null && hasHtmlOrXml -> {
                        // Nur XML/HTML
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

                    currentTab.pdf == null -> {
                        EmptyVisualsView()
                    }

                    !hasHtmlOrXml || viewMode == ViewMode.PDF_ONLY -> {
                        PdfViewer(
                            pdf = currentTab.pdf!!,
                            modifier = Modifier.fillMaxSize().padding(top = 16.dp)
                        )
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
    onModeChanged: (ViewMode) -> Unit,
    modifier: Modifier = Modifier // <- das hier hinzufügen
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
