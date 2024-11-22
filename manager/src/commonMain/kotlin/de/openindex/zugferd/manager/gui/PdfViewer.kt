package de.openindex.zugferd.manager.gui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.vinceglb.filekit.core.PlatformFile

@Composable
expect fun PdfViewer(pdf: PlatformFile, modifier: Modifier = Modifier.fillMaxSize())
