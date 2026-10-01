package com.moodspace.app.data

data class User(
    val id: Long,
    val name: String,
    val email: String,
    val favoriteActivities: String,
    val remindersEnabled: Boolean
)

data class MoodEntry(val id: Long, val mood: String, val note: String, val createdAt: Long)

data class Credentials(val userId: Long, val hash: String, val salt: String)
