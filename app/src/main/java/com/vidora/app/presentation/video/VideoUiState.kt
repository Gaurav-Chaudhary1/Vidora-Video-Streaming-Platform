package com.vidora.app.presentation.video

import com.vidora.app.data.remote.models.comment.Comment
import com.vidora.app.data.remote.models.video.Video

sealed class VideoUiState {
    object Idle : VideoUiState()
    object Loading: VideoUiState()

    data class DetailLoaded(val video: Video) : VideoUiState()
    data class UpdateSuccess(val video: Video) : VideoUiState()

    data class Success(val video: Video): VideoUiState()
    data class Deleted(val message: String?): VideoUiState()
    data class Interaction(val likes: Int, val dislikes: Int): VideoUiState()
    data class Comments(val comments: List<Comment>): VideoUiState()
    data class Error(val message: String): VideoUiState()
    data class Saved(val saved: Boolean) : VideoUiState()
    data class Message(val text: String) : VideoUiState()
    data class Lists(
        val page: Int,
        val totalPages: Int,
        val totalVideos: Int,
        val videos: kotlin.collections.List<Video>
    ): VideoUiState()
}