package it.unibo.appranzo

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.authorization
import it.unibo.appranzo.data.repositories.UserRepository
import it.unibo.appranzo.model.security.JwtData
import org.koin.ktor.ext.inject

fun Application.configureSecurity(userRepository: UserRepository) {
   val jwtData: JwtData by inject()
    install(Authentication) {
        jwt ("auth-jwt") {
            verifier(
                JWT
                    .require(Algorithm.HMAC256(jwtData.secretAccess))
                    .withAudience(jwtData.audience)
                    .build()
            )
            validate { credential ->
                if (credential.payload.audience.contains(jwtData.audience)) JWTPrincipal(credential.payload) else null
            }
        }
        jwt ("auth-refresh-jwt") {
            verifier(
                JWT
                    .require(Algorithm.HMAC256(jwtData.secretRefresh))
                    .withAudience(jwtData.audience)
                    .build()
            )
            validate { credential ->
                val authorizationHeader = this.request.authorization()
                var token: String? = null

                if (authorizationHeader != null && authorizationHeader.lowercase().startsWith("bearer ")) {
                    token = authorizationHeader.substring("bearer ".length)
                }
                else return@validate null
                if(token!=null) {
                        val claim = credential.payload.getClaim("username")
                        val userName = claim.asString()
                        if (userRepository.checkRefreshToken(userName, token))
                            if (credential.payload.audience.contains(jwtData.audience)) JWTPrincipal(credential.payload) else return@validate null
                    }
                else   return@validate null
            }
        }
    }
}
