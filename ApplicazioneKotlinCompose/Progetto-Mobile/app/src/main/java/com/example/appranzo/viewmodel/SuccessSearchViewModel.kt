package com.example.appranzo.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appranzo.communication.remote.RestApiClient
import com.example.appranzo.data.models.Place
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SuccessSearchViewModel(
    private val restApiClient: RestApiClient
) : ViewModel() {

    private val _places = MutableStateFlow<List<Place>>(emptyList())
    val places: StateFlow<List<Place>> = _places.asStateFlow()
    private val _favouritePlaces = MutableStateFlow<List<Place>>(emptyList())
    val favouritePlaces: StateFlow<List<Place>> = _favouritePlaces.asStateFlow()

    init {
        viewModelScope.launch {
            val _favouritePlaces= restApiClient.getFavorites()
        }
    }

    fun load(categoryId: Int) {
        viewModelScope.launch {
            _places.value = restApiClient.getPlacesByCategory(categoryId)
        }
    }


    fun toggleFavourites(place:Place){
        val a = viewModelScope.launch {
            restApiClient.toggleFavourite(placeId = place.id)
        }
    }

}
