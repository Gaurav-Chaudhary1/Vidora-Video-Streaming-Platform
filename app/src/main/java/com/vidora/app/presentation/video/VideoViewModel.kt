package com.vidora.app.presentation.video

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vidora.app.data.repository.file.FileRepository
import com.vidora.app.data.repository.video.VideoRepository
import com.vidora.app.utils.toRequestBody
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import okhttp3.MultipartBody
import javax.inject.Inject

@HiltViewModel
class VideoViewModel @Inject constructor(
    private val repository: VideoRepository,
    private val fileRepo: FileRepository
) : ViewModel() {

    private val _state = MutableStateFlow<VideoUiState>(VideoUiState.Idle)
    val state: StateFlow<VideoUiState> = _state.asStateFlow()

    // ––––– detail video state –––––
    private val _detail = MutableStateFlow<VideoUiState>(VideoUiState.Idle)
    val detail: StateFlow<VideoUiState> = _detail.asStateFlow()

    // ––––– up‑next list state –––––
    private val _upNext = MutableStateFlow<VideoUiState>(VideoUiState.Idle)
    val upNext: StateFlow<VideoUiState> = _upNext.asStateFlow()


    fun getVideo(id: String) = viewModelScope.launch {
        _detail.value = VideoUiState.Loading
        repository.getVideo(id)
            .onSuccess { _detail.value = VideoUiState.Success(it) }
            .onFailure { _detail.value = VideoUiState.Error(it.message.orEmpty()) }
    }

    fun loadUpNext(channelId: String, excludeId: String) = viewModelScope.launch {
        _upNext.value = VideoUiState.Loading
        repository.listVideos(channelId)
            .onSuccess { page ->
                val list = page.videos
                    .filter    { it.id != excludeId }
                _upNext.value = VideoUiState.Lists(
                    page       = page.page,
                    totalPages = page.totalPages,
                    totalVideos= page.totalVideos,
                    videos     = list
                )
            }
            .onFailure { _upNext.value = VideoUiState.Error(it.message.orEmpty()) }
    }

    fun uploadVideo(
        title: String,
        description: String,
        categories: String,
        tags: String,
        visibility: String,
        videoFile: MultipartBody.Part,
        thumbnail: MultipartBody.Part?
    ) {
        viewModelScope.launch {
            _state.value = VideoUiState.Loading

            val titleBody = title.toRequestBody()
            val descriptionBody = description.toRequestBody()
            val categoriesBody = categories.toRequestBody()
            val tagsBody = tags.toRequestBody()
            val visibilityBody = visibility.toRequestBody()

            repository.uploadVideo(
                titleBody,
                descriptionBody,
                categoriesBody,
                tagsBody,
                visibilityBody,
                videoFile,
                thumbnail
            )
                .onSuccess { _state.value = VideoUiState.Success(it) }
                .onFailure { _state.value = VideoUiState.Error(it.message.orEmpty()) }
        }
    }

    fun showVideo(id: String) = viewModelScope.launch {
        _detail.value = VideoUiState.Loading
        repository.getVideo(id)
            .onSuccess { _detail.value = VideoUiState.DetailLoaded(it) }
            .onFailure { _detail.value = VideoUiState.Error(it.message.orEmpty()) }
    }

    fun listVideos(
        channelId: String? = null,
        page: Int? = 1,
        limit: Int? = 20
    ) {
        viewModelScope.launch {
            _state.value = VideoUiState.Loading
            repository.listVideos(channelId, page, limit)
                .onSuccess { pageData ->
                    val currentList = when (val current = _state.value) {
                        is VideoUiState.Lists -> current.videos
                        else -> emptyList()
                    }

                    val newList = if (page == 1) pageData.videos
                    else currentList + pageData.videos

                    _state.value = VideoUiState.Lists(
                        videos = newList,
                        page = pageData.page,
                        totalPages = pageData.totalPages,
                        totalVideos = pageData.totalVideos
                    )
                }
                .onFailure {
                    _state.value = VideoUiState.Error(it.message.orEmpty())
                }
        }
    }

    fun refresh(channelId: String? = null, page: Int? = 1, limit: Int? = 20) {
        viewModelScope.launch {
            listVideos(channelId, page, limit)
        }
    }

    fun updateVideo(
        id: String,
        data: Map<String, String>,
        thumbnail: MultipartBody.Part?
    ) {
        viewModelScope.launch {
            _state.value = VideoUiState.Loading
            val partsMap = data.mapValues { it.value.toRequestBody() }
            repository.updateVideo(id, partsMap, thumbnail)
                .onSuccess {
                    _detail.value = VideoUiState.DetailLoaded(it)
                    _state.value = VideoUiState.UpdateSuccess(it)
                }
                .onFailure {
                    _state.value = VideoUiState.Error(it.message.orEmpty())
                }
        }
    }

    fun deleteVideo(
        id: String
    ) {
        viewModelScope.launch {
            _state.value = VideoUiState.Loading
            repository.deleteVideo(id)
                .onSuccess { _state.value = VideoUiState.Deleted("Video deleted") }
                .onFailure { _state.value = VideoUiState.Error(it.message.orEmpty()) }
        }
    }

    fun likeVideo(id: String) {
        viewModelScope.launch {
            repository.likeVideo(id)
                .onSuccess { (likes, dislikes) ->
                    _state.value = VideoUiState.Interaction(likes, dislikes)
                }
                .onFailure { _state.value = VideoUiState.Error(it.message.orEmpty()) }
        }
    }

    fun dislikeVideo(id: String) {
        viewModelScope.launch {
            repository.dislikeVideo(id)
                .onSuccess { (likes, dislikes) ->
                    _state.value = VideoUiState.Interaction(likes, dislikes)
                }
                .onFailure { _state.value = VideoUiState.Error(it.message.orEmpty()) }
        }
    }

    fun addComment(id: String, text: String) {
        viewModelScope.launch {
            repository.addComment(id, text)
                .onSuccess { loadComments(id) }
                .onFailure { _state.value = VideoUiState.Error(it.message.orEmpty()) }
        }
    }

    fun loadComments(id: String) {
        viewModelScope.launch {
            repository.getComments(id)
                .onSuccess { _state.value = VideoUiState.Comments(it) }
                .onFailure { _state.value = VideoUiState.Error(it.message.orEmpty()) }
        }
    }

    fun deleteComment(videoId: String, commentId: String){
        viewModelScope.launch {
            repository.deleteComment(videoId, commentId)
                .onSuccess { loadComments(id = videoId) }
                .onFailure { _state.value = VideoUiState.Error(it.message.orEmpty()) }
        }
    }

    fun addWatchHistory(id: String){
        viewModelScope.launch {
            repository.addWatchHistory(id)
                .onSuccess { _state.value = VideoUiState.Message("") }
                .onFailure { _state.value = VideoUiState.Error(it.message.orEmpty()) }
        }
    }

    fun toggleSaveForLater(id: String){
        viewModelScope.launch {
            repository.toggleSaveForLater(id)
                .onSuccess { _state.value = VideoUiState.Saved(it) }
                .onFailure { _state.value = VideoUiState.Error(it.message.orEmpty()) }
        }
    }

    fun addDownload(id: String){
        viewModelScope.launch {
            repository.addDownload(id)
                .onSuccess { _state.value = VideoUiState.Message("Downloaded successfully") }
                .onFailure { _state.value = VideoUiState.Error(it.message.orEmpty()) }
        }
    }

    fun addViews(videoId: String) {
        viewModelScope.launch {
            repository.addViews(videoId)
                .onSuccess {  }
                .onFailure { _state.value = VideoUiState.Error(it.message.orEmpty()) }
        }
    }

    fun clearState() {
        _state.value = VideoUiState.Idle
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
    suspend fun signedUrlForSingle(rawUrl: String): String? =
        runCatching { fileRepo.fetchSignedUrl(rawUrl).getOrThrow() }
            .getOrNull()

}