package it.unibo.appranzo.data.repositories

import it.unibo.appranzo.commons.data.FriendshipRequestStatus
import it.unibo.appranzo.communication.dtos.UserDto
import it.unibo.appranzo.communication.dtos.friendship.FriendshipRequestDto
import it.unibo.appranzo.data.database.daos.FriendshipEntity
import it.unibo.appranzo.data.database.daos.FriendshipRequestEntity
import it.unibo.appranzo.data.database.daos.UserEntity
import it.unibo.appranzo.data.database.tables.FriendshipRequestTable
import it.unibo.appranzo.data.database.tables.FriendshipsTable
import it.unibo.appranzo.data.database.tables.UsersTable
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.or
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import kotlin.math.max
import kotlin.math.min

class FriendsRepository {
    fun sendFriendshipRequest(senderUsername: String, receiverUsername: String): FriendshipRequestDto?=
        transaction {
            val sender = UserEntity.find { UsersTable.username eq senderUsername }.firstOrNull()
            val receiver = UserEntity.find { UsersTable.username eq receiverUsername }.firstOrNull()
            if (sender == null || receiver == null || sender.id == receiver.id) {
                return@transaction null
            }
            val obtained =
                FriendshipRequestEntity.find { ((FriendshipRequestTable.receiver eq receiver.id) and (FriendshipRequestTable.sender eq sender.id)
                    or ((FriendshipRequestTable.sender eq receiver.id) and (FriendshipRequestTable.receiver eq sender.id)))
                }
                    .firstOrNull()

            if (obtained == null) {
                val request = FriendshipRequestEntity.new {
                    this.sender = sender.id
                    this.receiver = receiver.id
                    this.status = FriendshipRequestStatus.PENDING
                }
                return@transaction FriendshipRequestDto(
                    id = request.id.value, senderId = request.sender.value,
                    receiverId = request.receiver.value, request.status
                )
            }
            else {
                null
            }
        }

    fun removeAFriend(removerUsername: String,friendToRemoveId:Int): Boolean=transaction{
        val remover = UserEntity.find { UsersTable.username eq removerUsername }.firstOrNull()

        if(remover==null)  return@transaction false
        val removerId=remover.id.value
        if (removerId == friendToRemoveId) {
            return@transaction false
        }
        val user1 = min(removerId, friendToRemoveId)
        val user2 = max(removerId, friendToRemoveId)

        val friendshipEntity = FriendshipEntity.find {
            (FriendshipsTable.user1 eq user1) and (FriendshipsTable.user2 eq user2)
        }.firstOrNull()

        if (friendshipEntity == null) {
            return@transaction false
        }
        val requestId = friendshipEntity.request?.value ?:null

        friendshipEntity.delete()

        if (requestId != null) {
            FriendshipRequestEntity.findById(requestId)?.delete()
        }

        true

    }

    fun allMyFriends(username: String): List<UserDto> = transaction{
        val user = UserEntity.find { UsersTable.username eq username }.firstOrNull()
            ?: return@transaction emptyList()
        val userId = user.id.value
        val friendshipRows = FriendshipsTable.selectAll().where {
            (FriendshipsTable.user1 eq userId) or (FriendshipsTable.user2 eq userId)
        }.toList()
        val friendIds = friendshipRows.map { row ->
            val user1 = row[FriendshipsTable.user1].value
            val user2 = row[FriendshipsTable.user2].value
            if (user1 == userId) user2 else user1
        }
        if (friendIds.isEmpty()) {
            return@transaction emptyList()
        }
        val friends = UserEntity.find { UsersTable.id inList friendIds }

        friends.map { it.toDto() }
    }

    fun acceptFriendshipRequest(username: String, friendshipRequestId:Int): Boolean = transaction{
        val user = UserEntity.find { UsersTable.username eq username }.firstOrNull()
            ?: return@transaction false

        val request = FriendshipRequestEntity.findById(friendshipRequestId)
            ?: return@transaction false

        val isValid = request.receiver.value == user.id.value &&
                request.status == FriendshipRequestStatus.PENDING

        if (!isValid) {
            return@transaction false
        }

        request.status = FriendshipRequestStatus.ACCEPTED

        true

    }

    fun rejectFriendshipRequest(username: String, friendshipRequestId:Int): Boolean = transaction{
        val user = UserEntity.find { UsersTable.username eq username }.firstOrNull()
            ?: return@transaction false
        val userId=user.id.value
        val request = FriendshipRequestEntity.findById(friendshipRequestId)
            ?: return@transaction false
        val isValid = (request.receiver.value == userId || request.sender.value == userId) &&
                request.status == FriendshipRequestStatus.PENDING
        if (!isValid) {
            return@transaction false
        }
        request.delete()
        true


    }

    fun getPendingRequestsForUser(username: String): List<FriendshipRequestDto> = transaction {
        val user = UserEntity.find { UsersTable.username eq username }.firstOrNull()
            ?: return@transaction emptyList()
        val userId=user.id.value
        FriendshipRequestEntity.find {
            (FriendshipRequestTable.receiver eq userId) and (FriendshipRequestTable.status eq FriendshipRequestStatus.PENDING)
        }.map { FriendshipRequestDto(
            id = it.id.value, senderId = it.sender.value,
            receiverId = it.receiver.value, it.status
        ) }
    }
}