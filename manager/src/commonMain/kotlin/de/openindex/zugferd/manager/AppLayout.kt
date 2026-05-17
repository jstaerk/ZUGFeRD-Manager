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

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.VerticalDivider
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import de.openindex.zugferd.manager.gui.Label
import de.openindex.zugferd.manager.sections.CheckSectionState
import de.openindex.zugferd.manager.sections.VisualsSectionState
import de.openindex.zugferd.manager.utils.stringResource
import de.openindex.zugferd.quba.generated.resources.AppSidebarQuit
import de.openindex.zugferd.quba.generated.resources.Res
import de.openindex.zugferd.quba.generated.resources.ic_app_logo
import java.awt.KeyEventDispatcher
import java.awt.KeyboardFocusManager
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.painterResource

@Composable
fun AppLayout() {
    //val blur: Float by animateFloatAsState(
    //    targetValue = if (appState.locked) 10f else 0f,
    //    animationSpec = tween(
    //        easing = FastOutLinearInEasing,
    //        durationMillis = 250,
    //    ),
    //    label = "blur",
    //)

    // Single global AWT key dispatcher for Ctrl+F / ESC search handling.
    // Uses _APP_STATE.sectionSync (@Volatile) instead of _APP_STATE.section
    // (Compose snapshot state) to avoid stale reads on the AWT event thread.
    DisposableEffect(Unit) {
        val dispatcher = KeyEventDispatcher { awtEvent ->
            if (awtEvent.id == java.awt.event.KeyEvent.KEY_PRESSED
                && awtEvent.keyCode == java.awt.event.KeyEvent.VK_F
                && awtEvent.isControlDown
            ) {
                when (_APP_STATE.sectionSync) {
                    AppSection.VISUALISATION ->
                        (AppSection.VISUALISATION.state as? VisualsSectionState)?.isSearchOpen = true
                    AppSection.CHECK ->
                        (AppSection.CHECK.state as? CheckSectionState)?.isSearchOpen = true
                    else -> {}
                }
                true // consume — prevents JCEF from opening its own find bar
            } else if (awtEvent.id == java.awt.event.KeyEvent.KEY_PRESSED
                && awtEvent.keyCode == java.awt.event.KeyEvent.VK_ESCAPE
            ) {
                when (_APP_STATE.sectionSync) {
                    AppSection.VISUALISATION -> {
                        val state = AppSection.VISUALISATION.state as? VisualsSectionState
                        if (state?.isSearchOpen == true) { state.isSearchOpen = false; true } else false
                    }
                    AppSection.CHECK -> {
                        val state = AppSection.CHECK.state as? CheckSectionState
                        if (state?.isSearchOpen == true) { state.isSearchOpen = false; true } else false
                    }
                    else -> false
                }
            } else {
                false
            }
        }
        KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(dispatcher)
        onDispose {
            KeyboardFocusManager.getCurrentKeyboardFocusManager().removeKeyEventDispatcher(dispatcher)
        }
    }

    Row(
        verticalAlignment = Alignment.Top,
        modifier = Modifier.fillMaxSize(),
    ) {
        AppNavigation()
        VerticalDivider()
        AppContent()
    }
}

@Composable
private fun AppContent() {
    Surface(
        modifier = Modifier.fillMaxSize(),
    ) {
        LocalAppState.current.section.content()
    }


}

@Composable
private fun AppNavigation() {
    var shutdownRequested by remember { mutableStateOf(false) }

    NavigationRail(
        containerColor = MaterialTheme.colorScheme.primaryContainer,
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        //windowInsets = NavigationRailDefaults.windowInsets,
        //header = { Text(text = "Head") },
        modifier = Modifier,
    ) {
        Spacer(
            modifier = Modifier
                .height(4.dp),
        )

        //Text(text = "Item")
        AppSection.entries.forEach {
            AppSectionNavigationItem(
                section = it,
            )
        }

        Spacer(
            modifier = Modifier
                .weight(1f, fill = true),
        )

        Image(
            painter = painterResource(Res.drawable.ic_app_logo),
            contentDescription = APP_TITLE,
            modifier = Modifier
                .padding(vertical = 8.dp)
                .size(36.dp),
        )

        Text(
            text = "v${APP_VERSION_SHORT}",
            softWrap = false,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier
                .padding(all = 8.dp),
        )

        HorizontalDivider(
            modifier = Modifier
                .padding(vertical = 8.dp)
                .width(50.dp)
        )

        AppNavigationItem(
            label = Res.string.AppSidebarQuit,
            activeIcon = Icons.Default.Cancel,
            selected = false,
            onClick = {
                shutdownRequested = true
            },
        )
    }

    if (shutdownRequested) {
        LocalShutdownHandler.current?.shutdown()
    }
}

@Composable
private fun AppNavigationItem(
    label: StringResource,
    activeIcon: ImageVector,
    inactiveIcon: ImageVector = activeIcon,
    selected: Boolean,
    onClick: () -> Unit,
) {
    NavigationRailItem(
        selected = selected,
        onClick = onClick,
        icon = {
            Icon(
                imageVector = if (selected) activeIcon else inactiveIcon,
                contentDescription = stringResource(label),
            )
        },
        enabled = true,
        label = { Label(text = label) },
        alwaysShowLabel = true,
        colors = NavigationRailItemDefaults.colors(
            indicatorColor = MaterialTheme.colorScheme.secondaryContainer,
            selectedIconColor = MaterialTheme.colorScheme.onSecondaryContainer,
            selectedTextColor = MaterialTheme.colorScheme.onPrimaryContainer,
            unselectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
            unselectedTextColor = MaterialTheme.colorScheme.onPrimaryContainer,
        ),
        //interactionSource = null,
        modifier = Modifier,
    )
}


@Composable
private fun AppSectionNavigationItem(section: AppSection) {
    val appState = LocalAppState.current

    AppNavigationItem(
        label = section.label,
        activeIcon = section.activeIcon,
        inactiveIcon = section.inactiveIcon,
        selected = appState.isSection(section),
        onClick = { appState.setSection(section) },
    )
}
