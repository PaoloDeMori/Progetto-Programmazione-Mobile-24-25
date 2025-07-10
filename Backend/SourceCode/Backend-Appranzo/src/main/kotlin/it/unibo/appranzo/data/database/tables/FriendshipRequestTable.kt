package it.unibo.appranzo.data.database.tables

import it.unibo.appranzo.commons.data.FriendshipRequestStatus
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.ReferenceOption

object FriendshipRequestTable: IntIdTable("FRIENDSHIP_REQUEST") {
    val sender = reference("sender_id", UsersTable, onDelete = ReferenceOption.CASCADE)
    val receiver = reference("receiver_id", UsersTable, onDelete = ReferenceOption.CASCADE)
    val status = enumerationByName("status", 10, FriendshipRequestStatus::class)
        .default(FriendshipRequestStatus.PENDING)
}