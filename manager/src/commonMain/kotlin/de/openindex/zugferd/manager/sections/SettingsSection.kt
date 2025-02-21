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
import de.openindex.zugferd.manager.gui.ActionDropDownButton
import de.openindex.zugferd.manager.gui.Label
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
import de.openindex.zugferd.manager.utils.stringResource
import de.openindex.zugferd.manager.utils.title
import de.openindex.zugferd.zugferd_manager.generated.resources.AppSettings
import de.openindex.zugferd.zugferd_manager.generated.resources.AppSettingsChrome
import de.openindex.zugferd.zugferd_manager.generated.resources.AppSettingsChromeHardwareAcceleration
import de.openindex.zugferd.zugferd_manager.generated.resources.AppSettingsChromeHardwareAccelerationRestart
import de.openindex.zugferd.zugferd_manager.generated.resources.AppSettingsPdf
import de.openindex.zugferd.zugferd_manager.generated.resources.AppSettingsPdfConvertAuto
import de.openindex.zugferd.zugferd_manager.generated.resources.AppSettingsPdfInfo
import de.openindex.zugferd.zugferd_manager.generated.resources.AppSettingsProduct
import de.openindex.zugferd.zugferd_manager.generated.resources.AppSettingsProductAdd
import de.openindex.zugferd.zugferd_manager.generated.resources.AppSettingsProductEdit
import de.openindex.zugferd.zugferd_manager.generated.resources.AppSettingsProductExport
import de.openindex.zugferd.zugferd_manager.generated.resources.AppSettingsProductExportFileName
import de.openindex.zugferd.zugferd_manager.generated.resources.AppSettingsProductImport
import de.openindex.zugferd.zugferd_manager.generated.resources.AppSettingsProductImportSelectFile
import de.openindex.zugferd.zugferd_manager.generated.resources.AppSettingsProductInfo
import de.openindex.zugferd.zugferd_manager.generated.resources.AppSettingsProductRemoveAll
import de.openindex.zugferd.zugferd_manager.generated.resources.AppSettingsProductRemoveAllConfirm
import de.openindex.zugferd.zugferd_manager.generated.resources.AppSettingsRecipient
import de.openindex.zugferd.zugferd_manager.generated.resources.AppSettingsRecipientAdd
import de.openindex.zugferd.zugferd_manager.generated.resources.AppSettingsRecipientEdit
import de.openindex.zugferd.zugferd_manager.generated.resources.AppSettingsRecipientExport
import de.openindex.zugferd.zugferd_manager.generated.resources.AppSettingsRecipientExportFileName
import de.openindex.zugferd.zugferd_manager.generated.resources.AppSettingsRecipientImport
import de.openindex.zugferd.zugferd_manager.generated.resources.AppSettingsRecipientImportSelectFile
import de.openindex.zugferd.zugferd_manager.generated.resources.AppSettingsRecipientInfo
import de.openindex.zugferd.zugferd_manager.generated.resources.AppSettingsRecipientRemoveAll
import de.openindex.zugferd.zugferd_manager.generated.resources.AppSettingsRecipientRemoveAllConfirm
import de.openindex.zugferd.zugferd_manager.generated.resources.AppSettingsSender
import de.openindex.zugferd.zugferd_manager.generated.resources.AppSettingsSenderAdd
import de.openindex.zugferd.zugferd_manager.generated.resources.AppSettingsSenderEdit
import de.openindex.zugferd.zugferd_manager.generated.resources.AppSettingsSenderExport
import de.openindex.zugferd.zugferd_manager.generated.resources.AppSettingsSenderExportFileName
import de.openindex.zugferd.zugferd_manager.generated.resources.AppSettingsSenderImport
import de.openindex.zugferd.zugferd_manager.generated.resources.AppSettingsSenderImportSelectFile
import de.openindex.zugferd.zugferd_manager.generated.resources.AppSettingsSenderInfo
import de.openindex.zugferd.zugferd_manager.generated.resources.AppSettingsSenderRemoveAll
import de.openindex.zugferd.zugferd_manager.generated.resources.AppSettingsSenderRemoveAllConfirm
import de.openindex.zugferd.zugferd_manager.generated.resources.AppSettingsTheme
import de.openindex.zugferd.zugferd_manager.generated.resources.AppSettingsThemeAuto
import de.openindex.zugferd.zugferd_manager.generated.resources.AppSettingsThemeDark
import de.openindex.zugferd.zugferd_manager.generated.resources.AppSettingsThemeInfo
import de.openindex.zugferd.zugferd_manager.generated.resources.AppSettingsThemeLight
import de.openindex.zugferd.zugferd_manager.generated.resources.Res
import io.github.vinceglb.filekit.compose.rememberFilePickerLauncher
import io.github.vinceglb.filekit.compose.rememberFileSaverLauncher
import io.github.vinceglb.filekit.core.PickerMode
import io.github.vinceglb.filekit.core.PickerType
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.jetbrains.compose.resources.getString

/**
 * Main view of the settings section.
 */
@Composable
fun SettingsSection(state: SettingsSectionState) {
    VerticalScrollBox {
        Column(
            verticalArrangement = Arrangement.spacedBy(24.dp),
            modifier = Modifier
                .padding(vertical = 16.dp, horizontal = 20.dp),
        ) {
            SectionTitle(
                text = Res.string.AppSettings,
            )

            SenderSettings(state)
            RecipientSettings(state)
            ProductSettings(state)
            PdfSettings(state)
            ChromeSettings(state)
            ThemeSettings(state)
        }
    }
}

/**
 * Section for sender settings.
 */
@Composable
@Suppress("UNUSED_PARAMETER", "DuplicatedCode")
private fun SenderSettings(state: SettingsSectionState) {
    val scope = rememberCoroutineScope()
    val senders = LocalSenders.current
    var showDeleteDialog by remember { mutableStateOf(false) }

    // Sender import handler.
    val importer = rememberFilePickerLauncher(
        type = PickerType.File(listOf("json")),
        mode = PickerMode.Single,
        title = stringResource(Res.string.AppSettingsSenderImportSelectFile).title(),
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

    // Sender export handler.
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
        // Sender section subtitle with additional actions.
        SectionSubTitle(
            text = Res.string.AppSettingsSender,
            actions = {
                ActionDropDownButton { doClose ->
                    // Import senders.
                    DropdownMenuItem(
                        text = {
                            Label(
                                text = Res.string.AppSettingsSenderImport,
                            )
                        },
                        onClick = {
                            doClose()
                            importer.launch()
                        }
                    )

                    // Export senders.
                    DropdownMenuItem(
                        text = {
                            Label(
                                text = Res.string.AppSettingsSenderExport,
                            )
                        },
                        onClick = {
                            doClose()
                            exporter.launch(
                                baseName = runBlocking { getString(Res.string.AppSettingsSenderExportFileName) },
                                extension = "json",
                            )
                        }
                    )

                    HorizontalDivider()

                    // Remove all senders.
                    DropdownMenuItem(
                        text = {
                            Label(
                                text = Res.string.AppSettingsSenderRemoveAll,
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

        // Sender section information.
        SectionInfo(
            text = Res.string.AppSettingsSenderInfo,
        )

        // Modify default sender items.
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
                stringResource(
                    Res.string.AppSettingsSenderEdit
                        .takeIf { item?.isSaved == true }
                        ?: Res.string.AppSettingsSenderAdd
                ).title()
            },
        )
    }

    // Show confirmation before removing senders.
    if (showDeleteDialog) {
        QuestionDialog(
            question = stringResource(Res.string.AppSettingsSenderRemoveAllConfirm),
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

/**
 * Section for recipient settings.
 */
@Composable
@Suppress("UNUSED_PARAMETER", "DuplicatedCode")
private fun RecipientSettings(state: SettingsSectionState) {
    val scope = rememberCoroutineScope()
    val recipients = LocalRecipients.current
    var showDeleteDialog by remember { mutableStateOf(false) }

    // Recipient import handler.
    val importer = rememberFilePickerLauncher(
        type = PickerType.File(listOf("json")),
        mode = PickerMode.Single,
        title = stringResource(Res.string.AppSettingsRecipientImportSelectFile).title(),
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

    // Recipient export handler.
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
        // Recipient section subtitle with additional actions.
        SectionSubTitle(
            text = Res.string.AppSettingsRecipient,
            actions = {
                ActionDropDownButton { doClose ->
                    // Import recipients.
                    DropdownMenuItem(
                        text = {
                            Label(
                                text = Res.string.AppSettingsRecipientImport,
                            )
                        },
                        onClick = {
                            doClose()
                            importer.launch()
                        }
                    )

                    // Export recipients.
                    DropdownMenuItem(
                        text = {
                            Label(
                                text = Res.string.AppSettingsRecipientExport,
                            )
                        },
                        onClick = {
                            doClose()
                            exporter.launch(
                                baseName = runBlocking { getString(Res.string.AppSettingsRecipientExportFileName) },
                                extension = "json",
                            )
                        }
                    )

                    HorizontalDivider()

                    // Remove all recipients.
                    DropdownMenuItem(
                        text = {
                            Label(
                                text = Res.string.AppSettingsRecipientRemoveAll,
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

        // Recipient section information.
        SectionInfo(
            text = Res.string.AppSettingsRecipientInfo,
        )

        // Modify default recipient items.
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
                stringResource(
                    Res.string.AppSettingsRecipientEdit
                        .takeIf { item?.isSaved == true }
                        ?: Res.string.AppSettingsRecipientAdd
                ).title()
            },
        )
    }

    // Show confirmation before removing recipients.
    if (showDeleteDialog) {
        QuestionDialog(
            question = Res.string.AppSettingsRecipientRemoveAllConfirm,
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

/**
 * Section for product settings.
 */
@Composable
@Suppress("UNUSED_PARAMETER", "DuplicatedCode")
private fun ProductSettings(state: SettingsSectionState) {
    val scope = rememberCoroutineScope()
    val products = LocalProducts.current
    var showDeleteDialog by remember { mutableStateOf(false) }

    // Product import handler.
    val importer = rememberFilePickerLauncher(
        type = PickerType.File(listOf("json")),
        mode = PickerMode.Single,
        title = stringResource(Res.string.AppSettingsProductImportSelectFile).title(),
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

    // Product export handler.
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
        // Product section subtitle with additional actions.
        SectionSubTitle(
            text = Res.string.AppSettingsProduct,
            actions = {
                ActionDropDownButton { doClose ->
                    // Import products.
                    DropdownMenuItem(
                        text = {
                            Label(
                                text = Res.string.AppSettingsProductImport,
                            )
                        },
                        onClick = {
                            doClose()
                            importer.launch()
                        }
                    )

                    // Export products.
                    DropdownMenuItem(
                        text = {
                            Label(
                                text = Res.string.AppSettingsProductExport,
                            )
                        },
                        onClick = {
                            doClose()
                            exporter.launch(
                                baseName = runBlocking { getString(Res.string.AppSettingsProductExportFileName) },
                                extension = "json",
                            )
                        }
                    )

                    HorizontalDivider()

                    // Remove all products.
                    DropdownMenuItem(
                        text = {
                            Label(
                                text = Res.string.AppSettingsProductRemoveAll,
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

        // Product section information.
        SectionInfo(
            text = Res.string.AppSettingsProductInfo,
        )

        // Modify default product items.
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
                stringResource(
                    Res.string.AppSettingsProductEdit
                        .takeIf { item?.isSaved == true }
                        ?: Res.string.AppSettingsProductAdd
                ).title()
            },
        )
    }

    // Show confirmation before removing products.
    if (showDeleteDialog) {
        QuestionDialog(
            question = Res.string.AppSettingsProductRemoveAllConfirm,
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

/**
 * Section for theme settings.
 */
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

        // Theme section subtitle.
        SectionSubTitle(
            text = Res.string.AppSettingsTheme,
        )

        // Theme section information.
        SectionInfo(
            text = Res.string.AppSettingsThemeInfo,
        )

        // Selection between light and dark mode.
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier,
        ) {
            // automatic selection
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
                        contentDescription = stringResource(Res.string.AppSettingsThemeAuto),
                    )
                    Label(
                        text = stringResource(Res.string.AppSettingsThemeAuto),
                    )
                }
            }

            // light mode selection
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
                        contentDescription = stringResource(Res.string.AppSettingsThemeLight),
                    )
                    Label(
                        text = stringResource(Res.string.AppSettingsThemeLight),
                    )
                }
            }

            // dark mode selection
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
                        contentDescription = stringResource(Res.string.AppSettingsThemeDark)
                    )
                    Label(
                        text = stringResource(Res.string.AppSettingsThemeDark),
                    )
                }
            }
        }
    }
}

/**
 * Section for PDF settings.
 */
@Composable
@Suppress("UNUSED_PARAMETER")
private fun PdfSettings(state: SettingsSectionState) {
    val scope = rememberCoroutineScope()
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier,
    ) {
        val preferences = LocalPreferences.current

        // PDF section subtitle.
        SectionSubTitle(
            text = Res.string.AppSettingsPdf,
        )

        // PDF section information.
        SectionInfo(
            text = stringResource(Res.string.AppSettingsPdfInfo, APP_TITLE),
        )

        // Toggle for automatic PDF/A conversion.
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

            Label(
                text = Res.string.AppSettingsPdfConvertAuto,
            )
        }
    }
}

/**
 * Section for Chrome settings.
 */
@Composable
@Suppress("UNUSED_PARAMETER")
private fun ChromeSettings(state: SettingsSectionState) {
    val scope = rememberCoroutineScope()
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier,
    ) {
        val preferences = LocalPreferences.current

        // Chrome section subtitle.
        SectionSubTitle(
            text = Res.string.AppSettingsChrome,
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier,
        ) {
            Switch(
                checked = preferences.chromeGpuEnabled,
                onCheckedChange = {
                    preferences.setChromeGpuEnabled(it)
                    scope.launch {
                        preferences.save()
                    }
                }
            )

            Column {
                Label(
                    text = Res.string.AppSettingsChromeHardwareAcceleration,
                )
                Text(
                    text = "(${stringResource(Res.string.AppSettingsChromeHardwareAccelerationRestart)})",
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
    }
}
