package com.example.appranzo.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appranzo.communication.remote.RestApiClient
import com.example.appranzo.data.models.Category
import com.example.appranzo.data.models.Place
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

@OptIn(FlowPreview::class)
class SearchViewModel(
    private val restApiClient: RestApiClient
) : ViewModel() {
    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    private val _categories = MutableStateFlow<List<Category>>(emptyList())
    val categories: StateFlow<List<Category>> = _categories.asStateFlow()

    private val _results = MutableStateFlow<List<Place>>(emptyList())
    val results: StateFlow<List<Place>> = _results.asStateFlow()

    private var _isNavigationInProgress = MutableStateFlow<Boolean>(false)
    val isNavigationInProgress: StateFlow<Boolean> = _isNavigationInProgress.asStateFlow()


    init {
        viewModelScope.launch {
            _categories.value = restApiClient.getCategories()
        }
        viewModelScope.launch {
            _query
                .debounce(300)
                .distinctUntilChanged()
                .flatMapLatest { term ->
                    if (term.isBlank()) flowOf(emptyList())
                    else flow { emit(restApiClient.searchPlaces(term)) }
                }
                .collect { list -> _results.value = list }
        }
    }

    fun startNavigationLock() {
        if (_isNavigationInProgress.value) return

        _isNavigationInProgress.value = true
        viewModelScope.launch {
            delay(900)
            _isNavigationInProgress.value = false
        }
    }
        fun setQuery(text: String) {
            _query.value = text
        }
}
