package com.vidora.app.data.repository.search

import com.vidora.app.data.remote.models.search.SearchResult

interface SearchRepository {
    suspend fun searchAll(query: String): Result<SearchResult>
}