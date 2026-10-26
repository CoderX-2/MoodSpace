package com.moodspace.app.util

import android.util.Patterns
import androidx.annotation.StringRes
import com.moodspace.app.R

/** Cada validación devuelve el id del mensaje de error, o null si el valor es válido. */
object Validators {

    @StringRes
    fun name(value: String): Int? =
        if (value.isBlank()) R.string.error_name_required else null

    @StringRes
    fun email(value: String): Int? =
        if (!Patterns.EMAIL_ADDRESS.matcher(value.trim()).matches()) R.string.error_email_invalid else null

    @StringRes
    fun password(value: String): Int? =
        if (value.length < 6) R.string.error_password_short else null

    @StringRes
    fun taskTitle(value: String): Int? =
        if (value.isBlank()) R.string.error_task_title_required else null

    @StringRes
    fun habitName(value: String): Int? =
        if (value.isBlank()) R.string.error_habit_name_required else null
}
