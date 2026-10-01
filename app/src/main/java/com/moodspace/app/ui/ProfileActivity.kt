package com.moodspace.app.ui

import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.activity.viewModels
import com.google.android.material.materialswitch.MaterialSwitch
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.moodspace.app.R

// HU03 – Editar perfil (nombre y preferencias)
class ProfileActivity : SessionActivity() {

    private val viewModel: ProfileViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!sessionOk) return
        setContentView(R.layout.activity_profile)
        title = getString(R.string.title_profile)

        val tilName = findViewById<TextInputLayout>(R.id.tilName)
        val etName = findViewById<TextInputEditText>(R.id.etName)
        val etEmail = findViewById<TextInputEditText>(R.id.etEmail)
        val etFavorites = findViewById<TextInputEditText>(R.id.etFavorites)
        val swReminders = findViewById<MaterialSwitch>(R.id.swReminders)

        viewModel.user.observe(this) { user ->
            user?.let {
                etName.setText(it.name)
                etEmail.setText(it.email)
                etFavorites.setText(it.favoriteActivities)
                swReminders.isChecked = it.remindersEnabled
            }
        }

        viewModel.state.observe(this) { state ->
            when (state) {
                is ProfileState.Saved -> {
                    Toast.makeText(this, R.string.profile_updated, Toast.LENGTH_SHORT).show()
                    finish()
                }
                is ProfileState.Error ->
                    if (state.field == Field.NAME) tilName.error = getString(state.message)
                    else Toast.makeText(this, state.message, Toast.LENGTH_SHORT).show()
            }
        }

        findViewById<Button>(R.id.btnSave).setOnClickListener {
            tilName.error = null
            viewModel.save(
                etName.text.toString(),
                etFavorites.text.toString(),
                swReminders.isChecked
            )
        }

        if (savedInstanceState == null) viewModel.loadUser()
    }
}
