package com.moodspace.app.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.activity.viewModels
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.material.materialswitch.MaterialSwitch
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.moodspace.app.R
import com.moodspace.app.util.ReminderNotificationHelper

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

        swReminders.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                    ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.POST_NOTIFICATIONS), 101)
                } else {
                    ReminderNotificationHelper.scheduleDailyReminder(this)
                }
            } else {
                ReminderNotificationHelper.cancelReminder(this)
            }
        }

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
                    if (swReminders.isChecked) {
                        ReminderNotificationHelper.scheduleDailyReminder(this)
                    } else {
                        ReminderNotificationHelper.cancelReminder(this)
                    }
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
