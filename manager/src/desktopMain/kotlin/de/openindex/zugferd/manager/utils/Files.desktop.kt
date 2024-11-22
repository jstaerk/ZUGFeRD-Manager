package de.openindex.zugferd.manager.utils

import io.github.vinceglb.filekit.core.PlatformDirectory
import io.github.vinceglb.filekit.core.PlatformFile
import java.io.IOException
import java.nio.file.Path
import kotlin.io.path.absolutePathString
import kotlin.io.path.createDirectories
import kotlin.io.path.exists
import kotlin.io.path.isDirectory

actual val PlatformFile.directory: PlatformDirectory?
    get() = if (file.parentFile != null) PlatformDirectory(file.parentFile) else null
