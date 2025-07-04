package com.vidora.app.data.remote.models.channel

data class Channel(
    val id: String,
    val name: String,
    val description: String?,
    val handle: String,
    val profilePictureUrl: String?,
    val bannerUrl: String?,
    val socialLinks: Map<String, String>?,
    val totalSubscribers: Int,
    val totalViews: Int,
    val location: String?,
    val contactEmail: String?,
    val videos: List<String>?,
    val tags: List<String>?,
    val createdAt: String?
)