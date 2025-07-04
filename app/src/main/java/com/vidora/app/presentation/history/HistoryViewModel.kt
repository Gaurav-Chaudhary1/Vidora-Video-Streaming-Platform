package com.vidora.app.presentation.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vidora.app.data.repository.file.FileRepository
import com.vidora.app.data.repository.history.HistoryRepo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val repository: HistoryRepo,
    private val fileRepo: FileRepository
): ViewModel() {

    private val _historyState = MutableStateFlow<HistoryUIState>(HistoryUIState.Idle)
    val historyState: StateFlow<HistoryUIState> = _historyState

    private val _savedState     = MutableStateFlow<HistoryUIState>(HistoryUIState.Idle)
    val savedState: StateFlow<HistoryUIState> = _savedState

    private val _downloadsState = MutableStateFlow<HistoryUIState>(HistoryUIState.Idle)
    val downloadState: StateFlow<HistoryUIState> = _downloadsState

    fun loadWatchHistory() {
        viewModelScope.launch {
            _historyState.value = HistoryUIState.Loading
            repository.getWatchHistory()
                .onSuccess { _historyState.value = HistoryUIState.Success(it) }
                .onFailure { _historyState.value = HistoryUIState.Error(it.message.orEmpty()) }
        }
    }

    fun loadSavedVideos() {
        viewModelScope.launch {
            _savedState.value = HistoryUIState.Loading
            repository.getSavedVideos()
                .onSuccess { _savedState.value = HistoryUIState.Success(it) }
                .onFailure { _savedState.value = HistoryUIState.Error(it.message.orEmpty()) }
        }
    }

    fun loadDownloadedVideos() {
        viewModelScope.launch {
            _downloadsState.value = HistoryUIState.Loading
            repository.getDownloadedVideos()
                .onSuccess { _downloadsState.value = HistoryUIState.Success(it) }
                .onFailure { _downloadsState.value = HistoryUIState.Error(it.message.orEmpty()) }
        }
    }

    fun signedUrlFor(rawUrl: String?) = flow {
        if (rawUrl.isNullOrBlank()) {
            emit(null)
        } else {
            val signed = runCatching { fileRepo.fetchSignedUrl(rawUrl).getOrThrow() }
                .getOrNull()
            emit(signed)
        }
    }.flowOn(Dispatchers.IO)

    /** One‑off suspend helper for produceState */
    suspend fun signedUrlForHistory(rawUrl: String): String? =
        runCatching { fileRepo.fetchSignedUrl(rawUrl).getOrThrow() }
            .getOrNull()
}