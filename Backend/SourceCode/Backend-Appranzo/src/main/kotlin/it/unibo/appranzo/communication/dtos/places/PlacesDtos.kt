package it.unibo.appranzo.communication.dtos.places

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PositionDto(
        val latitude: Double,
        val longitude: Double
)

@Serializable
data class PlaceDto(
    val id: Int,
    val name: String,
    val description: String? = null,
    val city: String,
    val address: String? = null,
    val latitude: Double,
    val longitude: Double,
    val photoUrl: String? = null,
    val categoryId: Int,
    val distanceFromUser: Double? = null,
    val averageRating: Double?
)

@Serializable
data class CategoryDto(
    val id: Int,
    val name: String
)


@Serializable
data class ReverseGeocodingResponse(
    val address: AddressDto
)

@Serializable
data class AddressDto(
    @SerialName("road")
    val road: String? = null,
    @SerialName("house_number")
    val houseNumber: String? = null
)

@Serializable
data class ResearchDto(
    val latitude: Double?,
    val longitude: Double?,
    val researchInput: String
)

@Serializable
data class RequestId(
    val id:Int
)