package it.unibo.appranzo.routes

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.auth.authenticate
import io.ktor.server.request.receive
import io.ktor.server.request.receiveMultipart
import io.ktor.server.response.respond
import io.ktor.server.response.respondFile
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.util.toUpperCasePreservingASCIIRules
import it.unibo.appranzo.commons.GeneralDtos
import it.unibo.appranzo.communication.dtos.places.CategoryDto
import it.unibo.appranzo.communication.dtos.places.FavoriteRequest
import it.unibo.appranzo.communication.dtos.places.PlaceDto
import it.unibo.appranzo.communication.dtos.places.PositionDto
import it.unibo.appranzo.communication.dtos.places.RequestId
import it.unibo.appranzo.communication.dtos.places.ResearchDto
import it.unibo.appranzo.communication.dtos.reviews.ReviewDto
import it.unibo.appranzo.communication.dtos.reviews.ReviewRequestDto
import it.unibo.appranzo.data.database.daos.PlaceEntity
import it.unibo.appranzo.data.database.tables.PlacesTable
import it.unibo.appranzo.model.places.ClientPhotosService
import it.unibo.appranzo.model.places.PlacesService
import it.unibo.appranzo.model.places.ReviewsService
import it.unibo.appranzo.model.security.AuthenticationService
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.transactions.transaction

import java.io.File

object StringUtils{
    val invalidChar = listOf("..","/","$",".")
}

fun Route.placesRoute(placesService: PlacesService,authenticationService: AuthenticationService,photoPath :String, reviewsService: ReviewsService,clientPhotoService: ClientPhotosService) {
    get("/places/photos/{name}") {
        val name = call.parameters["name"]

        if (name.isNullOrEmpty() || StringUtils.invalidChar.contains(name)) {
            call.respond(HttpStatusCode.BadRequest)
            return@get
        } else {
            val photo = File(photoPath, name)
            if (photo.exists()) {
                call.respondFile(photo)
            } else {
                call.respond(HttpStatusCode.BadRequest)
            }
        }
    }
    authenticate("auth-jwt") {
        suspend fun getUserIdFromCall(call: ApplicationCall): EntityID<Int>? {
            val token = call.request.headers["Authorization"]?.removePrefix("Bearer ")?.trim()
            val userName = token?.let { authenticationService.getUsernameFromToken(it) }
            return userName?.let { authenticationService.getUserIdFromUsername(it) }
        }



        post("/places/nearPlaces") {
            val positionRequest: PositionDto = call.receive<PositionDto>()
            val result = placesService.findRestaurantByPosition(positionRequest.latitude, positionRequest.longitude)
            val toSend: List<PlaceDto> = transaction {result.mapNotNull { it.toDto() }}
            call.respond(HttpStatusCode.OK, toSend)
        }

        post("/places/byId") {
            val placeId: Int = call.receive<RequestId>().id
            val result = placesService.findRestaurantById(EntityID(placeId, PlacesTable))
            val toSend: PlaceDto? = transaction{result?.toDto()}
            if(toSend!=null) {
                call.respond(HttpStatusCode.OK, toSend)
            }
            else{
                call.respond(HttpStatusCode.BadRequest)
            }
        }

        get("/places/category/{id}") {
            val userID = getUserIdFromCall(call) ?: return@get call.respond(HttpStatusCode.BadRequest)
            val idParam = call.parameters["id"]?.toIntOrNull() ?: return@get call.respond(HttpStatusCode.BadRequest)
            val entities = placesService.getPlacesByCategory(userID, EntityID(idParam, PlacesTable))
            val dtos = transaction {entities.mapNotNull { it.toDto() }}
            call.respond(HttpStatusCode.OK, dtos)
        }


        post("/places/search") {
            val positionRequest: ResearchDto = call.receive<ResearchDto>()
            val result: List<PlaceEntity>
            if (positionRequest.latitude != null && positionRequest.longitude != null) {
                result = placesService.searchByNameAndCoordinates(
                    positionRequest.researchInput,
                    positionRequest.latitude,
                    positionRequest.longitude
                )
            } else {
                result = placesService.searchByName(input = positionRequest.researchInput)
            }
            val toSend: List<PlaceDto> = transaction {result.mapNotNull { it.toDto() }}
            call.respond(HttpStatusCode.OK, toSend)
        }
        post("/favorites/add") {
            val addRequest: FavoriteRequest = call.receive<FavoriteRequest>()
            val userID = getUserIdFromCall(call)
            if (userID != null) {
                if (placesService.addFavorite(userID, addRequest.placeId)) {
                    call.respond(HttpStatusCode.OK, GeneralDtos.SUCCESSFULL)
                } else {
                    call.respond(HttpStatusCode.BadRequest, GeneralDtos.IMPOSSIBLE_TO_ADD)
                }
            } else {
                call.respond(HttpStatusCode.BadRequest, GeneralDtos.IMPOSSIBLE_TO_ADD)
            }
        }

        post("/favorites/remove") {
            val addRequest: FavoriteRequest = call.receive<FavoriteRequest>()
            val userID = getUserIdFromCall(call)
            if (userID != null) {
                if (placesService.removeFavorite(userID, addRequest.placeId) > 0) {
                    call.respond(HttpStatusCode.OK, GeneralDtos.SUCCESSFULL)
                } else {
                    call.respond(HttpStatusCode.BadRequest, GeneralDtos.IMPOSSIBLE_TO_ADD)
                }
            } else {
                call.respond(HttpStatusCode.BadRequest, GeneralDtos.IMPOSSIBLE_TO_ADD)
            }
        }

        post("/favorites/toggle") {
            val request: FavoriteRequest = call.receive()
            val userID = getUserIdFromCall(call)

            if (userID == null) {
                call.respond(HttpStatusCode.BadRequest, GeneralDtos.IMPOSSIBLE_TO_ADD)
                return@post
            }

            val isFavorite = placesService.isFavourites(request.placeId,userID)

            val success = if (isFavorite) {
                placesService.removeFavorite(userID, request.placeId) > 0
            } else {
                placesService.addFavorite(userID, request.placeId)
            }

            if (success) {
                call.respond(HttpStatusCode.OK, GeneralDtos.SUCCESSFULL)
            } else {
                call.respond(HttpStatusCode.BadRequest, GeneralDtos.IMPOSSIBLE_TO_ADD)
            }
        }

        post("/favorites") {
            val userID = getUserIdFromCall(call)
            if (userID != null) {
                val favouritesPlaces = placesService.getFavorites(userID)
                call.respond(HttpStatusCode.OK, favouritesPlaces.mapNotNull { transaction {it.toDto()} })
            } else {
                call.respond(HttpStatusCode.BadRequest, GeneralDtos.IMPOSSIBLE_TO_ADD)
            }
        }


        get("/city/{city}") {
            val city: String = call.parameters["city"] ?: return@get call.respond(
                HttpStatusCode.BadRequest, GeneralDtos.MISSING_VALUE
            )
            val searchedList: List<PlaceEntity>? = placesService.placeByCity(city.toUpperCasePreservingASCIIRules())
            if (searchedList == null || searchedList.isEmpty()) {
                call.respond(HttpStatusCode.BadRequest, GeneralDtos.VOID_RESULT)
            } else {
                val toSend: List<PlaceDto> = searchedList.mapNotNull { transaction {it.toDto()} }
                call.respond(HttpStatusCode.OK, toSend)
            }
        }

        post("/reviews/add/photos") {
            val userId = getUserIdFromCall(call) ?: return@post call.respond(HttpStatusCode.Unauthorized)
            val parts  = call.receiveMultipart()
            val created= reviewsService.addReviewWithPhotos(userId, parts)
            if (created != null) call.respond(HttpStatusCode.Created)
            else               call.respond(HttpStatusCode.BadRequest)
        }

        post("/reviews/add") {
            val userId = getUserIdFromCall(call) ?: return@post call.respond(HttpStatusCode.Unauthorized)
            val dto    = call.receive<ReviewRequestDto>()
            val created= reviewsService.addReview(userId, dto)
            if (created != null) call.respond(HttpStatusCode.Created)
            else               call.respond(HttpStatusCode.BadRequest)
        }


        post("/reviews") {
            try {
                val research = call.receive<RequestId>()
                val placeId  = research.id

                val reviews = reviewsService
                    .getReviewsForPlace(placeId)
                    .map { review ->
                        val user = authenticationService.getUserFromId(review.userId)
                        val username = user?.userName ?: "Unknown"

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

                call.respond(HttpStatusCode.OK, reviews)
            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    "Error while retrieving reviews: ${e.localizedMessage}"
                )
            }
        }


        get("/categories") {
            val cats = placesService
                .getAllCategories()
                .map { CategoryDto(it.id.value, it.name) }
            call.respond(HttpStatusCode.OK, cats)
        }

        get("/places/topRated") {
            val topRatedPlaces = placesService.getAllPlacesSortedByRating()
            val toSend: List<PlaceDto> = transaction {topRatedPlaces.mapNotNull { it.toDto() }}
            call.respond(HttpStatusCode.OK, toSend)
        }

        get("/places/{id}/coordinates") {
            val id = call.parameters["id"]?.toIntOrNull()
                ?: return@get call.respond(HttpStatusCode.BadRequest)
            val coords = placesService.getCoordinates(EntityID(id, PlacesTable))
            if (coords != null) call.respond(HttpStatusCode.OK, coords)
            else                call.respond(HttpStatusCode.NotFound)
        }


    }
}