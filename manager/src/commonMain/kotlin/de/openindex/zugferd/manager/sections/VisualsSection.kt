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
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import de.openindex.zugferd.manager.model.DocumentTab
import de.openindex.zugferd.manager.sections.VisualsSectionState
import de.openindex.zugferd.manager.utils.SectionState
import java.io.File
import javax.swing.JFileChooser

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