package de.openindex.zugferd.manager.utils

import java.util.Locale

actual fun getCountryCodes(): List<String> {
    return Locale
        .getISOCountries()
        .asList()
        .sorted()
}

actual fun getCountryName(code: String): String {
    return Locale
        .of("", code)
        .getDisplayCountry(Locale.getDefault())
}

actual fun getDefaultCountryCode(): String =
    Locale.getDefault().country