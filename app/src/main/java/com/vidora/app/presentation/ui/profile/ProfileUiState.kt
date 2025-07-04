package com.vidora.app.presentation.ui.profile

import com.vidora.app.data.remote.models.auth.UserProfile

data class ProfileUiState(
    val loading: Boolean = true,
    val profile: UserProfile? = null,
    val signedImageUrl: String? = null,
    val error: String? = null
)
