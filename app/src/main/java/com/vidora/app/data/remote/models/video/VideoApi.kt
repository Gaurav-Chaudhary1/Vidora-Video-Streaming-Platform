package com.vidora.app.data.remote.models.video

import com.vidora.app.data.remote.models.comment.CommentResponseDto
import com.vidora.app.data.remote.models.comment.LikeDislikeResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.PartMap
import retrofit2.http.Path
import retrofit2.http.Query

interface VideoApi {

    @Multipart
    @POST("videos/upload")
    suspend fun uploadVideo(
        @Part("title") title: RequestBody,
        @Part("description") description: RequestBody?,
        @Part("categories") categories: RequestBody,
        @Part("tags") tags: RequestBody?,
        @Part("visibility") visibility: RequestBody,
        @Part videoFile: MultipartBody.Part,
        @Part thumbnailImage: MultipartBody.Part?
    ): Response<VideoResponseDto>

    @GET("videos/{id}")
    suspend fun getVideoById(
        @Path("id") id: String,
        @Query("edit") edit: String? = null
    ): Response<VideoResponseDto>

    @GET("videos")
    suspend fun listVideos(
        @Query("channelId") id: String? = null,
        @Query("page") page: Int? = null,
        @Query("limit") limit: Int? = null
    ): Response<VideoPageResponseDto>

    @Multipart
    @PUT("videos/{id}")
    suspend fun updateVideo(
        @Path("id") id: String,
        @PartMap parts: Map<String, @JvmSuppressWildcards RequestBody>,
        @Part thumbnail: MultipartBody.Part?
    ): Response<VideoResponseDto>

    @DELETE("videos/{videoId}")
    suspend fun deleteVideo(
        @Path("videoId") id: String
    ): Response<Unit>

    @POST("videos/{videoId}/like")
    suspend fun likeVideo(
        @Path("videoId") id: String
    ): Response<LikeDislikeResponse>

    @POST("videos/{videoId}/dislike")
    suspend fun dislikeVideo(
        @Path("videoId") id: String
    ): Response<LikeDislikeResponse>

    @POST("videos/{videoId}/comment")
    suspend fun addComment(
        @Path("videoId") id: String,
        @Body body: Map<String, String>
    ): Response<CommentResponseDto>

    @GET("videos/{videoId}/comments")
    suspend fun getComments(@Path("videoId") id: String): Response<List<CommentResponseDto>>

    @DELETE("videos/{videoId}/comments/{commentId}")
    suspend fun deleteComment(
        @Path("videoId") videoId: String,
        @Path("commentId") commentId: String
    ): Response<BaseResponse>

    @POST("videos/{videoId}/watch")
    suspend fun addWatchHistory(
        @Path("videoId") id: String
    ): Response<BaseResponse>

    @POST("videos/{videoId}/view")
    suspend fun addViews(
        @Path("videoId") id: String
    ): Response<Unit>

    @POST("videos/{videoId}/save")
    suspend fun toggleSaveForLater(
        @Path("videoId") id: String
    ): Response<SaveResponse>

    @POST("videos/{videoId}/download")
    suspend fun addDownload(
        @Path("videoId") id: String
    ): Response<BaseResponse>

}