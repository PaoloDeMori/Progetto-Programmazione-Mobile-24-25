package it.unibo.appranzo.data.database.daos

import it.unibo.appranzo.data.database.tables.FavoritesTable
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID

class FavoriteEntity(id: EntityID<Int>): IntEntity(id){
    companion object: IntEntityClass<FavoriteEntity>(FavoritesTable)
    var placeId by FavoritesTable.placeId
    var userId by FavoritesTable.userId
}