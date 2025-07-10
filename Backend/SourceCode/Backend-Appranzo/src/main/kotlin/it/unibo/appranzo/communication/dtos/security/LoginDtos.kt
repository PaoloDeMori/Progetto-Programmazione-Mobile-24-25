package it.unibo.appranzo.communication.dtos.security

import it.unibo.appranzo.commons.security.LoginErrorReason
import it.unibo.appranzo.commons.security.RegistrationErrorReason
import kotlinx.serialization.Serializable

@Serializable
data class ErrorRegistrationResponseDto(
    val errorSignal: RegistrationErrorReason,
    val optionalMessage: String?
)

@Serializable
data class ErrorLoginResponseDto(
    val errorSignal: LoginErrorReason,
    val optionalMessage: String?
)