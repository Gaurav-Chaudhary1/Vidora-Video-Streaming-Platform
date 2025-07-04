package com.vidora.app.data.remote.models.video

import com.google.gson.annotations.SerializedName

data class Video(
    val id: String,
    val title: String,
    val description: String,
    val channelId: String,
    val channelName: String,
    val channelProfilePictureUrl: String?,
    val uploaderId: String,
    val uploaderName: String,
    val categories: List<String>,
    val tags: List<String>,
    val visibility: String,
    val videoUrls: VideoUrlsDto,
    val thumbnailUrl: String?,
    val duration: Double,
    val sizeInMB: Double,
    val views: Int,
    val likes: List<String>,
    val dislikes: List<String>,
    val comments: List<String>,
    val isMonetized: Boolean,
    val isAgeRestricted: Boolean,
    val createdAt: String?
)

data class VideoResponseDto(
    @SerializedName("_id") val _id: String,
    val title: String,
    val description: String,
    val channelId: ChannelDto,
    val uploader: UploaderDto,
    val categories: List<String>,
    val tags: List<String>,
    val visibility: String,
    val videoUrls: VideoUrlsDto,
    val thumbnailUrl: String?,
    val duration: Double,
    val sizeInMB: Double,
    val views: Int,
    val likes: List<String>,
    val dislikes: List<String>,
    val comments: List<String>,
    val isMonetized: Boolean,
    val isAgeRestricted: Boolean,
    val createdAt: String?
)

fun VideoResponseDto.toVideoDomain(): Video = Video(
    id = _id,
    title = title,
    description = description,
    channelId = channelId.id,
    channelName = channelId.name,
    channelProfilePictureUrl = channelId.profilePictureUrl,
    uploaderId = uploader.id,
    uploaderName = "${uploader.firstName} ${uploader.lastName}",
    categories = categories,
    tags = tags,
    visibility = visibility,
    videoUrls = videoUrls,
    thumbnailUrl = thumbnailUrl,
    duration = duration,
    sizeInMB = sizeInMB,
    views = views,
    likes = likes,
    dislikes = dislikes,
    comments = comments,
    isMonetized = isMonetized,
    isAgeRestricted = isAgeRestricted,
    createdAt = createdAt
)

// Channel summary for videos (simplified)
data class ChannelDto(
    @SerializedName("_id") val id: String,
    val name: String,
    val profilePictureUrl: String?,
    val handleChannelName: String
)

// Uploader summary for videos
data class UploaderDto(
    @SerializedName("_id") val id: String,
    val firstName: String,
    val lastName: String
)

data class VideoUrlsDto(
    val original: String,
    val resolutions: Map<String, String> = emptyMap()
)

data class SaveResponse(
    val saved: Boolean
)

data class BaseResponse(
    val message: String
)

data class SearchChannel(
    val id: String,
    val name: String,
    val profilePictureUrl: String?,
    val handleChannelName: String
)

fun ChannelDto.toChannelDomain(): SearchChannel = SearchChannel(
    id = id,
    name = name,
    profilePictureUrl = profilePictureUrl,
    handleChannelName = handleChannelName
)







