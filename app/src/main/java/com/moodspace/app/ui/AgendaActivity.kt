package com.moodspace.app.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import android.widget.Spinner
import android.widget.TextView
import androidx.activity.viewModels
import com.moodspace.app.R
import com.moodspace.app.data.TaskOrder

// HU08 – Completar tarea y HU09 – Priorizar agenda (lista de tareas)
class AgendaActivity : SessionActivity() {

    private val viewModel: TaskViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!sessionOk) return
        setContentView(R.layout.activity_agenda)
        title = getString(R.string.title_agenda)

        val list = findViewById<ListView>(R.id.lvTasks)
        val empty = findViewById<TextView>(R.id.tvEmpty)
        val spOrder = findViewById<Spinner>(R.id.spOrder)

        val adapter = TaskAdapter(this) { task, done -> viewModel.setDone(task, done) }
        list.adapter = adapter

        // Al tocar una tarea se abre su edición (HU07)
        list.setOnItemClickListener { _, _, position, _ ->
            val task = adapter.getItem(position) ?: return@setOnItemClickListener
            val intent = Intent(this, TaskFormActivity::class.java)
            intent.putExtra(TaskFormActivity.EXTRA_TASK_ID, task.id)
            startActivity(intent)
        }

        spOrder.adapter = ArrayAdapter(
            this, android.R.layout.simple_spinner_dropdown_item, resources.getStringArray(R.array.task_orders)
        )
        spOrder.setSelection(viewModel.order.ordinal)
        spOrder.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selected = TaskOrder.values()[position]
                if (selected != viewModel.order) {
                    viewModel.order = selected
                    viewModel.loadAgenda()
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) = Unit
        }

        viewModel.tasks.observe(this) { tasks ->
            adapter.clear()
            adapter.addAll(tasks)
            empty.visibility = if (tasks.isEmpty()) View.VISIBLE else View.GONE
        }

        findViewById<Button>(R.id.btnNewTask).setOnClickListener {
            startActivity(Intent(this, TaskFormActivity::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        // Refresca la lista al volver de crear o editar una tarea
        if (sessionOk) viewModel.loadAgenda()
    }
}
