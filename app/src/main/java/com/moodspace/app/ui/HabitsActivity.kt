package com.moodspace.app.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ListView
import android.widget.TextView
import androidx.activity.viewModels
import com.moodspace.app.R

// HU11 – Registrar cumplimiento (lista de hábitos del día)
class HabitsActivity : SessionActivity() {

    private val viewModel: HabitViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!sessionOk) return
        setContentView(R.layout.activity_habits)
        title = getString(R.string.title_habits)

        val list = findViewById<ListView>(R.id.lvHabits)
        val empty = findViewById<TextView>(R.id.tvHabitsEmpty)

        val adapter = HabitAdapter(this) { habit, done -> viewModel.setDoneToday(habit, done) }
        list.adapter = adapter

        viewModel.habits.observe(this) { habits ->
            adapter.clear()
            adapter.addAll(habits)
            empty.visibility = if (habits.isEmpty()) View.VISIBLE else View.GONE
        }

        findViewById<Button>(R.id.btnNewHabit).setOnClickListener {
            startActivity(Intent(this, HabitFormActivity::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        // Refresca la lista al volver de crear un hábito
        if (sessionOk) viewModel.loadHabits()
    }
}
