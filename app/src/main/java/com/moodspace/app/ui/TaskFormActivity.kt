package com.moodspace.app.ui

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.viewModels
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.moodspace.app.R
import com.moodspace.app.data.TaskPriority
import com.moodspace.app.util.DateFormats
import java.util.Calendar

// HU06 – Crear tarea y HU07 – Editar tarea (misma pantalla; si llega un id, edita)
class TaskFormActivity : SessionActivity() {

    private val viewModel: TaskViewModel by viewModels()

    private lateinit var etDueDate: TextInputEditText
    private lateinit var btnClearDate: Button

    private var taskId = TaskViewModel.NEW_TASK
    private var dueDate: Long? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!sessionOk) return
        setContentView(R.layout.activity_task_form)

        taskId = intent.getLongExtra(EXTRA_TASK_ID, TaskViewModel.NEW_TASK)
        val editing = taskId != TaskViewModel.NEW_TASK
        title = getString(if (editing) R.string.title_task_edit else R.string.title_task_new)

        val tilTitle = findViewById<TextInputLayout>(R.id.tilTitle)
        val etTitle = findViewById<TextInputEditText>(R.id.etTitle)
        val etDescription = findViewById<TextInputEditText>(R.id.etDescription)
        val spPriority = findViewById<Spinner>(R.id.spPriority)
        etDueDate = findViewById(R.id.etDueDate)
        btnClearDate = findViewById(R.id.btnClearDate)

        spPriority.adapter = ArrayAdapter(
            this, android.R.layout.simple_spinner_dropdown_item, resources.getStringArray(R.array.priorities)
        )
        spPriority.setSelection(TaskPriority.MEDIUM - 1)

        // Al girar la pantalla los campos de texto y el spinner se restauran solos; la fecha se guarda aparte
        val restoring = savedInstanceState != null
        if (savedInstanceState != null && savedInstanceState.containsKey(STATE_DUE)) {
            dueDate = savedInstanceState.getLong(STATE_DUE)
        }
        showDueDate()

        viewModel.task.observe(this) { task ->
            if (restoring) return@observe
            if (task == null) {
                Toast.makeText(this, R.string.error_task_not_found, Toast.LENGTH_SHORT).show()
                finish()
                return@observe
            }
            etTitle.setText(task.title)
            etDescription.setText(task.description)
            spPriority.setSelection((task.priority - 1).coerceIn(0, 2))
            dueDate = task.dueDate
            showDueDate()
        }

        viewModel.state.observe(this) { state ->
            when (state) {
                is TaskFormState.Saved -> {
                    Toast.makeText(
                        this,
                        if (editing) R.string.task_updated else R.string.task_saved,
                        Toast.LENGTH_SHORT
                    ).show()
                    finish()
                }
                is TaskFormState.Error ->
                    if (state.message == R.string.error_task_title_required) {
                        tilTitle.error = getString(state.message)
                    } else {
                        Toast.makeText(this, state.message, Toast.LENGTH_SHORT).show()
                    }
            }
        }

        etDueDate.setOnClickListener { pickDate() }
        btnClearDate.setOnClickListener {
            dueDate = null
            showDueDate()
        }
        findViewById<Button>(R.id.btnSaveTask).setOnClickListener {
            tilTitle.error = null
            viewModel.save(
                taskId,
                etTitle.text.toString(),
                etDescription.text.toString(),
                dueDate,
                spPriority.selectedItemPosition + 1
            )
        }

        if (editing && !restoring) viewModel.loadTask(taskId)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        dueDate?.let { outState.putLong(STATE_DUE, it) }
    }

    private fun showDueDate() {
        val due = dueDate
        etDueDate.setText(if (due != null) DateFormats.date(due) else "")
        btnClearDate.visibility = if (due != null) View.VISIBLE else View.GONE
    }

    private fun pickDate() {
        val cal = Calendar.getInstance()
        dueDate?.let { cal.timeInMillis = it }
        DatePickerDialog(
            this,
            { _, year, month, day ->
                dueDate = DateFormats.startOfDay(year, month, day)
                showDueDate()
            },
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    companion object {
        const val EXTRA_TASK_ID = "task_id"
        private const val STATE_DUE = "due_date"
    }
}
