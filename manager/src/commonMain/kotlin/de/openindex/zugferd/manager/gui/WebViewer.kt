package de.openindex.zugferd.manager.gui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
expect fun WebViewer(html: String, modifier: Modifier = Modifier.fillMaxSize())
