package com.vidora.app.data.repository.subscription

import com.vidora.app.data.remote.models.subscription.ChannelResponse
import com.vidora.app.data.remote.models.subscription.SubscribeResponse

interface SubscriptionRepo {

    suspend fun subscribeChannel(channelId: String): Result<SubscribeResponse>

    suspend fun unsubscribeChannel(channelId: String): Result<SubscribeResponse>

    suspend fun mySubscribedChannel(): Result<List<ChannelResponse>>

}