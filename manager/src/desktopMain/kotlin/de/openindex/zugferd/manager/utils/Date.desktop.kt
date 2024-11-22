package de.openindex.zugferd.manager.utils

import kotlinx.datetime.LocalDate
import java.util.Calendar
import java.util.Date
import java.util.TimeZone

fun LocalDate.toJavaDate(): Date =
    Calendar
        .getInstance(TimeZone.getDefault())
        .apply {
            set(this@toJavaDate.year, this@toJavaDate.monthNumber - 1, this@toJavaDate.dayOfMonth)
        }
        .time
