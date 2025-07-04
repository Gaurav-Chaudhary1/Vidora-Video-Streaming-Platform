package com.vidora.app.data.repository.channel

import com.vidora.app.data.remote.models.channel.ChannelApi
import com.vidora.app.data.remote.models.channel.Channel
import okhttp3.MultipartBody
import okhttp3.RequestBody
import javax.inject.Inject

class ChannelRepositoryImpl @Inject constructor(
    private val api: ChannelApi
): ChannelRepository {
    override suspend fun createChannel(
        name: RequestBody,
        description: RequestBody,
        profileImage: MultipartBody.Part?
    ): Result<Channel> {
        return try {
            val response = api.createChannel(name, description, profileImage)
            if (response.isSuccessful && response.body() != null){
                Result.success(response.body()!!.toDomain())
            } else {
                Result.failure(Exception(response.message()))
            }
        } catch (e: Exception){
            Result.failure(e)
        }
    }

    override suspend fun updateChannel(
        channelId: String,
        parts: Map<String, RequestBody>,
        profileImage: MultipartBody.Part?,
        bannerImage: MultipartBody.Part?
    ): Result<Channel> {
        return try {
            val response = api.updateChannel(channelId, parts, profileImage,bannerImage)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!.toDomain())
            } else {
                Result.failure(Exception(response.message()))
            }
        } catch (e: Exception){
            Result.failure(e)
        }
    }

    override suspend fun getPublicChannel(identifier: String): Result<Channel> {
        return try {
            val response = api.getPublicChannel(identifier)
            if (response.isSuccessful && response.body() != null){
                Result.success(response.body()!!.toDomain())
            } else{
                Result.failure(Exception(response.message()))
            }
        } catch (e: Exception){
            Result.failure(e)
        }
    }
}