package com.vidora.app.presentation.history

import com.vidora.app.data.remote.models.video.history.VideoDetails

sealed class HistoryUIState {
    object Idle: HistoryUIState()
    object Loading: HistoryUIState()

    data class Success(val videos: List<VideoDetails>): HistoryUIState()
    data class Error(val message: String): HistoryUIState()
}