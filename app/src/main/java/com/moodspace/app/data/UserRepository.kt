package com.moodspace.app.data

import com.moodspace.app.util.PasswordHasher

class UserRepository(private val db: MoodDbHelper) {

    /** Devuelve el id del nuevo usuario, o -1 si el correo ya está registrado. */
    fun register(name: String, email: String, password: String): Long {
        val salt = PasswordHasher.newSalt()
        return db.insertUser(name.trim(), email.trim().lowercase(), PasswordHasher.hash(password, salt), salt)
    }

    /** Devuelve el usuario si las credenciales son correctas; null en caso contrario. */
    fun login(email: String, password: String): User? {
        val cred = db.getCredentials(email.trim().lowercase()) ?: return null
        if (PasswordHasher.hash(password, cred.salt) != cred.hash) return null
        return db.getUser(cred.userId)
    }

    fun getUser(id: Long): User? = db.getUser(id)

    fun updateProfile(id: Long, name: String, favorites: String, reminders: Boolean): Boolean =
        db.updateProfile(id, name, favorites, reminders)
}
