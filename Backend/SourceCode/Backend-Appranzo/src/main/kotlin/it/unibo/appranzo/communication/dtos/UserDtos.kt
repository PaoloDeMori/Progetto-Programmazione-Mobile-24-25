package it.unibo.appranzo.communication.dtos

import kotlinx.serialization.Serializable

@Serializable
data class LoginRequestsDtos(
    val username: String,
    val password: String,
    )

@Serializable
data class RegistrationRequestsDtos(
    val username: String,
    val password: String,
    val email: String,
    val photoUrl: String?
)

@Serializable
data class TokenDtos(
    val accessToken :String,
    val refreshToken: String
)

@Serializable
data class UserDto(
    val id: Int,
    val username: String,
    val email: String,
    val points: Int,
    val photoUrl: String?
)