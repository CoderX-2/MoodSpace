package com.moodspace.app.data

import com.moodspace.app.util.DateFormats

class HabitRepository(private val db: MoodDbHelper) {

    /** HU10 – Devuelve el id del nuevo hábito, o -1 si no se pudo guardar. */
    fun add(userId: Long, name: String, description: String): Long =
        db.insertHabit(userId, name.trim(), description.trim())

    /** Hábitos del usuario con su estado de hoy y su racha (HU11). */
    fun list(userId: Long): List<Habit> {
        val logs = db.getHabitLogDays(userId)
        val today = DateFormats.startOfToday()
        return db.getHabitRows(userId).map { row ->
            val days = logs[row.id] ?: emptySet()
            Habit(row.id, row.name, row.description, row.createdAt, today in days, streak(days, today))
        }
    }

    /** HU11 – Registra (o quita) el cumplimiento de hoy. */
    fun setDoneToday(habitId: Long, userId: Long, done: Boolean): Boolean =
        db.setHabitDone(habitId, userId, DateFormats.startOfToday(), done)

    companion object {
        /**
         * Días consecutivos cumplidos. Si hoy todavía no se cumple, la racha sigue viva
         * y se cuenta hasta ayer; se pierde hasta que pase un día completo sin cumplir.
         */
        fun streak(days: Set<Long>, today: Long): Int {
            var cursor = if (today in days) today else DateFormats.previousDay(today)
            var count = 0
            while (cursor in days) {
                count++
                cursor = DateFormats.previousDay(cursor)
            }
            return count
        }
    }
}
