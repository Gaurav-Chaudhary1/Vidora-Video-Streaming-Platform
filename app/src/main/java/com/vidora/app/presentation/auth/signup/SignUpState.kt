package com.vidora.app.presentation.auth.signup

import com.vidora.app.data.remote.models.auth.UserProfile

data class SignUpState(
    val user: UserProfile? = null,
    val isSuccess: Boolean = false,
    val error: String? = null
)