package com.vidora.app.presentation.subscription

import com.vidora.app.data.remote.models.subscription.ChannelResponse
import com.vidora.app.data.remote.models.subscription.SubscribeResponse

sealed class SubscriptionUIState {
    object Idle: SubscriptionUIState()
    object Loading: SubscriptionUIState()

    data class Subscribed(val response: SubscribeResponse): SubscriptionUIState()
    data class ListLoaded(val channels: List<ChannelResponse>): SubscriptionUIState()
    data class Error(val message: String): SubscriptionUIState()
}