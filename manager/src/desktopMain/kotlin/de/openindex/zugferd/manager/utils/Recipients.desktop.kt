package de.openindex.zugferd.manager.utils

import de.openindex.zugferd.manager.APP_LOGGER
import de.openindex.zugferd.manager.model.TradeParty
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.decodeFromStream
import kotlinx.serialization.json.encodeToStream
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

private val RECIPIENTS_FILE: Path by lazy {
    DATA_DIR.resolve("recipients.json")
}

@OptIn(ExperimentalPathApi::class, ExperimentalSerializationApi::class)
actual suspend fun loadRecipientsData(): List<TradeParty> {
    return withContext(Dispatchers.IO) {
        if (!RECIPIENTS_FILE.exists()) {
            return@withContext listOf()
        }

        if (!RECIPIENTS_FILE.isRegularFile()) {
            APP_LOGGER.warn("Recipients are invalid.")
            RECIPIENTS_FILE.deleteRecursively()
            return@withContext listOf()
        }

        try {
            RECIPIENTS_FILE
                .inputStream()
                .use { JSON_IMPORT.decodeFromStream(it) }
        } catch (e: Exception) {
            APP_LOGGER.warn("Recipients are not readable.", e)

            val backupFile = BACKUPS_DIR
                .resolve("unreadable")
                .resolve("recipients.${System.currentTimeMillis()}.json")
                .createParentDirectories()

            RECIPIENTS_FILE.moveTo(backupFile, true)

            listOf()
        }
    }
}

@OptIn(ExperimentalSerializationApi::class)
actual suspend fun saveRecipientsData(data: List<TradeParty>) {
    withContext(Dispatchers.IO) {
        val tempFile = if (RECIPIENTS_FILE.isRegularFile()) {
            RECIPIENTS_FILE.copyTo(
                target = Files.createTempFile("recipients-", ".json"),
                overwrite = true,
            )
        } else {
            null
        }

        try {
            RECIPIENTS_FILE
                .outputStream()
                .use { JSON_EXPORT.encodeToStream(data, it) }
        } catch (e: Exception) {
            APP_LOGGER.warn("Recipients are not writable.", e)
            tempFile?.copyTo(RECIPIENTS_FILE)
        } finally {
            tempFile?.deleteIfExists()
        }
    }
}
