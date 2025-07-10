package com.example.appranzo.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appranzo.communication.remote.RestApiClient
import com.example.appranzo.communication.remote.loginDtos.ProfileReviewDto
import com.example.appranzo.communication.remote.loginDtos.ReviewDto
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

    class ProfileReviewsViewModel(
        private val api: RestApiClient
    ) : ViewModel() {

        private val _reviews = MutableStateFlow<List<ProfileReviewDto>>(emptyList())
        val reviews: StateFlow<List<ProfileReviewDto>> = _reviews.asStateFlow()

        init {
            loadMyReviews()
        }

        private fun loadMyReviews() {
            viewModelScope.launch {
                val raw: List<ReviewDto> = api.getMyReviews()

                val placeIds = raw.map { it.placeId }.toSet()
                val lookup: Map<Int, String> = placeIds
                    .map { id ->
                        async { id to (api.placeById(id)?.name ?: "—") }
                    }
                    .awaitAll()
                    .toMap()

                val ui = raw.map { dto ->
                    ProfileReviewDto(
                        id                = dto.id,
                        placeId           = dto.placeId,
                        placeName         = lookup[dto.placeId]!!,
                        userId            = dto.userId,
                        username          = dto.username,
                        rating            = dto.rating,
                        priceLevel        = dto.priceLevel,
                        ambienceRating    = dto.ambienceRating,
                        ingredientQuality = dto.ingredientQuality,
                        comment           = dto.comment,
                        creationDate      = dto.creationDate,
                        photoUrls         = dto.photoUrls
                    )
                }

                _reviews.value = ui
            }
        }
    }

