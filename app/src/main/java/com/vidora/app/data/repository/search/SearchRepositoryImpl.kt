package com.vidora.app.data.repository.search

import com.vidora.app.data.remote.models.search.SearchApi
import com.vidora.app.data.remote.models.search.toDomain
import javax.inject.Inject

class SearchRepositoryImpl @Inject constructor(
    private val api: SearchApi
) : SearchRepository {
    override suspend fun searchAll(query: String) = runCatching {
        val resp = api.searchAll(query)
        if (!resp.isSuccessful || resp.body() == null) {
            throw Exception(resp.message())
        }
        resp.body()!!.toDomain()
    }
}