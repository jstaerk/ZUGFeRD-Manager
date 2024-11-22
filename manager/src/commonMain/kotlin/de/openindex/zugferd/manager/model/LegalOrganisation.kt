package de.openindex.zugferd.manager.model

import kotlinx.serialization.Serializable

@Serializable
data class LegalOrganisation(
    val schemedID: SchemedID? = null,
    val tradingBusinessName: String? = null,
)
