package de.openindex.zugferd.manager.utils

expect fun getCountryCodes(): List<String>

expect fun getCountryName(code: String): String

expect fun getDefaultCountryCode(): String

fun getCountries(): Map<String, String> {
    return mapOf(
        *getCountryCodes().map { code ->
            Pair(code, getCountryName(code))
        }.toTypedArray()
    )
}