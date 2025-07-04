package com.vidora.app.di

import com.vidora.app.data.remote.models.auth.AuthApi
import com.vidora.app.data.remote.models.channel.ChannelApi
import com.vidora.app.data.remote.models.search.SearchApi
import com.vidora.app.data.remote.models.subscription.SubscribeApi
import com.vidora.app.data.remote.models.video.VideoApi
import com.vidora.app.data.remote.models.video.history.HistoryApi
import com.vidora.app.data.repository.auth.AuthRepository
import com.vidora.app.data.repository.auth.AuthRepositoryImpl
import com.vidora.app.data.repository.channel.ChannelRepository
import com.vidora.app.data.repository.channel.ChannelRepositoryImpl
import com.vidora.app.data.repository.history.HistoryRepo
import com.vidora.app.data.repository.history.HistoryRepoImpl
import com.vidora.app.data.repository.search.SearchRepository
import com.vidora.app.data.repository.search.SearchRepositoryImpl
import com.vidora.app.data.repository.subscription.SubscriptionRepo
import com.vidora.app.data.repository.subscription.SubscriptionRepoImpl
import com.vidora.app.data.repository.video.VideoRepository
import com.vidora.app.data.repository.video.VideoRepositoryImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideAuthRepository(api: AuthApi): AuthRepository =
        AuthRepositoryImpl(api)

    @Provides
    @Singleton
    fun provideChannelRepository(api: ChannelApi): ChannelRepository =
        ChannelRepositoryImpl(api)

    @Provides
    @Singleton
    fun providesVideoRepository(api: VideoApi): VideoRepository =
        VideoRepositoryImpl(api)

    @Provides
    @Singleton
    fun providesSearchRepo(api: SearchApi): SearchRepository =
        SearchRepositoryImpl(api)

    @Provides
    @Singleton
    fun providesSubscriptionRepo(api: SubscribeApi): SubscriptionRepo =
        SubscriptionRepoImpl(api)

    @Provides
    @Singleton
    fun providesHistoryRepo(api: HistoryApi): HistoryRepo =
        HistoryRepoImpl(api)

}