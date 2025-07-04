package com.vidora.app.data.remote.models.search

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface SearchApi {

    @GET("search")
    suspend fun searchAll(
        @Query("query") query: String
    ): Response<SearchResponse>
}