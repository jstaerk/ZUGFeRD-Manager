package de.openindex.zugferd.manager.utils

fun getCurrencies(): Map<String, String> {
    return buildMap {
        getCurrencyCodes()
            .sortedBy { it.lowercase() }
            .forEach {
                val symbol = getCurrencySymbol(it)
                put(it, if (symbol != null) "$symbol (${it})" else it)
            }
    }
}

expect fun getCurrencyCodes(): List<String>

expect fun getCurrencySymbol(currency: String): String?
