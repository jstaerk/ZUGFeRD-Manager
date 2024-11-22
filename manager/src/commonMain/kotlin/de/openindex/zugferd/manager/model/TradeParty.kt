package de.openindex.zugferd.manager.model

import de.openindex.zugferd.manager.utils.trimToNull
import kotlinx.serialization.Serializable

@Serializable
data class TradeParty(
    @Suppress("PropertyName")
    val _key: UInt = 0.toUInt(),
    @Suppress("PropertyName")
    val _defaultPaymentMethod: PaymentMethod = PaymentMethod.SEPA_CREDIT_TRANSFER,

    val name: String = "",
    val zip: String? = null,
    val street: String? = null,
    val location: String? = null,
    val country: String? = null,
    val id: String? = null,
    val taxID: String? = null,
    val vatID: String? = null,
    val creditorReferenceId: String? = null,
    val description: String? = null,
    val additionalAddress: String? = null,
    val additionalAddressExtension: String? = null,
    val bankDetails: List<BankDetails> = listOf(),
    val debitDetails: List<DirectDebit> = listOf(),
    val contact: Contact? = null,
    val legalOrg: LegalOrganisation? = null,
    val globalId: SchemedID? = null,
    val uriUniversalCommunicationId: SchemedID? = null,
) {
    val isSaved: Boolean
        get() = _key > 0.toUInt()

    val summary: String
        get() = buildList {
            add(name.trim().takeIf { it.isNotBlank() } ?: "???")

            val address = buildList {
                if (!zip.isNullOrBlank()) {
                    add(zip.trim())
                }
                if (!location.isNullOrBlank()) {
                    add(location.trim())
                }
            }.joinToString(" ")

            if (address.trim().isNotBlank()) {
                add(address)
            }

            if (id?.trimToNull() != null) {
                add("#${id.trim()}")
            }
        }.joinToString(" | ")
}
