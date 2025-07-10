package com.example.appranzo.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appranzo.communication.remote.RestApiClient
import com.example.appranzo.communication.remote.loginDtos.ReviewDto

import com.example.appranzo.data.models.Place
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

private const val TAG = "PlaceDetailVM"

sealed interface RestaurantDetailActualState {
    data object Loading : RestaurantDetailActualState
    data class Success(val restaurant: Place) : RestaurantDetailActualState
    data class Error(val message: String) : RestaurantDetailActualState
}

class PlaceDetailViewModel(
    private val restApiClient: RestApiClient
) : ViewModel() {
    private val _state =
        MutableStateFlow<RestaurantDetailActualState>(RestaurantDetailActualState.Loading)
    val state: StateFlow<RestaurantDetailActualState> = _state

    private val _reviews = MutableStateFlow<List<ReviewDto>>(emptyList())
    val reviews: StateFlow<List<ReviewDto>> = _reviews.asStateFlow()

    private val _isReviewsLoading = MutableStateFlow(true)
    val isReviewsLoading: StateFlow<Boolean> = _isReviewsLoading.asStateFlow()

    fun loadRestaurantById(id: Int) {
        Log.d(TAG, "Loading restaurant with id=$id")
        if (_state.value !is RestaurantDetailActualState.Loading) {
            _state.value = RestaurantDetailActualState.Loading
        }

        viewModelScope.launch {
            try {
                val place = restApiClient.placeById(id)
                if (place != null) {
                    Log.d(TAG, "Restaurant loaded: $place")
                    _state.value = RestaurantDetailActualState.Success(place)
                } else {
                    Log.w(TAG, "Restaurant null for id=$id")
                    _state.value = RestaurantDetailActualState.Error("Error while loading place")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Exception loading restaurant", e)
                _state.value = RestaurantDetailActualState.Error("Error while loading place")
            }
        }
    }

    fun loadReviews(placeId: Int) {
        Log.d(TAG, "Start loading reviews for placeId=$placeId")
        _isReviewsLoading.value = true
        viewModelScope.launch {
            try {
                val result = restApiClient.getReviews(placeId)
                Log.d(TAG, "Received ${result.size} reviews: $result")
                _reviews.value = result
            } catch (e: Exception) {
                Log.e(TAG, "Error loading reviews for placeId=$placeId", e)
                _reviews.value = emptyList()
            } finally {
                _isReviewsLoading.value = false
                Log.d(TAG, "Finished loading reviews (loading flag=false)")
            }
        }
    }

}
