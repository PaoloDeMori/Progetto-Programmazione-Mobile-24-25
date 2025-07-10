package it.unibo.appranzo.model.places

import it.unibo.appranzo.data.database.daos.ClientPhotoEntity
import it.unibo.appranzo.data.repositories.ClientPhotoRepository
import org.jetbrains.exposed.dao.id.EntityID

class ClientPhotosService(val clientPhotoRepository: ClientPhotoRepository){
    fun getPhotosFromReview(reviewId: EntityID<Int>): List<ClientPhotoEntity>{
        return clientPhotoRepository.getPhotosFromReview(reviewId)
    }
}