package com.moodspace.app.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.moodspace.app.data.HabitRepository
import com.moodspace.app.data.MoodDbHelper
import com.moodspace.app.data.MoodKind
import com.moodspace.app.data.MoodRepository
import com.moodspace.app.data.SessionManager
import com.moodspace.app.data.Suggestion
import com.moodspace.app.data.SuggestionEngine
import com.moodspace.app.data.TaskOrder
import com.moodspace.app.data.TaskRepository
import com.moodspace.app.util.DateFormats

/** @param moodLabel etiqueta del estado de hoy (ej. "😄 Feliz"), o null si aún no lo registra. */
data class SuggestionsResult(val moodLabel: String?, val items: List<Suggestion>)

// HU12 – Recibir sugerencias
class SuggestionViewModel(app: Application) : AndroidViewModel(app) {

    private val db = MoodDbHelper.getInstance(app)
    private val moods = MoodRepository(db)
    private val tasks = TaskRepository(db)
    private val habits = HabitRepository(db)
    private val session = SessionManager(app)

    private val _result = MutableLiveData<SuggestionsResult>()
    val result: LiveData<SuggestionsResult> = _result

    fun load() {
        val userId = session.userId()
        val entry = moods.today(userId)
        val kind = entry?.let { MoodKind.fromLabel(it.mood) }
        val items = SuggestionEngine.build(
            kind,
            tasks.agenda(userId, TaskOrder.PRIORITY),
            habits.list(userId),
            DateFormats.startOfToday()
        )
        _result.value = SuggestionsResult(entry?.mood, items)
    }
}
