package com.vidora.app.data.repository.channel

import com.vidora.app.data.remote.models.channel.Channel
import okhttp3.MultipartBody
import okhttp3.RequestBody

interface ChannelRepository {

    suspend fun createChannel(
        name: RequestBody,
        description: RequestBody,
        profileImage: MultipartBody.Part?
    ): Result<Channel>

    suspend fun updateChannel(
        channelId: String,
        parts: Map<String, RequestBody>,
        profileImage: MultipartBody.Part?,
        bannerImage: MultipartBody.Part?
    ): Result<Channel>

    suspend fun getPublicChannel(identifier: String): Result<Channel>
}