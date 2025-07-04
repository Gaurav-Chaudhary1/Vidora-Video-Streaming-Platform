package com.vidora.app.presentation.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vidora.app.data.local.datastore.UserPreferences
import com.vidora.app.data.repository.auth.AuthRepository
import com.vidora.app.data.repository.file.FileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val authRepo: AuthRepository,
    private val prefs: UserPreferences,
    private val fileRepo: FileRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState

    init {
        viewModelScope.launch {
            // 1. Load token & profile
            val token = prefs.getToken().orEmpty()
            if (token.isBlank()) {
                _uiState.update { it.copy(loading = false, error = "Not signed in") }
                return@launch
            }

            runCatching { authRepo.getProfile() }
                .onSuccess { user ->
                    _uiState.update { it.copy(profile = user, loading = false, error = null) }
                    // Only fetch signed URL if a valid picture URL exists
                    user.profilePictureUrl
                        ?.takeIf { it.isNotBlank() }
                        ?.let { fetchSignedImageUrl(it) }
                }
                .onFailure { ex ->
                    _uiState.update { it.copy(loading = false, error = ex.message) }
                }
        }
    }

    private fun fetchSignedImageUrl(rawUrl: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(loading = true, error = null) }
            fileRepo.fetchSignedUrl(rawUrl)
                .onSuccess { signed ->
                    _uiState.update { it.copy(signedImageUrl = signed, loading = false) }
                }
                .onFailure { ex ->
                    _uiState.update { it.copy(error = ex.message, loading = false) }
                }
        }
    }

    suspend fun signOut() {
        prefs.getToken()?.takeIf { it.isNotEmpty() }?.let {
            prefs.clearToken()
        }
    }

    /** Fetches profile & avatar again */
    fun refreshProfile() {
        viewModelScope.launch {
            _uiState.update { it.copy(loading = true, error = null) }

            runCatching { authRepo.getProfile() }
                .onSuccess { user ->
                    _uiState.update { it.copy(profile = user, loading = false, error = null) }
                    user.profilePictureUrl
                        ?.takeIf { it.isNotBlank() }
                        ?.let { fetchSignedImageUrl(it) }
                }
                .onFailure { ex ->
                    _uiState.update { it.copy(loading = false, error = ex.message) }
                }
        }
    }
}
