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
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import de.openindex.zugferd.zugferd_manager.generated.resources.AppSidebarQuit
import de.openindex.zugferd.zugferd_manager.generated.resources.Res
import de.openindex.zugferd.zugferd_manager.generated.resources.application
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun AppLayout(
) {
    val appState = LocalAppState.current
    //val blur: Float by animateFloatAsState(
    //    targetValue = if (appState.locked) 10f else 0f,
    //    animationSpec = tween(
    //        easing = FastOutLinearInEasing,
    //        durationMillis = 250,
    //    ),
    //    label = "blur",
    //)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier,
                    ) {
                        Image(
                            painter = painterResource(Res.drawable.application),
                            contentDescription = APP_TITLE,
                            modifier = Modifier
                                .padding(start = 4.dp, end = 38.dp)
                                .width(40.dp)
                        )

                        Text(
                            text = APP_TITLE,
                            style = MaterialTheme.typography.headlineMedium,
                        )
                    }
                },
                actions = {
                    appState.section.actions()
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                ),
                modifier = Modifier,
            )
        },
        //bottomBar = {},
        //snackbarHost = {},
        //floatingActionButton = {},
        //floatingActionButtonPosition = FabPosition.End,
        containerColor = MaterialTheme.colorScheme.background,
        contentColor = contentColorFor(MaterialTheme.colorScheme.background),
        //contentWindowInsets = ScaffoldDefaults.contentWindowInsets,
        //modifier = Modifier,
        //modifier = appState.lockedModifier,
        modifier = Modifier
        //.blur(blur.dp),
    ) { innerPadding ->
        Row(
            verticalAlignment = Alignment.Top,
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
        ) {
            AppNavigation()
            AppContent()
        }
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
            AppSectionNavigationItem(section = it)
        }

        Spacer(
            modifier = Modifier
                .weight(1f, fill = true),
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
            label = stringResource(Res.string.AppSidebarQuit),
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
    label: String,
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
                contentDescription = label,
            )
        },
        enabled = true,
        label = { Text(text = label) },
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
        label = stringResource(section.label),
        activeIcon = section.activeIcon,
        inactiveIcon = section.inactiveIcon,
        selected = appState.isSection(section),
        onClick = { appState.setSection(section) },
    )
}
