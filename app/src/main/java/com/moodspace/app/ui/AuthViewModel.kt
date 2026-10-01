package com.moodspace.app.ui

import android.app.Application
import androidx.annotation.StringRes
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.moodspace.app.R
import com.moodspace.app.data.MoodDbHelper
import com.moodspace.app.data.SessionManager
import com.moodspace.app.data.UserRepository
import com.moodspace.app.util.Validators

enum class Field { NAME, EMAIL, PASSWORD, CONFIRM }

sealed class AuthState {
    object Success : AuthState()
    data class Error(val field: Field?, @StringRes val message: Int) : AuthState()
}

// HU01 y HU02 – Registro e inicio de sesión
class AuthViewModel(app: Application) : AndroidViewModel(app) {

    private val repo = UserRepository(MoodDbHelper.getInstance(app))
    private val session = SessionManager(app)

    private val _state = MutableLiveData<AuthState>()
    val state: LiveData<AuthState> = _state

    fun isLoggedIn(): Boolean = session.isLoggedIn()

    fun logout() = session.clear()

    fun register(name: String, email: String, password: String, confirm: String) {
        Validators.name(name)?.let { _state.value = AuthState.Error(Field.NAME, it); return }
        Validators.email(email)?.let { _state.value = AuthState.Error(Field.EMAIL, it); return }
        Validators.password(password)?.let { _state.value = AuthState.Error(Field.PASSWORD, it); return }
        if (password != confirm) {
            _state.value = AuthState.Error(Field.CONFIRM, R.string.error_password_mismatch)
            return
        }
        val id = repo.register(name, email, password)
        if (id == -1L) {
            _state.value = AuthState.Error(Field.EMAIL, R.string.error_email_taken)
            return
        }
        session.save(id)
        _state.value = AuthState.Success
    }

    fun login(email: String, password: String) {
        if (email.isBlank() || password.isEmpty()) {
            _state.value = AuthState.Error(null, R.string.error_fields_required)
            return
        }
        val user = repo.login(email, password)
        if (user == null) {
            _state.value = AuthState.Error(null, R.string.error_login_invalid)
        } else {
            session.save(user.id)
            _state.value = AuthState.Success
        }
    }
}
