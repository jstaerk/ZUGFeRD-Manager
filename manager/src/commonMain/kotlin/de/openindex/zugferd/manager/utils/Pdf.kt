package de.openindex.zugferd.manager.utils

import io.github.vinceglb.filekit.core.PlatformFile

expect suspend fun isPdfArchive(pdfFile: PlatformFile): Boolean
