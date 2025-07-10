package it.unibo.appranzo.data.database.daos

import it.unibo.appranzo.communication.dtos.friendship.FriendshipRequestDto
import it.unibo.appranzo.data.database.tables.FriendshipRequestTable
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID

class FriendshipRequestEntity(id: EntityID<Int>): IntEntity(id){
    companion object: IntEntityClass<FriendshipRequestEntity>(FriendshipRequestTable)

    var sender by FriendshipRequestTable.sender
    var receiver by FriendshipRequestTable.receiver
    var status by FriendshipRequestTable.status

    fun FriendshipRequestEntity.toDto(): FriendshipRequestDto {
        return FriendshipRequestDto(
            id = this.id.value,
            senderId = this.sender.value,
            receiverId = this.receiver.value,
            status = this.status
        )
    }
}