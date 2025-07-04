package com.vidora.app.presentation.auth.login

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vidora.app.data.local.datastore.UserPreferences
import com.vidora.app.data.remote.models.auth.LoginRequest
import com.vidora.app.data.repository.auth.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val repository: AuthRepository,
    private val userPrefs: UserPreferences
) : ViewModel() {

    var state by mutableStateOf(LoginState())
        private set

    var isLoading by mutableStateOf(false)
        private set

    fun login(email: String, password: String) {
        isLoading = true
        viewModelScope.launch {
            try {
                val response = repository.login(LoginRequest(email, password))
                userPrefs.saveToken(response.token)    // <-- DataStore
                userPrefs.saveUserEmail(response.user.email)
                state = state.copy(user = response.user, isSuccess = true, error = null)
            } catch (e: Exception) {
                val raw = e.message ?: "Unknown error"
                // crude way to strip { "message": "..." } → get just the inner text
                val msg = raw.substringAfter(
                    "\"message\":\""
                ).substringBefore("\"}")
                state = state.copy(error = msg)
            } finally {
                isLoading = false
            }
        }
    }

    fun clearError() {
        state = state.copy(error = null)
    }
}