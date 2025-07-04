package com.vidora.app.presentation.auth.login

import com.vidora.app.data.remote.models.auth.UserProfile

data class LoginState(
    val user: UserProfile? = null,
    val isSuccess: Boolean = false,
    val error: String? = null
)

