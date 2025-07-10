package com.example.appranzo.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.appranzo.communication.remote.RestApiClient
import com.example.appranzo.data.models.Category
import com.example.appranzo.data.models.Place
import com.example.appranzo.util.permissions.LocationService
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HomePageState(
    val categories: List<Category> = emptyList(),
    val nearPlaces: List<Place> = emptyList(),
    val favouritePlaces: List<Place> = emptyList(),
    val topRatedPlaces: List<Place> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class PlacesViewModel(private val restApiClient: RestApiClient, application: Application, private val locationService: LocationService) : AndroidViewModel(application) {
    private val _homePageState = MutableStateFlow(HomePageState())
    val homePageState: StateFlow<HomePageState> = _homePageState.asStateFlow()
    private val _errorMessage = MutableSharedFlow<String>()
    val errorMessage: SharedFlow<String> = _errorMessage.asSharedFlow()

    init {
        loadCategories()
        loadTopRatedPlaces()
        viewModelScope.launch {
            val favorites = restApiClient.getFavorites()
            _homePageState.update { it.copy(favouritePlaces = favorites) }
        }
    }

    private fun loadCategories() {
        viewModelScope.launch {
            _homePageState.update { it.copy(categories = restApiClient.getCategories()) }
        }
    }

    fun loadNearPlaces(latitude: Double, longitude: Double) {
        viewModelScope.launch {
            _homePageState.update {
                it.copy(
                    nearPlaces = restApiClient.getNearRestaurants(
                        latitude,
                        longitude
                    ), isLoading = false
                )
            }
        }
    }
    private fun loadFavorites() {
        viewModelScope.launch {
            val favs = restApiClient.getFavorites()
            _homePageState.update { it.copy(favouritePlaces = favs) }
        }
    }

    fun toggleFavourites(place: Place) {
        viewModelScope.launch {
            val ok = restApiClient.toggleFavourite(place.id)
            if (ok) {
                loadFavorites()
            } else {
                _errorMessage.emit("Impossibile aggiornare i preferiti")
            }
        }
    }


    fun loadTopRatedPlaces(){
        val a = viewModelScope.launch {
            _homePageState.update {
                it.copy(
                    topRatedPlaces = restApiClient.getTopRated(),
                    isLoading = false
                )
            }
        }
    }
}