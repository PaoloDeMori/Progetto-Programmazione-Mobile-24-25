package com.example.appranzo.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appranzo.communication.remote.RestApiClient
import com.example.appranzo.communication.remote.loginDtos.ReviewDto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch


data class ReviewDetailUi(
    val id: Int,
    val placeName: String,
    val username: String,
    val creationDate: String,
    val rating: Int,
    val priceLevel: Int,
    val ambienceRating: Int,
    val ingredientQuality: Int,
    val comment: String,
    val photos: List<String>
)

class ReviewDetailViewModel(
    private val api: RestApiClient
) : ViewModel() {

    private val _review = MutableStateFlow<ReviewDetailUi?>(null)
    val review: StateFlow<ReviewDetailUi?> = _review.asStateFlow()


    fun loadReviewDetail(placeId: Int, reviewId: Int) {
        viewModelScope.launch {
            val all: List<ReviewDto> = api.getReviews(placeId)
            val dto = all.find { it.id == reviewId } ?: return@launch

            val placeName = api.placeById(dto.placeId)?.name.orEmpty()

            _review.value = ReviewDetailUi(
                id                = dto.id,
                placeName         = placeName,
                username          = dto.username,
                creationDate      = dto.creationDate,
                rating            = dto.rating.toInt(),
                priceLevel        = dto.priceLevel.toInt(),
                ambienceRating    = dto.ambienceRating.toInt(),
                ingredientQuality = dto.ingredientQuality.toInt(),
                comment           = dto.comment.orEmpty(),
                photos            = dto.photoUrls
            )
        }
    }
}
