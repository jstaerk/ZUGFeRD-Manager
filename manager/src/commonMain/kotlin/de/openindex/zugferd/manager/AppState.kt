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

package de.openindex.zugferd.manager

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import de.openindex.zugferd.manager.sections.CheckSection
import de.openindex.zugferd.manager.sections.CheckSectionActions
import de.openindex.zugferd.manager.sections.CheckSectionState
import de.openindex.zugferd.manager.sections.CreateSection
import de.openindex.zugferd.manager.sections.CreateSectionActions
import de.openindex.zugferd.manager.sections.CreateSectionState
import de.openindex.zugferd.manager.sections.SettingsSection
import de.openindex.zugferd.manager.sections.SettingsSectionState
import de.openindex.zugferd.manager.sections.ViewSection
import de.openindex.zugferd.manager.sections.ViewSectionState
import de.openindex.zugferd.manager.sections.ViewSectionActions
import de.openindex.zugferd.manager.utils.SectionState

class AppState {
    private var _section = mutableStateOf(AppSection.CREATE)
    val section get() = _section.value
    fun isSection(section: AppSection): Boolean {
        return _section.value == section
    }

    fun setSection(section: AppSection) {
        _section.value = section
    }

    private var _locked = mutableStateOf(false)
    val locked get() = _locked.value
    val lockedModifier
        get() = if (_locked.value)
            Modifier
                .blur(10.dp)
        else
            Modifier

    fun isLocked(): Boolean {
        return _locked.value
    }

    fun setLocked(locked: Boolean) {
        _locked.value = locked
    }
}

enum class AppSection(
    private val state: SectionState
) {
    VIEW(ViewSectionState()),
    CREATE(CreateSectionState()),
    CHECK(CheckSectionState()),
    SETTINGS(SettingsSectionState());

    val label: String
        get() = when (this) {
            VIEW -> "Ansehen"
            CREATE -> "Erzeugen"
            CHECK -> "Prüfen"
            SETTINGS -> "Optionen"
        }

    val activeIcon: ImageVector
        get() = when (this) {
            VIEW -> Icons.Default.EditNote
            CREATE -> Icons.Default.EditNote
            CHECK -> Icons.Default.Search
            SETTINGS -> Icons.Default.Settings
        }

    val inactiveIcon: ImageVector
        get() = when (this) {
            VIEW -> Icons.Default.EditNote
            CREATE -> Icons.Default.EditNote
            CHECK -> Icons.Default.Search
            SETTINGS -> Icons.Default.Settings
        }

    @Composable
    fun content() {
        when (this) {
            VIEW -> ViewSection(state = state as ViewSectionState)
            CREATE -> CreateSection(state = state as CreateSectionState)
            CHECK -> CheckSection(state = state as CheckSectionState)
            SETTINGS -> SettingsSection(state = state as SettingsSectionState)
        }
    }

    @Composable
    fun actions() {
        when (this) {
            VIEW -> ViewSectionActions(state = state as ViewSectionState)
            CREATE -> CreateSectionActions(state = state as CreateSectionState)
            CHECK -> CheckSectionActions(state = state as CheckSectionState)
            SETTINGS -> {}
        }
    }
}

val LocalAppState = compositionLocalOf { AppState() }
