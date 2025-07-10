package it.unibo.appranzo.data.repositories

import it.unibo.appranzo.data.database.daos.ClientPhotoEntity
import it.unibo.appranzo.data.database.tables.ClientPhotosTable
import it.unibo.appranzo.data.database.tables.ReviewsTable
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.transactions.transaction

class ClientPhotoRepository {

    fun addPhoto(reviewId: Int, photoUrl: String): ClientPhotoEntity? = transaction {
        ClientPhotoEntity.new {
            this.reviewId = EntityID(reviewId, ReviewsTable)
            this.photoUrl = photoUrl
        }
    }

    fun getPhotosFromReview(reviewId: EntityID<Int>): List<ClientPhotoEntity> = transaction {
        ClientPhotoEntity.find { ClientPhotosTable.reviewId eq reviewId }.toList()
    }
}