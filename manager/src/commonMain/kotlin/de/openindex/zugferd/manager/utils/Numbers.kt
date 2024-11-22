package de.openindex.zugferd.manager.utils

expect val Number.formatAsPercentage: String

expect val Number.formatAsPrice: String

expect val Number.formatAsQuantity: String

private fun parseDouble(value: String): Double? =
    value
        .trimToNull()
        ?.replace(" ", "")
        ?.replace(',', '.')
        ?.let { txt ->
            return@let if (txt.endsWith(".")) {
                txt.plus("0")
            } else {
                txt
            }
        }
        ?.toDoubleOrNull()


fun parsePercentage(value: String): Double? = parseDouble(value)

fun parsePrice(value: String): Double? = parseDouble(value)

fun parseQuantity(value: String): Double? = parseDouble(value)
