package it.unibo.appranzo.commons.security
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
enum class LoginErrorReason{
    @SerialName("CREDENTIALS_INVALID")
    CREDENTIALS_INVALID,
    @SerialName("DATABASE_ERROR")
    DATABASE_ERROR
}

@Serializable
enum class RegistrationErrorReason{
    @SerialName("USERNAME_INVALID")
    USERNAME_INVALID,
    @SerialName("PASSWORD_INVALID")
    PASSWORD_INVALID,
    @SerialName("USERNAME_TAKEN")
    USERNAME_TAKEN,
    @SerialName("EMAIL_TAKEN")
    EMAIL_TAKEN,
    @SerialName("DATABASE_ERROR")
    DATABASE_ERROR
}