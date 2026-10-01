package com.moodspace.app.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.TextView
import androidx.activity.viewModels
import com.moodspace.app.R
import com.moodspace.app.util.DateFormats

// HU05 – Consultar historial reciente
class HistoryActivity : SessionActivity() {

    private val viewModel: MoodViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!sessionOk) return
        setContentView(R.layout.activity_history)
        title = getString(R.string.title_history)

        val list = findViewById<ListView>(R.id.lvHistory)
        val empty = findViewById<TextView>(R.id.tvEmpty)

        viewModel.entries.observe(this) { entries ->
            val lines = entries.map {
                val date = DateFormats.dateTime(it.createdAt)
                if (it.note.isBlank()) "${it.mood}\n$date" else "${it.mood}\n$date\n“${it.note}”"
            }
            list.adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, lines)
            empty.visibility = if (lines.isEmpty()) View.VISIBLE else View.GONE

            // Al seleccionar un registro se abre su detalle
            list.setOnItemClickListener { _, _, position, _ ->
                val intent = Intent(this, MoodDetailActivity::class.java)
                intent.putExtra(MoodDetailActivity.EXTRA_ENTRY_ID, entries[position].id)
                startActivity(intent)
            }
        }

        viewModel.loadRecent()
    }
}
