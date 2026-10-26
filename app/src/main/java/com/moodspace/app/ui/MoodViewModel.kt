package com.moodspace.app.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.moodspace.app.data.MoodDbHelper
import com.moodspace.app.data.MoodEntry
import com.moodspace.app.data.MoodKind
import com.moodspace.app.data.MoodRepository
import com.moodspace.app.data.SessionManager

// HU04 y HU05 – Registro e historial de estados de ánimo
class MoodViewModel(app: Application) : AndroidViewModel(app) {

    private val repo = MoodRepository(MoodDbHelper.getInstance(app))
    private val session = SessionManager(app)

    private val _entries = MutableLiveData<List<MoodEntry>>()
    val entries: LiveData<List<MoodEntry>> = _entries

    private val _entry = MutableLiveData<MoodEntry?>()
    val entry: LiveData<MoodEntry?> = _entry

    private val _saved = MutableLiveData<String>()
    val saved: LiveData<String> = _saved

    private val _todayMood = MutableLiveData<MoodKind?>()
    val todayMood: LiveData<MoodKind?> = _todayMood

    fun saveMood(mood: String, note: String) {
        repo.add(session.userId(), mood, note.trim())
        _saved.value = mood
    }

    fun loadRecent() {
        _entries.value = repo.recent(session.userId())
    }

    fun loadEntry(id: Long) {
        _entry.value = repo.get(id, session.userId())
    }

    /** Estado de hoy (HU13 lo usa para sugerir música); null si aún no registra ninguno. */
    fun loadTodayMood() {
        _todayMood.value = repo.today(session.userId())?.let { MoodKind.fromLabel(it.mood) }
    }
}
