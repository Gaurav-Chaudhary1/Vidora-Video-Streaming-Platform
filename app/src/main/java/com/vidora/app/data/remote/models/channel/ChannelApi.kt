package com.vidora.app.data.remote.models.channel

import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.PartMap
import retrofit2.http.Path

interface ChannelApi {

    @Multipart
    @POST("channels/create-channel")
    suspend fun createChannel(
        @Part("name") name: RequestBody,
        @Part("description") description: RequestBody,
        @Part profileImage: MultipartBody.Part?
    ): Response<ChannelResponseDto>

    @Multipart
    @PUT("channels/{channelId}")
    suspend fun updateChannel(
        @Path("channelId") channelId: String,
        @PartMap parts: Map<String, @JvmSuppressWildcards RequestBody>,
        @Part profileImage: MultipartBody.Part?,
        @Part bannerImage: MultipartBody.Part?
    ): Response<ChannelResponseDto>

    @GET("channels/public/{identifier}")
    suspend fun getPublicChannel(
        @Path("identifier") identifier: String
    ): Response<ChannelResponseDto>
}