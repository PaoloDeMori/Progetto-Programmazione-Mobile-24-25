package it.unibo.appranzo.model.security

import it.unibo.appranzo.commons.security.LoginErrorReason
import it.unibo.appranzo.commons.security.RegistrationErrorReason
import it.unibo.appranzo.data.database.daos.UserEntity
import it.unibo.appranzo.data.repositories.UserRepository
import org.jetbrains.exposed.dao.id.EntityID

class AuthenticationService(val userRepo: UserRepository,val jwtHelper: JwtHelper)  {
    fun login(username: String, password: String): LoginResult {
        try {
            val user: UserEntity? = userRepo.findUserByUsername(username)

            return if (user != null) {
                if (Encrypter.compareStrings(password, user.password)) {
                    val refreshToken = jwtHelper.generateRefreshJWT(user)
                    userRepo.updateRefreshToken(Encrypter.encrypt(refreshToken)!!,user)
                    LoginResult.Success(
                        jwtHelper.generateAccessJWT(user),
                        refreshToken
                    )
                } else {
                    LoginResult.LoginError(LoginErrorReason.CREDENTIALS_INVALID)
                }
            } else {
                LoginResult.LoginError(LoginErrorReason.CREDENTIALS_INVALID)
            }
        }
        catch (e: Exception){
            return LoginResult.LoginError(
                LoginErrorReason.DATABASE_ERROR,
                "Unexpected Error While registering this account, specific error: ${e.message}")
        }
    }

    fun register(
        username: String,
        password: String,
        email: String,
        photoUrl: String?
    ): RegistrationResult {
        val encryptedPwd: String? = Encrypter.encrypt(password)
        val refreshToken: String? = jwtHelper.generateRefreshJWT(username)
        val hashedRefreshToken: String?
        val userEntity: UserEntity?
        try {
            when {
                username.isBlank() -> return RegistrationResult.RegistrationError(
                    RegistrationErrorReason.USERNAME_INVALID
                )

                encryptedPwd.isNullOrBlank() -> return RegistrationResult.RegistrationError(
                    RegistrationErrorReason.PASSWORD_INVALID
                )
            }

            if (userRepo.findUserByUsername(username) != null) {
                return RegistrationResult.RegistrationError(
                    RegistrationErrorReason.USERNAME_TAKEN
                )
            }
            if (userRepo.findUserByEmail(email) != null) {
                return RegistrationResult.RegistrationError(
                    RegistrationErrorReason.EMAIL_TAKEN
                )
            }

            if (refreshToken== null){
                return RegistrationResult.RegistrationError(
                    RegistrationErrorReason.DATABASE_ERROR,
                    "Unexpected Error While registering this account"
                )
            }
           hashedRefreshToken= Encrypter.encrypt(refreshToken)
            if(hashedRefreshToken!=null) {
                userEntity = userRepo.saveNewUser(
                    username, encryptedPwd, email, hashedRefreshToken, photoUrl
                )
            }
            else{
                return RegistrationResult.RegistrationError(
                    RegistrationErrorReason.DATABASE_ERROR,
                    "Unexpected Error While registering this account"
                )
            }

            if (userEntity != null) {
                return RegistrationResult.Success(jwtHelper.generateAccessJWT(userEntity),refreshToken)
            }
            return RegistrationResult.RegistrationError(
                RegistrationErrorReason.DATABASE_ERROR,
                "Unexpected Error While registering this account"
            )
        } catch (e: Exception) {
            return RegistrationResult.RegistrationError(
                RegistrationErrorReason.DATABASE_ERROR,
                "Unexpected Error While registering this account, specific error: ${e.message}"
            )
        }
    }

    fun changeHashedRefreshToken(token:String,username: String): Boolean{
        val user = userRepo.findUserByUsername(username)
        if (user!=null) {
            userRepo.updateRefreshToken(token,user)
            return true
        }
        return false
    }

    fun getUsernameFromToken(token: String): String? {
        return jwtHelper.getUsernameFromAccessToken(token)
    }

    fun getUsernameFromRefreshToken(token: String): String? {
        return jwtHelper.getUsernameFromRefreshToken(token)
    }

    fun getUserIdFromUsername(username: String): EntityID<Int>?{
       return userRepo.findUserByUsername(username)?.userId
    }

    fun getUserFromId(id: EntityID<Int>): UserEntity?{
        return userRepo.findUserById(id)
    }

}