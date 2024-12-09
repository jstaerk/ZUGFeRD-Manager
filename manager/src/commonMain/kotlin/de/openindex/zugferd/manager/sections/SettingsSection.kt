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

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import de.openindex.zugferd.manager.APP_TITLE
import de.openindex.zugferd.manager.gui.ActionsButton
import de.openindex.zugferd.manager.gui.ProductItemSettings
import de.openindex.zugferd.manager.gui.QuestionDialog
import de.openindex.zugferd.manager.gui.SectionInfo
import de.openindex.zugferd.manager.gui.SectionSubTitle
import de.openindex.zugferd.manager.gui.SectionTitle
import de.openindex.zugferd.manager.gui.TradePartyItemSettings
import de.openindex.zugferd.manager.gui.VerticalScrollBox
import de.openindex.zugferd.manager.utils.LocalPreferences
import de.openindex.zugferd.manager.utils.LocalProducts
import de.openindex.zugferd.manager.utils.LocalRecipients
import de.openindex.zugferd.manager.utils.LocalSenders
import io.github.vinceglb.filekit.compose.rememberFilePickerLauncher
import io.github.vinceglb.filekit.compose.rememberFileSaverLauncher
import io.github.vinceglb.filekit.core.PickerMode
import io.github.vinceglb.filekit.core.PickerType
import kotlinx.coroutines.launch

@Composable
fun SettingsSection(state: SettingsSectionState) {
    VerticalScrollBox {
        Column(
            verticalArrangement = Arrangement.spacedBy(24.dp),
            modifier = Modifier
                .padding(vertical = 16.dp, horizontal = 20.dp),
        ) {
            SectionTitle(
                text = "Einstellungen zum Programm",
            )

            SenderSettings(state)
            RecipientSettings(state)
            ProductSettings(state)
            PdfASettings(state)
            ThemeSettings(state)
        }
    }
}

@Composable
@Suppress("UNUSED_PARAMETER")
private fun SenderSettings(state: SettingsSectionState) {
    val scope = rememberCoroutineScope()
    val senders = LocalSenders.current
    var showDeleteDialog by remember { mutableStateOf(false) }

    val importer = rememberFilePickerLauncher(
        type = PickerType.File(listOf("json")),
        mode = PickerMode.Single,
        title = "Datei zum Import wählen",
    ) { file ->
        if (file == null) {
            return@rememberFilePickerLauncher
        }
        scope.launch {
            senders.import(
                sourceFile = file,
            )
        }
    }

    val exporter = rememberFileSaverLauncher { file ->
        if (file == null) {
            return@rememberFileSaverLauncher
        }
        scope.launch {
            senders.export(
                targetFile = file,
            )
        }
    }

    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier,
    ) {
        SectionSubTitle(
            text = "Absender",
            actions = {
                ActionsButton { doClose ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = "Absender importieren",
                                softWrap = false,
                            )
                        },
                        onClick = {
                            doClose()
                            importer.launch()
                        }
                    )

                    DropdownMenuItem(
                        text = {
                            Text(
                                text = "Absender exportieren",
                                softWrap = false,
                            )
                        },
                        onClick = {
                            doClose()
                            exporter.launch(
                                baseName = "zugferd-absender",
                                extension = "json",
                            )
                        }
                    )

                    HorizontalDivider()

                    DropdownMenuItem(
                        text = {
                            Text(
                                text = "Alle Absender löschen",
                                softWrap = false,
                            )
                        },
                        onClick = {
                            doClose()
                            showDeleteDialog = true
                        }
                    )
                }
            },
        )

        SectionInfo(
            text = "Hier können die Absenderdaten für Rechnungen erfasst werden, damit diese nicht bei jeder Rechnungserstellung neu eingetragen werden müssen.",
        )

        TradePartyItemSettings(
            tradeParties = senders.senders,
            onSave = { item ->
                scope.launch {
                    senders.put(
                        sender = item,
                    )
                    senders.save()
                }
            },
            onRemove = { item ->
                scope.launch {
                    senders.remove(
                        sender = item,
                    )
                    senders.save()
                }
            },
            dialogTitle = { item ->
                "Absender bearbeiten"
                    .takeIf { item?.isSaved == true }
                    ?: "Absender hinzufügen"
            },
        )
    }

    if (showDeleteDialog) {
        QuestionDialog(
            question = "Sollen wirklich alle Absender gelöscht werden?",
            onCancel = { showDeleteDialog = false },
            onAccept = {
                showDeleteDialog = false
                senders.removeAll()
                scope.launch {
                    senders.save()
                }
            }
        )
    }
}

@Composable
@Suppress("UNUSED_PARAMETER")
private fun RecipientSettings(state: SettingsSectionState) {
    val scope = rememberCoroutineScope()
    val recipients = LocalRecipients.current
    var showDeleteDialog by remember { mutableStateOf(false) }

    val importer = rememberFilePickerLauncher(
        type = PickerType.File(listOf("json")),
        mode = PickerMode.Single,
        title = "Datei zum Import wählen",
    ) { file ->
        if (file == null) {
            return@rememberFilePickerLauncher
        }
        scope.launch {
            recipients.import(
                sourceFile = file,
            )
        }
    }

    val exporter = rememberFileSaverLauncher { file ->
        if (file == null) {
            return@rememberFileSaverLauncher
        }
        scope.launch {
            recipients.export(
                targetFile = file,
            )
        }
    }

    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier,
    ) {
        SectionSubTitle(
            text = "Empfänger",
            actions = {
                ActionsButton { doClose ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = "Empfänger importieren",
                                softWrap = false,
                            )
                        },
                        onClick = {
                            doClose()
                            importer.launch()
                        }
                    )

                    DropdownMenuItem(
                        text = {
                            Text(
                                text = "Empfänger exportieren",
                                softWrap = false,
                            )
                        },
                        onClick = {
                            doClose()
                            exporter.launch(
                                baseName = "zugferd-empfaenger",
                                extension = "json",
                            )
                        }
                    )

                    HorizontalDivider()

                    DropdownMenuItem(
                        text = {
                            Text(
                                text = "Alle Empfänger löschen",
                                softWrap = false,
                            )
                        },
                        onClick = {
                            doClose()
                            showDeleteDialog = true
                        }
                    )
                }
            },
        )

        SectionInfo(
            text = "Hier können die Empfängerdaten für Rechnungen erfasst werden, damit diese nicht bei jeder Rechnungserstellung neu eingetragen werden müssen.",
        )

        TradePartyItemSettings(
            isCustomer = true,
            tradeParties = recipients.recipients,
            onSave = { item ->
                scope.launch {
                    recipients.put(
                        recipient = item,
                    )
                    recipients.save()
                }
            },
            onRemove = { item ->
                scope.launch {
                    recipients.remove(
                        recipient = item,
                    )
                    recipients.save()
                }
            },
            dialogTitle = { item ->
                "Empfänger bearbeiten"
                    .takeIf { item?.isSaved == true }
                    ?: "Empfänger hinzufügen"
            },
        )
    }

    if (showDeleteDialog) {
        QuestionDialog(
            question = "Sollen wirklich alle Empfänger gelöscht werden?",
            onCancel = { showDeleteDialog = false },
            onAccept = {
                showDeleteDialog = false
                recipients.removeAll()
                scope.launch {
                    recipients.save()
                }
            }
        )
    }
}

@Composable
@Suppress("UNUSED_PARAMETER")
private fun ProductSettings(state: SettingsSectionState) {
    val scope = rememberCoroutineScope()
    val products = LocalProducts.current
    var showDeleteDialog by remember { mutableStateOf(false) }

    val importer = rememberFilePickerLauncher(
        type = PickerType.File(listOf("json")),
        mode = PickerMode.Single,
        title = "Datei zum Import wählen",
    ) { file ->
        if (file == null) {
            return@rememberFilePickerLauncher
        }
        scope.launch {
            products.import(
                sourceFile = file,
            )
        }
    }

    val exporter = rememberFileSaverLauncher { file ->
        if (file == null) {
            return@rememberFileSaverLauncher
        }
        scope.launch {
            products.export(
                targetFile = file,
            )
        }
    }

    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier,
    ) {
        SectionSubTitle(
            text = "Rechnungsposten",
            actions = {
                ActionsButton { doClose ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = "Posten importieren",
                                softWrap = false,
                            )
                        },
                        onClick = {
                            doClose()
                            importer.launch()
                        }
                    )

                    DropdownMenuItem(
                        text = {
                            Text(
                                text = "Posten exportieren",
                                softWrap = false,
                            )
                        },
                        onClick = {
                            doClose()
                            exporter.launch(
                                baseName = "zugferd-rechnungsposten",
                                extension = "json",
                            )
                        }
                    )

                    HorizontalDivider()

                    DropdownMenuItem(
                        text = {
                            Text(
                                text = "Alle Posten löschen",
                                softWrap = false,
                            )
                        },
                        onClick = {
                            doClose()
                            showDeleteDialog = true
                        }
                    )
                }
            },
        )

        SectionInfo(
            text = "Hier können die Rechnungsposten erfasst werden, damit diese nicht bei jeder Rechnungserstellung neu eingetragen werden müssen.",
        )

        ProductItemSettings(
            products = products.products,
            onSave = { item ->
                scope.launch {
                    products.put(
                        product = item,
                    )
                    products.save()
                }
            },
            onRemove = { item ->
                scope.launch {
                    products.remove(
                        product = item,
                    )
                    products.save()
                }
            },
            dialogTitle = { item ->
                "Rechnungsposten bearbeiten"
                    .takeIf { item?.isSaved == true }
                    ?: "Rechnungsposten hinzufügen"
            },
        )
    }

    if (showDeleteDialog) {
        QuestionDialog(
            question = "Sollen wirklich alle Rechnungsposten gelöscht werden?",
            onCancel = { showDeleteDialog = false },
            onAccept = {
                showDeleteDialog = false
                products.removeAll()
                scope.launch {
                    products.save()
                }
            }
        )
    }
}

@Composable
@Suppress("UNUSED_PARAMETER")
@OptIn(ExperimentalLayoutApi::class)
private fun ThemeSettings(state: SettingsSectionState) {
    val scope = rememberCoroutineScope()
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier,
    ) {
        val preferences = LocalPreferences.current

        SectionSubTitle(
            text = "Darstellung",
        )

        SectionInfo(
            text = "Das Programm kann in heller oder dunkler Darstellung angezeigt werden. Bei automatischer Darstellung wird die Einstellung aus dem Betriebssystem übernommen.",
        )

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier,
        ) {
            Button(
                onClick = {
                    preferences.setDarkMode(null)
                    scope.launch {
                        preferences.save()
                    }
                },
                colors = if (preferences.isThemeAuto) {
                    ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                    )
                } else {
                    ButtonDefaults.buttonColors()
                },
                modifier = Modifier,
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier,
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Ansicht automatisch wählen.",
                    )
                    Text(
                        text = "automatisch",
                        softWrap = false,
                    )
                }
            }

            Button(
                onClick = {
                    preferences.setDarkMode(false)
                    scope.launch {
                        preferences.save()
                    }
                },
                colors = if (preferences.isThemeLight) {
                    ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                    )
                } else {
                    ButtonDefaults.buttonColors()
                },
                modifier = Modifier,
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier,
                ) {
                    Icon(
                        imageVector = Icons.Default.LightMode,
                        contentDescription = "Helle Ansicht verwenden.",
                    )
                    Text(
                        text = "hell",
                        softWrap = false,
                    )
                }
            }

            Button(
                onClick = {
                    preferences.setDarkMode(true)
                    scope.launch {
                        preferences.save()
                    }
                },
                colors = if (preferences.isThemeDark) {
                    ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                    )
                } else {
                    ButtonDefaults.buttonColors()
                },
                modifier = Modifier,
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier,
                ) {
                    Icon(
                        imageVector = Icons.Default.DarkMode,
                        contentDescription = "Dunkle Ansicht verwenden.",
                    )
                    Text(
                        text = "dunkel",
                        softWrap = false,
                    )
                }
            }
        }
    }
}

@Composable
@Suppress("UNUSED_PARAMETER")
private fun PdfASettings(state: SettingsSectionState) {
    val scope = rememberCoroutineScope()
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier,
    ) {
        val preferences = LocalPreferences.current

        SectionSubTitle(
            text = "PDF/A-Format bei Erzeugung von E-Rechnungen",
        )

        SectionInfo(
            text = "Bei der Erzeugung von E-Rechnungen muss die gewählte Rechnungsdatei im PDF/A-Format vorliegen. " +
                    "Im Optimalfall sollte dieses Dateiformat direkt aus Word / LibreOffice etc. heraus exportiert werden. " +
                    "$APP_TITLE kann eine gewählte PDF-Datei automatisch umwandeln. Dies ist aber nicht 100%ig zuverlässig " +
                    "und kann zu Fehlern in der erzeugten E-Rechnung führen. Falls eine Umwandlung innerhalb von $APP_TITLE " +
                    "trotzdem gewünscht ist, kann diese bei Bedarf automatisch durchgeführt werden, wenn eine PDF-Rechnung " +
                    "ausgewählt wird.",
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier,
        ) {
            Switch(
                checked = preferences.autoConvertToPdfA,
                onCheckedChange = {
                    preferences.setAutoConvertToPdfA(it)
                    scope.launch {
                        preferences.save()
                    }
                }
            )

            Text(
                text = "PDF-Dateien automatisch in PDF/A umwandeln",
            )
        }
    }
}
