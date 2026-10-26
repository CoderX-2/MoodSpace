package com.moodspace.app.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.TextView
import androidx.activity.viewModels
import com.moodspace.app.R
import com.moodspace.app.data.Suggestion
import com.moodspace.app.data.SuggestionKind

// HU12 – Recibir sugerencias para organizar el día según el estado
class SuggestionsActivity : SessionActivity() {

    private val viewModel: SuggestionViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!sessionOk) return
        setContentView(R.layout.activity_suggestions)
        title = getString(R.string.title_suggestions)

        val tvMood = findViewById<TextView>(R.id.tvSuggestionMood)
        val list = findViewById<ListView>(R.id.lvSuggestions)
        val adapter = SuggestionAdapter(this)
        list.adapter = adapter

        // Cada sugerencia lleva a la pantalla donde se puede actuar
        list.setOnItemClickListener { _, _, position, _ ->
            val suggestion = adapter.getItem(position) ?: return@setOnItemClickListener
            when (suggestion.kind) {
                SuggestionKind.MOOD -> finish() // la pantalla principal registra el estado
                SuggestionKind.TASK -> startActivity(Intent(this, AgendaActivity::class.java))
                SuggestionKind.HABIT -> startActivity(Intent(this, HabitsActivity::class.java))
                SuggestionKind.MUSIC -> startActivity(Intent(this, MusicActivity::class.java))
            }
        }

        viewModel.result.observe(this) { result ->
            tvMood.text = if (result.moodLabel != null) {
                getString(R.string.suggestions_for_mood, result.moodLabel)
            } else {
                getString(R.string.suggestions_no_mood)
            }
            adapter.clear()
            adapter.addAll(result.items)
        }
    }

    override fun onResume() {
        super.onResume()
        // Recalcula al volver: pudo cambiar una tarea o un hábito
        if (sessionOk) viewModel.load()
    }
}

private class SuggestionAdapter(context: Context) : ArrayAdapter<Suggestion>(context, 0) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView
            ?: LayoutInflater.from(context).inflate(R.layout.item_suggestion, parent, false)
        val item = getItem(position) ?: return view

        view.findViewById<TextView>(R.id.tvSuggestionIcon).text = when (item.kind) {
            SuggestionKind.MOOD -> "💬"
            SuggestionKind.TASK -> "✅"
            SuggestionKind.HABIT -> "🔁"
            SuggestionKind.MUSIC -> "🎵"
        }
        view.findViewById<TextView>(R.id.tvSuggestionTitle).text =
            if (item.arg != null) context.getString(item.title, item.arg) else context.getString(item.title)
        view.findViewById<TextView>(R.id.tvSuggestionDetail).text = context.getString(item.detail)
        return view
    }
}
