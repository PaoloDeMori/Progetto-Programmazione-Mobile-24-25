package it.unibo.appranzo.data.repositories

import io.ktor.client.HttpClient
import io.ktor.util.toLowerCasePreservingASCIIRules
import io.ktor.util.toUpperCasePreservingASCIIRules
import it.unibo.appranzo.commons.geography.GeographyHelper.Helper.findDelta
import it.unibo.appranzo.commons.geography.GeographyHelper.Helper.haversineFormula
import it.unibo.appranzo.data.database.daos.PlaceEntity
import it.unibo.appranzo.data.database.tables.CategoriesTable
import it.unibo.appranzo.data.database.tables.PlacesTable
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.Op
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.lowerCase
import org.jetbrains.exposed.sql.transactions.transaction
import java.math.BigDecimal

class PlaceRepository(val citiesRepository: CitiesRepository, val categoriesRepository: CategoriesRepository, val httpClient: HttpClient) {
    companion object {
        val StreetsNames = setOf(
            "via", "viale", "strada", "corso", "piazza", "piazzale", "vicolo", "rampa",
            "rotatoria", "borgo", "corte", "crescente", "largo", "sentiero", "traversa",
            "calle", "contrada", "passaggio", "vico", "lungomare", "salita", "sottopasso",

            "street", "st", "avenue", "ave", "road", "rd", "lane", "ln", "boulevard", "blvd",
            "drive", "dr", "court", "ct", "circle", "cir", "place", "pl", "terrace", "ter",
            "way", "walk", "alley", "allee", "parkway", "pkwy", "expressway", "expwy", "square", "sq"
        )
    }

    fun findRestaurantById(id: EntityID<Int>): PlaceEntity? {
        return transaction {
            PlaceEntity.find { PlacesTable.id eq id }.firstOrNull()
        }
    }

    fun findRestaurantsByCity(city: String): List<PlaceEntity> {
        return transaction {
            val cityIdFound: EntityID<Int>? = citiesRepository.findCityIdByName(city.toUpperCasePreservingASCIIRules())
            if (cityIdFound != null) {
                PlaceEntity.find { PlacesTable.cityId eq cityIdFound }.toList()
            } else {
                emptyList()
            }
        }
    }

    fun findRestaurantsByCityId(city: EntityID<Int>): List<PlaceEntity> {
        return transaction {
            val places = PlaceEntity.find { PlacesTable.cityId eq city }.toList()
            places
        }
    }

    fun findRestaurantsByCityByAddress(city: String, road: String): List<PlaceEntity> {
        return transaction {
            val filteredRoad = "%${road.trim()}%"
            val cityIdFound: EntityID<Int>? = citiesRepository.findCityIdByName(city.toUpperCasePreservingASCIIRules())
            val places = cityIdFound?.let {
                PlaceEntity.find { (PlacesTable.cityId eq it) and (PlacesTable.address.lowerCase() like filteredRoad) }
                    .toList()
            } ?: emptyList()
            places
        }
    }


    fun findRestaurantsByCityIdByAddress(city: EntityID<Int>, road: String): List<PlaceEntity> {
        return transaction {
            val filteredRoad = "%${road.trim()}%"
            val places =
                PlaceEntity.find { (PlacesTable.cityId eq city) and (PlacesTable.address.lowerCase() like filteredRoad) }
                    .toList()
            places
        }
    }

    fun findPlacesByCategory(categoryId: EntityID<Int>): List<PlaceEntity> = transaction {
        PlaceEntity.find { PlacesTable.categoryId eq categoryId }.toList()
    }

    fun findNearRestaurants(
        userLatitude: BigDecimal,
        userLongitude: BigDecimal,
        nameFilter: String?
    ): List<PlaceEntity> {
        return transaction {
            var distanceFromUser = 5.0
            var listPlaceEntity: List<PlaceEntity>

            var minLatitude: BigDecimal
            var maxLatitude: BigDecimal
            var minLongitude: BigDecimal
            var maxLongitude: BigDecimal

            do {
                val newDelta = findDelta(distanceFromUser, userLatitude.toDouble())
                minLatitude = userLatitude - newDelta.latitude.toBigDecimal()
                maxLatitude = userLatitude + newDelta.latitude.toBigDecimal()
                minLongitude = userLongitude - newDelta.longitude.toBigDecimal()
                maxLongitude = userLongitude + newDelta.longitude.toBigDecimal()

                listPlaceEntity = transaction {
                    PlaceEntity.find {
                        (PlacesTable.latitude greaterEq minLatitude) and
                                (PlacesTable.latitude lessEq maxLatitude) and
                                (PlacesTable.longitude greaterEq minLongitude) and
                                (PlacesTable.longitude lessEq maxLongitude) and
                                if (nameFilter != null)
                                    (PlacesTable.name.lowerCase() like "%${nameFilter.lowercase()}%")
                                else
                                    Op.TRUE
                    }
                }.limit(25).toList()
                distanceFromUser++
            } while (distanceFromUser < 10.0 && listPlaceEntity.isEmpty())
            listPlaceEntity.sortedBy {
                it.distanceFromUser = haversineFormula(
                    userLatitude.toDouble(), userLongitude.toDouble(),
                    it.latitudeDouble, it.longitudeDouble
                )
                it.distanceFromUser
            }
        }
    }

    fun saveNewPlace(
        name: String, description: String?, city: String, address: String?, latitude: Double,
        longitude: Double, photoUrl: String?, categoryName: String
    ): PlaceEntity? {
        val cityId = citiesRepository.findCityIdByName(city)
        if (cityId == null) return null
        return transaction {
            val categoryId: EntityID<Int>? = categoriesRepository.findCategoryIdByName(categoryName)
            PlaceEntity.new {
                this.name = name
                this.longitude = longitude.toBigDecimal()
                this.latitude = latitude.toBigDecimal()
                this.cityId = cityId
                description?.let { this.description = it }
                address?.let { this.address = it }
                photoUrl?.let { this.photoUrl = it }
                categoryId?.let { this.categoryId = it } ?: EntityID(1, CategoriesTable)
            }
        }

    }

    fun saveNewPlace(
        name: String, description: String?, city: String, address: String?, latitude: Double,
        longitude: Double, photoUrl: String?, categoryId: EntityID<Int>, region: String?
    ): PlaceEntity? {
        var cityId = citiesRepository.findCityIdByName(city)
        if (cityId == null) cityId = citiesRepository.addCity(city, region)
        return transaction {
            PlaceEntity.new {
                this.name = name
                this.longitude = longitude.toBigDecimal()
                this.latitude = latitude.toBigDecimal()
                this.cityId = cityId
                description?.let { this.description = it }
                address?.let { this.address = it }
                photoUrl?.let { this.photoUrl = it }
                this.categoryId = categoryId
            }
        }

    }

    fun findSimilarName(input: String, latitude: Double?, longitude: Double?): List<PlaceEntity> {
        val listPlaceEntity: List<PlaceEntity> = transaction {
            val lowerInput = input.toLowerCasePreservingASCIIRules()
            PlaceEntity.find { PlacesTable.name.lowerCase() like "%${lowerInput}%" }.limit(25).toList()
        }
        if (latitude != null && longitude != null) {
            return listPlaceEntity.sortedBy {
                haversineFormula(
                    latitude, longitude,
                    it.latitudeDouble, it.longitudeDouble
                )
            }
        }
        return listPlaceEntity
    }

    fun findCityIdInInput(input: String): Pair<EntityID<Int>?, String> {
        val words = input.trim().split("\\s+".toRegex())
        val size = words.size

        for (length in size downTo 1) {
            for (start in 0..(size - length)) {
                val cityCandidate = words.subList(start, start + length).joinToString(" ")
                val cityId = citiesRepository.findCityIdByName(cityCandidate)
                if (cityId != null) {
                    val remainingWords = words.take(start) + words.drop(start + length)
                    val remaining = remainingWords.joinToString(" ")
                    return cityId to remaining
                }
            }
        }
        return null to input
    }

    private fun searchInternal(input: String, latitude: Double?, longitude: Double?): List<PlaceEntity> {
        val (cityId, remainingAddress) = findCityIdInInput(input)
        var listToReturn: List<PlaceEntity> = emptyList()
        if (cityId != null) {
            val filteredAddress = remainingAddress.split("\\s+".toRegex())
                .filter { it.lowercase() !in StreetsNames }
                .joinToString(" ")
            if (filteredAddress.isNotBlank()) {
                val list1 = findRestaurantsByCityIdByAddress(cityId, filteredAddress)
                val list2 = findSimilarName(filteredAddress, latitude, longitude)
                listToReturn = (list1 + list2).distinct().onEach {
                    if (latitude != null && longitude != null) {
                        it.distanceFromUser =
                            haversineFormula(latitude, longitude, it.latitudeDouble, it.longitudeDouble)
                    }
                }.sortedBy {
                    if (latitude != null && longitude != null) it.distanceFromUser
                    else 0.0
                }
            }
            listToReturn = (listToReturn + (findRestaurantsByCityId(cityId)).distinct().onEach {
                if (latitude != null && longitude != null) {
                    it.distanceFromUser =
                        haversineFormula(latitude, longitude, it.latitudeDouble, it.longitudeDouble)
                }
            }.sortedBy {
                if (latitude != null && longitude != null) it.distanceFromUser
                else 0.0
            }).distinct()
        } else {
            val list3 = findSimilarName(input, latitude, longitude)
            listToReturn = (list3).distinct().onEach {
                if (latitude != null && longitude != null) {
                    it.distanceFromUser =
                        haversineFormula(latitude, longitude, it.latitudeDouble, it.longitudeDouble)
                }
            }.sortedBy {
                if (latitude != null && longitude != null) it.distanceFromUser
                else 0.0
            }
        }
        return listToReturn
    }

    fun search(input: String): List<PlaceEntity> = searchInternal(input, null, null)

    fun search(input: String, latitude: Double, longitude: Double): List<PlaceEntity>? =
        searchInternal(input, latitude, longitude)

    fun getAllPlacesSortedByRating(): List<PlaceEntity> = transaction {
        PlaceEntity.all().sortedByDescending {
            it.toDto()?.averageRating ?: 0.0
        }.take(10)    }

    fun updatePhotoUrl(placeId: EntityID<Int>, newPhotoUrl: String): Boolean {
        return transaction {
            PlaceEntity.findById(placeId)?.let {
                it.photoUrl = newPhotoUrl
                true
            } ?: false
        }
    }

}