package de.openindex.zugferd.manager.model

import de.openindex.zugferd.manager.utils.trimToNull
import org.mustangproject.BankDetails as _BankDetails

fun BankDetails.build(): _BankDetails? {
    return _BankDetails()
        .setIBAN(iban.trim())
        .setBIC(bic?.trimToNull())
        .setAccountName(accountName?.trimToNull())
        .takeIf { it.iban.isNotBlank() }
}
