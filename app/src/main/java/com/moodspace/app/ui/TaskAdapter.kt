package com.moodspace.app.ui

import android.content.Context
import android.graphics.Paint
import android.graphics.Typeface
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.CheckBox
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.moodspace.app.R
import com.moodspace.app.data.Task
import com.moodspace.app.data.TaskPriority
import com.moodspace.app.util.DateFormats

/** Fila de la agenda: casilla para completar (HU08), título, prioridad y fecha límite (HU09). */
class TaskAdapter(
    context: Context,
    private val onToggle: (task: Task, done: Boolean) -> Unit
) : ArrayAdapter<Task>(context, 0) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView
            ?: LayoutInflater.from(context).inflate(R.layout.item_task, parent, false)
        val task = getItem(position) ?: return view

        val cbDone = view.findViewById<CheckBox>(R.id.cbDone)
        val tvTitle = view.findViewById<TextView>(R.id.tvTaskTitle)
        val tvMeta = view.findViewById<TextView>(R.id.tvTaskMeta)

        // Se quita el listener antes de fijar el estado para no disparar un cambio falso al reciclar filas
        cbDone.setOnCheckedChangeListener(null)
        cbDone.isChecked = task.done
        cbDone.setOnCheckedChangeListener { _, checked -> onToggle(task, checked) }

        tvTitle.text = task.title
        tvTitle.paintFlags = if (task.done) {
            tvTitle.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
        } else {
            tvTitle.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
        }
        view.alpha = if (task.done) 0.55f else 1f

        tvMeta.text = buildMeta(task)
        return view
    }

    private fun buildMeta(task: Task): CharSequence {
        val labels = context.resources.getStringArray(R.array.priorities)
        val index = (task.priority - 1).coerceIn(0, labels.size - 1)
        val priorityColor = ContextCompat.getColor(
            context,
            when (task.priority) {
                TaskPriority.HIGH -> R.color.priority_high
                TaskPriority.LOW -> R.color.priority_low
                else -> R.color.priority_medium
            }
        )

        val text = SpannableStringBuilder()
        text.append(context.getString(R.string.task_priority_label, labels[index]))
        text.setSpan(ForegroundColorSpan(priorityColor), 0, text.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        text.setSpan(StyleSpan(Typeface.BOLD), 0, text.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

        val due = task.dueDate
        if (due != null) {
            text.append("  ·  ")
            val overdue = !task.done && due < DateFormats.startOfToday()
            val start = text.length
            text.append(
                context.getString(
                    if (overdue) R.string.task_overdue else R.string.task_due,
                    DateFormats.date(due)
                )
            )
            if (overdue) {
                text.setSpan(
                    ForegroundColorSpan(ContextCompat.getColor(context, R.color.task_overdue)),
                    start, text.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }
        }
        return text
    }
}
