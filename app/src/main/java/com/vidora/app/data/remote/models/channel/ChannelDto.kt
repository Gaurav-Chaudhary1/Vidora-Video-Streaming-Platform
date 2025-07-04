package com.vidora.app.data.remote.models.channel

import coil.request.Tags
import com.google.gson.annotations.SerializedName

data class ChannelResponseDto(
    @SerializedName("_id") val id: String,
    val name: String,
    val description: String?,
    @SerializedName("handleChannelName") val handle: String,
    val profilePictureUrl: String?,
    val bannerUrl: String?,
    val socialLinks: Map<String, String>?,
    val totalSubscribers: Int?,
    val totalViews: Int?,
    val location: String?,
    val contactEmail: String?,
    val videos: List<String>?,
    val tags: List<String>,
    val createdAt: String?
){
    fun toDomain(): Channel = Channel(
        id = id,
        name = name,
        description = description,
        handle = handle,
        profilePictureUrl = profilePictureUrl,
        bannerUrl = bannerUrl,
        socialLinks = socialLinks,
        totalSubscribers = totalSubscribers ?: 0,
        totalViews = totalViews ?: 0,
        location = location,
        contactEmail = contactEmail,
        videos = this.videos,
        tags = tags,
        createdAt = createdAt
    )
}