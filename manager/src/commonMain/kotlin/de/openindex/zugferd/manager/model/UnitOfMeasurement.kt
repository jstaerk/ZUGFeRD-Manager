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
 * The Unit Of Measurement Enumeration
 *
 * Based on Recommendation N°. 20 - Codes for Units of Measure Used in International Trade
 *
 * https://www.xrepository.de/details/urn:xoev-de:kosit:codeliste:rec20_3
 */
enum class UnitOfMeasurement(
    val code: String,
    val description: String,
    val pluralDescription: String,
) {
    /** The lump sum. */
    LUMP_SUM("LS", "Pauschale", "Pauschalen"),

    /** The Percent (%). */
    PERCENT("P1", "Prozent", "Prozent"),


    /** piece */
    PIECE("H87", "Stück (Anzahl)", "Stücke (Anzahl)"),

    /** A unit of count defining the number of pieces */
    UNIT("C62", "Einheit (Anzahl)", "Einheiten (Anzahl)"),

    /** A unit of count defining the number of articles (items). */
    ARTICLE("NAR", "Artikel (Anzahl)", "Artikel (Anzahl)"),

    /** A number of objects grouped together to a set */
    SET("SET", "Set", "Sets"),

    /** The number of pairs. */
    PAIR("NPR", "Paar", "Paare"),


    /** The Year. */
    YEAR("ANN", "Jahr", "Jahre"),

    /** The Month. */
    MONTH("MON", "Monat", "Monate"),

    /** The week. */
    WEEK("WEE", "Woche", "Wochen"),

    /** The day count */
    DAY("DAY", "Tag", "Tage"),

    /** The Hour (h). */
    HOUR("HUR", "Stunde", "Stunden"),

    /** The Minute (min). */
    MINUTE("MIN", "Minute", "Minuten"),

    /** The Second (s). */
    SECOND("SEC", "Sekunde", "Sekunden"),


    /** The Hectare (ha). */
    HECTARE("HAR", "Hektar", "Hektar"),

    /** The square meter (m2). */
    METER_SQUARE("MTK", "Quadratmeter", "Quadratmeter"),

    /** The square millimeter (mm2). */
    MILLIMETER_SQUARE("MMK", "Quadratmillimeter", "Quadratmillimeter"),


    /** The Mile. 1609,344 m */
    MILE("SMI", "Meile", "Meilen"),

    /** The Kilometer (km). */
    KILOMETER("KMT", "Kilometer", "Kilometer"),

    /** The Meter (m). */
    METER("MTR", "Meter", "Meter"),

    /** The Millimeter (mm). */
    MILLIMETER("MMT", "Millimeter", "Millimeter"),


    /** The cubic meter (m3). */
    METER_CUBIC("MTQ", "Kubikmeter", "Kubikmeter"),

    /** The Liter (l). */
    LITRE("LTR", "Liter", "Liter"),


    /** The metric ton (t). */
    TON_METRIC("TNE", "Tonne (metrisch)", "Tonnen (metrisch)"),

    /** The Kilogram (kg). */
    KILOGRAM("KGM", "Kilogramm", "Kilogramm"),


    /** The Kilowatt hour (kWh). */
    KILOWATT_HOUR("KWH", "Kilowattstunde", "Kilowattstunden"),

    ;

    companion object {
        fun getByCode(code: String?): UnitOfMeasurement? {
            return entries.firstOrNull { it.code == code }
        }
    }
}