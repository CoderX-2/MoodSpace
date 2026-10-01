package com.moodspace.app.data

import android.content.Context

class SessionManager(context: Context) {
    private val prefs = context.getSharedPreferences("moodspace_session", Context.MODE_PRIVATE)

    fun save(userId: Long) = prefs.edit().putLong("user_id", userId).apply()

    fun userId(): Long = prefs.getLong("user_id", -1L)

    fun isLoggedIn(): Boolean = userId() != -1L

    fun clear() = prefs.edit().clear().apply()
}
