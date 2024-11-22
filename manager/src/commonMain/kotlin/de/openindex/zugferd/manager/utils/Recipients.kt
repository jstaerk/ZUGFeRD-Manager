package de.openindex.zugferd.manager.utils

import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.mutableStateOf
import de.openindex.zugferd.manager.model.TradeParty
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking

class Recipients(data: List<TradeParty>) {
    private val _recipients = mutableStateOf(data)
    val recipients get() = _recipients.value.sortedBy { it.name.lowercase() }

    private val nextKey: UInt
        get() = if (_recipients.value.isNotEmpty()) {
            _recipients.value.maxOf { it._key } + 1.toUInt()
        } else {
            1.toUInt()
        }

    fun put(recipient: TradeParty) {
        _recipients.value = if (recipient._key == 0.toUInt()) {
            _recipients.value
                .plus(recipient.copy(_key = nextKey))
        } else {
            _recipients.value
                .filter { it._key != recipient._key }
                .plus(recipient)
        }
    }

    fun remove(recipient: TradeParty) {
        remove(recipient._key)
    }

    @Suppress("MemberVisibilityCanBePrivate")
    fun remove(key: UInt) {
        _recipients.value = _recipients.value
            .filter { it._key != key }
    }

    suspend fun save() {
        saveRecipientsData(
            data = recipients,
        )
    }
}

val LocalRecipients = compositionLocalOf { loadRecipients() }

fun loadRecipients(): Recipients =
    runBlocking(Dispatchers.IO) {
        Recipients(loadRecipientsData())
    }

expect suspend fun loadRecipientsData(): List<TradeParty>

expect suspend fun saveRecipientsData(data: List<TradeParty>)
