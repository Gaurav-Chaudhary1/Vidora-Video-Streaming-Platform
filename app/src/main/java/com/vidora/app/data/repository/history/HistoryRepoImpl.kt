package com.vidora.app.data.repository.history

import com.vidora.app.data.remote.models.video.history.HistoryApi
import com.vidora.app.data.remote.models.video.history.VideoDetails
import javax.inject.Inject

class HistoryRepoImpl @Inject constructor(
    private val api: HistoryApi
) : HistoryRepo {

    override suspend fun getWatchHistory(): Result<List<VideoDetails>> = runCatching {
        val resp = api.getWatchHistory()
        if (!resp.isSuccessful || resp.body() == null) {
            throw Exception(resp.message())
        }
        resp.body()!!.videos
    }

    override suspend fun getSavedVideos(): Result<List<VideoDetails>> = runCatching {
        val resp = api.getSavedVideos()
        if (!resp.isSuccessful || resp.body() == null) {
            throw Exception(resp.message())
        }
        resp.body()!!.videos
    }

    override suspend fun getDownloadedVideos(): Result<List<VideoDetails>> = runCatching {
        val resp = api.getDownloadedVideos()
        if (!resp.isSuccessful || resp.body() == null) {
            throw Exception(resp.message())
        }
        resp.body()!!.videos
    }
}