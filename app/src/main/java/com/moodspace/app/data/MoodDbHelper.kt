package com.moodspace.app.data

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteConstraintException
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class MoodDbHelper private constructor(context: Context) :
    SQLiteOpenHelper(context, "moodspace.db", null, DB_VERSION) {

    override fun onConfigure(db: SQLiteDatabase) {
        super.onConfigure(db)
        db.setForeignKeyConstraintsEnabled(true)
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """CREATE TABLE users (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT NOT NULL,
                email TEXT NOT NULL UNIQUE,
                password_hash TEXT NOT NULL,
                salt TEXT NOT NULL,
                favorite_activities TEXT NOT NULL DEFAULT '',
                reminders_enabled INTEGER NOT NULL DEFAULT 0
            )"""
        )
        db.execSQL(
            """CREATE TABLE mood_entries (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                user_id INTEGER NOT NULL,
                mood TEXT NOT NULL,
                note TEXT,
                created_at INTEGER NOT NULL,
                FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
            )"""
        )
        createTasksTable(db)
        createHabitTables(db)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // v1 -> v2 (sprint 2): se agrega la tabla de tareas sin tocar los datos existentes.
        if (oldVersion < 2) createTasksTable(db)
        // v2 -> v3 (sprint 3): se agregan hábitos y su registro diario.
        if (oldVersion < 3) createHabitTables(db)
    }

    private fun createTasksTable(db: SQLiteDatabase) {
        db.execSQL(
            """CREATE TABLE IF NOT EXISTS tasks (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                user_id INTEGER NOT NULL,
                title TEXT NOT NULL,
                description TEXT,
                due_date INTEGER,
                priority INTEGER NOT NULL DEFAULT 2,
                done INTEGER NOT NULL DEFAULT 0,
                created_at INTEGER NOT NULL,
                completed_at INTEGER,
                FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
            )"""
        )
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_tasks_user ON tasks(user_id)")
    }

    private fun createHabitTables(db: SQLiteDatabase) {
        db.execSQL(
            """CREATE TABLE IF NOT EXISTS habits (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                user_id INTEGER NOT NULL,
                name TEXT NOT NULL,
                description TEXT,
                created_at INTEGER NOT NULL,
                FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
            )"""
        )
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_habits_user ON habits(user_id)")
        // Un registro por hábito y día: evita duplicar el cumplimiento de un mismo día.
        db.execSQL(
            """CREATE TABLE IF NOT EXISTS habit_logs (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                habit_id INTEGER NOT NULL,
                day INTEGER NOT NULL,
                UNIQUE (habit_id, day),
                FOREIGN KEY (habit_id) REFERENCES habits(id) ON DELETE CASCADE
            )"""
        )
    }

    // ---- Usuarios ----

    /** Devuelve -1 si el correo ya existe. */
    fun insertUser(name: String, email: String, hash: String, salt: String): Long {
        val values = ContentValues().apply {
            put("name", name)
            put("email", email)
            put("password_hash", hash)
            put("salt", salt)
        }
        return writableDatabase.insert("users", null, values)
    }

    fun getCredentials(email: String): Credentials? {
        readableDatabase.rawQuery(
            "SELECT id, password_hash, salt FROM users WHERE email = ?", arrayOf(email)
        ).use { c ->
            return if (c.moveToFirst()) Credentials(c.getLong(0), c.getString(1), c.getString(2)) else null
        }
    }

    fun getUser(id: Long): User? {
        readableDatabase.rawQuery(
            "SELECT id, name, email, favorite_activities, reminders_enabled FROM users WHERE id = ?",
            arrayOf(id.toString())
        ).use { c ->
            return if (c.moveToFirst()) readUser(c) else null
        }
    }

    fun updateProfile(id: Long, name: String, favorites: String, reminders: Boolean): Boolean {
        val values = ContentValues().apply {
            put("name", name)
            put("favorite_activities", favorites)
            put("reminders_enabled", if (reminders) 1 else 0)
        }
        return try {
            writableDatabase.update("users", values, "id = ?", arrayOf(id.toString())) > 0
        } catch (e: SQLiteConstraintException) {
            false
        }
    }

    private fun readUser(c: Cursor) =
        User(c.getLong(0), c.getString(1), c.getString(2), c.getString(3) ?: "", c.getInt(4) == 1)

    // ---- Estados de ánimo ----

    fun insertMood(userId: Long, mood: String, note: String): Long {
        val values = ContentValues().apply {
            put("user_id", userId)
            put("mood", mood)
            put("note", note)
            put("created_at", System.currentTimeMillis())
        }
        return writableDatabase.insert("mood_entries", null, values)
    }

    fun getRecentMoods(userId: Long, limit: Int): List<MoodEntry> {
        val list = mutableListOf<MoodEntry>()
        readableDatabase.rawQuery(
            "SELECT id, mood, note, created_at FROM mood_entries " +
                "WHERE user_id = ? ORDER BY created_at DESC LIMIT ?",
            arrayOf(userId.toString(), limit.toString())
        ).use { c ->
            while (c.moveToNext()) {
                list.add(MoodEntry(c.getLong(0), c.getString(1), c.getString(2) ?: "", c.getLong(3)))
            }
        }
        return list
    }

    fun getMood(id: Long, userId: Long): MoodEntry? {
        readableDatabase.rawQuery(
            "SELECT id, mood, note, created_at FROM mood_entries WHERE id = ? AND user_id = ?",
            arrayOf(id.toString(), userId.toString())
        ).use { c ->
            return if (c.moveToFirst())
                MoodEntry(c.getLong(0), c.getString(1), c.getString(2) ?: "", c.getLong(3))
            else null
        }
    }

    // ---- Tareas (HU06 a HU09) ----

    fun insertTask(userId: Long, title: String, description: String, dueDate: Long?, priority: Int): Long {
        val values = ContentValues().apply {
            put("user_id", userId)
            put("title", title)
            put("description", description)
            if (dueDate != null) put("due_date", dueDate) else putNull("due_date")
            put("priority", priority)
            put("done", 0)
            put("created_at", System.currentTimeMillis())
        }
        return writableDatabase.insert("tasks", null, values)
    }

    fun updateTask(
        id: Long, userId: Long, title: String, description: String, dueDate: Long?, priority: Int
    ): Boolean {
        val values = ContentValues().apply {
            put("title", title)
            put("description", description)
            if (dueDate != null) put("due_date", dueDate) else putNull("due_date")
            put("priority", priority)
        }
        return writableDatabase.update(
            "tasks", values, "id = ? AND user_id = ?", arrayOf(id.toString(), userId.toString())
        ) > 0
    }

    fun setTaskDone(id: Long, userId: Long, done: Boolean): Boolean {
        val values = ContentValues().apply {
            put("done", if (done) 1 else 0)
            if (done) put("completed_at", System.currentTimeMillis()) else putNull("completed_at")
        }
        return writableDatabase.update(
            "tasks", values, "id = ? AND user_id = ?", arrayOf(id.toString(), userId.toString())
        ) > 0
    }

    fun getTask(id: Long, userId: Long): Task? {
        readableDatabase.rawQuery(
            "$TASK_COLUMNS FROM tasks WHERE id = ? AND user_id = ?",
            arrayOf(id.toString(), userId.toString())
        ).use { c ->
            return if (c.moveToFirst()) readTask(c) else null
        }
    }

    /**
     * Agenda del usuario (HU09): pendientes primero; dentro de cada grupo se ordena por
     * prioridad y luego por fecha, o al revés, según [order]. Las tareas sin fecha van al final.
     */
    fun getTasks(userId: Long, order: TaskOrder): List<Task> {
        val orderBy = when (order) {
            TaskOrder.PRIORITY ->
                "done ASC, priority ASC, (due_date IS NULL) ASC, due_date ASC, created_at DESC"
            TaskOrder.DATE ->
                "done ASC, (due_date IS NULL) ASC, due_date ASC, priority ASC, created_at DESC"
        }
        val list = mutableListOf<Task>()
        readableDatabase.rawQuery(
            "$TASK_COLUMNS FROM tasks WHERE user_id = ? ORDER BY $orderBy",
            arrayOf(userId.toString())
        ).use { c ->
            while (c.moveToNext()) list.add(readTask(c))
        }
        return list
    }

    // ---- Hábitos (HU10 y HU11) ----

    /** Fila de un hábito sin su cumplimiento; el repositorio calcula racha y estado de hoy. */
    data class HabitRow(val id: Long, val name: String, val description: String, val createdAt: Long)

    fun insertHabit(userId: Long, name: String, description: String): Long {
        val values = ContentValues().apply {
            put("user_id", userId)
            put("name", name)
            put("description", description)
            put("created_at", System.currentTimeMillis())
        }
        return writableDatabase.insert("habits", null, values)
    }

    fun getHabitRows(userId: Long): List<HabitRow> {
        val list = mutableListOf<HabitRow>()
        readableDatabase.rawQuery(
            "SELECT id, name, description, created_at FROM habits WHERE user_id = ? ORDER BY created_at ASC",
            arrayOf(userId.toString())
        ).use { c ->
            while (c.moveToNext()) {
                list.add(HabitRow(c.getLong(0), c.getString(1), c.getString(2) ?: "", c.getLong(3)))
            }
        }
        return list
    }

    /** Días con cumplimiento de todos los hábitos del usuario: id del hábito -> días (inicio del día). */
    fun getHabitLogDays(userId: Long): Map<Long, Set<Long>> {
        val map = mutableMapOf<Long, MutableSet<Long>>()
        readableDatabase.rawQuery(
            "SELECT l.habit_id, l.day FROM habit_logs l " +
                "JOIN habits h ON h.id = l.habit_id WHERE h.user_id = ?",
            arrayOf(userId.toString())
        ).use { c ->
            while (c.moveToNext()) map.getOrPut(c.getLong(0)) { mutableSetOf() }.add(c.getLong(1))
        }
        return map
    }

    /** Marca o desmarca el cumplimiento de [day]. Devuelve false si el hábito no es del usuario. */
    fun setHabitDone(habitId: Long, userId: Long, day: Long, done: Boolean): Boolean {
        readableDatabase.rawQuery(
            "SELECT 1 FROM habits WHERE id = ? AND user_id = ?",
            arrayOf(habitId.toString(), userId.toString())
        ).use { c -> if (!c.moveToFirst()) return false }

        val db = writableDatabase
        if (done) {
            val values = ContentValues().apply {
                put("habit_id", habitId)
                put("day", day)
            }
            db.insertWithOnConflict("habit_logs", null, values, SQLiteDatabase.CONFLICT_IGNORE)
        } else {
            db.delete("habit_logs", "habit_id = ? AND day = ?", arrayOf(habitId.toString(), day.toString()))
        }
        return true
    }

    private fun readTask(c: Cursor) = Task(
        id = c.getLong(0),
        title = c.getString(1),
        description = c.getString(2) ?: "",
        dueDate = if (c.isNull(3)) null else c.getLong(3),
        priority = c.getInt(4),
        done = c.getInt(5) == 1,
        createdAt = c.getLong(6),
        completedAt = if (c.isNull(7)) null else c.getLong(7)
    )

    companion object {
        private const val DB_VERSION = 3
        private const val TASK_COLUMNS =
            "SELECT id, title, description, due_date, priority, done, created_at, completed_at"

        @Volatile
        private var instance: MoodDbHelper? = null

        fun getInstance(context: Context): MoodDbHelper =
            instance ?: synchronized(this) {
                instance ?: MoodDbHelper(context.applicationContext).also { instance = it }
            }
    }
}
