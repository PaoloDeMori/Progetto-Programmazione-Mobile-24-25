package com.example.appranzo.communication.remote.loginDtos

import com.example.appranzo.data.models.Place
import kotlinx.serialization.SerialName
import kotlinx. serialization. Serializable;

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
){
    fun toDto(): Place {
        return Place(
            id,
            name,
            description,
            address,
            city,
            photoUrl,
            "",
            averageRating?:1.0,
            distanceFromUser,
            latitude,
            longitude
        )
    }
}

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

@Serializable
data class FavoriteRequest(val placeId: Int)
