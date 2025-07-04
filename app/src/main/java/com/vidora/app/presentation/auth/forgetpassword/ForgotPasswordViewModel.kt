package com.vidora.app.presentation.auth.forgetpassword

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vidora.app.data.repository.auth.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ForgotPasswordViewModel @Inject constructor(
    private val repository: AuthRepository
): ViewModel() {

    var state by  mutableStateOf(ForgotPasswordState())
        private set

    var isLoading by mutableStateOf(false)
        private set

    fun requestResetCode(email: String) {
        isLoading = true
        viewModelScope.launch {
            try {
                val successMessage = repository.resetRequest(email)
                state = state.copy(requestSent = true, message = successMessage)
            } catch (e: Exception) {
                state = state.copy(message = e.message)
            } finally {
                isLoading = false
            }
        }
    }

    fun resetPassword(email: String, code: String, newPassword: String) {
        isLoading = true
        viewModelScope.launch {
            try {
                val successMessage = repository.resetPassword(email, code, newPassword)
                state = state.copy(isResetSuccess = true, message = successMessage)
            } catch (e: Exception) {
                state = state.copy(message = e.message)
            } finally {
                isLoading = false
            }
        }
    }

    fun clearMessage() {
        state = state.copy(message = null)
    }
}