package com.moodspace.app.data

class MoodRepository(private val db: MoodDbHelper) {

    fun add(userId: Long, mood: String, note: String): Long = db.insertMood(userId, mood, note)

    fun recent(userId: Long, limit: Int = RECENT_LIMIT): List<MoodEntry> =
        db.getRecentMoods(userId, limit)

    fun get(id: Long, userId: Long): MoodEntry? = db.getMood(id, userId)

    companion object {
        const val RECENT_LIMIT = 20
    }
}
