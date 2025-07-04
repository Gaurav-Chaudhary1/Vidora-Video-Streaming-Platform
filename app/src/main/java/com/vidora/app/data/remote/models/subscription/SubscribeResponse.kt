package com.vidora.app.data.remote.models.subscription

import com.google.gson.annotations.SerializedName

data class SubscribeResponse(
    val subscribed: Boolean,
    val totalSubscribers: Int
)

data class ChannelResponse(
    @SerializedName("_id") val id: String,
    val name: String,
    val description: String,
    val profilePictureUrl: String?,
    val handleChannelName: String,
    val totalSubscribers: Int,
    val videos: List<VideoResponse>
)

data class VideoResponse(
    @SerializedName("_id") val id: String,
    val title: String,
    val thumbnailUrl: String?
)