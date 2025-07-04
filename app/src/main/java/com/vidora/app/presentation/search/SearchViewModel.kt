package com.vidora.app.presentation.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vidora.app.data.local.datastore.SearchHistory
import com.vidora.app.data.repository.search.SearchRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val repository: SearchRepository,
    private val historyRepo: SearchHistory
) : ViewModel() {

    private val _state = MutableStateFlow<SearchState>(SearchState.Idle)
    val state: StateFlow<SearchState> = _state

    val history: StateFlow<List<String>> =
        historyRepo.historyFlow
            .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    fun search(query: String) {
        viewModelScope.launch {
            historyRepo.addQuery(query)
            _state.value = SearchState.Loading
            repository.searchAll(query)
                .onSuccess { _state.value = SearchState.Success(it) }
                .onFailure { _state.value = SearchState.Error(it.message.orEmpty()) }
        }
    }

    fun clearHistory() {
        viewModelScope.launch {
            historyRepo.clearHistory()
        }
    }
}