package it.unibo.appranzo.data.database.daos

import it.unibo.appranzo.communication.dtos.places.PlaceDto
import it.unibo.appranzo.data.database.tables.PlacesTable
import it.unibo.appranzo.data.database.tables.ReviewsTable
import it.unibo.appranzo.data.repositories.CitiesRepository
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID

class PlaceEntity(id: EntityID<Int>): IntEntity(id){
    companion object: IntEntityClass<PlaceEntity>(PlacesTable)

    var name by PlacesTable.name
    var description by PlacesTable.description
    var cityId by PlacesTable.cityId
    var address by PlacesTable.address
    var latitude by PlacesTable.latitude
    var longitude by PlacesTable.longitude
    var photoUrl by PlacesTable.photoUrl
    var categoryId by PlacesTable.categoryId
    var distanceFromUser: Double? = null
    val reviews by ReviewEntity referrersOn ReviewsTable.placeId



    val latitudeDouble:Double
        get() = latitude.toDouble()
    val longitudeDouble: Double
        get() = longitude.toDouble()

    fun toDto(): PlaceDto? {
        val reviews = this.reviews.toList()
        val averageRating = if (reviews.isNotEmpty()) {
            reviews.map { review ->
                val price = review.priceLevel?.toDouble() ?: 0.0
                val ambience = review.ambienceRating?.toDouble() ?: 0.0
                val quality = review.ingredientQuality?.toDouble() ?: 0.0
                (price + ambience + quality) / 3.0
            }.average()
        } else {
            null
        }
        val cityName = CitiesRepository().findNameById(this.cityId)
        if(cityName!=null) {
          return PlaceDto(
                id = this.id.value,
                name = this.name,
                description = this.description,
                city = cityName,
                address = this.address,
                latitude = this.latitude.toDouble(),
                longitude = this.longitude.toDouble(),
                photoUrl = this.photoUrl,
                categoryId = this.categoryId.value,
                distanceFromUser = this.distanceFromUser,
                averageRating = averageRating
            )
        }
        else{return null}
    }
}