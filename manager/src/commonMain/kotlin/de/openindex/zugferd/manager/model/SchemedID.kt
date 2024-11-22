package de.openindex.zugferd.manager.model

import kotlinx.serialization.Serializable

@Serializable
data class SchemedID(
    val scheme: String,
    val id: String,
)
