package de.openindex.zugferd.manager.model

import kotlinx.serialization.Serializable

@Serializable
data class DirectDebit(
    val iban: String,
    val mandate: String,
)
