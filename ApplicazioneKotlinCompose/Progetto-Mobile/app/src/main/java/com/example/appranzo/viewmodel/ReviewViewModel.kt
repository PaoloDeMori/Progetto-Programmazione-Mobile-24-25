package com.example.appranzo.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appranzo.communication.remote.RestApiClient
import com.example.appranzo.communication.remote.loginDtos.ReviewRequestDto
import com.example.appranzo.communication.remote.loginDtos.ReviewDto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

sealed class SubmissionState {
    data object Idle : SubmissionState()
    data object Loading : SubmissionState()
    data object Success : SubmissionState()
    data class Error(val message: String) : SubmissionState()
}

class ReviewViewModel(
    private val restApiClient: RestApiClient, private val badgeRoadViewModel: BadgeRoadViewModel, private val profileViewModel: ProfileDetailViewModel
) : ViewModel() {

    private val _priceLevel = MutableStateFlow<Byte>(0)
    val priceLevel: StateFlow<Byte> = _priceLevel.asStateFlow()

    private val _ambienceRating = MutableStateFlow<Byte>(0)
    val ambienceRating: StateFlow<Byte> = _ambienceRating.asStateFlow()

    private val _ingredientQuality = MutableStateFlow<Byte>(0)
    val ingredientQuality: StateFlow<Byte> = _ingredientQuality.asStateFlow()

    private val _content = MutableStateFlow("")
    val content: StateFlow<String> = _content.asStateFlow()

    private val _photos = MutableStateFlow<List<Pair<String, ByteArray>>>(emptyList())
    val photos: StateFlow<List<Pair<String, ByteArray>>> = _photos.asStateFlow()

    private val _submitState = MutableStateFlow<SubmissionState>(SubmissionState.Idle)
    val submitState: StateFlow<SubmissionState> = _submitState.asStateFlow()

    fun onPriceLevelChange(value: Int) {
        _priceLevel.value = value.coerceIn(1, 5).toByte()
    }

    fun onAmbienceRatingChange(value: Int) {
        _ambienceRating.value = value.coerceIn(1, 5).toByte()
    }

    fun onIngredientQualityChange(value: Int) {
        _ingredientQuality.value = value.coerceIn(1, 5).toByte()
    }

    fun onContentChange(value: String) {
        _content.value = value
    }

    fun addPhoto(image: ByteArray) {
        _photos.value += ("photos" to image)
    }

    fun removePhoto(index: Int) {
        _photos.value = _photos.value.toMutableList().also { it.removeAt(index) }
    }

    fun resetState() {
        _priceLevel.value = 0
        _ambienceRating.value = 0
        _ingredientQuality.value = 0
        _content.value = ""
        _photos.value = emptyList()
        _submitState.value = SubmissionState.Idle
    }

    fun submitReview(placeId: Int) {
        viewModelScope.launch {
            _submitState.value = SubmissionState.Loading
            try {
                val dto = ReviewRequestDto(
                    placeId           = placeId,
                    priceLevel        = _priceLevel.value,
                    ambienceRating    = _ambienceRating.value,
                    ingredientQuality = _ingredientQuality.value,
                    comment           = _content.value.takeIf { it.isNotBlank() }
                )
                val success = if (_photos.value.isEmpty()) {
                    restApiClient.addReview(dto)
                } else {
                    restApiClient.addReviewWithPhotos(dto, _photos.value)
                }

                _submitState.value = if (success) {
                    profileViewModel.refresh()
                    profileViewModel.user.first()?.let {badgeRoadViewModel.loadBadgeData(it)}
                    SubmissionState.Success
                }
                else SubmissionState.Error("Errore durante l'invio")
            } catch (e: Exception) {
                _submitState.value = SubmissionState.Error(e.localizedMessage ?: "Errore sconosciuto")
            }
        }
    }


    suspend fun loadReviews(placeId: Int): List<ReviewDto> {
        return try {
            restApiClient.getReviews(placeId)
        } catch (e: Exception) {
            emptyList()
        }
    }
}
