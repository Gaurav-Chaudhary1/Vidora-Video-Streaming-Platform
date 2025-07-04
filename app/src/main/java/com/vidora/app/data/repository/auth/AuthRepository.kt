package com.vidora.app.data.repository.auth

import com.vidora.app.data.remote.models.auth.AuthResponse
import com.vidora.app.data.remote.models.auth.LoginRequest
import com.vidora.app.data.remote.models.auth.UserProfile
import java.io.File

interface AuthRepository {
    suspend fun login(request: LoginRequest): AuthResponse
    suspend fun signup(
        firstName: String,
        lastName: String,
        email: String,
        password: String,
        imageFile: File? = null
    ): AuthResponse
    suspend fun getProfile(): UserProfile
    suspend fun resetRequest(email: String): String
    suspend fun resetPassword(email: String, code: String, newPassword: String): String
}
