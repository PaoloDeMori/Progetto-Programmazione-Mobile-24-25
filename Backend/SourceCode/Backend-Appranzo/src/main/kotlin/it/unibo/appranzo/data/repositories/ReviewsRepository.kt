package it.unibo.appranzo.data.repositories

import it.unibo.appranzo.data.database.daos.ReviewEntity
import it.unibo.appranzo.data.database.tables.PlacesTable
import it.unibo.appranzo.data.database.tables.ReviewsTable
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDateTime
import kotlin.math.roundToInt

class ReviewsRepository {
    fun addReview(
        userId: EntityID<Int>,
        placeId: Int,
        priceLevel: Byte,
        ambienceRating: Byte,
        ingredientQuality: Byte,
        comment: String?
    ): ReviewEntity? = transaction {
        val computedRating = listOf(priceLevel, ambienceRating, ingredientQuality)
            .average()
            .roundToInt().toByte()

        ReviewEntity.new {
            this.userId            = userId
            this.placeId           = EntityID(placeId, PlacesTable)
            this.rating            = computedRating
            this.priceLevel        = priceLevel
            this.ambienceRating    = ambienceRating
            this.ingredientQuality = ingredientQuality
            this.comment           = comment
            this.creationDate      = LocalDateTime.now()
        }
    }


    fun getReviewsByPlace(placeId: Int): List<ReviewEntity> = transaction {
        ReviewEntity.find { ReviewsTable.placeId eq placeId }
            .orderBy(ReviewsTable.creationDate to SortOrder.DESC)
            .toList()
    }


    fun getReviewsByUser(userId: EntityID<Int>): List<ReviewEntity> = transaction {
        ReviewEntity.find { ReviewsTable.userId eq userId }
            .orderBy(ReviewsTable.creationDate to SortOrder.DESC)
            .toList()
    }

    fun deleteReview(reviewId: Int, userId: EntityID<Int>) = transaction {
        ReviewsTable.deleteWhere {
            (ReviewsTable.id eq reviewId) and (ReviewsTable.userId eq userId)
        }
    }

    fun findReviewById(reviewId: Int): ReviewEntity? = transaction {
        ReviewEntity.findById(reviewId)
    }

}