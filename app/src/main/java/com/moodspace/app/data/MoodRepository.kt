package com.moodspace.app.data

import com.moodspace.app.util.DateFormats

class MoodRepository(private val db: MoodDbHelper) {

    fun add(userId: Long, mood: String, note: String): Long = db.insertMood(userId, mood, note)

    fun recent(userId: Long, limit: Int = RECENT_LIMIT): List<MoodEntry> =
        db.getRecentMoods(userId, limit)

    fun get(id: Long, userId: Long): MoodEntry? = db.getMood(id, userId)

    /** Último estado registrado hoy, o null si hoy aún no ha registrado ninguno. */
    fun today(userId: Long): MoodEntry? =
        recent(userId, 1).firstOrNull()?.takeIf { it.createdAt >= DateFormats.startOfToday() }

    companion object {
        const val RECENT_LIMIT = 20
    }
}
