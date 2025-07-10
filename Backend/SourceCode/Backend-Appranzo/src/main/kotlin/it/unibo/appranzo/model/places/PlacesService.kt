package it.unibo.appranzo.model.places

import io.ktor.util.toUpperCasePreservingASCIIRules
import it.unibo.appranzo.communication.dtos.places.PositionDto
import it.unibo.appranzo.data.database.daos.CategoryEntity
import it.unibo.appranzo.data.database.daos.PlaceEntity
import it.unibo.appranzo.data.repositories.CategoriesRepository
import it.unibo.appranzo.data.repositories.FavoritesRepository
import it.unibo.appranzo.data.repositories.PlaceRepository
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.dao.id.EntityID

@Serializable
enum class Favorite_Toggle_Result {
    ADDED_FAVORITE,
    REMOVED_FAVORITE
}

class PlacesService(val placesRepository: PlaceRepository, val favoritesRepository: FavoritesRepository, private val categoriesRepository: CategoriesRepository) {

    fun findRestaurantById(placeId: EntityID<Int>): PlaceEntity?{
        return placesRepository.findRestaurantById(placeId)
    }

    fun findRestaurantByPosition(latitude: Double, longitude: Double): List<PlaceEntity>{
           return placesRepository.findNearRestaurants(latitude.toBigDecimal(),longitude.toBigDecimal(),null)
        }

    fun getPlacesByCategory(userId: EntityID<Int>, categoryId: EntityID<Int>): List<PlaceEntity> {
        return placesRepository.findPlacesByCategory(categoryId)
    }


    fun placeByCity(city: String): List<PlaceEntity>{
        return placesRepository.findRestaurantsByCity(city.toUpperCasePreservingASCIIRules())
    }
    fun searchByName(input:String):List<PlaceEntity>{
        return placesRepository.search(input)?:emptyList()
    }

    fun searchByNameAndCoordinates(input:String,latitude: Double,longitude: Double):List<PlaceEntity>{
        return placesRepository.search(input,latitude,longitude)?:emptyList()
    }
    fun getAllCategories(): List<CategoryEntity> =
        categoriesRepository.getAllCategories()



    fun getFavorites(userId: EntityID<Int>): List<PlaceEntity>{
       return favoritesRepository.findFavoritesByUserId(userId).mapNotNull { findRestaurantById(it.placeId)}
    }

    fun isFavourites(placeEntityId: Int,userId: EntityID<Int>): Boolean{
        val favorites = getFavorites(userId)
        return favorites.any { it.id.value == placeEntityId }
    }


    fun addFavorite(userId: EntityID<Int>,placeId: Int): Boolean{
      return  favoritesRepository.addNewFavorite(userId,placeId)
    }

    fun removeFavorite(userId: EntityID<Int>,placeId: Int):Int{
        return favoritesRepository.removeFavorite(userId,placeId)
    }

    fun toggleFavourite(userId: EntityID<Int>, placeId: Int): Favorite_Toggle_Result{
        if(favoritesRepository.removeFavorite(userId,placeId)==0){
            favoritesRepository.addNewFavorite(userId,placeId)
            return Favorite_Toggle_Result.ADDED_FAVORITE
        }
        else{
            return Favorite_Toggle_Result.REMOVED_FAVORITE
        }
    }

    fun getAllPlacesSortedByRating(): List<PlaceEntity> {
        return placesRepository.getAllPlacesSortedByRating()
    }

    fun getCoordinates(placeId: EntityID<Int>): PositionDto? =
        findRestaurantById(placeId)
            ?.let { PositionDto(it.latitude.toDouble(), it.longitude.toDouble()) }

}