package de.openindex.zugferd.manager.utils

import io.github.vinceglb.filekit.core.PlatformDirectory
import io.github.vinceglb.filekit.core.PlatformFile

expect val PlatformFile.directory: PlatformDirectory?
