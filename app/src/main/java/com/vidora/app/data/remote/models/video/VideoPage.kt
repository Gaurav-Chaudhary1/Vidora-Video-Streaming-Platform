package com.vidora.app.data.remote.models.video

data class VideoPage(
    val page: Int,
    val totalPages: Int,
    val totalVideos: Int,
    val videos: List<Video>
)

data class VideoPageResponseDto(
    val page: Int,
    val totalPages: Int,
    val totalVideos: Int,
    val videos: List<VideoResponseDto>
)

fun VideoPageResponseDto.toPageDomain(): VideoPage = VideoPage(
    page = page,
    totalPages = totalPages,
    totalVideos = totalVideos,
    videos = videos.map { it.toVideoDomain() }
)