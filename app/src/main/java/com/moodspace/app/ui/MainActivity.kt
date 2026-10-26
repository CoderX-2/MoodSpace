package com.moodspace.app.ui

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import com.google.android.material.textfield.TextInputEditText
import com.moodspace.app.R

// HU04 – Registrar estado (pantalla principal); desde aquí se accede a agenda, hábitos, sugerencias y música
class MainActivity : SessionActivity() {

    private val moodViewModel: MoodViewModel by viewModels()
    private val profileViewModel: ProfileViewModel by viewModels()
    private val authViewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!sessionOk) return
        setContentView(R.layout.activity_main)

        val spMood = findViewById<Spinner>(R.id.spMood)
        val etNote = findViewById<TextInputEditText>(R.id.etNote)
        val tvGreeting = findViewById<TextView>(R.id.tvGreeting)

        val moods = resources.getStringArray(R.array.moods)
        spMood.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, moods)

        profileViewModel.user.observe(this) { user ->
            tvGreeting.text = getString(R.string.greeting, user?.name ?: "")
        }
        moodViewModel.saved.observe(this) { mood ->
            Toast.makeText(this, getString(R.string.mood_saved, mood), Toast.LENGTH_SHORT).show()
        }

        findViewById<Button>(R.id.btnSaveMood).setOnClickListener {
            moodViewModel.saveMood(spMood.selectedItem.toString(), etNote.text.toString())
            etNote.text?.clear()
        }
        findViewById<Button>(R.id.btnAgenda).setOnClickListener {
            startActivity(Intent(this, AgendaActivity::class.java))
        }
        findViewById<Button>(R.id.btnHabits).setOnClickListener {
            startActivity(Intent(this, HabitsActivity::class.java))
        }
        findViewById<Button>(R.id.btnSuggestions).setOnClickListener {
            startActivity(Intent(this, SuggestionsActivity::class.java))
        }
        findViewById<Button>(R.id.btnMusic).setOnClickListener {
            startActivity(Intent(this, MusicActivity::class.java))
        }
        findViewById<Button>(R.id.btnHistory).setOnClickListener {
            startActivity(Intent(this, HistoryActivity::class.java))
        }
        findViewById<Button>(R.id.btnProfile).setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }
        findViewById<Button>(R.id.btnLogout).setOnClickListener {
            authViewModel.logout()
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        // Refresca el saludo por si se editó el perfil (HU03)
        if (sessionOk) profileViewModel.loadUser()
    }
}
