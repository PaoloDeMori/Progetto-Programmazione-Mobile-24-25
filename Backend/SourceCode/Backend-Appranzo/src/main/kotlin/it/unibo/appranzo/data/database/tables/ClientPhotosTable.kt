package it.unibo.appranzo.data.database.tables

import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.ReferenceOption

object ClientPhotosTable : IntIdTable("CLIENT_PHOTOS") {
    val photoUrl = varchar("photo_url", 255)
    val reviewId = reference("review_id", ReviewsTable, onDelete = ReferenceOption.CASCADE)
}
