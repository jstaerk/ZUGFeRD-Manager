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
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.ThumbDown
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import de.openindex.zugferd.manager.gui.PdfViewer
import de.openindex.zugferd.manager.gui.SectionSubTitle
import de.openindex.zugferd.manager.gui.SectionTitle
import de.openindex.zugferd.manager.gui.Tooltip
import de.openindex.zugferd.manager.gui.VerticalScrollBox
import de.openindex.zugferd.manager.gui.WebViewer
import de.openindex.zugferd.manager.utils.LocalPreferences
import de.openindex.zugferd.manager.utils.ValidationMessage
import de.openindex.zugferd.manager.utils.ValidationSeverity
import de.openindex.zugferd.manager.utils.ValidationType
import de.openindex.zugferd.manager.utils.XmlVisualTransformation
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun CheckSection(state: CheckSectionState) {
    val selectedPdf = state.selectedPdf

    if (selectedPdf == null) {
        EmptyView(state)
    } else {
        Column {

        }
        Row(
            modifier = Modifier
                .fillMaxSize(),
        ) {
            Column(
                modifier = Modifier
                    .weight(0.6f, fill = true),
            ) {
                VerticalScrollBox(
                    modifier = Modifier
                        .weight(1f, fill = true),
                ) {
                    CheckView(state)
                }

                /*
                AnimatedVisibility(visible = !selectedPdfIsArchive) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(color = MaterialTheme.colorScheme.secondaryContainer),
                    ) {
                        Text(
                            text = "Die gewählte Rechnung liegt nicht im PDF/A-1 oder PDF/A-3 Format vor. Damit kann keine E-Rechnung erzeugt werden.",
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            style = MaterialTheme.typography.bodySmall,
                            softWrap = true,
                            modifier = Modifier
                                .padding(16.dp)
                        )
                    }
                }
                */

                /*
                AnimatedVisibility(visible = !isValid && selectedPdfIsArchive) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(color = MaterialTheme.colorScheme.secondaryContainer),
                    ) {
                        Text(
                            text = "Die Angaben zur Rechnung sind unvollständig. Eine E-Rechnung kann erst erzeugt werden, wenn alle nötigen Angaben vorhanden sind.",
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            style = MaterialTheme.typography.bodySmall,
                            softWrap = true,
                            modifier = Modifier
                                .padding(16.dp)
                        )
                    }
                }
                */
            }

            Column(
                modifier = Modifier
                    .weight(0.4f, fill = true),
            ) {
                //PdfViewer(pdf = selectedPdf)
                DetailsView(state)
            }
        }
    }
}

@Composable
@OptIn(ExperimentalFoundationApi::class)
fun CheckSectionActions(state: CheckSectionState) {
    val scope = rememberCoroutineScope()
    val preferences = LocalPreferences.current

    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
            .padding(end = 8.dp),
    ) {
        Tooltip(
            text = "Eine E-Rechnung zur Prüfung auswählen."
        ) {
            TextButton(
                onClick = {
                    scope.launch(Dispatchers.IO) {
                        state.selectPdf(
                            preferences = preferences,
                        )
                    }
                },
                colors = ButtonDefaults.textButtonColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                )
            ) {
                Text(
                    text = "E-Rechnung wählen",
                    softWrap = false,
                )
            }
        }
    }
}

@Composable
@Suppress("UNUSED_PARAMETER")
private fun EmptyView(state: CheckSectionState) {
    Row(
        modifier = Modifier
            .fillMaxSize(),
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxSize(),
        )
        {
            Text(
                text = "Bitte wähle eine E-Rechnung aus.",
                softWrap = false,
            )
        }
    }
}

@Composable
private fun CheckView(state: CheckSectionState) {
    val selectedPdf = state.selectedPdf!!
    val validation = state.selectedPdfValidation

    Column(
        verticalArrangement = Arrangement.spacedBy(24.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp, horizontal = 20.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .fillMaxWidth(),
        ) {
            AnimatedVisibility(visible = validation != null) {
                Icon(
                    imageVector = if (validation?.isValid == true)
                        Icons.Default.ThumbUp
                    else
                        Icons.Default.ThumbDown,
                    contentDescription = if (validation?.isValid == true)
                        "Validierung erfolgreich"
                    else
                        "Validierung nicht erfolgreich",
                    modifier = Modifier
                        .size(32.dp)
                )
            }

            SectionTitle(
                text = if (validation == null)
                    "E-Rechnung „${selectedPdf.name}“ wird geprüft..."
                else if (validation.isValid)
                    "E-Rechnung „${selectedPdf.name}“ ist gültig"
                else
                    "E-Rechnung „${selectedPdf.name}“ ist ungültig",
                modifier = Modifier
                    .weight(1f, fill = true),
            )
        }

        if (validation == null) {
            CircularProgressIndicator()
        } else {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
            ) {

                SectionSubTitle(
                    text = "Zusammenfassung",
                )

                ValidationSummary(state)
            }


            if (validation.messages.isNotEmpty()) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                ) {

                    SectionSubTitle(
                        text = "Mitteilungen",
                    )

                    ValidationMessages(state)
                }
            }
        }
    }

    /*TextField(
        value = JSON_EXPORT.encodeToString(validation),
        readOnly = true,
        singleLine = false,
        onValueChange = {},
        modifier = Modifier.fillMaxSize(),
    )*/
}

@Composable
private fun DetailsView(state: CheckSectionState) {
    val preferences = LocalPreferences.current
    val selectedPdf = state.selectedPdf
    var tabState by remember { mutableStateOf(0) }
    val isPdfTab by derivedStateOf { tabState == 0 }
    val isHtmlTab by derivedStateOf { tabState == 1 && state.selectedPdfHtml != null }
    val isXmlTab by derivedStateOf { tabState == 2 && state.selectedPdfXml != null }

    val systemIsDark = isSystemInDarkTheme()
    val xmlVisualTransformation = remember(preferences.isThemeDark, systemIsDark) {
        XmlVisualTransformation(darkMode = preferences.darkMode ?: systemIsDark)
    }
    val xmlTextStyle = MaterialTheme.typography.bodyMedium.copy(
        fontFamily = FontFamily.Monospace,
    )

    //val webViewState = rememberWebViewStateWithHTMLData(
    //    data = state.selectedPdfHtml ?: "Hallo Welt"
    //)
    //
    //DisposableEffect(Unit) {
    //    webViewState.webSettings.apply {
    //        isJavaScriptEnabled = true
    //        desktopWebSettings.apply {
    //            offScreenRendering = true
    //            transparent = false
    //            disablePopupWindows = true
    //        }
    //    }
    //    onDispose { }
    //}

    TabRow(
        selectedTabIndex = tabState,
    ) {
        Tab(
            selected = isPdfTab,
            onClick = { tabState = 0 },
            text = {
                Text(
                    text = "Gewählte PDF",
                    softWrap = false,
                )
            },
        )

        if (state.selectedPdfHtml != null) {
            Tab(
                selected = isHtmlTab,
                onClick = { tabState = 1 },
                text = {
                    Text(
                        text = "XML-Visualisierung",
                        softWrap = false,
                    )
                },
            )
        }

        if (state.selectedPdfXml != null) {
            Tab(
                selected = isXmlTab,
                onClick = { tabState = 2 },
                text = {
                    Text(
                        text = "XML-Rohdaten",
                        softWrap = false,
                    )
                },
            )
        }
    }

    if (isPdfTab) {
        PdfViewer(
            pdf = selectedPdf!!,
            modifier = Modifier.fillMaxSize(),
        )
    }

    if (isHtmlTab) {
        WebViewer(
            html = state.selectedPdfHtml ?: "",
            modifier = Modifier.fillMaxSize(),
        )

        //WebView(
        //    state = webViewState,
        //    captureBackPresses = false,
        //    //modifier = Modifier.fillMaxSize(),
        //    modifier = Modifier.height(500.dp).height(500.dp),
        //)
    }

    if (isXmlTab) {
        TextField(
            value = state.selectedPdfXml ?: "",
            onValueChange = {},
            readOnly = true,
            singleLine = false,
            shape = RectangleShape,
            textStyle = xmlTextStyle,
            visualTransformation = xmlVisualTransformation,
            modifier = Modifier.fillMaxSize(),
        )
    }
}

@Composable
private fun ValidationSummary(state: CheckSectionState) {
    val validation = state.selectedPdfValidation!!

    Card(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .padding(vertical = 8.dp, horizontal = 16.dp)
                .fillMaxWidth(),
        ) {
            Text(
                text = buildAnnotatedString {
                    val bold = SpanStyle(fontWeight = FontWeight.Bold)
                    val unknown = SpanStyle(fontStyle = FontStyle.Normal)

                    withStyle(style = SpanStyle(fontSize = 0.9.em)) {
                        append("Profil\n")
                    }
                    withStyle(style = bold.takeIf { validation.profile != null } ?: unknown) {
                        append(
                            validation.profile?.split(":")?.joinToString("\n") ?: "unbekannt"
                        )
                        append("\n")
                    }

                    withStyle(style = SpanStyle(fontSize = 0.9.em)) {
                        append("Version\n")
                    }
                    withStyle(style = bold.takeIf { validation.version != null } ?: unknown) {
                        append(
                            validation.version ?: "unbekannt"
                        )
                        append("\n")
                    }

                    withStyle(style = SpanStyle(fontSize = 0.9.em)) {
                        append("Signatur\n")
                    }
                    withStyle(style = bold.takeIf { validation.signature != null } ?: unknown) {
                        append(
                            validation.signature ?: "unbekannt"
                        )
                    }
                },
                style = MaterialTheme.typography.bodyLarge,
            )

            Text(
                text = buildAnnotatedString {
                    withStyle(style = SpanStyle(fontSize = 0.9.em)) {
                        append("Mitteilungen\n")
                    }

                    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                        append(
                            validation.countErrors.toString()
                        )
                        append(
                            " Fehler\n"
                        )
                    }

                    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                        append(
                            validation.countWarnings.toString()
                        )
                        append(
                            " Warnungen\n"
                        )
                    }

                    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                        append(
                            validation.countNotices.toString()
                        )
                        append(
                            " Hinweise"
                        )
                    }
                },
                style = MaterialTheme.typography.bodyLarge,
                softWrap = false,
            )
        }
    }
}

@Composable
private fun ValidationMessages(state: CheckSectionState) {
    val validation = state.selectedPdfValidation!!

    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
            .fillMaxWidth(),
    ) {
        validation.messages.forEach {
            ValidationMessage(it)
        }
    }
}

@Composable
private fun ValidationMessage(message: ValidationMessage) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .padding(vertical = 8.dp, horizontal = 16.dp)
                .fillMaxWidth(),
        ) {
            Icon(
                imageVector = when (message.severity) {
                    ValidationSeverity.FATAL, ValidationSeverity.ERROR -> Icons.Default.Error
                    ValidationSeverity.WARNING -> Icons.Default.Warning
                    ValidationSeverity.NOTICE -> Icons.Default.Info
                },
                contentDescription = "",
                modifier = Modifier
                    .size(48.dp)
            )

            Text(
                text = buildAnnotatedString {
                    withStyle(style = SpanStyle(fontSize = 0.9.em)) {
                        append(
                            when (message.severity) {
                                ValidationSeverity.FATAL -> "Kritischer Fehler"
                                ValidationSeverity.ERROR -> "Fehler"
                                ValidationSeverity.WARNING -> "Warnung"
                                ValidationSeverity.NOTICE -> "Hinweis"
                            }
                        )

                        append(" ")

                        append(
                            when (message.type) {
                                ValidationType.XML -> "(XML)"
                                ValidationType.PDF -> "(PDF)"
                                else -> "(allgemein)"
                            }
                        )

                        append("\n")
                    }

                    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                        append(
                            message.message
                        )
                    }
                },
                style = MaterialTheme.typography.bodyLarge,
            )
        }
    }
}
