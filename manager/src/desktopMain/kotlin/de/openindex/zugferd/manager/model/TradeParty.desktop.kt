package de.openindex.zugferd.manager.model

import de.openindex.zugferd.manager.utils.trimToNull
import org.mustangproject.TradeParty as _TradeParty

fun TradeParty.build(): _TradeParty? {
    return _TradeParty()
        .setName(name.trim())
        .setZIP(zip?.trimToNull())
        .setStreet(street?.trimToNull())
        .setLocation(location?.trimToNull())
        .setCountry(country?.trimToNull())
        .setTaxID(taxID?.trimToNull())
        .setVATID(vatID?.trimToNull())
        .setID(id?.trimToNull())
        .setDescription(description?.trimToNull())
        .setAdditionalAddress(additionalAddress?.trimToNull())
        .setAdditionalAddressExtension(additionalAddressExtension?.trimToNull())
        .setContact(contact?.build())
        .let { party ->
            bankDetails
                .mapNotNull { it.build() }
                .forEach { bankDetail ->
                    bankDetail.setAccountName(
                        bankDetail.accountName?.trimToNull() ?: name.trim()
                    )
                    party.addBankDetails(bankDetail)
                }
            party
        }
        .let { party ->
            debitDetails
                .mapNotNull { it.build() }
                .forEach { party.addDebitDetails(it) }
            party
        }
        .takeIf { it.name.isNotBlank() }
}
