package it.unibo.appranzo.model.security

import it.unibo.appranzo.commons.security.LoginErrorReason
import it.unibo.appranzo.commons.security.RegistrationErrorReason

sealed class RegistrationResult {
    data class Success(val jwtAccessToken: String, val jwtRefreshToken: String): RegistrationResult()
    data class RegistrationError(val errorReason: RegistrationErrorReason, val additionalInfos: String = "Error"): RegistrationResult()

}

sealed class LoginResult {
    data class Success(val jwtAccessToken: String,val jwtRefreshToken: String): LoginResult()
    data class LoginError(val errorReason: LoginErrorReason, val additionalInfos: String = "Error"): LoginResult()



}