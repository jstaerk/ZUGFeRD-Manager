package de.openindex.zugferd.manager.model

import kotlinx.serialization.Serializable

@Serializable
data class BankDetails(
    val iban: String = "",
    val bic: String? = null,
    val accountName: String? = null,
    val directDebitMandateId: String? = null,
)
