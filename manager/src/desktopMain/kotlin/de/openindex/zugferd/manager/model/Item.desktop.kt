package de.openindex.zugferd.manager.model

import de.openindex.zugferd.manager.utils.trimToNull
import java.math.RoundingMode
import org.mustangproject.Item as _Item

fun Item.build(): _Item? {
    return _Item()
        .setProduct(product?.build())
        .setQuantity(quantity.toBigDecimal().setScale(2, RoundingMode.HALF_EVEN))
        .setPrice(price.toBigDecimal().setScale(2, RoundingMode.HALF_EVEN))
        .let { item ->
            val n = notes?.trimToNull()
            if (n != null) {
                item.addNote(notes)
            }

            item
        }
        .takeIf { it.product != null }
}
