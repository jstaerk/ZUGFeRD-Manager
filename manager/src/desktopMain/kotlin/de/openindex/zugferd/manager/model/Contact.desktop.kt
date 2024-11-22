package de.openindex.zugferd.manager.model

import de.openindex.zugferd.manager.utils.trimToNull
import org.mustangproject.Contact as _Contact

fun Contact.build(): _Contact? {
    return _Contact()
        .setName(name.trim())
        .setPhone(phone?.trimToNull())
        .setEMail(email?.trimToNull())
        .setFax(fax?.trimToNull())
        .setZIP(zip?.trimToNull())
        .setStreet(street?.trimToNull())
        .setLocation(location?.trimToNull())
        .setCountry(country?.trimToNull())
        .takeIf { it.name.isNotBlank() }
}
