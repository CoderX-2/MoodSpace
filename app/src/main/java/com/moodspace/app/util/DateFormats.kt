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
