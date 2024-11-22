package de.openindex.zugferd.manager.model

import de.openindex.zugferd.manager.utils.trimToNull
import java.math.RoundingMode
import org.mustangproject.Product as _Product

fun Product.build(): _Product? {
    return _Product()
        .setName(name.trim())
        .setDescription(description?.trimToNull() ?: "")
        .setUnit(unit.trimToNull() ?: UnitOfMeasurement.UNIT.code)
        .setVATPercent(vatPercent.toBigDecimal().setScale(1, RoundingMode.HALF_EVEN))
        .setTaxExemptionReason(taxExemptionReason?.trimToNull())
        .setTaxCategoryCode(taxCategoryCode.trimToNull() ?: TaxCategoryCode.NORMAL_TAX.code)
        //.let { product ->
        //    if (product.taxCategoryCode.equals(TaxCategoryCode.INTRA_COMMUNITY_SUPPLY.code, true)) {
        //        product.setIntraCommunitySupply()
        //    }
        //    product
        //}
        .takeIf { it.name.isNotBlank() && it.unit.isNotBlank() }
}
