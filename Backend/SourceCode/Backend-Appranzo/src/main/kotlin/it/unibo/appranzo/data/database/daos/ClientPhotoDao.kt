package it.unibo.appranzo.data.database.daos

import it.unibo.appranzo.data.database.tables.ClientPhotosTable
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID

class ClientPhotoEntity(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<ClientPhotoEntity>(ClientPhotosTable)

    var photoUrl by ClientPhotosTable.photoUrl
    var reviewId by ClientPhotosTable.reviewId
}