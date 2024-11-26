package de.openindex.zugferd.manager.utils

import de.openindex.zugferd.manager.APP_LOGGER
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.decodeFromStream
import kotlinx.serialization.json.encodeToStream
import java.nio.file.Path
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.deleteIfExists
import kotlin.io.path.deleteRecursively
import kotlin.io.path.exists
import kotlin.io.path.inputStream
import kotlin.io.path.isRegularFile
import kotlin.io.path.outputStream

private val PREFERENCES_FILE: Path by lazy {
    APP_WORK_DIR.resolve("preferences.json")
}

@OptIn(ExperimentalPathApi::class, ExperimentalSerializationApi::class)
actual suspend fun loadPreferencesData(): PreferencesData {
    return withContext(Dispatchers.IO) {
        if (!PREFERENCES_FILE.exists()) {
            return@withContext PreferencesData()
        }

        if (!PREFERENCES_FILE.isRegularFile()) {
            APP_LOGGER.warn("Settings are invalid.")
            PREFERENCES_FILE.deleteRecursively()
            return@withContext PreferencesData()
        }

        try {
            PREFERENCES_FILE
                .inputStream()
                .use { JSON_IMPORT.decodeFromStream(it) }

        } catch (e: Exception) {
            APP_LOGGER.warn("Settings are not readable.", e)
            PREFERENCES_FILE.deleteIfExists()
            PreferencesData()
        }
    }
}

@OptIn(ExperimentalSerializationApi::class)
actual suspend fun savePreferencesData(data: PreferencesData) {
    withContext(Dispatchers.IO) {
        try {
            PREFERENCES_FILE
                .outputStream()
                .use { JSON_EXPORT.encodeToStream(data, it) }
        } catch (e: Exception) {
            APP_LOGGER.warn("Settings are not writable.", e)
            PREFERENCES_FILE.deleteIfExists()
        }
    }
}
