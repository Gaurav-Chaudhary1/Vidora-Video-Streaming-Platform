package com.vidora.app.data.remote.models.auth

import com.google.gson.annotations.SerializedName

data class LoginRequest(
    val email: String,
    val password: String
)

data class AuthResponse(
    val token: String,
    val user: UserProfile
)

data class UserProfile(
    @SerializedName("_id")
    val id: String,
    val email: String,
    val firstName: String,
    val lastName: String,
    val profilePictureUrl: String?,
    val channelId: String?
)

data class ResetRequestBody(val email: String)

data class ResetPasswordBody(
    val email: String,
    val code: String,
    val newPassword: String
)

data class GenericMessageResponse(val message: String)

data class ProfileResponse(val user: UserProfile)
