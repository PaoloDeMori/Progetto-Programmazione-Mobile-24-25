package it.unibo.appranzo.data.database.tables

import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.javatime.datetime


object ReviewsTable : IntIdTable("REVIEWS") {
    val placeId           = reference("place_id", PlacesTable, onDelete = ReferenceOption.CASCADE)
    val userId            = reference("user_id", UsersTable, onDelete = ReferenceOption.CASCADE)
    val rating            = byte("rating")
    val priceLevel        = byte("price_level")
    val ambienceRating    = byte("ambience_rating")
    val ingredientQuality = byte("ingredient_quality")
    val comment           = text("comment").nullable()
    val creationDate      = datetime("creation_date")
}
