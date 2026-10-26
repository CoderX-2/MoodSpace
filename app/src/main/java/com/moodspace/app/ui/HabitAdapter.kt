package com.moodspace.app.ui

import android.content.Context
import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.CheckBox
import android.widget.TextView
import com.moodspace.app.R
import com.moodspace.app.data.Habit

/** Fila de hábito: casilla para registrar el cumplimiento de hoy (HU11) y su racha. */
class HabitAdapter(
    context: Context,
    private val onToggle: (habit: Habit, done: Boolean) -> Unit
) : ArrayAdapter<Habit>(context, 0) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView
            ?: LayoutInflater.from(context).inflate(R.layout.item_habit, parent, false)
        val habit = getItem(position) ?: return view

        val cbDone = view.findViewById<CheckBox>(R.id.cbHabitDone)
        val tvName = view.findViewById<TextView>(R.id.tvHabitName)
        val tvMeta = view.findViewById<TextView>(R.id.tvHabitMeta)

        // Se quita el listener antes de fijar el estado para no disparar un cambio falso al reciclar filas
        cbDone.setOnCheckedChangeListener(null)
        cbDone.isChecked = habit.doneToday
        cbDone.setOnCheckedChangeListener { _, checked -> onToggle(habit, checked) }

        tvName.text = habit.name
        tvName.paintFlags = if (habit.doneToday) {
            tvName.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
        } else {
            tvName.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
        }

        tvMeta.text = if (habit.streak == 0) {
            context.getString(R.string.habit_no_streak)
        } else {
            context.resources.getQuantityString(R.plurals.habit_streak, habit.streak, habit.streak)
        }
        tvMeta.visibility = View.VISIBLE
        return view
    }
}
