package it.unibo.appranzo.data.database.daos

import it.unibo.appranzo.data.database.tables.FriendshipsTable
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID

class FriendshipEntity(id: EntityID<Int>): IntEntity(id){
    companion object: IntEntityClass<FriendshipEntity>(FriendshipsTable)

    var user1 by FriendshipsTable.user1
    var user2 by FriendshipsTable.user2
    var request by FriendshipsTable.request
    val friendshipDate by FriendshipsTable.friendshipDate
    }