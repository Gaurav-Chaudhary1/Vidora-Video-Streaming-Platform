package com.vidora.app.data.repository.auth

import com.vidora.app.data.remote.models.auth.AuthApi
import com.vidora.app.data.remote.models.auth.AuthResponse
import com.vidora.app.data.remote.models.auth.LoginRequest
import com.vidora.app.data.remote.models.auth.ResetPasswordBody
import com.vidora.app.data.remote.models.auth.ResetRequestBody
import com.vidora.app.data.remote.models.auth.UserProfile
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val api: AuthApi
) : AuthRepository {

    override suspend fun login(request: LoginRequest): AuthResponse {
        val response = api.login(request)
        if (response.isSuccessful) {
            return response.body()!!
        } else {
            val errorJson = response.errorBody()?.string().orEmpty()
            throw Exception(parseErrorMessage(errorJson, "Login failed"))
        }
    }

    override suspend fun signup(
        firstName: String,
        lastName: String,
        email: String,
        password: String,
        imageFile: File?
    ): AuthResponse {
        val createPart = { value: String -> value.toRequestBody("text/plain".toMediaTypeOrNull()) }

        val imagePart = imageFile?.let {
            val reqFile = it.asRequestBody("image/*".toMediaTypeOrNull())
            MultipartBody.Part.createFormData("profileImage", it.name, reqFile)
        }

        val response = api.signup(
            createPart(firstName),
            createPart(lastName),
            createPart(email),
            createPart(password),
            imagePart
        )

        if (response.isSuccessful) {
            return response.body()!!
        } else {
            val errorJson = response.errorBody()?.string().orEmpty()
            throw Exception(parseErrorMessage(errorJson, "Signup failed"))
        }
    }


    override suspend fun getProfile(): UserProfile {
        val response = api.getProfile()
        if (response.isSuccessful) {
            return response.body()!!.user   // because you wrapped it in `ProfileResponse(val user: UserProfile)`
        } else {
            val errorJson = response.errorBody()?.string().orEmpty()
            throw Exception(parseErrorMessage(errorJson, "Unauthorized"))
        }
    }

    override suspend fun resetRequest(email: String): String {
        val body = ResetRequestBody(email)
        val response = api.resetRequest(body)
        if (response.isSuccessful){
            return response.body()!!.message
        } else {
            val errorJson = response.errorBody()?.string().orEmpty()
            throw Exception(parseErrorMessage(errorJson, "Reset request failed"))
        }
    }

    override suspend fun resetPassword(
        email: String,
        code: String,
        newPassword: String): String
    {
        val body = ResetPasswordBody(email, code, newPassword)
        val response = api.resetPassword(body)
        if (response.isSuccessful){
            return response.body()!!.message
        } else {
            val errorJson = response.errorBody()?.string().orEmpty()
            throw Exception(parseErrorMessage(errorJson, "Reset password failed"))
        }
    }

    /**
     * Helper: If server returned { "message": "..." }, extract it.
     * Otherwise return defaultMsg.
     */
    private fun parseErrorMessage(json: String, defaultMsg: String): String {
        return try {
            // A minimal manual parse, assuming the JSON is { "message": "Something went wrong" }
            val regex = "\"message\"\\s*:\\s*\"([^\"]+)\"".toRegex()
            val match = regex.find(json)
            match?.groups?.get(1)?.value ?: defaultMsg
        } catch (e: Exception) {
            defaultMsg
        }
    }

}