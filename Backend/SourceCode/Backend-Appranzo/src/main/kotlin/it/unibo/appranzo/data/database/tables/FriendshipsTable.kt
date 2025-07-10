package it.unibo.appranzo.data.database.tables

import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.javatime.CurrentDateTime
import org.jetbrains.exposed.sql.javatime.datetime

object FriendshipsTable : IntIdTable("FRIENDSHIPS") {
    val user1 = reference("user_id_1", UsersTable, onDelete = ReferenceOption.CASCADE)
    val user2 = reference("user_id_2", UsersTable, onDelete = ReferenceOption.CASCADE)
    val request = optReference("request_id", FriendshipRequestTable, onDelete = ReferenceOption.SET_NULL)
    val friendshipDate = datetime("friendship_date").defaultExpression(CurrentDateTime)

    init {
        uniqueIndex("Only_One_Friendship_For_Couple", user1, user2)
    }
}
