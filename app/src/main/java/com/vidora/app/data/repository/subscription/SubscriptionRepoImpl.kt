package com.vidora.app.data.repository.subscription

import com.vidora.app.data.remote.models.subscription.ChannelResponse
import com.vidora.app.data.remote.models.subscription.SubscribeApi
import javax.inject.Inject

class SubscriptionRepoImpl @Inject constructor(
    private val api: SubscribeApi
): SubscriptionRepo {

    override suspend fun subscribeChannel(channelId: String) = runCatching {
        val resp = api.subscribeChannel(channelId)
        if (!resp.isSuccessful || resp.body() == null) {
            throw Exception(resp.message())
        }
        resp.body()!!
    }

    override suspend fun unsubscribeChannel(channelId: String) = runCatching {
        val resp = api.unsubscribeChannel(channelId)
        if (!resp.isSuccessful || resp.body() == null){
            throw Exception(resp.message())
        }
        resp.body()!!
    }

    override suspend fun mySubscribedChannel() = runCatching {
        val resp = api.mySubscribedChannel()
        if (!resp.isSuccessful || resp.body() == null){
            throw Exception(resp.message())
        }
        resp.body()!!
    }

}