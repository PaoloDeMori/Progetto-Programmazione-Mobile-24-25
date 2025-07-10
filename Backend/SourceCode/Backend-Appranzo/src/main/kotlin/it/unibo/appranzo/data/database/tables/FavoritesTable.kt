package it.unibo.appranzo.data.database.tables

import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.ReferenceOption

object FavoritesTable : IntIdTable("FAVORITES") {
    val placeId = reference("place_id", PlacesTable, onDelete = ReferenceOption.CASCADE)
    val userId = reference("user_id", UsersTable, onDelete = ReferenceOption.CASCADE)

    init {
        uniqueIndex("UNIQUE_FAVORITE", placeId, userId)
    }
}
