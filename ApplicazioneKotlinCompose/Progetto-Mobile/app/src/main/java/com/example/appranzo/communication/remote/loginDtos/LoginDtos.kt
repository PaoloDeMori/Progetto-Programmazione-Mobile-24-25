package com.example.appranzo.communication.remote.loginDtos

import kotlinx.serialization.Serializable

@Serializable
sealed class AuthResults {
    @Serializable
    data class ErrorRegistrationResponseDto(
        val errorSignal: RegistrationErrorReason,
        val optionalMessage: String?
    ): AuthResults()

    @Serializable
    data class ErrorLoginResponseDto(
        val errorSignal: LoginErrorReason,
        val optionalMessage: String?
    ): AuthResults()

    @Serializable
    data class TokenDtos(
        val accessToken: String,
        val refreshToken: String
    ): AuthResults()
}