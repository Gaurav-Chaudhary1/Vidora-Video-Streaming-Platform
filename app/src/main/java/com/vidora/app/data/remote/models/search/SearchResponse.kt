package com.vidora.app.data.remote.models.search

import com.vidora.app.data.remote.models.video.ChannelDto
import com.vidora.app.data.remote.models.video.SearchChannel
import com.vidora.app.data.remote.models.video.Video
import com.vidora.app.data.remote.models.video.VideoResponseDto
import com.vidora.app.data.remote.models.video.toChannelDomain
import com.vidora.app.data.remote.models.video.toVideoDomain

data class SearchResponse(
    val channel: ChannelDto?,
    val channelVideos: List<VideoResponseDto>,
    val videos: List<VideoResponseDto>
)

data class SearchResult(
    val channel: SearchChannel?,
    val channelVideos: List<Video>,
    val videos: List<Video>
)

fun SearchResponse.toDomain(): SearchResult = SearchResult(
    channel = this.channel?.toChannelDomain(),
    channelVideos = this.channelVideos.map { it.toVideoDomain() },
    videos = this.videos.map { it.toVideoDomain() }
)