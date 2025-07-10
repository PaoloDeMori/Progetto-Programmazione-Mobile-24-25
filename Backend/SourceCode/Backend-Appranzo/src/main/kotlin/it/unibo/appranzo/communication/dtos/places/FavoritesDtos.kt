package it.unibo.appranzo.communication.dtos.places

import kotlinx.serialization.Serializable

@Serializable
data class FavoriteRequest(
        val placeId: Int
        )