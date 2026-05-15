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

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.*
import androidx.compose.foundation.draganddrop.dragAndDropTarget
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import de.openindex.zugferd.manager.AppSection
import de.openindex.zugferd.manager.LocalAppState
import de.openindex.zugferd.manager.gui.ActionButtonWithTooltip
import de.openindex.zugferd.manager.gui.NotificationBar
import de.openindex.zugferd.manager.gui.PdfViewer
import de.openindex.zugferd.manager.gui.Tooltip
import de.openindex.zugferd.manager.gui.WebViewer
import de.openindex.zugferd.manager.gui.XmlViewer
import de.openindex.zugferd.manager.model.DocumentTab
import de.openindex.zugferd.manager.utils.createDragAndDropTarget
import de.openindex.zugferd.manager.utils.showTabContextMenu
import de.openindex.zugferd.manager.sections.CheckSectionState
import de.openindex.zugferd.manager.utils.stringResource
import de.openindex.zugferd.quba.generated.resources.AppCheckSelectMessage
import de.openindex.zugferd.quba.generated.resources.AppVisualisationNoXml
import de.openindex.zugferd.quba.generated.resources.Res
import kotlin.math.roundToInt
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.awt.Cursor
import java.awt.KeyboardFocusManager
import java.awt.KeyEventDispatcher
import de.openindex.zugferd.quba.generated.resources.AppVisualisationSelectFile
import de.openindex.zugferd.quba.generated.resources.AppVisualisationSelectInfo
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.input.key.*
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.pointer.isPressed


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

    // Global AWT key listener — works even when JCEF/PDF viewer has focus.
    // Ctrl+F opens search; ESC closes search.
    DisposableEffect(Unit) {
        val dispatcher = KeyEventDispatcher { awtEvent ->
            when {
                awtEvent.id == java.awt.event.KeyEvent.KEY_PRESSED
                        && awtEvent.keyCode == java.awt.event.KeyEvent.VK_F
                        && awtEvent.isControlDown -> {
                    state.isSearchOpen = true
                    false
                }
                awtEvent.id == java.awt.event.KeyEvent.KEY_PRESSED
                        && awtEvent.keyCode == java.awt.event.KeyEvent.VK_ESCAPE
                        && state.isSearchOpen -> {
                    state.isSearchOpen = false
                    true
                }
                else -> false
            }
        }
        KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(dispatcher)
        onDispose {
            KeyboardFocusManager.getCurrentKeyboardFocusManager().removeKeyEventDispatcher(dispatcher)
        }
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
@OptIn(ExperimentalFoundationApi::class)
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
internal fun CompactSearchBar(
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
                            when {
                                (event.key == Key.Enter || event.key == Key.NumPadEnter) && event.type == KeyEventType.KeyUp -> {
                                    if (value.isNotBlank()) onSubmit()
                                    true
                                }
                                event.key == Key.Escape && event.type == KeyEventType.KeyUp -> {
                                    onClose()
                                    true
                                }
                                else -> false
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

@Composable
private fun TabRowWithControls(state: VisualsSectionState) {
    val scope = rememberCoroutineScope()
    val appState = LocalAppState.current
    val density = LocalDensity.current

    val maxTabWidth = 200.dp
    val minTabWidth = 80.dp
    val tabSpacing = 4.dp
    val plusButtonSize = 32.dp
    val stripPadding = 8.dp

    // Drag state — lifted here so all tabs can react in real-time.
    var draggedIndex by remember { mutableStateOf<Int?>(null) }
    var dragOffsetX by remember { mutableStateOf(0f) }
    // Mutable array so drag lambdas always read the current step without restarting.
    val tabStepPxRef = remember { floatArrayOf(0f) }

    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
        val tabCount = state.documents.size
        val availableForTabs = maxWidth - plusButtonSize - stripPadding * 2 - tabSpacing
        val rawTabWidth = if (tabCount == 0) maxTabWidth
                          else (availableForTabs - tabSpacing * (tabCount - 1)) / tabCount
        val targetTabWidth = rawTabWidth.coerceIn(minTabWidth, maxTabWidth)

        val tabWidth by animateDpAsState(targetValue = targetTabWidth, label = "tabWidth")

        // Keep ref in sync each frame so drag lambdas always read the correct step.
        tabStepPxRef[0] = with(density) { (tabWidth + tabSpacing).toPx() }

        // Visual insertion index derived from current drag position.
        val insertIndex: Int? = draggedIndex?.let { from ->
            val step = tabStepPxRef[0]
            if (step > 0f) (from + dragOffsetX / step).roundToInt()
                .coerceIn(0, state.documents.lastIndex)
            else from
        }

        val needsScroll = tabCount > 0 && rawTabWidth < minTabWidth
        val scrollState = rememberScrollState()

        LaunchedEffect(state.selectedIndex) {
            if (needsScroll && state.documents.isNotEmpty()) {
                with(density) {
                    val itemWidth = (tabWidth + tabSpacing).toPx()
                    scrollState.animateScrollTo((itemWidth * state.selectedIndex).toInt())
                }
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(tabSpacing),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .weight(1f)
                    .padding(start = stripPadding)
                    .then(if (needsScroll) Modifier.horizontalScroll(scrollState) else Modifier),
            ) {
                state.documents.forEachIndexed { index, doc ->
                    val from = draggedIndex
                    val to = insertIndex
                    val step = tabStepPxRef[0]

                    // How far should this tab shift visually to make room for the dragged tab?
                    val shiftTarget: Float = when {
                        from == null || to == null -> 0f
                        index == from -> 0f                          // dragged tab: offset applied directly
                        from < to && index in (from + 1)..to -> -step  // dragging right: shift left
                        from > to && index in to until from -> step    // dragging left: shift right
                        else -> 0f
                    }

                    DocumentTabItem(
                        document = doc,
                        width = tabWidth,
                        isSelected = index == state.selectedIndex,
                        isDragged = index == draggedIndex,
                        dragOffsetPx = if (index == draggedIndex) dragOffsetX else 0f,
                        shiftTarget = shiftTarget,
                        onSelect = { state.selectedIndex = index },
                        onClose = { state.removeTab(index) },
                        onRightClick = { screenX, screenY ->
                            val checkState = AppSection.CHECK.state as? CheckSectionState
                            val docFile = doc.sourceFile
                            showTabContextMenu(
                                screenX = screenX,
                                screenY = screenY,
                                onClose = { state.removeTab(index) },
                                onCloseOthers = { state.removeOtherTabs(index) },
                                onCloseToRight = if (index < state.documents.lastIndex) ({ state.removeTabsToRight(index) }) else null,
                                openInOtherLabel = "In Prüfen öffnen",
                                onOpenInOther = if (checkState != null && docFile != null) ({
                                    scope.launch(Dispatchers.IO) {
                                        checkState.selectFile(docFile, appState)
                                    }
                                    appState.setSection(AppSection.CHECK)
                                }) else null,
                            )
                        },
                        onDragStart = {
                            draggedIndex = index
                            dragOffsetX = 0f
                        },
                        onDragDelta = { delta ->
                            dragOffsetX += delta
                        },
                        onDragEnd = {
                            val fromIdx = draggedIndex
                            if (fromIdx != null) {
                                val s = tabStepPxRef[0]
                                val toIdx = if (s > 0f)
                                    (fromIdx + dragOffsetX / s).roundToInt()
                                        .coerceIn(0, state.documents.lastIndex)
                                else fromIdx
                                draggedIndex = null
                                dragOffsetX = 0f
                                if (fromIdx != toIdx) state.moveTab(fromIdx, toIdx)
                            } else {
                                draggedIndex = null
                                dragOffsetX = 0f
                            }
                        },
                    )
                }
            }

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .padding(horizontal = stripPadding)
                    .size(plusButtonSize)
                    .clip(RoundedCornerShape(6.dp))
                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(6.dp))
                    .clickable { scope.launch(Dispatchers.IO) { state.selectFile(appState) } },
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Weitere Datei öffnen",
                    modifier = Modifier.size(18.dp),
                    tint = MaterialTheme.colorScheme.onSurface,
                )
            }
        }
    }
}

@Composable
private fun DocumentTabItem(
    document: DocumentTab,
    width: Dp,
    isSelected: Boolean,
    isDragged: Boolean,
    dragOffsetPx: Float,
    shiftTarget: Float,
    onSelect: () -> Unit,
    onClose: () -> Unit,
    onRightClick: (screenX: Int, screenY: Int) -> Unit,
    onDragStart: () -> Unit,
    onDragDelta: (Float) -> Unit,
    onDragEnd: () -> Unit,
) {
    // Sibling tabs animate smoothly to their shifted positions.
    // The dragged tab uses the raw dragOffsetPx for instant cursor-following.
    val animatedShift by animateFloatAsState(
        targetValue = if (isDragged) 0f else shiftTarget,
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "tabShift",
    )
    val visualOffsetPx = if (isDragged) dragOffsetPx else animatedShift

    // Keep callbacks fresh without restarting the pointerInput coroutine.
    val updatedOnDragStart = rememberUpdatedState(onDragStart)
    val updatedOnDragDelta = rememberUpdatedState(onDragDelta)
    val updatedOnDragEnd = rememberUpdatedState(onDragEnd)

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .width(width)
            // Dragged tab floats above siblings; offset is layout-neutral (Row sees original size).
            .zIndex(if (isDragged) 2f else 0f)
            .offset { IntOffset(visualOffsetPx.roundToInt(), 0) }
            .scale(if (isDragged) 1.05f else 1f)
            .clip(RoundedCornerShape(6.dp))
            .background(
                if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                else MaterialTheme.colorScheme.surface
            )
            .border(
                width = if (isSelected || isDragged) 2.dp else 1.dp,
                color = if (isDragged || isSelected) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.outline,
                shape = RoundedCornerShape(6.dp)
            )
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { updatedOnDragStart.value() },
                    onDragEnd = { updatedOnDragEnd.value() },
                    onDragCancel = { updatedOnDragEnd.value() },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        updatedOnDragDelta.value(dragAmount.x)
                    },
                )
            }
            .clickable(onClick = onSelect)
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent()
                        if (event.type == PointerEventType.Press && event.buttons.isPressed(1)) {
                            val mouseEvent = event.nativeEvent as? java.awt.event.MouseEvent
                            onRightClick(mouseEvent?.xOnScreen ?: 0, mouseEvent?.yOnScreen ?: 0)
                        }
                    }
                }
            }
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            text = document.name.ifBlank { "Neuer Tab" },
            color = if (isSelected) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.bodyMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )

        Spacer(modifier = Modifier.width(6.dp))

        if (document.isLoading) {
            CircularProgressIndicator(modifier = Modifier.size(14.dp), strokeWidth = 2.dp)
        } else {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Tab schließen",
                modifier = Modifier
                    .size(14.dp)
                    .clickable(onClick = onClose),
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            )
        }
    }
}

@Composable
private fun CurrentTabContent(state: VisualsSectionState, search: SearchState?) {
    val currentTab = state.documents.getOrNull(state.selectedIndex) ?: return

    if (currentTab.isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    val hasPdf = currentTab.pdf != null
    val hasXml = currentTab.xml != null
    val hasHtml = currentTab.html != null
    val hasCode = hasXml || hasHtml || currentTab.isHtmlLoading

    if (!hasPdf && !hasCode) {
        EmptyVisualsView(state)
        return
    }

    // hasStableCode excludes isHtmlLoading intentionally:
    // plain PDFs briefly have isHtmlLoading=true (HTML generation returns null),
    // which would incorrectly trigger SPLIT mode for one frame.
    val hasStableCode = hasXml || hasHtml

    // Reset viewMode when the tab changes; initialize based on stable content.
    var viewMode by remember(currentTab) {
        mutableStateOf(when {
            hasPdf && hasStableCode -> ViewMode.SPLIT
            hasStableCode -> ViewMode.CODE_ONLY
            else -> ViewMode.PDF_ONLY
        })
    }

    // Correct viewMode when content availability changes asynchronously
    // (e.g. XML/HTML arrives after the PDF was already shown).
    LaunchedEffect(hasPdf, hasStableCode) {
        when {
            !hasPdf && viewMode != ViewMode.CODE_ONLY -> viewMode = ViewMode.CODE_ONLY
            !hasStableCode && viewMode == ViewMode.CODE_ONLY -> viewMode = ViewMode.PDF_ONLY
            !hasStableCode && viewMode == ViewMode.SPLIT -> viewMode = ViewMode.PDF_ONLY
            hasStableCode && hasPdf && viewMode == ViewMode.PDF_ONLY -> viewMode = ViewMode.SPLIT
        }
    }

    var tabState by remember(currentTab) { mutableStateOf(0) }
    val splitRatioState = remember(currentTab) { mutableStateOf(0.5f) }

    Column(modifier = Modifier.fillMaxSize()) {
        // Mode toggle — only shown when both PDF and code content are available
        if (hasPdf && hasCode) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                ViewModeToggle(currentMode = viewMode, onModeChanged = { viewMode = it })
            }
        }

        // Informational banner for plain PDFs without embedded e-invoice data
        if (hasPdf && !hasCode) {
            NotificationBar(text = stringResource(Res.string.AppVisualisationNoXml))
        }

        // ContentArea keeps PdfViewer at a fixed slot in the composition tree.
        // This prevents JCEF browser disposal when switching between SPLIT and PDF_ONLY,
        // which would otherwise cause blank rendering (two browsers sharing one CEF client).
        ContentArea(
            viewMode = viewMode,
            splitRatioState = splitRatioState,
            codeContent = {
                if (hasCode) CodeOnlyView(currentTab, tabState, { tabState = it }, search)
            },
            pdfContent = {
                currentTab.pdf?.let { pdf ->
                    PdfViewer(pdf = pdf, modifier = Modifier.fillMaxSize(), search = search)
                }
            },
            modifier = Modifier.weight(1f),
        )
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
    // HTML-Tab anzeigen sobald HTML bereit ODER noch geladen wird
    val showHtmlTab = hasHtml || tab.isHtmlLoading

    Column(modifier = Modifier.fillMaxSize()) {
        // Tab row: HTML-Tab (ggf. mit Lade-Indikator) + XML-Tab
        if (showHtmlTab && hasXml) {
            TabRow(selectedTabIndex = selectedTab) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { onTabChange(0) },
                    text = {
                        if (tab.isHtmlLoading && !hasHtml) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Text("HTML Vorschau")
                                Spacer(modifier = Modifier.width(6.dp))
                                CircularProgressIndicator(
                                    modifier = Modifier.size(12.dp),
                                    strokeWidth = 2.dp
                                )
                            }
                        } else {
                            Text("HTML Vorschau")
                        }
                    }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { onTabChange(1) },
                    text = { Text("XML") }
                )
            }
        }

        // Content
        Box(modifier = Modifier.fillMaxSize()) {
            when {
                selectedTab == 0 && hasHtml -> {
                    WebViewer(html = tab.html!!, modifier = Modifier.fillMaxSize(), search = search)
                }

                selectedTab == 0 && tab.isHtmlLoading -> {
                    // HTML wird noch generiert – Spinner anzeigen
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "HTML wird generiert…",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                hasXml && (selectedTab == 1 || !showHtmlTab) -> {
                    XmlViewer(xml = tab.xml!!, modifier = Modifier.fillMaxSize(), search = search)
                }

                else -> {
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

// Three fixed composition slots: [0] code viewer | [1] divider | [2] PDF viewer.
// Drag deltas are accumulated each mouse event and flushed once per display frame
// via withFrameNanos — throttles JCEF resize calls to ~60/sec.
@Composable
private fun ContentArea(
    viewMode: ViewMode,
    splitRatioState: MutableState<Float>,
    codeContent: @Composable () -> Unit,
    pdfContent: @Composable () -> Unit,
    modifier: Modifier = Modifier,
) {
    val dividerPx = with(LocalDensity.current) { 6.dp.roundToPx() }
    val containerWidth = remember { intArrayOf(0) }
    val pendingDelta = remember { floatArrayOf(0f) }

    fun applyDelta() {
        val d = pendingDelta[0]
        val w = containerWidth[0]
        if (d != 0f && w > 0) {
            pendingDelta[0] = 0f
            splitRatioState.value = (splitRatioState.value + d / w).coerceIn(0.1f, 0.9f)
        }
    }

    LaunchedEffect(splitRatioState) {
        while (true) { withFrameNanos { applyDelta() } }
    }

    val dividerColor = MaterialTheme.colorScheme.outline
    Layout(
        modifier = modifier.fillMaxSize(),
        content = {
            Box { if (viewMode != ViewMode.PDF_ONLY) codeContent() }
            Box(
                modifier = if (viewMode == ViewMode.SPLIT) Modifier
                    .background(dividerColor)
                    .pointerHoverIcon(PointerIcon(Cursor(Cursor.E_RESIZE_CURSOR)))
                    .draggable(
                        orientation = Orientation.Horizontal,
                        state = rememberDraggableState { pendingDelta[0] += it },
                        onDragStopped = { applyDelta() },
                    )
                else Modifier
            )
            Box { if (viewMode != ViewMode.CODE_ONLY) pdfContent() }
        }
    ) { measurables, constraints ->
        containerWidth[0] = constraints.maxWidth
        val ratio = splitRatioState.value
        val h = if (constraints.hasBoundedHeight) constraints.maxHeight else 0

        val (codeW, divW, pdfW) = when (viewMode) {
            ViewMode.PDF_ONLY  -> Triple(0, 0, constraints.maxWidth)
            ViewMode.CODE_ONLY -> Triple(constraints.maxWidth, 0, 0)
            ViewMode.SPLIT -> {
                val cw = ((constraints.maxWidth - dividerPx) * ratio).toInt()
                    .coerceIn(0, constraints.maxWidth - dividerPx)
                Triple(cw, dividerPx, constraints.maxWidth - cw - dividerPx)
            }
        }

        fun slot(w: Int) = constraints.copy(minWidth = w, maxWidth = w, minHeight = h, maxHeight = h)
        val code    = measurables[0].measure(slot(codeW))
        val divider = measurables[1].measure(slot(divW))
        val pdf     = measurables[2].measure(slot(pdfW))

        layout(constraints.maxWidth, h) {
            code.placeRelative(0, 0)
            divider.placeRelative(codeW, 0)
            pdf.placeRelative(codeW + divW, 0)
        }
    }
}

