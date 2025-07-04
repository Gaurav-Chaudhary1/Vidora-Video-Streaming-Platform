package com.vidora.app.presentation.search

import com.vidora.app.data.remote.models.search.SearchResult

sealed class SearchState {
    object Idle: SearchState()
    object Loading: SearchState()

    data class Success(val data: SearchResult): SearchState()
    data class Error(val message: String): SearchState()
}