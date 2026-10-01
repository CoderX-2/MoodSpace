package com.moodspace.app.ui

import android.app.Application
import androidx.annotation.StringRes
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.moodspace.app.R
import com.moodspace.app.data.MoodDbHelper
import com.moodspace.app.data.SessionManager
import com.moodspace.app.data.User
import com.moodspace.app.data.UserRepository
import com.moodspace.app.util.Validators

sealed class ProfileState {
    object Saved : ProfileState()
    data class Error(val field: Field?, @StringRes val message: Int) : ProfileState()
}

// HU03 – Editar perfil
class ProfileViewModel(app: Application) : AndroidViewModel(app) {

    private val repo = UserRepository(MoodDbHelper.getInstance(app))
    private val session = SessionManager(app)

    private val _user = MutableLiveData<User?>()
    val user: LiveData<User?> = _user

    private val _state = MutableLiveData<ProfileState>()
    val state: LiveData<ProfileState> = _state

    fun loadUser() {
        _user.value = repo.getUser(session.userId())
    }

    fun save(name: String, favorites: String, reminders: Boolean) {
        Validators.name(name)?.let { _state.value = ProfileState.Error(Field.NAME, it); return }
        val ok = repo.updateProfile(session.userId(), name.trim(), favorites.trim(), reminders)
        _state.value = if (ok) ProfileState.Saved else ProfileState.Error(null, R.string.error_generic)
    }
}
