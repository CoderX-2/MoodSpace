package com.moodspace.app.ui

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.moodspace.app.data.SessionManager

/** Base de las pantallas que requieren sesión. Si no hay sesión, regresa al login. */
abstract class SessionActivity : AppCompatActivity() {

    protected var sessionOk = false
        private set

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sessionOk = SessionManager(this).isLoggedIn()
        if (!sessionOk) {
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }
}
