package it.unibo.appranzo.data.database.daos

import it.unibo.appranzo.data.database.tables.ReviewsTable
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID

class ReviewEntity(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<ReviewEntity>(ReviewsTable)

    var placeId           by ReviewsTable.placeId
    var userId            by ReviewsTable.userId
    var rating            by ReviewsTable.rating
    var priceLevel        by ReviewsTable.priceLevel
    var ambienceRating    by ReviewsTable.ambienceRating
    var ingredientQuality by ReviewsTable.ingredientQuality
    var comment           by ReviewsTable.comment
    var creationDate      by ReviewsTable.creationDate
}