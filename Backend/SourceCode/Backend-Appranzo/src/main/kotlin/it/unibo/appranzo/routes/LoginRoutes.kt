package it.unibo.appranzo.routes

import io.ktor.http.HttpStatusCode
import io.ktor.server.auth.authenticate
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import it.unibo.appranzo.commons.GeneralDtos
import it.unibo.appranzo.commons.security.LoginErrorReason
import it.unibo.appranzo.commons.security.RegistrationErrorReason
import it.unibo.appranzo.communication.dtos.LoginRequestsDtos
import it.unibo.appranzo.communication.dtos.RegistrationRequestsDtos
import it.unibo.appranzo.communication.dtos.TokenDtos
import it.unibo.appranzo.communication.dtos.reviews.ReviewDto
import it.unibo.appranzo.communication.dtos.security.ErrorLoginResponseDto
import it.unibo.appranzo.communication.dtos.security.ErrorRegistrationResponseDto
import it.unibo.appranzo.data.database.daos.UserEntity
import it.unibo.appranzo.data.database.tables.UsersTable
import it.unibo.appranzo.model.places.ClientPhotosService
import it.unibo.appranzo.model.places.ReviewsService
import it.unibo.appranzo.model.security.AuthenticationService
import it.unibo.appranzo.model.security.Encrypter
import it.unibo.appranzo.model.security.LoginResult
import it.unibo.appranzo.model.security.RegistrationResult
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.transactions.transaction
import kotlin.text.removePrefix

fun Route.loginRoute(auth: AuthenticationService) {
    post("/login") {
        val loginInfo: LoginRequestsDtos = call.receive<LoginRequestsDtos>()
        val loginResult = auth.login(loginInfo.username, loginInfo.password)

        when (loginResult) {
            is LoginResult.LoginError -> call.respond(
                HttpStatusCode.BadRequest,
                ErrorLoginResponseDto(LoginErrorReason.CREDENTIALS_INVALID, loginResult.additionalInfos)
            )

            is LoginResult.Success -> call.respond(
                HttpStatusCode.Accepted,
                TokenDtos(loginResult.jwtAccessToken, loginResult.jwtRefreshToken)
            )
        }
    }
    authenticate("auth-refresh-jwt") {
        get("/login/trylogin") {
            call.respond(HttpStatusCode.Accepted)
        }
    }
}

fun Route.expiredAccessTokenRoute(auth: AuthenticationService){
    authenticate ( "auth-refresh-jwt" ){
            post("/token/expiredAccess") {
                val header =call.request.headers["Authorization"]?.removePrefix("Bearer ")?.trim()
                val userName = header?.let {  auth.getUsernameFromRefreshToken(it)}
                if (userName == null) {call.respond(HttpStatusCode.BadRequest,ErrorLoginResponseDto(LoginErrorReason.CREDENTIALS_INVALID,"invalidToken")); return@post}
                val newAccessToken = auth.jwtHelper.generateAccessJWT(userName)
                val newRefreshToken = auth.jwtHelper.generateRefreshJWT(userName)
                val hashedRefresh = Encrypter.encrypt(newRefreshToken)
                if(hashedRefresh!=null && auth.changeHashedRefreshToken(hashedRefresh, userName)) {
                        call.respond(HttpStatusCode.OK, TokenDtos(newAccessToken,newRefreshToken))
                }
                else{call.respond(HttpStatusCode.BadRequest,ErrorLoginResponseDto(LoginErrorReason.CREDENTIALS_INVALID,"invalidToken")); return@post}
            }
    }
}

fun Route.registrationRoute(auth: AuthenticationService){
    post("/register"){
        val registrationInfo: RegistrationRequestsDtos = call.receive()

        val registrationResult: RegistrationResult = auth.register(registrationInfo.username,
            registrationInfo.password,
            registrationInfo.email,
            registrationInfo.photoUrl)

        when(registrationResult){
            is RegistrationResult.Success -> call.respond(HttpStatusCode.Accepted, TokenDtos(registrationResult.jwtAccessToken,registrationResult.jwtRefreshToken))
            is RegistrationResult.RegistrationError->
                when(registrationResult.errorReason){
                    RegistrationErrorReason.EMAIL_TAKEN -> call.respond(HttpStatusCode.Conflict,
                        ErrorRegistrationResponseDto(RegistrationErrorReason.EMAIL_TAKEN,
                            registrationResult.additionalInfos))
                    RegistrationErrorReason.USERNAME_TAKEN -> call.respond(HttpStatusCode.Conflict,
                        ErrorRegistrationResponseDto(RegistrationErrorReason.USERNAME_TAKEN,
                            registrationResult.additionalInfos))
                    RegistrationErrorReason.USERNAME_INVALID -> call.respond(HttpStatusCode.Conflict,
                        ErrorRegistrationResponseDto(RegistrationErrorReason.USERNAME_INVALID,
                            registrationResult.additionalInfos))
                    RegistrationErrorReason.PASSWORD_INVALID -> call.respond(HttpStatusCode.Conflict,
                        ErrorRegistrationResponseDto(RegistrationErrorReason.PASSWORD_INVALID,
                            registrationResult.additionalInfos))
                    RegistrationErrorReason.DATABASE_ERROR -> call.respond(HttpStatusCode.Conflict,
                        ErrorRegistrationResponseDto(RegistrationErrorReason.DATABASE_ERROR,
                            registrationResult.additionalInfos))

                }
        }
    }
}
fun Route.userRoute(authService: AuthenticationService) {
    authenticate("auth-jwt") {
        get("/users/me") {
            val bearer = call.request.headers["Authorization"]
                ?.removePrefix("Bearer ")
                ?.trim()
            val username = bearer?.let { authService.getUsernameFromToken(it) }
            if (username == null) {
                return@get call.respond(HttpStatusCode.Unauthorized)
            }

            val userEntity = authService.getUserIdFromUsername(username)
                ?.let { id ->
                    transaction { UserEntity.findById(id) }
                }
            if (userEntity == null) {
                return@get call.respond(HttpStatusCode.NotFound)
            }

            call.respond(HttpStatusCode.OK, userEntity.toDto())
        }

        get("/users/{id}") {
            val id = call.parameters["id"]?: return@get call.respond(
            HttpStatusCode.BadRequest, GeneralDtos.MISSING_VALUE
        )
            if (id == null) {
                return@get call.respond(HttpStatusCode.Unauthorized)
            }

            val idInt = try {
                id.toInt()
            } catch (e: NumberFormatException) {
                return@get call.respond(HttpStatusCode.BadRequest, "ID NON VALIDO")
            }
            val userEntity = authService.getUserFromId(EntityID(idInt, UsersTable))
            if (userEntity == null) {
                return@get call.respond(HttpStatusCode.NotFound)
            }
            call.respond(HttpStatusCode.OK, userEntity.toDto())
        }

    }
}

fun Route.userReviewsRoute(
    authService: AuthenticationService,
    reviewsService: ReviewsService,
    clientPhotoService: ClientPhotosService
) {
    authenticate("auth-jwt") {
        get("/reviews/me") {
            try {
                val bearer = call.request.headers["Authorization"]
                    ?.removePrefix("Bearer ")
                    ?.trim()
                    ?: throw IllegalStateException("Missing Authorization header")
                val username = authService.getUsernameFromToken(bearer)
                    ?: throw IllegalStateException("Invalid token: [$bearer]")

                val userId = authService.getUserIdFromUsername(username)
                    ?: throw IllegalStateException("No user found for username=$username")

                val entities = reviewsService.getReviewsForUser(userId)

                val dtos: List<ReviewDto> = entities.map { review ->
                    val photoUrls = clientPhotoService
                        .getPhotosFromReview(review.id)
                        .map { it.photoUrl }

                    ReviewDto(
                        id                = review.id.value,
                        placeId           = review.placeId.value,
                        userId            = review.userId.value,
                        username          = username,
                        rating            = review.rating,
                        priceLevel        = review.priceLevel,
                        ambienceRating    = review.ambienceRating,
                        ingredientQuality = review.ingredientQuality,
                        comment           = review.comment,
                        creationDate      = review.creationDate.toString(),
                        photoUrls         = photoUrls
                    )
                }

                call.respond(HttpStatusCode.OK, dtos)
            } catch (e: Exception) {
                e.printStackTrace()
                call.respond(
                    HttpStatusCode.InternalServerError,
                    mapOf(
                        "exception"  to e.toString(),
                        "stackTrace" to e.stackTraceToString()
                    )
                )
            }
        }
    }
}


