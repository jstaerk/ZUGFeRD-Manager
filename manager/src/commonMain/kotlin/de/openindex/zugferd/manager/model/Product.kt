/*
 * Copyright (c) 2024-2025 Andreas Rudolph <andy@openindex.de>.
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package de.openindex.zugferd.manager.model

import de.openindex.zugferd.manager.utils.formatAsPrice
import de.openindex.zugferd.manager.utils.getCurrencySymbol
import kotlinx.serialization.Serializable

@Serializable
data class Product(
    @Suppress("PropertyName")
    val _key: UInt? = null,
    @Suppress("PropertyName")
    val _defaultPricePerUnit: Double = 0.0,

    val name: String = "",
    val description: String? = null,
    val unit: String = UnitOfMeasurement.UNIT.code,
    val vatPercent: Double = TaxCategoryCode.NORMAL_TAX.defaultPercentage,
    val taxExemptionReason: String? = null,
    val taxCategoryCode: String = TaxCategoryCode.NORMAL_TAX.code,
) {
    val isSaved: Boolean
        get() = _key != null

    val summary: String
        get() = buildList {
            add(name.trim().takeIf { it.isNotBlank() } ?: "???")

            add(
                buildString {
                    if (_defaultPricePerUnit > 0) {
                        append(_defaultPricePerUnit.formatAsPrice)
                        append(" ")
                        append(getCurrencySymbol(DEFAULT_CURRENCY) ?: DEFAULT_CURRENCY)
                        append(" ")
                    }
                    append("pro ")
                    append(UnitOfMeasurement.getByCode(unit)?.description ?: unit)
                }
            )

            add(TaxCategoryCode.getByCode(taxCategoryCode)?.description ?: taxCategoryCode)
        }.joinToString(" | ")
}
