package de.openindex.zugferd.manager.utils

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json

@OptIn(ExperimentalSerializationApi::class)
val JSON_EXPORT = Json {
    encodeDefaults = true
    prettyPrint = true
    prettyPrintIndent = "  "
}

@OptIn(ExperimentalSerializationApi::class)
val JSON_IMPORT = Json {
    isLenient = true
    ignoreUnknownKeys = true
    allowTrailingComma = true
}
