package de.openindex.zugferd.manager.utils

fun String.trimToNull(): String? =
    trim().takeIf { it.isNotBlank() }
