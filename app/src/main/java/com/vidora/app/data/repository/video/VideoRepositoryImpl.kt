package com.vidora.app.data.repository.video

import com.vidora.app.data.remote.models.comment.toCommentDomain
import com.vidora.app.data.remote.models.video.VideoApi
import com.vidora.app.data.remote.models.video.VideoPage
import com.vidora.app.data.remote.models.video.toPageDomain
import com.vidora.app.data.remote.models.video.toVideoDomain
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.IOException
import javax.inject.Inject

class VideoRepositoryImpl @Inject constructor(
    private val api: VideoApi
): VideoRepository {
    override suspend fun uploadVideo(
        title: RequestBody,
        description: RequestBody?,
        categories: RequestBody,
        tags: RequestBody?,
        visibility: RequestBody,
        videoFile: MultipartBody.Part,
        thumbnail: MultipartBody.Part?
    ) = runCatching {
        val resp = api.uploadVideo(
            title,
            description,
            categories,
            tags,
            visibility,
            videoFile,
            thumbnail)
        if (!resp.isSuccessful || resp.body() == null) throw  Exception(resp.message())
        resp.body()!!.toVideoDomain()
    }

    override suspend fun getVideo(id: String) = runCatching {
        val resp = api.getVideoById(id, "true") // Pass edit=true
        if (!resp.isSuccessful || resp.body() == null) {
            throw Exception("Load failed: ${resp.code()} ${resp.errorBody()?.string()}")
        }
        resp.body()!!.toVideoDomain()
    }

    override suspend fun listVideos(
        channelId: String?,
        page: Int?,
        limit: Int?
    ): Result<VideoPage> = runCatching {
        // 1) Fire off the HTTP request
        val response = api.listVideos(channelId, page, limit)

        // 2) HTTP‐status check
        if (!response.isSuccessful) {
            // you could also read response.errorBody()?.string() for more detail
            throw IOException("List failed: ${response.code()} ${response.message()}")
        }

        // 3) Null‐body check
        val dtoPage = response.body()
            ?: throw IOException("List failed: empty response body")

        // 4) Map the DTOs to your domain and return
        dtoPage.toPageDomain()
    }


    override suspend fun updateVideo(
        id: String,
        parts: Map<String, RequestBody>,
        thumbnail: MultipartBody.Part?
    ) = runCatching {
        val resp = api.updateVideo(id, parts, thumbnail)
        if (!resp.isSuccessful || resp.body() == null) {
            throw Exception("Update failed: ${resp.code()} ${resp.errorBody()?.string()}")
        }
        resp.body()!!.toVideoDomain()
    }

    override suspend fun deleteVideo(id: String) = runCatching {
        val resp = api.deleteVideo(id)
        if (!resp.isSuccessful) throw Exception("Delete failed: ${resp.code()}")
    }

    override suspend fun likeVideo(id: String) = runCatching {
        val resp = api.likeVideo(id)
        if (!resp.isSuccessful || resp.body() == null) throw Exception(resp.message())
        Pair(resp.body()!!.likes, resp.body()!!.dislikes)
    }

    override suspend fun dislikeVideo(id: String) = runCatching {
        val resp = api.dislikeVideo(id)
        if (!resp.isSuccessful || resp.body() == null) throw Exception(resp.message())
        Pair(resp.body()!!.likes, resp.body()!!.dislikes)
    }

    override suspend fun addComment(videoId: String, text: String) = runCatching {
        val resp = api.addComment(videoId, mapOf("text" to text))
        if (!resp.isSuccessful || resp.body() == null) throw Exception(resp.message())
        resp.body()!!.toCommentDomain()
    }

    override suspend fun getComments(videoId: String) = runCatching {
        val resp = api.getComments(videoId)
        if (!resp.isSuccessful || resp.body() == null) throw Exception(resp.message())
        resp.body()!!.map { it.toCommentDomain() }
    }

    override suspend fun deleteComment(videoId: String, commentId: String) = runCatching {
        val resp = api.deleteComment(videoId, commentId)
        if (!resp.isSuccessful || resp.body() == null) throw Exception("Delete failed: ${resp.code()}")
    }

    override suspend fun addWatchHistory(videoId: String) = runCatching {
        val resp = api.addWatchHistory(videoId)
        if (!resp.isSuccessful) throw Exception(resp.message())
    }

    override suspend fun toggleSaveForLater(videoId: String) = runCatching {
        val resp = api.toggleSaveForLater(videoId)
        if (!resp.isSuccessful || resp.body() == null) throw Exception(resp.message())
        resp.body()!!.saved
    }

    override suspend fun addDownload(videoId: String) = runCatching {
        val resp = api.addDownload(videoId)
        if (!resp.isSuccessful) throw Exception(resp.message())
    }

    override suspend fun addViews(videoId: String): Result<Unit> {
        return try {
            val response = api.addViews(videoId)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Failed to add view"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}