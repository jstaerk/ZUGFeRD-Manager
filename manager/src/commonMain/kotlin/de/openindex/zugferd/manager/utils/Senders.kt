package de.openindex.zugferd.manager.utils

import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.mutableStateOf
import de.openindex.zugferd.manager.model.TradeParty
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking

class Senders(data: List<TradeParty>) {
    private val _senders = mutableStateOf(data)
    val senders get() = _senders.value.sortedBy { it.name.lowercase() }

    private val nextKey: UInt
        get() = if (_senders.value.isNotEmpty()) {
            _senders.value.maxOf { it._key } + 1.toUInt()
        } else {
            1.toUInt()
        }

    fun put(sender: TradeParty, preferences: Preferences? = null) {
        _senders.value = if (sender._key == 0.toUInt()) {
            val newSender = sender.copy(_key = nextKey)
            preferences?.setPreviousSenderKey(newSender._key)

            _senders.value
                .plus(newSender)
        } else {
            preferences?.setPreviousSenderKey(sender._key)

            _senders.value
                .filter { it._key != sender._key }
                .plus(sender)
        }
    }

    fun remove(sender: TradeParty) {
        remove(sender._key)
    }

    @Suppress("MemberVisibilityCanBePrivate")
    fun remove(key: UInt) {
        _senders.value = _senders.value
            .filter { it._key != key }
    }

    suspend fun save() {
        saveSendersData(
            data = senders,
        )
    }
}

val LocalSenders = compositionLocalOf { loadSenders() }

fun loadSenders(): Senders =
    runBlocking(Dispatchers.IO) {
        Senders(loadSendersData())
    }

expect suspend fun loadSendersData(): List<TradeParty>

expect suspend fun saveSendersData(data: List<TradeParty>)
