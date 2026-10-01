package com.moodspace.app.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object DateFormats {
    fun dateTime(millis: Long): String =
        SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("es", "MX")).format(Date(millis))
}
