package com.vidora.app.presentation.channel

import com.vidora.app.data.remote.models.channel.Channel

sealed class ChannelState {
    object Idle : ChannelState()
    object Loading : ChannelState()
    data class Success(
        val channel: Channel,
        val signedProfileUrl: String?,
        val signedBannerUrl: String?
    ) : ChannelState()
    data class Error(val message: String) : ChannelState()
}