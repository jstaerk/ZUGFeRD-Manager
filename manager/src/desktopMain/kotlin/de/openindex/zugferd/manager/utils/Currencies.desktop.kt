package de.openindex.zugferd.manager.utils

import java.util.Currency
import java.util.Locale

actual fun getCurrencyCodes(): List<String> {
    return Currency
        .getAvailableCurrencies()
        .map { it.currencyCode }
        .sorted()
}

actual fun getCurrencySymbol(currency: String): String? {
    return Currency.getInstance(currency)
        ?.getSymbol(Locale.getDefault())
        ?.takeIf { !it.equals(currency, true) }
}
