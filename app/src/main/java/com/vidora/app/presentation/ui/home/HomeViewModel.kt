package com.vidora.app.presentation.ui.home

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vidora.app.data.local.datastore.UserPreferences
import com.vidora.app.data.remote.models.auth.UserProfile
import com.vidora.app.data.repository.auth.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: AuthRepository,
    private val userPrefs: UserPreferences
): ViewModel() {
    var profile by mutableStateOf<UserProfile?>(null)
        private set

    var error by mutableStateOf<String?>(null)
        private set

    init {
        viewModelScope.launch {
            val token = userPrefs.getToken()
            if (!token.isNullOrEmpty()){
              try {
                  profile = repository.getProfile()
              } catch (e: Exception) {
                  error = e.message
              }
            }
        }
    }
}