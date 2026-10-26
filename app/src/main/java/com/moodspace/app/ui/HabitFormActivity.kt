package com.moodspace.app.ui

import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.activity.viewModels
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.moodspace.app.R

// HU10 – Crear hábito
class HabitFormActivity : SessionActivity() {

    private val viewModel: HabitViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!sessionOk) return
        setContentView(R.layout.activity_habit_form)
        title = getString(R.string.title_habit_new)

        val tilName = findViewById<TextInputLayout>(R.id.tilHabitName)
        val etName = findViewById<TextInputEditText>(R.id.etHabitName)
        val etDescription = findViewById<TextInputEditText>(R.id.etHabitDescription)

        viewModel.state.observe(this) { state ->
            when (state) {
                is HabitFormState.Saved -> {
                    Toast.makeText(this, R.string.habit_saved, Toast.LENGTH_SHORT).show()
                    finish()
                }
                is HabitFormState.Error ->
                    if (state.message == R.string.error_habit_name_required) {
                        tilName.error = getString(state.message)
                    } else {
                        Toast.makeText(this, state.message, Toast.LENGTH_SHORT).show()
                    }
            }
        }

        findViewById<Button>(R.id.btnSaveHabit).setOnClickListener {
            tilName.error = null
            viewModel.save(etName.text.toString(), etDescription.text.toString())
        }
    }
}
