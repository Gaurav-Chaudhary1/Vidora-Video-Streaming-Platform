package com.vidora.app.presentation.auth.signup

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vidora.app.data.local.datastore.UserPreferences
import com.vidora.app.data.repository.auth.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class SignUpViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val userPrefs: UserPreferences
): ViewModel() {

    var state by mutableStateOf(SignUpState())
        private set

    var selectedImageFile by mutableStateOf<File?>(null)

    var isLoading by mutableStateOf(false)
        private set

    fun signUp(firstName: String, lastName: String, email: String, password: String) {
        isLoading = true
        viewModelScope.launch {
            try {
                val response = authRepository.signup(
                    firstName, lastName, email, password, selectedImageFile
                )
                userPrefs.saveToken(response.token)
                userPrefs.saveUserEmail(response.user.email)
                state = state.copy(user = response.user, isSuccess = true, error = null)
            } catch (e: Exception) {
                state = state.copy(error = e.message ?: "Unknown error!")
            } finally {
                isLoading = false
            }
        }
    }

    fun clearError() {
        state = state.copy(error = null)
    }
}