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

/**
 * https://github.com/ZUGFeRD/mustangproject/issues/463
 */
enum class TaxCategoryCode(
    val code: String,
    val description: String,
    val defaultPercentage: Double,
    val defaultExemptionReason: String? = null,
) {
    /**
     * Umsatzsteuer fällt mit Normalsatz an.
     */
    NORMAL_TAX(
        code = "S",
        defaultPercentage = 19.0,
        description = "normale Besteuerung",
    ),

    /**
     * Nach dem Nullsatz zu versteuernde Waren.
     */
    ZERO_RATED(
        code = "Z",
        defaultPercentage = 0.0,
        description = "keine Besteuerung",
        defaultExemptionReason = "Ware ist nach dem Nullsatz besteuert",
    ),

    /**
     * Steuerbefreit.
     */
    SMALL_BUSINESS(
        code = "E",
        defaultPercentage = 0.0,
        description = "Kleinunternehmer / steuerbefreit",
        defaultExemptionReason = "Kleinunternehmer gemäß §19 UStG",
    ),

    /**
     * Umkehrung der Steuerschuldnerschaft.
     */
    REVERSE_TAX(
        code = "AE",
        defaultPercentage = 0.0,
        description = "Umkehrung der Steuerschuldnerschaft",
    ),

    /**
     * Kein Ausweis der Umsatzsteuer bei innergemeinschaftlichen Lieferungen.
     */
    INTRA_COMMUNITY_SUPPLY(
        code = "K",
        defaultPercentage = 0.0,
        description = "innergemeinschaftliche Transaktion",
        defaultExemptionReason = "Lieferung ins EU-Ausland",
    ),

    /**
     * Steuer nicht erhoben aufgrund von Export außerhalb der EU.
     */
    EXPORT_OUT_OF_COMMUNITY(
        code = "G",
        defaultPercentage = 0.0,
        description = "Export außerhalb der EU",
        defaultExemptionReason = "Export ins Nicht-EU-Ausland",
    ),

    /**
     * Außerhalb des Steueranwendungsbereichs.
     */
    UNTAXED_SERVICE(
        code = "O",
        defaultPercentage = 0.0,
        description = "Unversteuerte Leistung",
        defaultExemptionReason = "Leistung außerhalb des Steueranwendungsbereichs",
    ),

    ;

    companion object {
        fun getByCode(code: String?): TaxCategoryCode? {
            return TaxCategoryCode.entries.firstOrNull { it.code == code }
        }
    }
}
