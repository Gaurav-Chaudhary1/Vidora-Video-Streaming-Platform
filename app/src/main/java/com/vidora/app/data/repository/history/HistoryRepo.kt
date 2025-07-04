package com.vidora.app.data.repository.history

import com.vidora.app.data.remote.models.video.history.VideoDetails

interface HistoryRepo {
    suspend fun getWatchHistory(): Result<List<VideoDetails>>
    suspend fun getSavedVideos(): Result<List<VideoDetails>>
    suspend fun getDownloadedVideos(): Result<List<VideoDetails>>
}