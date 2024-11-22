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
    /** A unit of count defining the number of articles (items).  */
    //ARTICLE("NAR", "Anzahl Artikel"),

    /** A unit of count defining the number of pieces  */
    UNIT("C62", "Einheit", "Einheiten"),

    /** A number of objects grouped together to a set  */
    //SET("SET", "Anzahl Sets"),

    /** The number of pairs.  */
    //PAIR("NPR", "Anzahl Paare"),

    /** The Hectare (ha).  */
    //HECTARE("HAR", "Hectare (ha)"),

    /** The Hour (h).  */
    HOUR("HUR", "Stunde", "Stunden"),

    /** The Kilogram (kg).  */
    //KILOGRAM("KGM", "Kilogram (kg)"),

    /** The Kilometer (km).  */
    //KILOMETER("KMT", "Kilometer (km)"),

    /** The Kilowatt hour (kWh).  */
    //KILOWATT_HOUR("KWH", "Kilowatt hour (kWh)"),

    /** The lump sum.  */
    //LUMP_SUM("LS", "lump sum"),

    /** The Liter (l).  */
    //LITRE("LTR", "Liter (l)"),

    /** The Minute (min).  */
    MINUTE("MIN", "Minute", "Minuten"),

    /** The square millimeter (mm2).  */
    //MILLIMETER_SQUARE("MMK", "square millimeter (mm2)"),

    /** The Millimeter (mm).  */
    //MILLIMETER("MMT", "Millimeter (mm)"),

    /** The square meter (m2).  */
    //METER_SQUARE("MTK", "square meter  (m2)"),

    /** The cubic meter (m3).  */
    //METER_CUBIC("MTQ", "cubic meter (m3)"),

    /** The Meter (m).  */
    //METER("MTR", "Meter (m)"),

    /** The Second (s).  */
    //SECOND("SEC", "Second (s)"),

    /** The Percent (%).  */
    //PERCENT("P1", "Percent (%)"),

    /** The metric ton (t).  */
    //TON_METRIC("TNE", "Metric ton (t)"),

    /** The day count  */
    DAY("DAY", "Tag", "Tage"),

    /** The week.  */
    //WEEK("WEE", "Wochen"),

    /** The Month.  */
    //MONTH("MON", "Monate"),

    /** The Mile. 1609,344 m  */
    //MILE("SMI", "International Mile"),

    ;

    companion object {
        fun getByCode(code: String?): UnitOfMeasurement? {
            return entries.firstOrNull { it.code == code }
        }
    }
}