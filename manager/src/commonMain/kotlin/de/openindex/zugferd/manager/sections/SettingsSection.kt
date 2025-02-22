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

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import de.openindex.zugferd.manager.LocalAppState
import de.openindex.zugferd.manager.gui.ActionDropDownButton
import de.openindex.zugferd.manager.gui.CountryField
import de.openindex.zugferd.manager.gui.CurrencyField
import de.openindex.zugferd.manager.gui.DecimalField
import de.openindex.zugferd.manager.gui.Label
import de.openindex.zugferd.manager.gui.LanguageField
import de.openindex.zugferd.manager.gui.ProductItemSettings
import de.openindex.zugferd.manager.gui.QuestionDialog
import de.openindex.zugferd.manager.gui.SectionInfo
import de.openindex.zugferd.manager.gui.SectionSubTitle
import de.openindex.zugferd.manager.gui.SectionTitle
import de.openindex.zugferd.manager.gui.Tooltip
import de.openindex.zugferd.manager.gui.TradePartyItemSettings
import de.openindex.zugferd.manager.gui.VerticalScrollBox
import de.openindex.zugferd.manager.utils.Language
import de.openindex.zugferd.manager.utils.getCountryDefaultCurrency
import de.openindex.zugferd.manager.utils.getCountryDefaultTax
import de.openindex.zugferd.manager.utils.getString
import de.openindex.zugferd.manager.utils.stringResource
import de.openindex.zugferd.manager.utils.title
import de.openindex.zugferd.zugferd_manager.generated.resources.AppSettings
import de.openindex.zugferd.zugferd_manager.generated.resources.AppSettingsChrome
import de.openindex.zugferd.zugferd_manager.generated.resources.AppSettingsChromeAcceleration
import de.openindex.zugferd.zugferd_manager.generated.resources.AppSettingsChromeAccelerationRestart
import de.openindex.zugferd.zugferd_manager.generated.resources.AppSettingsCreate
import de.openindex.zugferd.zugferd_manager.generated.resources.AppSettingsCreatePdfAutoConvert
import de.openindex.zugferd.zugferd_manager.generated.resources.AppSettingsCreatePdfAutoConvertExperimental
import de.openindex.zugferd.zugferd_manager.generated.resources.AppSettingsCreatePdfAutoConvertWarning
import de.openindex.zugferd.zugferd_manager.generated.resources.AppSettingsCreatePdfRemoveAttachments
import de.openindex.zugferd.zugferd_manager.generated.resources.AppSettingsCreatePdfRemoveAttachmentsExperimental
import de.openindex.zugferd.zugferd_manager.generated.resources.AppSettingsCreatePdfRemoveAttachmentsWarning
import de.openindex.zugferd.zugferd_manager.generated.resources.AppSettingsGeneral
import de.openindex.zugferd.zugferd_manager.generated.resources.AppSettingsGeneralCountry
import de.openindex.zugferd.zugferd_manager.generated.resources.AppSettingsGeneralCurrency
import de.openindex.zugferd.zugferd_manager.generated.resources.AppSettingsGeneralInfo
import de.openindex.zugferd.zugferd_manager.generated.resources.AppSettingsGeneralLanguage
import de.openindex.zugferd.zugferd_manager.generated.resources.AppSettingsGeneralVat
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
import de.openindex.zugferd.zugferd_manager.generated.resources.AppSettingsSectionInfo
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
import de.openindex.zugferd.zugferd_manager.generated.resources.AppSettingsThemeLight
import de.openindex.zugferd.zugferd_manager.generated.resources.Res
import io.github.vinceglb.filekit.compose.rememberFilePickerLauncher
import io.github.vinceglb.filekit.compose.rememberFileSaverLauncher
import io.github.vinceglb.filekit.core.PickerMode
import io.github.vinceglb.filekit.core.PickerType
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.jetbrains.compose.resources.StringResource

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

            GeneralSettings(state)
            SenderSettings(state)
            RecipientSettings(state)
            ProductSettings(state)
            CreateSettings(state)
            ChromeSettings(state)
            ThemeSettings(state)
        }
    }
}

/**
 * Section for General settings.
 */
@Composable
@Suppress("UNUSED_PARAMETER")
private fun GeneralSettings(state: SettingsSectionState) {
    val scope = rememberCoroutineScope()

    Section(
        title = Res.string.AppSettingsGeneral,
        info = Res.string.AppSettingsGeneralInfo,
    ) {
        val preferences = LocalAppState.current.preferences

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier
                .fillMaxWidth(),
        ) {
            // Application language.
            LanguageField(
                label = Res.string.AppSettingsGeneralLanguage,
                language = preferences.language.code,
                onSelect = { newLanguageCode ->
                    val language = Language.getByCode(newLanguageCode)
                        ?: return@LanguageField

                    preferences.setLanguage(language)
                    scope.launch {
                        preferences.save()
                    }
                },
                modifier = Modifier
                    .weight(0.5f, fill = true),
            )

            // Default country.
            CountryField(
                label = Res.string.AppSettingsGeneralCountry,
                country = preferences.country,
                onSelect = { newCountryCode ->
                    preferences.setCountry(newCountryCode)
                    scope.launch {
                        if (newCountryCode != null) {
                            val defaultTax = getCountryDefaultTax(newCountryCode)
                            if (defaultTax != null) {
                                preferences.setVatPercentage(defaultTax)
                            }

                            val defaultCurrency = getCountryDefaultCurrency(newCountryCode)
                            if (defaultCurrency != null) {
                                preferences.setCurrency(defaultCurrency)
                            }
                        }
                        preferences.save()
                    }
                },
                modifier = Modifier
                    .weight(0.5f, fill = true),
            )

            // Default VAT.
            DecimalField(
                label = Res.string.AppSettingsGeneralVat,
                value = preferences.vatPercentage,
                minPrecision = 0,
                maxPrecision = 1,
                onValueChange = { newVatPercentage ->
                    preferences.setVatPercentage(newVatPercentage ?: 0.toDouble())
                    scope.launch {
                        preferences.save()
                    }
                },
                modifier = Modifier
                    .weight(0.5f, fill = true),
            )

            // Default currency.
            CurrencyField(
                label = Res.string.AppSettingsGeneralCurrency,
                currency = preferences.currency,
                onSelect = { newCurrencyCode ->
                    preferences.setCurrency(newCurrencyCode)
                    scope.launch {
                        preferences.save()
                    }
                },
                modifier = Modifier
                    .weight(0.5f, fill = true),
            )
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
    val senders = LocalAppState.current.senders
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

    Section(
        title = Res.string.AppSettingsSender,
        info = Res.string.AppSettingsSenderInfo,
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
    ) {
        // Default sender items.
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
    val recipients = LocalAppState.current.recipients
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

    Section(
        title = Res.string.AppSettingsRecipient,
        info = Res.string.AppSettingsRecipientInfo,
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
    ) {
        // Default recipient items.
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
    val products = LocalAppState.current.products
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

    Section(
        title = Res.string.AppSettingsProduct,
        info = Res.string.AppSettingsProductInfo,
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
    ) {
        // Default product items.
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
private fun ThemeSettings(state: SettingsSectionState) =
    Section(
        title = Res.string.AppSettingsTheme,
    ) {
        // Theme selection (light and dark mode).
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier,
        ) {
            val scope = rememberCoroutineScope()
            val preferences = LocalAppState.current.preferences

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

/**
 * Section for invoice create settings.
 */
@Composable
@Suppress("UNUSED_PARAMETER")
private fun CreateSettings(state: SettingsSectionState) =
    Section(
        title = stringResource(Res.string.AppSettingsCreate).title(),
    ) {
        val scope = rememberCoroutineScope()
        val preferences = LocalAppState.current.preferences

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

            Column {
                Label(
                    text = Res.string.AppSettingsCreatePdfAutoConvert,
                )
                Text(
                    text = "(${stringResource(Res.string.AppSettingsCreatePdfAutoConvertExperimental)})",
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }

        AnimatedVisibility(visible = preferences.autoConvertToPdfA) {
            SectionInfo(
                text = Res.string.AppSettingsCreatePdfAutoConvertWarning,
            )
        }

        // Toggle for automatic removal of attachments.
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier,
        ) {
            Switch(
                checked = preferences.autoRemoveAttachments,
                onCheckedChange = {
                    preferences.setAutoRemoveAttachments(it)
                    scope.launch {
                        preferences.save()
                    }
                }
            )

            Column {
                Label(
                    text = Res.string.AppSettingsCreatePdfRemoveAttachments,
                )
                Text(
                    text = "(${stringResource(Res.string.AppSettingsCreatePdfRemoveAttachmentsExperimental)})",
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }

        AnimatedVisibility(visible = preferences.autoRemoveAttachments) {
            SectionInfo(
                text = Res.string.AppSettingsCreatePdfRemoveAttachmentsWarning,
            )
        }
    }

/**
 * Section for Chrome settings.
 */
@Composable
@Suppress("UNUSED_PARAMETER")
private fun ChromeSettings(state: SettingsSectionState) =
    Section(
        title = Res.string.AppSettingsChrome,
    ) {
        val scope = rememberCoroutineScope()
        val preferences = LocalAppState.current.preferences

        // Toggle for Chrome hardware acceleration.
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
                    text = Res.string.AppSettingsChromeAcceleration,
                )
                Text(
                    text = "(${stringResource(Res.string.AppSettingsChromeAccelerationRestart)})",
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
    }

/**
 * Reusable section component for the settings view.
 */
@Composable
@OptIn(ExperimentalFoundationApi::class)
private fun Section(
    title: String,
    info: String? = null,
    actions: @Composable RowScope.() -> Unit = {},
    content: @Composable ColumnScope.() -> Unit,
) = Column(
    verticalArrangement = Arrangement.spacedBy(12.dp),
    modifier = Modifier,
) {
    var infoVisible by remember { mutableStateOf(false) }

    SectionSubTitle(
        text = title,
        actions = {
            actions()

            if (info != null) {
                Tooltip(
                    text = Res.string.AppSettingsSectionInfo,
                ) {
                    IconButton(
                        onClick = { infoVisible = !infoVisible },
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = stringResource(Res.string.AppSettingsSectionInfo),
                        )
                    }
                }
            }
        },
    )

    if (info != null) {
        AnimatedVisibility(visible = infoVisible) {
            SectionInfo(
                text = info,
            )
        }
    }

    content()
}

/**
 * Reusable section component for the settings view.
 */
@Composable
private fun Section(
    title: StringResource,
    info: StringResource? = null,
    actions: @Composable RowScope.() -> Unit = {},
    content: @Composable ColumnScope.() -> Unit,
) = Section(
    title = stringResource(title).title(),
    info = if (info != null) stringResource(info) else null,
    actions = actions,
    content = content,
)