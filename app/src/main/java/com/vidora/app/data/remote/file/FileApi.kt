package com.vidora.app.data.remote.file

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

interface FileApi {
    @GET("files/signed-url")
    suspend fun getSignedUrl(
        @Header("Authorization") bearer: String,
        @Query(value = "fileUrl", encoded = true)
        fileUrl: String
    ): Response<SignedUrlResponse>
}


data class SignedUrlResponse(
    val signedUrl: String
)