package com.moodspace.app.ui

import android.app.Application
import androidx.annotation.StringRes
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.moodspace.app.R
import com.moodspace.app.data.Habit
import com.moodspace.app.data.HabitRepository
import com.moodspace.app.data.MoodDbHelper
import com.moodspace.app.data.SessionManager
import com.moodspace.app.util.Validators

sealed class HabitFormState {
    object Saved : HabitFormState()
    data class Error(@StringRes val message: Int) : HabitFormState()
}

// HU10 – Crear hábito, HU11 – Registrar cumplimiento
class HabitViewModel(app: Application) : AndroidViewModel(app) {

    private val repo = HabitRepository(MoodDbHelper.getInstance(app))
    private val session = SessionManager(app)

    private val _habits = MutableLiveData<List<Habit>>()
    val habits: LiveData<List<Habit>> = _habits

    private val _state = MutableLiveData<HabitFormState>()
    val state: LiveData<HabitFormState> = _state

    fun loadHabits() {
        _habits.value = repo.list(session.userId())
    }

    fun save(name: String, description: String) {
        Validators.habitName(name)?.let { _state.value = HabitFormState.Error(it); return }
        val ok = repo.add(session.userId(), name, description) != -1L
        _state.value = if (ok) HabitFormState.Saved else HabitFormState.Error(R.string.error_generic)
    }

    /** HU11 – Marca o desmarca el hábito de hoy y refresca la lista (y la racha). */
    fun setDoneToday(habit: Habit, done: Boolean) {
        repo.setDoneToday(habit.id, session.userId(), done)
        loadHabits()
    }
}
