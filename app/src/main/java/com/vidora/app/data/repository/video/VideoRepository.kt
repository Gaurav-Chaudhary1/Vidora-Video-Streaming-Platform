package com.vidora.app.data.repository.video

import com.vidora.app.data.remote.models.comment.Comment
import com.vidora.app.data.remote.models.video.Video
import com.vidora.app.data.remote.models.video.VideoPage
import com.vidora.app.data.remote.models.video.VideoPageResponseDto
import okhttp3.MultipartBody
import okhttp3.RequestBody

interface VideoRepository {

    suspend fun uploadVideo(
        title: RequestBody,
        description: RequestBody?,
        categories: RequestBody,
        tags: RequestBody?,
        visibility: RequestBody,
        videoFile: MultipartBody.Part,
        thumbnail: MultipartBody.Part?,
    ): Result<Video>

    suspend fun getVideo(id: String): Result<Video>

    suspend fun listVideos(
        channelId: String? = null,
        page: Int? = null,
        limit: Int? = null
    ): Result<VideoPage>

    suspend fun updateVideo(
        id: String,
        parts: Map<String, RequestBody>,
        thumbnail: MultipartBody.Part?
    ): Result<Video>

    suspend fun deleteVideo(id: String): Result<Unit>

    suspend fun likeVideo(id: String): Result<Pair<Int, Int>>

    suspend fun dislikeVideo(id: String): Result<Pair<Int, Int>>

    suspend fun addComment(videoId: String, text: String): Result<Comment>

    suspend fun getComments(videoId: String): Result<List<Comment>>

    suspend fun deleteComment(videoId: String, commentId: String): Result<Unit>

    suspend fun addWatchHistory(videoId: String): Result<Unit>

    suspend fun toggleSaveForLater(videoId: String): Result<Boolean>

    suspend fun addDownload(videoId: String): Result<Unit>

    suspend fun addViews(videoId: String): Result<Unit>

}