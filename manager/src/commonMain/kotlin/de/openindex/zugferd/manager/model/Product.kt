package de.openindex.zugferd.manager.model

import de.openindex.zugferd.manager.utils.formatAsPrice
import de.openindex.zugferd.manager.utils.getCurrencySymbol
import kotlinx.serialization.Serializable

@Serializable
data class Product(
    @Suppress("PropertyName")
    val _key: UInt = 0.toUInt(),
    @Suppress("PropertyName")
    val _defaultPricePerUnit: Double = 0.0,

    val name: String = "",
    val description: String? = null,
    val unit: String = UnitOfMeasurement.UNIT.code,
    val vatPercent: Double = TaxCategoryCode.NORMAL_TAX.defaultPercentage,
    val taxExemptionReason: String? = null,
    val taxCategoryCode: String = TaxCategoryCode.NORMAL_TAX.code,
) {
    val isSaved: Boolean
        get() = _key > 0.toUInt()

    val summary: String
        get() = buildList {
            add(name.trim().takeIf { it.isNotBlank() } ?: "???")

            add(
                buildString {
                    if (_defaultPricePerUnit > 0) {
                        append(_defaultPricePerUnit.formatAsPrice)
                        append(" ")
                        append(getCurrencySymbol(DEFAULT_CURRENCY) ?: DEFAULT_CURRENCY)
                        append(" ")
                    }
                    append("pro ")
                    append(UnitOfMeasurement.getByCode(unit)?.description ?: unit)
                }
            )

            add(TaxCategoryCode.getByCode(taxCategoryCode)?.description ?: taxCategoryCode)
        }.joinToString(" | ")
}
