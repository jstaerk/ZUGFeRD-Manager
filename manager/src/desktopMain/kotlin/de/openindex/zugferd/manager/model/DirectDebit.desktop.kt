package de.openindex.zugferd.manager.model

import de.openindex.zugferd.manager.utils.trimToNull
import org.mustangproject.DirectDebit as _DirectDebit

fun DirectDebit.build(): _DirectDebit? {
    return _DirectDebit(
        iban.trim(),
        mandate.trimToNull() ?: "",
    ).takeIf { iban.isNotBlank() }
}
