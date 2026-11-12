package com.moodspace.app.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.moodspace.app.R

// HU02 – Inicio de sesión
class LoginActivity : AppCompatActivity() {

    private val viewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Si ya hay sesión guardada, entra directo
        if (viewModel.isLoggedIn()) {
            goToMain()
            return
        }

        setContentView(R.layout.activity_login)
        supportActionBar?.hide()

        val etEmail = findViewById<TextInputEditText>(R.id.etEmail)
        val etPassword = findViewById<TextInputEditText>(R.id.etPassword)
        val tilEmail = findViewById<TextInputLayout>(R.id.tilEmail)
        val tilPassword = findViewById<TextInputLayout>(R.id.tilPassword)

        viewModel.state.observe(this) { state ->
            when (state) {
                is AuthState.Success -> goToMain()
                is AuthState.Error ->
                    Toast.makeText(this, state.message, Toast.LENGTH_SHORT).show()
            }
        }

        findViewById<Button>(R.id.btnLogin).setOnClickListener {
            tilEmail.error = null
            tilPassword.error = null
            viewModel.login(etEmail.text.toString(), etPassword.text.toString())
        }

        findViewById<Button>(R.id.btnGoRegister).setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    private fun goToMain() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }
}
