package it.unibo.appranzo.model.security

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import it.unibo.appranzo.data.database.daos.UserEntity
import kotlinx.serialization.json.Json
import java.io.File
import java.util.Date

class JwtHelper(val config: JwtData){

    companion object {
        private var config2: JwtData? = null
        fun loadJwtConfig(path: String = "env.json"): JwtData {
            if (config2 == null) {
                val fileContent = File(path).readText()
                config2 = Json.decodeFromString(fileContent)
            }
            return config2 ?: error("JWT impossible to load data")
        }
    }

        fun generateAccessJWT(user: UserEntity): String {
            return JWT.create()
                .withAudience(config.audience)
                .withClaim("username", user.userName)
                .withExpiresAt(Date(System.currentTimeMillis() + config.expirationAccessTime))
                .sign(Algorithm.HMAC256(config.secretAccess))
        }

    fun generateAccessJWT(username: String): String {
        return JWT.create()
            .withAudience(config.audience)
            .withClaim("username", username)
            .withExpiresAt(Date(System.currentTimeMillis() + config.expirationAccessTime))
            .sign(Algorithm.HMAC256(config.secretAccess))
    }


    fun generateRefreshJWT(user: UserEntity): String {
        return JWT.create()
            .withAudience(config.audience)
            .withClaim("username", user.userName)
            .withExpiresAt(Date(System.currentTimeMillis() + config.expirationRefreshTime))
            .sign(Algorithm.HMAC256(config.secretRefresh))
    }

    fun generateRefreshJWT(username: String): String {
        return JWT.create()
            .withAudience(config.audience)
            .withClaim("username", username)
            .withExpiresAt(Date(System.currentTimeMillis() + config.expirationRefreshTime))
            .sign(Algorithm.HMAC256(config.secretRefresh))
    }

    fun getUsernameFromAccessToken(token: String): String? {
        return try {
            val verifier = JWT
                .require(Algorithm.HMAC256(config.secretAccess))
                .withAudience(config.audience)
                .build()

            val decodedToken = verifier.verify(token)
            decodedToken.getClaim("username").asString()
        } catch (e: Exception) {
            null
        }
    }

    fun getUsernameFromRefreshToken(token: String): String? {
        return try {
            val verifier = JWT
                .require(Algorithm.HMAC256(config.secretRefresh))
                .withAudience(config.audience)
                .build()

            val decodedToken = verifier.verify(token)
            decodedToken.getClaim("username").asString()
        } catch (e: Exception) {
            null
        }
    }
}
