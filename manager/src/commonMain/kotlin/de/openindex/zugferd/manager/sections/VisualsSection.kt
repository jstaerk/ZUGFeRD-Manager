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
import androidx.compose.foundation.draganddrop.dragAndDropTarget
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.CheckBoxOutlineBlank
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.ThumbDown
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import de.openindex.zugferd.manager.LocalAppState
import de.openindex.zugferd.manager.gui.ActionButtonWithTooltip
import de.openindex.zugferd.manager.gui.CountryField
import de.openindex.zugferd.manager.gui.CurrencyField
import de.openindex.zugferd.manager.gui.DecimalField
import de.openindex.zugferd.manager.gui.Label
import de.openindex.zugferd.manager.gui.LanguageField
import de.openindex.zugferd.manager.gui.PdfViewer
import de.openindex.zugferd.manager.gui.SectionInfo
import de.openindex.zugferd.manager.gui.SectionSubTitle
import de.openindex.zugferd.manager.gui.SectionTitle
import de.openindex.zugferd.manager.gui.Tooltip
import de.openindex.zugferd.manager.gui.VerticalScrollBox
import de.openindex.zugferd.manager.gui.WebViewer
import de.openindex.zugferd.manager.gui.XmlViewer
import de.openindex.zugferd.manager.model.ValidationSeverity
import de.openindex.zugferd.manager.model.ValidationType
import de.openindex.zugferd.manager.utils.Language
import de.openindex.zugferd.manager.utils.ValidationMessage
import de.openindex.zugferd.manager.utils.createDragAndDropTarget
import de.openindex.zugferd.manager.utils.getCountryDefaultCurrency
import de.openindex.zugferd.manager.utils.getCountryDefaultTax
import de.openindex.zugferd.manager.utils.pluralStringResource
import de.openindex.zugferd.manager.utils.stringResource
import de.openindex.zugferd.manager.utils.title
import de.openindex.zugferd.zugferd_manager.generated.resources.AppCheck
import de.openindex.zugferd.zugferd_manager.generated.resources.AppCheckDetailsHtml
import de.openindex.zugferd.zugferd_manager.generated.resources.AppCheckDetailsPdf
import de.openindex.zugferd.zugferd_manager.generated.resources.AppCheckDetailsXml
import de.openindex.zugferd.zugferd_manager.generated.resources.AppCheckFailed
import de.openindex.zugferd.zugferd_manager.generated.resources.AppCheckMessageMessageCopy
import de.openindex.zugferd.zugferd_manager.generated.resources.AppCheckMessages
import de.openindex.zugferd.zugferd_manager.generated.resources.AppCheckMessagesFilter
import de.openindex.zugferd.zugferd_manager.generated.resources.AppCheckMessagesFilterSeverity
import de.openindex.zugferd.zugferd_manager.generated.resources.AppCheckMessagesFilterType
import de.openindex.zugferd.zugferd_manager.generated.resources.AppCheckPassed
import de.openindex.zugferd.zugferd_manager.generated.resources.AppCheckSelect
import de.openindex.zugferd.zugferd_manager.generated.resources.AppCheckSelectInfo
import de.openindex.zugferd.zugferd_manager.generated.resources.AppCheckSelectMessage
import de.openindex.zugferd.zugferd_manager.generated.resources.AppCheckSummary
import de.openindex.zugferd.zugferd_manager.generated.resources.AppCheckSummaryErrors
import de.openindex.zugferd.zugferd_manager.generated.resources.AppCheckSummaryExport
import de.openindex.zugferd.zugferd_manager.generated.resources.AppCheckSummaryMessages
import de.openindex.zugferd.zugferd_manager.generated.resources.AppCheckSummaryNotices
import de.openindex.zugferd.zugferd_manager.generated.resources.AppCheckSummaryProfile
import de.openindex.zugferd.zugferd_manager.generated.resources.AppCheckSummarySignature
import de.openindex.zugferd.zugferd_manager.generated.resources.AppCheckSummaryUnknown
import de.openindex.zugferd.zugferd_manager.generated.resources.AppCheckSummaryVersion
import de.openindex.zugferd.zugferd_manager.generated.resources.AppCheckSummaryWarnings
import de.openindex.zugferd.zugferd_manager.generated.resources.AppSettingsGeneral
import de.openindex.zugferd.zugferd_manager.generated.resources.AppSettingsGeneralCountry
import de.openindex.zugferd.zugferd_manager.generated.resources.AppSettingsGeneralCurrency
import de.openindex.zugferd.zugferd_manager.generated.resources.AppSettingsGeneralInfo
import de.openindex.zugferd.zugferd_manager.generated.resources.AppSettingsGeneralLanguage
import de.openindex.zugferd.zugferd_manager.generated.resources.AppSettingsGeneralVat
import de.openindex.zugferd.zugferd_manager.generated.resources.AppSettingsSectionInfo
import de.openindex.zugferd.zugferd_manager.generated.resources.Res
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.StringResource

/**
 * Main view of the check section.
 */
@Composable
fun VisualsSection(state: VisualsSectionState) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(modifier = Modifier.fillMaxWidth()) {
            Button(onClick = { state.selectedText = "Mustertext 1" }) {
                Text("Knopf 1")
            }
            Spacer(modifier = Modifier.width(8.dp))
            Button(onClick = { state.selectedText = "Mustertext 2" }) {
                Text("Knopf 2")
            }
            Spacer(modifier = Modifier.width(8.dp))
            Button(onClick = { state.selectedText = "Mustertext 3" }) {
                Text("Knopf 3")
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = state.selectedText,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}
