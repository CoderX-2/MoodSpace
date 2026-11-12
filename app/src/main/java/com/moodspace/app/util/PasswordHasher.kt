package com.moodspace.app.util

import java.security.MessageDigest
import java.security.SecureRandom

object PasswordHasher {

    fun newSalt(): String {
        val bytes = ByteArray(16)
        SecureRandom().nextBytes(bytes)
        return bytes.toHex()
    }

    fun hash(password: String, salt: String): String =
        MessageDigest.getInstance("SHA-256")
            .digest((salt + password).toByteArray())
            .toHex()

    private fun ByteArray.toHex(): String = joinToString("") { "%02x".format(it) }
}
