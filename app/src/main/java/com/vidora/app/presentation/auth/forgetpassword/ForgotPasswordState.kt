package com.vidora.app.presentation.auth.forgetpassword

data class ForgotPasswordState(
    val requestSent: Boolean = false,
    val isResetSuccess: Boolean = false,
    val message: String? = null
)
