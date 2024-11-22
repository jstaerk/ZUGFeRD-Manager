package de.openindex.zugferd.manager.model

/**
 * https://github.com/ZUGFeRD/mustangproject/issues/463
 */
enum class TaxCategoryCode(
    val code: String,
    val description: String,
    val defaultPercentage: Double,
    val defaultExemptionReason: String? = null,
) {
    /**
     * Umsatzsteuer fällt mit Normalsatz an.
     */
    NORMAL_TAX(
        code = "S",
        defaultPercentage = 19.0,
        description = "normale Besteuerung",
    ),

    /**
     * Nach dem Nullsatz zu versteuernde Waren.
     */
    ZERO_RATED(
        code = "Z",
        defaultPercentage = 0.0,
        description = "keine Besteuerung",
        defaultExemptionReason = "Ware ist nach dem Nullsatz besteuert",
    ),

    /**
     * Steuerbefreit.
     */
    SMALL_BUSINESS(
        code = "E",
        defaultPercentage = 0.0,
        description = "Kleinunternehmer / steuerbefreit",
        defaultExemptionReason = "Kleinunternehmer gemäß §19 UStG",
    ),

    /**
     * Umkehrung der Steuerschuldnerschaft.
     */
    REVERSE_TAX(
        code = "AE",
        defaultPercentage = 0.0,
        description = "Umkehrung der Steuerschuldnerschaft",
    ),

    /**
     * Kein Ausweis der Umsatzsteuer bei innergemeinschaftlichen Lieferungen.
     */
    INTRA_COMMUNITY_SUPPLY(
        code = "K",
        defaultPercentage = 0.0,
        description = "innergemeinschaftliche Transaktion",
        defaultExemptionReason = "Lieferung ins EU-Ausland",
    ),

    /**
     * Steuer nicht erhoben aufgrund von Export außerhalb der EU.
     */
    EXPORT_OUT_OF_COMMUNITY(
        code = "G",
        defaultPercentage = 0.0,
        description = "Export außerhalb der EU",
        defaultExemptionReason = "Export ins Nicht-EU-Ausland",
    ),

    /**
     * Außerhalb des Steueranwendungsbereichs.
     */
    UNTAXED_SERVICE(
        code = "O",
        defaultPercentage = 0.0,
        description = "Unversteuerte Leistung",
        defaultExemptionReason = "Leistung außerhalb des Steueranwendungsbereichs",
    ),

    ;

    companion object {
        fun getByCode(code: String?): TaxCategoryCode? {
            return TaxCategoryCode.entries.firstOrNull { it.code == code }
        }
    }
}
