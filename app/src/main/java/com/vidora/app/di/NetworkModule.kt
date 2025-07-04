package com.vidora.app.di

import com.vidora.app.data.local.datastore.UserPreferences
import com.vidora.app.data.remote.models.auth.AuthApi
import com.vidora.app.data.remote.models.channel.ChannelApi
import com.vidora.app.data.remote.file.FileApi
import com.vidora.app.data.remote.models.search.SearchApi
import com.vidora.app.data.remote.models.subscription.SubscribeApi
import com.vidora.app.data.remote.models.video.VideoApi
import com.vidora.app.data.remote.models.video.history.HistoryApi
import com.vidora.app.utils.AuthInterceptor
import com.vidora.app.utils.Constants
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    fun providesBaseURL() = Constants.BASE_URL

    @Provides @Singleton
    fun provideAuthInterceptor(prefs: UserPreferences): AuthInterceptor =
        AuthInterceptor(prefs)

    @Provides @Singleton
    fun providesOkHttpClient(
        authInterceptor: AuthInterceptor
    ): OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(2, TimeUnit.MINUTES)
        .addInterceptor(authInterceptor)
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.HEADERS
        })
        .build()

    @Provides @Singleton
    fun providesRetrofit(
        BASE_URL: String,
        client: OkHttpClient
    ): Retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    @Provides
    @Singleton
    fun provideAuthApi(retrofit: Retrofit): AuthApi =
        retrofit.create(AuthApi::class.java)

    @Provides
    @Singleton
    fun provideChannelApi(retrofit: Retrofit): ChannelApi =
        retrofit.create(ChannelApi::class.java)

    @Provides
    @Singleton
    fun providesFileApi(retrofit: Retrofit): FileApi =
        retrofit.create(FileApi::class.java)

    @Provides
    @Singleton
    fun providesVideoApi(retrofit: Retrofit): VideoApi =
        retrofit.create(VideoApi::class.java)

    @Provides
    @Singleton
    fun providesSearchApi(retrofit: Retrofit): SearchApi =
        retrofit.create(SearchApi::class.java)

    @Provides
    @Singleton
    fun providesSubscriptionApi(retrofit: Retrofit): SubscribeApi =
        retrofit.create(SubscribeApi::class.java)

    @Provides
    @Singleton
    fun providesHistoryApi(retrofit: Retrofit): HistoryApi =
        retrofit.create(HistoryApi::class.java)
}
