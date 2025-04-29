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
import androidx.compose.ui.graphics.vector.ImageVector
import de.openindex.zugferd.manager.sections.CheckSection
import de.openindex.zugferd.manager.sections.CheckSectionActions
import de.openindex.zugferd.manager.sections.CheckSectionState
import de.openindex.zugferd.manager.sections.CreateSection
import de.openindex.zugferd.manager.sections.CreateSectionActions
import de.openindex.zugferd.manager.sections.CreateSectionState
import de.openindex.zugferd.manager.sections.SettingsSection
import de.openindex.zugferd.manager.sections.SettingsSectionState
import de.openindex.zugferd.manager.utils.FALLBACK_CURRENCY
import de.openindex.zugferd.manager.utils.Preferences
import de.openindex.zugferd.manager.utils.Products
import de.openindex.zugferd.manager.utils.Recipients
import de.openindex.zugferd.manager.utils.SectionState
import de.openindex.zugferd.manager.utils.Senders
import de.openindex.zugferd.manager.utils.getCountryDefaultCurrency
import de.openindex.zugferd.manager.utils.getCountryDefaultTax
import de.openindex.zugferd.manager.utils.getSystemCountryCode
import de.openindex.zugferd.manager.utils.initPreferences
import de.openindex.zugferd.manager.utils.loadPreferences
import de.openindex.zugferd.manager.utils.loadProducts
import de.openindex.zugferd.manager.utils.loadRecipients
import de.openindex.zugferd.manager.utils.loadSenders
import de.openindex.zugferd.manager.utils.setCurrentLanguage
import de.openindex.zugferd.quba.generated.resources.AppSidebarCheck
import de.openindex.zugferd.quba.generated.resources.AppSidebarCreate
import de.openindex.zugferd.quba.generated.resources.AppSidebarSettings
import de.openindex.zugferd.quba.generated.resources.Res
import kotlinx.coroutines.runBlocking
import org.jetbrains.compose.resources.StringResource

class AppState(
    val preferences: Preferences,
    val senders: Senders,
    val recipients: Recipients,
    val products: Products,
) {
    //
    // Section
    //

    private var _section = mutableStateOf(AppSection.CREATE)
    val section get() = _section.value

    fun isSection(section: AppSection): Boolean {
        return _section.value == section
    }

    fun setSection(section: AppSection) {
        _section.value = section
    }


    //
    // Locking
    //
    //
    //private var _locked = mutableStateOf(false)
    //val locked get() = _locked.value
    //val lockedModifier
    //    get() = if (_locked.value)
    //        Modifier
    //            .blur(10.dp)
    //    else
    //        Modifier
    //fun isLocked(): Boolean {
    //    return _locked.value
    //}
    //fun setLocked(locked: Boolean) {
    //    _locked.value = locked
    //}
}

enum class AppSection(
    private val state: SectionState
) {
    CREATE(CreateSectionState()),
    CHECK(CheckSectionState()),
    SETTINGS(SettingsSectionState());

    val label: StringResource
        get() = when (this) {
            CREATE -> Res.string.AppSidebarCreate
            CHECK -> Res.string.AppSidebarCheck
            SETTINGS -> Res.string.AppSidebarSettings
        }

    val activeIcon: ImageVector
        get() = when (this) {
            CREATE -> Icons.Default.EditNote
            CHECK -> Icons.Default.Search
            SETTINGS -> Icons.Default.Settings
        }

    val inactiveIcon: ImageVector
        get() = when (this) {
            CREATE -> Icons.Default.EditNote
            CHECK -> Icons.Default.Search
            SETTINGS -> Icons.Default.Settings
        }

    @Composable
    fun content() {
        when (this) {
            CREATE -> CreateSection(state = state as CreateSectionState)
            CHECK -> CheckSection(state = state as CheckSectionState)
            SETTINGS -> SettingsSection(state = state as SettingsSectionState)
        }
    }

    @Composable
    fun actions() {
        when (this) {
            CREATE -> CreateSectionActions(state = state as CreateSectionState)
            CHECK -> CheckSectionActions(state = state as CheckSectionState)
            SETTINGS -> {}
        }
    }
}

@Suppress("ObjectPropertyName")
val _APP_STATE: AppState by lazy {
    runBlocking {
        // Load system country before preferences are loaded.
        // Otherwise, the default locale might get overwritten.
        val systemCountry = getSystemCountryCode()

        val preferences = loadPreferences()

        // Register currency of system country,
        // if not already configured in preferences.
        if (preferences.currency == null) {
            preferences.setCurrency(
                getCountryDefaultCurrency(systemCountry)
                    ?: FALLBACK_CURRENCY
            )
        }

        // Register tax rate of system country,
        // if not already configured in preferences.
        if (preferences.vatPercentage == null) {
            preferences.setVatPercentage(
                getCountryDefaultTax(systemCountry)
            )
        }

        // Enforce system language according to application settings.
        setCurrentLanguage(
            language = preferences.language,
            country = preferences.country,
        )

        // Do further system initialization based on preferences.
        initPreferences(preferences)

        // Finally create the AppState instance.
        AppState(
            preferences = preferences,
            senders = loadSenders(),
            recipients = loadRecipients(),
            products = loadProducts(),
        )
    }
}

val LocalAppState = compositionLocalOf { _APP_STATE }
