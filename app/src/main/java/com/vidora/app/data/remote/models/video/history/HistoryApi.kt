package com.vidora.app.data.remote.models.video.history

import retrofit2.Response
import retrofit2.http.GET

interface HistoryApi {
    @GET("videos/history")
    suspend fun getWatchHistory(): Response<HistoryResponse>

    @GET("videos/saved")
    suspend fun getSavedVideos(): Response<HistoryResponse>

    @GET("videos/downloads")
    suspend fun getDownloadedVideos(): Response<HistoryResponse>
}