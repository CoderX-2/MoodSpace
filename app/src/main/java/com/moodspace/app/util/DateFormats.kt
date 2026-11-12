package com.moodspace.app.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object DateFormats {
    private val locale = Locale("es", "MX")

    fun dateTime(millis: Long): String =
        SimpleDateFormat("dd/MM/yyyy HH:mm", locale).format(Date(millis))

    fun date(millis: Long): String =
        SimpleDateFormat("dd/MM/yyyy", locale).format(Date(millis))

    fun timeOnly(millis: Long): String =
        SimpleDateFormat("HH:mm", locale).format(Date(millis))

    fun dayNameAndDate(millis: Long): String =
        SimpleDateFormat("EEEE d 'de' MMMM", locale).format(Date(millis)).replaceFirstChar { it.uppercase() }

    fun weekRangeText(startOfWeekMillis: Long): String {
        val startCal = Calendar.getInstance(locale).apply {
            timeInMillis = startOfWeekMillis
        }
        val endCal = Calendar.getInstance(locale).apply {
            timeInMillis = startOfWeekMillis
            add(Calendar.DAY_OF_MONTH, 6)
        }
        val startFmt = SimpleDateFormat("d 'de' MMM", locale).format(startCal.time)
        val endFmt = SimpleDateFormat("d 'de' MMM, yyyy", locale).format(endCal.time)
        return "$startFmt - $endFmt"
    }

    fun startOfWeek(millis: Long = System.currentTimeMillis()): Long {
        val cal = Calendar.getInstance(locale).apply {
            timeInMillis = millis
            firstDayOfWeek = Calendar.MONDAY
            set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return cal.timeInMillis
    }

    /** Inicio (00:00, hora local) del día indicado. [month] va de 0 a 11, como en Calendar. */
    fun startOfDay(year: Int, month: Int, day: Int): Long =
        Calendar.getInstance().apply {
            clear()
            set(year, month, day)
        }.timeInMillis

    fun startOfToday(): Long {
        val now = Calendar.getInstance()
        return startOfDay(now.get(Calendar.YEAR), now.get(Calendar.MONTH), now.get(Calendar.DAY_OF_MONTH))
    }

    /** Inicio del día anterior al indicado (hora local). */
    fun previousDay(startOfDay: Long): Long =
        Calendar.getInstance().apply {
            timeInMillis = startOfDay
            add(Calendar.DAY_OF_MONTH, -1)
        }.timeInMillis
}
