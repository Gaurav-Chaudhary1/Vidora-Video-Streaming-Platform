package com.vidora.app.data.remote.models.subscription

import retrofit2.Response
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface SubscribeApi {

    @POST("channels/{channelId}/subscribe")
    suspend fun subscribeChannel(
        @Path("channelId") id: String
    ): Response<SubscribeResponse>

    @DELETE("channels/{channelId}/subscribe")
    suspend fun unsubscribeChannel(
        @Path("channelId") id: String
    ): Response<SubscribeResponse>

    @GET("channels/me/subscriptions")
    suspend fun mySubscribedChannel(): Response<List<ChannelResponse>>

}