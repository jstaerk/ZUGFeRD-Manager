package de.openindex.zugferd.manager.model

import kotlinx.datetime.Clock
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
data class Item(
    @Transient
    val _uid: Long = Clock.System.now().toEpochMilliseconds(),

    val product: Product? = null,
    val quantity: Double = 1.0,
    val price: Double = 0.0,
    val notes: String? = null,
) {
    val totalNetPrice: Double
        get() = quantity * price

    val totalGrossPrice: Double
        get() = totalNetPrice + tax

    val tax: Double
        get() = ((product?.vatPercent ?: 0.0) / 100.0) * totalNetPrice
}
