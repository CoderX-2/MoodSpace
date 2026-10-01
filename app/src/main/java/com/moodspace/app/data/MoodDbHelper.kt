package com.moodspace.app.data

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteConstraintException
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class MoodDbHelper private constructor(context: Context) :
    SQLiteOpenHelper(context, "moodspace.db", null, 1) {

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
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // Sin migraciones: el esquema actual corresponde a la versión 1.
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

    companion object {
        @Volatile
        private var instance: MoodDbHelper? = null

        fun getInstance(context: Context): MoodDbHelper =
            instance ?: synchronized(this) {
                instance ?: MoodDbHelper(context.applicationContext).also { instance = it }
            }
    }
}
