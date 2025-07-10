package it.unibo.appranzo.communication.dtos.reviews

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