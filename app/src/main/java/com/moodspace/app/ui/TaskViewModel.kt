package com.moodspace.app.ui

import android.app.Application
import androidx.annotation.StringRes
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.moodspace.app.R
import com.moodspace.app.data.MoodDbHelper
import com.moodspace.app.data.SessionManager
import com.moodspace.app.data.Task
import com.moodspace.app.data.TaskOrder
import com.moodspace.app.data.TaskRepository
import com.moodspace.app.util.Validators

sealed class TaskFormState {
    object Saved : TaskFormState()
    data class Error(@StringRes val message: Int) : TaskFormState()
}

// HU06 – Crear tarea, HU07 – Editar tarea, HU08 – Completar tarea, HU09 – Priorizar agenda
class TaskViewModel(app: Application) : AndroidViewModel(app) {

    private val repo = TaskRepository(MoodDbHelper.getInstance(app))
    private val session = SessionManager(app)

    /** Orden elegido en la agenda; vive aquí para sobrevivir a una rotación de pantalla. */
    var order: TaskOrder = TaskOrder.PRIORITY

    private val _tasks = MutableLiveData<List<Task>>()
    val tasks: LiveData<List<Task>> = _tasks

    private val _task = MutableLiveData<Task?>()
    val task: LiveData<Task?> = _task

    private val _state = MutableLiveData<TaskFormState>()
    val state: LiveData<TaskFormState> = _state

    fun loadAgenda() {
        _tasks.value = repo.agenda(session.userId(), order)
    }

    fun loadTask(id: Long) {
        _task.value = repo.get(id, session.userId())
    }

    /** [id] = -1 crea una tarea nueva (HU06); cualquier otro valor edita esa tarea (HU07). */
    fun save(id: Long, title: String, description: String, dueDate: Long?, priority: Int) {
        Validators.taskTitle(title)?.let { _state.value = TaskFormState.Error(it); return }
        val userId = session.userId()
        val ok = if (id == NEW_TASK) {
            repo.add(userId, title, description, dueDate, priority) != -1L
        } else {
            repo.update(id, userId, title, description, dueDate, priority)
        }
        _state.value = if (ok) TaskFormState.Saved else TaskFormState.Error(R.string.error_generic)
    }

    /** HU08 – Marca o desmarca una tarea como completada y refresca la agenda. */
    fun setDone(task: Task, done: Boolean) {
        repo.setDone(task.id, session.userId(), done)
        loadAgenda()
    }

    companion object {
        const val NEW_TASK = -1L
    }
}
