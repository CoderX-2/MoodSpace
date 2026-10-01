package com.moodspace.app.ui

import android.os.Bundle
import android.widget.TextView
import androidx.activity.viewModels
import com.moodspace.app.R
import com.moodspace.app.util.DateFormats

// HU05 – Detalle de un registro del historial
class MoodDetailActivity : SessionActivity() {

    private val viewModel: MoodViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!sessionOk) return
        setContentView(R.layout.activity_mood_detail)
        title = getString(R.string.title_detail)

        val tvMood = findViewById<TextView>(R.id.tvDetailMood)
        val tvDate = findViewById<TextView>(R.id.tvDetailDate)
        val tvNote = findViewById<TextView>(R.id.tvDetailNote)

        viewModel.entry.observe(this) { entry ->
            if (entry == null) {
                finish()
            } else {
                tvMood.text = entry.mood
                tvDate.text = DateFormats.dateTime(entry.createdAt)
                tvNote.text = entry.note.ifBlank { getString(R.string.detail_no_note) }
            }
        }

        viewModel.loadEntry(intent.getLongExtra(EXTRA_ENTRY_ID, -1L))
    }

    companion object {
        const val EXTRA_ENTRY_ID = "entry_id"
    }
}
