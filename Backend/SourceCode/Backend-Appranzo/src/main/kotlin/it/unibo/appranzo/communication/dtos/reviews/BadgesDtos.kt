package it.unibo.appranzo.communication.dtos.reviews

import kotlinx.serialization.Serializable

@Serializable
data class BadgeDto(
    val id: Int,
    val name: String,
    val goalPoints: Int
)