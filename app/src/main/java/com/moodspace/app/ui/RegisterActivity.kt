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

// HU01 – Alta de cuenta
class RegisterActivity : AppCompatActivity() {

    private val viewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
        title = getString(R.string.title_register)

        val etName = findViewById<TextInputEditText>(R.id.etName)
        val etEmail = findViewById<TextInputEditText>(R.id.etEmail)
        val etPassword = findViewById<TextInputEditText>(R.id.etPassword)
        val etConfirm = findViewById<TextInputEditText>(R.id.etConfirm)

        val layouts = mapOf(
            Field.NAME to findViewById<TextInputLayout>(R.id.tilName),
            Field.EMAIL to findViewById<TextInputLayout>(R.id.tilEmail),
            Field.PASSWORD to findViewById<TextInputLayout>(R.id.tilPassword),
            Field.CONFIRM to findViewById<TextInputLayout>(R.id.tilConfirm)
        )

        viewModel.state.observe(this) { state ->
            when (state) {
                is AuthState.Success -> {
                    Toast.makeText(this, R.string.account_created, Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, MainActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                }
                is AuthState.Error ->
                    layouts[state.field]?.error = getString(state.message)
            }
        }

        findViewById<Button>(R.id.btnRegister).setOnClickListener {
            layouts.values.forEach { it.error = null }
            viewModel.register(
                etName.text.toString(),
                etEmail.text.toString(),
                etPassword.text.toString(),
                etConfirm.text.toString()
            )
        }
    }
}
