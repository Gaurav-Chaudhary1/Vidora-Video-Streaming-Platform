package com.vidora.app.data.remote.models.auth

import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface AuthApi {
    @POST("login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>

    @Multipart
    @POST("signup")
    suspend fun signup(
        @Part("firstName") firstName: RequestBody,
        @Part("lastName") lastName: RequestBody,
        @Part("email") email: RequestBody,
        @Part("password") password: RequestBody,
        @Part profileImage: MultipartBody.Part? = null
    ): Response<AuthResponse>

    @GET("me")
    suspend fun getProfile(): Response<ProfileResponse>

    @POST("reset-request")
    suspend fun resetRequest(@Body body: ResetRequestBody): Response<GenericMessageResponse>

    @POST("reset-password")
    suspend fun resetPassword(@Body body: ResetPasswordBody): Response<GenericMessageResponse>

}