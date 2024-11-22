package de.openindex.zugferd.manager.utils

import de.openindex.zugferd.manager.APP_LOGGER
import de.openindex.zugferd.manager.model.TradeParty
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.decodeFromStream
import kotlinx.serialization.json.encodeToStream
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.copyTo
import kotlin.io.path.createParentDirectories
import kotlin.io.path.deleteIfExists
import kotlin.io.path.deleteRecursively
import kotlin.io.path.exists
import kotlin.io.path.inputStream
import kotlin.io.path.isRegularFile
import kotlin.io.path.moveTo
import kotlin.io.path.outputStream
import kotlin.io.path.reader

private val SENDERS_FILE: Path by lazy {
    DATA_DIR.resolve("senders.json")
}

@OptIn(ExperimentalPathApi::class, ExperimentalSerializationApi::class)
actual suspend fun loadSendersData(): List<TradeParty> {
    return withContext(Dispatchers.IO) {
        if (!SENDERS_FILE.exists()) {
            return@withContext listOf()
        }

        if (!SENDERS_FILE.isRegularFile()) {
            APP_LOGGER.warn("Senders are invalid.")
            SENDERS_FILE.deleteRecursively()
            return@withContext listOf()
        }

        try {
            SENDERS_FILE
                .inputStream()
                .use { JSON_IMPORT.decodeFromStream(it) }
        } catch (e: Exception) {
            APP_LOGGER.warn("Senders are not readable.", e)

            val backupFile = BACKUPS_DIR
                .resolve("unreadable")
                .resolve("senders.${System.currentTimeMillis()}.json")
                .createParentDirectories()

            SENDERS_FILE.moveTo(backupFile, true)

            listOf()
        }
    }
}

@OptIn(ExperimentalSerializationApi::class)
actual suspend fun saveSendersData(data: List<TradeParty>) {
    withContext(Dispatchers.IO) {
        val tempFile = if (SENDERS_FILE.isRegularFile()) {
            SENDERS_FILE.copyTo(
                target = Files.createTempFile("senders-", ".json"),
                overwrite = true,
            )
        } else {
            null
        }

        try {
            SENDERS_FILE
                .outputStream()
                .use { JSON_EXPORT.encodeToStream(data, it) }
        } catch (e: Exception) {
            APP_LOGGER.warn("Senders are not writable.", e)
            tempFile?.copyTo(SENDERS_FILE)
        } finally {
            tempFile?.deleteIfExists()
        }
    }
}
