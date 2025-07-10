package it.unibo.appranzo.model.places

import io.ktor.http.content.MultiPartData
import io.ktor.http.content.PartData
import io.ktor.http.content.forEachPart
import it.unibo.appranzo.communication.dtos.reviews.ReviewRequestDto
import it.unibo.appranzo.data.database.daos.ReviewEntity
import it.unibo.appranzo.data.database.tables.PlacesTable
import it.unibo.appranzo.data.repositories.ClientPhotoRepository
import it.unibo.appranzo.data.repositories.PlaceRepository
import it.unibo.appranzo.data.repositories.ReviewsRepository
import it.unibo.appranzo.data.repositories.UserRepository
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.transactions.transaction
import java.io.File
import java.util.UUID
import io.ktor.utils.io.jvm.javaio.copyTo
import io.ktor.utils.io.jvm.javaio.toInputStream


class ReviewsService(
    private val reviewsRepository: ReviewsRepository,
    private val clientPhotoRepository: ClientPhotoRepository,
    private val photoStoragePath: String,
    private val userRepository: UserRepository,
    private val placeRepository: PlaceRepository
) {

    fun addReview(
        userId: EntityID<Int>,
        dto: ReviewRequestDto
    ): ReviewEntity? {
        listOf(dto.priceLevel, dto.ambienceRating, dto.ingredientQuality).forEach {
            if (it !in 1..5) return null
        }

        return transaction {
            val newReview = reviewsRepository.addReview(
                userId,
                dto.placeId,
                dto.priceLevel,
                dto.ambienceRating,
                dto.ingredientQuality,
                dto.comment
            ) ?: return@transaction null

            userRepository.addPoints(userId, 100)
            newReview
        }
    }

    suspend fun addReviewWithPhotos(
        userId: EntityID<Int>,
        multipartData: MultiPartData
    ): ReviewEntity? {
        var reviewRequest: ReviewRequestDto? = null
        val savedPhotoNames = mutableListOf<String>()

        try {
            multipartData.forEachPart { part ->
                when (part) {
                    is PartData.FormItem -> {
                        if (part.name == "review") {
                            reviewRequest = Json.decodeFromString(part.value)
                        }
                    }
                    is PartData.FileItem -> {
                        if (part.name == "photos") {
                            val originalName = part.originalFileName ?: "photo.jpg"
                            val ext = File(originalName).extension.ifBlank { "jpg" }
                            val filename = "${UUID.randomUUID()}.$ext"
                            val targetFile = File(photoStoragePath, filename)
                            part.provider().toInputStream().use { input ->
                                targetFile.outputStream().use { output ->
                                    input.copyTo(output)
                                }
                            }
                            savedPhotoNames.add(filename)
                        }
                    }
                    else -> {}
                }
                part.dispose()
            }

            val finalDto = reviewRequest ?: return null

            return transaction {
                val newReview = reviewsRepository.addReview(
                    userId,
                    finalDto.placeId,
                    finalDto.priceLevel,
                    finalDto.ambienceRating,
                    finalDto.ingredientQuality,
                    finalDto.comment
                ) ?: return@transaction null

                savedPhotoNames.forEach { photoName ->
                    clientPhotoRepository.addPhoto(newReview.id.value, photoName)
                }

                val place = placeRepository.findRestaurantById(EntityID(finalDto.placeId, PlacesTable))
                if(place!=null&&place.photoUrl==null){
                    placeRepository.updatePhotoUrl(place.id,savedPhotoNames.get(0))
                }
                userRepository.addPoints(userId, 100)
                newReview
            }
        } catch (e: Exception) {
            savedPhotoNames.forEach { name ->
                File(photoStoragePath, name).takeIf { it.exists() }?.delete()
            }
            return null
        }
    }

    fun getReviewsForPlace(placeId: Int): List<ReviewEntity> =
        reviewsRepository.getReviewsByPlace(placeId)

    fun getReviewsForUser(userId: EntityID<Int>): List<ReviewEntity> =
        reviewsRepository.getReviewsByUser(userId)

    fun deleteReview(reviewId: Int, userId: EntityID<Int>) =
        reviewsRepository.deleteReview(reviewId, userId)
}