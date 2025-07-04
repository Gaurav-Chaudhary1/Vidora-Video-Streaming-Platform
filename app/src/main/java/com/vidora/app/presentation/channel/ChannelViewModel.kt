package com.vidora.app.presentation.channel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.vidora.app.data.repository.channel.ChannelRepository
import com.vidora.app.data.repository.file.FileRepository
import com.vidora.app.utils.toRequestBody
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import javax.inject.Inject

@HiltViewModel
class ChannelViewModel @Inject constructor(
    private val repository: ChannelRepository,
    private val fileRepo: FileRepository
): ViewModel() {

    private val _state = MutableStateFlow<ChannelState>(ChannelState.Idle)
    val state: StateFlow<ChannelState> = _state.asStateFlow()

    fun loadChannel(id: String) {
        viewModelScope.launch {
            _state.value = ChannelState.Loading
            repository.getPublicChannel(id)
                .onSuccess { channel ->
                    // fetch signed URLs concurrently
                    val profileDeferred = async { channel.profilePictureUrl?.let { fileRepo.fetchSignedUrl(it).getOrNull() } }
                    val bannerDeferred = async { channel.bannerUrl?.let { fileRepo.fetchSignedUrl(it).getOrNull() } }
                    val signedProfile = profileDeferred.await()
                    val signedBanner = bannerDeferred.await()
                    _state.value = ChannelState.Success(channel, signedProfile, signedBanner)
                }
                .onFailure { ex ->
                    _state.value = ChannelState.Error(ex.message.orEmpty())
                }
        }
    }

    fun refreshChannel(id: String) {
        loadChannel(id)
    }

    fun createChannel(
        name: String,
        description: String,
        profileImagePart: MultipartBody.Part?
    ) {
        viewModelScope.launch {
            _state.value = ChannelState.Loading

            val nameBody = name.toRequestBody()
            val descBody = description.toRequestBody()

            repository.createChannel(nameBody, descBody, profileImagePart)
                .onSuccess { createdChannel ->
                    // Once the channel is created, immediately load it
                    // so we pick up all fields (including signed URLs)
                    loadChannel(createdChannel.id)
                }
                .onFailure { ex ->
                    _state.value = ChannelState.Error(ex.message.orEmpty())
                }
        }
    }

    fun updateChannel(
        channelId: String,
        name: String,
        description: String,
        location: String,
        tagsCsv: String,
        contactEmail: String,
        socialLinks: Map<String, String>,
        profileImagePart: MultipartBody.Part?,
        bannerImagePart: MultipartBody.Part?
    ) {
        viewModelScope.launch {
            _state.value = ChannelState.Loading

            // Build up all fields into the PartMap
            val parts = mutableMapOf<String, RequestBody>().apply {
                put("name", name.toRequestBody())
                put("description", description.toRequestBody())
                put("location", location.toRequestBody())
                put("tags", tagsCsv.toRequestBody())
                put("contactEmail", contactEmail.toRequestBody())

                if (socialLinks.isNotEmpty()) {
                    // Serialize the social‐links map to JSON
                    val json = Gson().toJson(socialLinks)
                    put("socialLinks", json.toRequestBody("application/json".toMediaTypeOrNull()))
                }
            }

            repository
                .updateChannel(channelId, parts, profileImagePart, bannerImagePart)
                .onSuccess { loadChannel(channelId) }
                .onFailure { ex ->
                    _state.value = ChannelState.Error(ex.message.orEmpty())
                }
        }

    }
    fun signedUrl(rawUrl: String?) = flow {
        if (rawUrl.isNullOrBlank()) {
            emit(null)
        } else {
            val signed = runCatching { fileRepo.fetchSignedUrl(rawUrl).getOrThrow() }
                .getOrNull()
            emit(signed)
        }
    }.flowOn(Dispatchers.IO)

    /** One‑off suspend helper for produceState */
    suspend fun signedUrlForChannel(rawUrl: String): String? =
        runCatching { fileRepo.fetchSignedUrl(rawUrl).getOrThrow() }
            .getOrNull()

}