package com.vidora.app.data.remote.models.video.history

import com.google.gson.annotations.SerializedName

data class HistoryResponse(
    val videos: List<VideoDetails>
)

data class VideoDetails(
    @SerializedName("_id") val id: String,
    val title: String,
    val description: String?,
    val thumbnailUrl: String?,
    val channelId: ChannelDetail,
    val views: Int,
    val likes: List<String>,
    val createdAt: String?
)

data class ChannelDetail(
    @SerializedName("_id") val id: String,
    val name: String,
    val profilePictureUrl: String?
)
