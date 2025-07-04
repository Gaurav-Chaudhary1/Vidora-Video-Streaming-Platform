package com.vidora.app.presentation.subscription

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vidora.app.data.local.datastore.SubscriptionDataStore
import com.vidora.app.data.repository.file.FileRepository
import com.vidora.app.data.repository.subscription.SubscriptionRepo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SubscriptionViewModel @Inject constructor(
    private val repository: SubscriptionRepo,
    private val dataStore: SubscriptionDataStore,
    private val fileRepo: FileRepository
): ViewModel() {

    private val _toggleState = MutableStateFlow<SubscriptionUIState>(SubscriptionUIState.Idle)
    val toggleState: StateFlow<SubscriptionUIState> = _toggleState

    private val _subscriptions = MutableStateFlow<SubscriptionUIState>(SubscriptionUIState.Idle)
    val subscriptions: StateFlow<SubscriptionUIState> = _subscriptions

    val subscribedIds: StateFlow<Set<String>> = dataStore.subscribedIds
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptySet())

    fun loadSubscriptions() {
        viewModelScope.launch {
            _subscriptions.value = SubscriptionUIState.Loading
            repository.mySubscribedChannel()
                .onSuccess { list -> _subscriptions.value = SubscriptionUIState.ListLoaded(list) }
                .onFailure { ex -> _subscriptions.value = SubscriptionUIState.Error(ex.message.orEmpty()) }
        }
    }

    fun subscribe(channelId: String) {
        viewModelScope.launch {
            dataStore.saveSubscription(channelId)
            _toggleState.value = SubscriptionUIState.Loading
            repository.subscribeChannel(channelId)
                .onSuccess { resp ->
                    _toggleState.value = SubscriptionUIState.Subscribed(resp)
                    loadSubscriptions()
                }
                .onFailure { ex -> _toggleState.value = SubscriptionUIState.Error(ex.message.orEmpty()) }
        }
    }

    fun unsubscribe(channelId: String) {
        viewModelScope.launch {
            dataStore.deleteSubscription(channelId)
            _toggleState.value = SubscriptionUIState.Loading
            repository.unsubscribeChannel(channelId)
                .onSuccess { resp ->
                    _toggleState.value = SubscriptionUIState.Subscribed(resp)
                    loadSubscriptions()
                }
                .onFailure { ex -> _toggleState.value = SubscriptionUIState.Error(ex.message.orEmpty()) }
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
    suspend fun signedUrlForSubscribe(rawUrl: String): String? =
        runCatching { fileRepo.fetchSignedUrl(rawUrl).getOrThrow() }
            .getOrNull()

}