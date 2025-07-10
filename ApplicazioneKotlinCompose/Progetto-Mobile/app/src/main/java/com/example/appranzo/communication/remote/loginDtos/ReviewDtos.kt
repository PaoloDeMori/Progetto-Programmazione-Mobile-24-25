package com.example.appranzo.communication.remote.loginDtos

import kotlinx.serialization.Serializable


@Serializable
data class ReviewRequestDto(
    val placeId: Int,
    val priceLevel: Byte,
    val ambienceRating: Byte,
    val ingredientQuality: Byte,
    val comment: String? = null
)


@Serializable
data class RequestIdDto(
    val id: Int
)


@Serializable
data class ReviewDto(
    val id: Int,
    val placeId: Int,
    val userId: Int,
    val username: String,
    val rating: Byte,
    val priceLevel: Byte,
    val ambienceRating: Byte,
    val ingredientQuality: Byte,
    val comment: String?,
    val creationDate: String,
    val photoUrls: List<String>
)


@Serializable
data class ProfileReviewDto(
    val id: Int,
    val placeId: Int,
    val placeName: String,
    val userId: Int,
    val username: String,
    val rating: Byte,
    val priceLevel: Byte,
    val ambienceRating: Byte,
    val ingredientQuality: Byte,
    val comment: String?,
    val creationDate: String,
    val photoUrls: List<String>
)
