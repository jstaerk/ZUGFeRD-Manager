package de.openindex.zugferd.manager.model

import kotlinx.serialization.Serializable

@Serializable
data class Contact(
    val name: String = "",
    val phone: String? = null,
    val email: String? = null,
    val fax: String? = null,
    val zip: String? = null,
    val street: String? = null,
    val location: String? = null,
    val country: String? = null,
)