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

import androidx.compose.foundation.*
import androidx.compose.foundation.draganddrop.dragAndDropTarget
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import de.openindex.zugferd.manager.LocalAppState
import de.openindex.zugferd.manager.gui.ActionButtonWithTooltip
import de.openindex.zugferd.manager.gui.PdfViewer
import de.openindex.zugferd.manager.gui.WebViewer
import de.openindex.zugferd.manager.gui.XmlViewer
import de.openindex.zugferd.manager.model.DocumentTab
import de.openindex.zugferd.manager.utils.createDragAndDropTarget
import de.openindex.zugferd.manager.utils.stringResource
import de.openindex.zugferd.quba.generated.resources.AppCheckSelectMessage
import de.openindex.zugferd.quba.generated.resources.Res
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.awt.Cursor
import de.openindex.zugferd.quba.generated.resources.AppVisualisationSelectFile
import de.openindex.zugferd.quba.generated.resources.AppVisualisationSelectInfo
import androidx.compose.material.icons.filled.Search
import androidx.compose.ui.input.key.*
import androidx.compose.ui.focus.focusRequester


data class SearchState(
    val query: String,
    val sequence: Int = 0,
)


@Composable
@OptIn(ExperimentalFoundationApi::class)
fun VisualsSection(state: VisualsSectionState) {
    val scope = rememberCoroutineScope()
    val appState = LocalAppState.current

    val activeSearch = remember(state.isSearchOpen, state.searchQuery, state.searchSequence) {
        if (state.isSearchOpen && state.searchQuery.isNotBlank()) {
            SearchState(query = state.searchQuery, sequence = state.searchSequence)
        } else {
            null
        }
    }

    LaunchedEffect(state.isSearchOpen) {
        if (!state.isSearchOpen) {
            state.searchQuery = ""
            state.searchSequence = 0
        }
    }

    val focusRequester = remember { androidx.compose.ui.focus.FocusRequester() }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .focusRequester(focusRequester)
            .focusable()
    ) {
        if (state.documents.isNotEmpty()) {
            TabRowWithControls(state)
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp)
                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(4.dp))
                .dragAndDropTarget(
                    target = remember {
                        createDragAndDropTarget { file ->
                            scope.launch {
                                try {
                                    if (state.documents.isEmpty()) {
                                        state.addTabWithFile(file, appState)
                                    } else {
                                        state.loadFileInCurrentTab(file, appState)
                                    }
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            }
                        }
                    },
                    shouldStartDragAndDrop = { true }
                )
        ) {
            if (state.documents.isEmpty()) {
                EmptyVisualsView(state)
            } else {
                CurrentTabContent(state, activeSearch)
            }
        }
    }
}

@Composable
fun VisualsSectionActions(state: VisualsSectionState) {
    val scope = rememberCoroutineScope()
    val appState = LocalAppState.current

    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.padding(end = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (state.documents.isNotEmpty()) {
            if (state.isSearchOpen) {
                CompactSearchBar(
                    value = state.searchQuery,
                    onValueChange = {
                        state.searchQuery = it
                        state.searchSequence = 0
                    },
                    onSubmit = {
                        if (state.searchQuery.isNotBlank()) {
                            state.searchSequence++
                        }
                    },
                    onClose = { state.isSearchOpen = false },
                    modifier = Modifier.width(220.dp)
                )
            } else {
                IconButton(onClick = { state.isSearchOpen = true }) {
                    Icon(Icons.Default.Search, "Suchen")
                }
            }
        }

        // Add button to select a PDF file for validation.
        ActionButtonWithTooltip(
            label = Res.string.AppVisualisationSelectFile,
            tooltip = Res.string.AppVisualisationSelectInfo,
            onClick = {
                scope.launch(Dispatchers.IO) {
                    state.selectFile(appState = appState,)
                                   }
            },
        )
    }
}

@Composable
private fun CompactSearchBar(
    value: String,
    onValueChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val textStyle = MaterialTheme.typography.bodySmall
    val focusRequester = remember { androidx.compose.ui.focus.FocusRequester() }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    Box(
        modifier = modifier
            .height(36.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(18.dp))
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Search,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Box(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 8.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    singleLine = true,
                    textStyle = textStyle.copy(color = MaterialTheme.colorScheme.onSurface),
                    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester)
                        .onPreviewKeyEvent { event ->
                            if ((event.key == Key.Enter || event.key == Key.NumPadEnter) && event.type == KeyEventType.KeyUp) {
                                if (value.isNotBlank()) {
                                    onSubmit()
                                }
                                true
                            } else {
                                false
                            }
                        },
                )
            }

            IconButton(
                onClick = { if (value.isNotBlank()) onSubmit() },
                modifier = Modifier.size(28.dp)
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = "Nächster Treffer",
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            IconButton(
                onClick = onClose,
                modifier = Modifier.size(28.dp)
            ) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = "Suche schließen",
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/*
@Composable
private fun TabRowWithControls(state: VisualsSectionState) {
    val scrollState = rememberScrollState()
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .horizontalScroll(scrollState)
            .fillMaxWidth()
            .padding(vertical = 8.dp) //<--------------------------------------------------------------------------
    ) {
        state.documents.forEachIndexed { index, doc ->
            DocumentTabItem(
                document = doc,
                isSelected = index == state.selectedIndex,
                onSelect = { state.selectedIndex = index },
                onClose = { state.removeTab(index) }
            )
            Spacer(modifier = Modifier.width(4.dp))
        }

        IconButton(
            onClick = { state.addNewTab() },
            modifier = Modifier.size(36.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Neuer Tab", modifier = Modifier.size(20.dp))
        }
    }
}

 */

@Composable
private fun TabRowWithControls(state: VisualsSectionState) {
    val coroutineScope = rememberCoroutineScope()
    val lazyListState = rememberLazyListState()

    // Zum ausgewählten Tab scrollen
    LaunchedEffect(state.selectedIndex) {
        lazyListState.animateScrollToItem(state.selectedIndex)
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        // Scroll-Indikator links
        if (lazyListState.firstVisibleItemIndex > 0) {
            Icon(
                Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Nach links scrollen",
                modifier = Modifier
                    .size(24.dp)
                    .clickable {
                        coroutineScope.launch {
                            lazyListState.animateScrollToItem(lazyListState.firstVisibleItemIndex - 1)
                        }
                    },
                tint = MaterialTheme.colorScheme.primary
            )
        }

        // Tabs mit LazyRow für bessere Performance
        LazyRow(
            state = lazyListState,
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            contentPadding = PaddingValues(horizontal = 8.dp)
        ) {
            itemsIndexed(state.documents) { index, doc ->
                DocumentTabItem(
                    document = doc,
                    isSelected = index == state.selectedIndex,
                    onSelect = { state.selectedIndex = index },
                    onClose = { state.removeTab(index) }
                )
            }
        }

        // Scroll-Indikator rechts
        if (lazyListState.firstVisibleItemIndex + lazyListState.layoutInfo.visibleItemsInfo.size < state.documents.size) {
            Icon(
                Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = "Nach rechts scrollen",
                modifier = Modifier
                    .size(24.dp)
                    .clickable {
                        coroutineScope.launch {
                            lazyListState.animateScrollToItem(lazyListState.firstVisibleItemIndex + 1)
                        }
                    },
                tint = MaterialTheme.colorScheme.primary
            )
        }

        // "Neuer Tab" Button
        IconButton(
            onClick = { state.addNewTab() },
            modifier = Modifier.size(36.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Neuer Tab", modifier = Modifier.size(20.dp))
        }
    }
}
@Composable
private fun DocumentTabItem(
    document: DocumentTab,
    isSelected: Boolean,
    onSelect: () -> Unit,
    onClose: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
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
            .clickable(onClick = onSelect)
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            text = document.name.ifBlank { "Neuer Tab" },
            color = if (isSelected) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.bodyMedium,
            maxLines = 1
        )

        Spacer(modifier = Modifier.width(8.dp))

        if (document.isLoading) {
            CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
        } else {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Tab schließen",
                modifier = Modifier
                    .size(16.dp)
                    .clickable(onClick = onClose),
                tint = MaterialTheme.colorScheme.error
            )
        }
    }
}

@Composable
private fun CurrentTabContent(state: VisualsSectionState, search: SearchState?) {
    val currentTab = state.documents.getOrNull(state.selectedIndex) ?: return
    var tabState by remember { mutableStateOf(0) }
    var viewMode by remember { mutableStateOf(ViewMode.SPLIT) }

    val hasPdf = currentTab.pdf != null
    val hasHtml = currentTab.html != null
    val hasXml = currentTab.xml != null
    val hasCode = hasHtml || hasXml

    LaunchedEffect(hasPdf, hasCode) {
        viewMode = when {
            !hasPdf && hasCode -> ViewMode.CODE_ONLY
            hasPdf && !hasCode -> ViewMode.PDF_ONLY
            hasPdf && hasCode -> ViewMode.SPLIT
            else -> ViewMode.PDF_ONLY
        }
    }

    if (currentTab.isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    if (!hasPdf && !hasCode) {
        EmptyVisualsView(state)
        return
    }

    Column(modifier = Modifier.fillMaxSize()) {
        if (hasPdf && hasCode) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                ViewModeToggle(currentMode = viewMode, onModeChanged = { viewMode = it })
            }
        }

        when (viewMode) {
            ViewMode.PDF_ONLY -> currentTab.pdf?.let { PdfViewer(pdf = it, modifier = Modifier.fillMaxSize(), search = search) }
            ViewMode.CODE_ONLY -> CodeOnlyView(currentTab, tabState, { tabState = it }, search)
            ViewMode.SPLIT -> ResizableSplitPane(
                leftContent = { CodeOnlyView(currentTab, tabState, { tabState = it }, search) },
                rightContent = { currentTab.pdf?.let { PdfViewer(pdf = it, modifier = Modifier.fillMaxSize(), search = search) } }
            )
        }
    }
}

@Composable
private fun EmptyVisualsView(state: VisualsSectionState) {
    val scope = rememberCoroutineScope()
    val appState = LocalAppState.current

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = stringResource(Res.string.AppCheckSelectMessage),
                textAlign = TextAlign.Center,
                softWrap = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            /*
            Button(onClick = { scope.launch(Dispatchers.IO) { state.selectFile(appState) } }) {
                Text("Datei auswählen")
            }
                         */
        }
    }
}

@Composable
private fun CodeOnlyView(tab: DocumentTab, selectedTab: Int, onTabChange: (Int) -> Unit, search: SearchState?) {
    val hasHtml = tab.html != null
    val hasXml = tab.xml != null

    Column(modifier = Modifier.fillMaxSize()) {
        // Tab row for HTML/XML
        if (hasHtml && hasXml) {
            TabRow(selectedTabIndex = selectedTab) {
                if (hasHtml) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { onTabChange(0) },
                        text = { Text("HTML Vorschau") }
                    )
                }

                if (hasXml) {
                    Tab(
                        selected = selectedTab == if (hasHtml) 1 else 0,
                        onClick = { onTabChange(if (hasHtml) 1 else 0) },
                        text = { Text("XML") }
                    )
                }
            }
        }

        // Content
        Box(modifier = Modifier.fillMaxSize()) {
            when {
                hasHtml && selectedTab == 0 -> {
                    WebViewer(html = tab.html!!, modifier = Modifier.fillMaxSize(), search = search)
                }

                hasXml && (selectedTab == 1 || !hasHtml) -> {
                    XmlViewer(xml = tab.xml!!, modifier = Modifier.fillMaxSize(), search = search)
                }

                else -> {
                    // Show empty state if no content
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Kein Inhalt verfügbar")
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



