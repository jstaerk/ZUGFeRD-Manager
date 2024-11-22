package de.openindex.zugferd.manager.utils

import java.text.NumberFormat
import java.util.Locale

private val PERCENTAGE_FORMAT by lazy {
    NumberFormat
        .getInstance(Locale.getDefault())
        .apply {
            isGroupingUsed = false
            maximumFractionDigits = 1
            minimumFractionDigits = 1
        }
}

private val PRICE_FORMAT by lazy {
    NumberFormat
        .getInstance(Locale.getDefault())
        .apply {
            isGroupingUsed = false
            maximumFractionDigits = 2
            minimumFractionDigits = 2
        }
}

private val QUANTITY_FORMAT by lazy {
    NumberFormat
        .getInstance(Locale.getDefault())
        .apply {
            isGroupingUsed = false
            maximumFractionDigits = 2
            minimumFractionDigits = 1
        }
}

actual val Number.formatAsPercentage: String
    get() = PERCENTAGE_FORMAT.format(this)

actual val Number.formatAsPrice: String
    get() = PRICE_FORMAT.format(this)

actual val Number.formatAsQuantity: String
    get() = QUANTITY_FORMAT.format(this)
