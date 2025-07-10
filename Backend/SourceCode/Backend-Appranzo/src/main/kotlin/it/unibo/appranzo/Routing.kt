package it.unibo.appranzo

import io.ktor.server.application.*
import io.ktor.server.routing.routing
import it.unibo.appranzo.model.friends.FriendsService
import it.unibo.appranzo.model.places.ClientPhotosService
import it.unibo.appranzo.model.places.PlacesService
import it.unibo.appranzo.model.places.ReviewsService
import it.unibo.appranzo.model.security.AuthenticationService
import it.unibo.appranzo.routes.expiredAccessTokenRoute
import it.unibo.appranzo.routes.friendsRoute
import it.unibo.appranzo.routes.loginRoute
import it.unibo.appranzo.routes.placesRoute
import it.unibo.appranzo.routes.registrationRoute
import it.unibo.appranzo.routes.testRoute
import it.unibo.appranzo.routes.userReviewsRoute
import it.unibo.appranzo.routes.userRoute
import org.koin.core.qualifier.named
import org.koin.ktor.ext.inject

fun Application.configureRouting() {
    val authService by inject<AuthenticationService>()
    val placesService by inject<PlacesService>()
    val photoPath: String by inject<String>(named("photoStoragePath"))
    val reviewsService by inject<ReviewsService>()
    val clientPhotosService by inject<ClientPhotosService>()
    val friendsService by inject<FriendsService>()



    routing {
        loginRoute(authService)
        registrationRoute(authService)
        userRoute(authService)
        userReviewsRoute(authService, reviewsService, clientPhotosService)
        placesRoute(placesService, authService, photoPath, reviewsService, clientPhotosService)
        expiredAccessTokenRoute(authService)
        friendsRoute(friendsService, authService)
        testRoute()
    }
}
