package it.unibo.appranzo.data.repositories

import it.unibo.appranzo.data.database.daos.FavoriteEntity
import it.unibo.appranzo.data.database.tables.FavoritesTable
import it.unibo.appranzo.data.database.tables.PlacesTable
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.transactions.transaction

class FavoritesRepository(placeRepository: PlaceRepository) {

    fun findFavoritesByUserId( userId: EntityID<Int>): List<FavoriteEntity>{
        return transaction {
            FavoriteEntity.find{
                FavoritesTable.userId eq userId
            }.toList()
        }
    }

    fun addNewFavorite(userId: EntityID<Int>, placeId:Int): Boolean{
        val placeEntityId = EntityID(placeId, PlacesTable)
        return transaction {
            if (FavoriteEntity.find {
                    (FavoritesTable.userId eq userId) and (FavoritesTable.placeId eq placeEntityId)
                }.empty()) {
                FavoriteEntity.new {
                    this.userId = userId
                    this.placeId = placeEntityId
                }
                true
            } else {
                false
            }
        }
    }

    fun removeFavorite(userId: EntityID<Int>, placeId:Int): Int {
        val placeEntityId = EntityID(placeId, PlacesTable)
        return transaction {
            FavoritesTable.deleteWhere{
                (this.userId eq userId ) and (this.placeId eq placeEntityId)
            }
        }
    }


}
