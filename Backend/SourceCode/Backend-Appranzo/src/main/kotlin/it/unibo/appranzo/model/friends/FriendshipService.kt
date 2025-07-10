package it.unibo.appranzo.model.friends

import it.unibo.appranzo.communication.dtos.UserDto
import it.unibo.appranzo.communication.dtos.friendship.FriendshipRequestDto
import it.unibo.appranzo.data.repositories.FriendsRepository

class FriendsService(private val friendsRepository: FriendsRepository) {

    fun allMyFriends(username: String): List<UserDto> {
        return friendsRepository.allMyFriends(username)
    }


    fun acceptFriendshipRequest(username: String, friendshipRequestId:Int): Boolean{
        return friendsRepository.acceptFriendshipRequest(username,friendshipRequestId)
    }

    fun rejectFriendshipRequest(username: String, friendshipRequestId:Int): Boolean{
        return friendsRepository.rejectFriendshipRequest(username,friendshipRequestId)
    }

    fun getPendingRequestsForUser(username: String): List<FriendshipRequestDto>{
        return friendsRepository.getPendingRequestsForUser(username)
    }

    fun removeAFriend(removerUsername: String,friendToRemoveId:Int): Boolean{
        return friendsRepository.removeAFriend(removerUsername,friendToRemoveId)
    }

    fun sendFriendshipRequest(senderUsername: String, receiverUsername: String): FriendshipRequestDto? {
        return friendsRepository.sendFriendshipRequest(senderUsername,receiverUsername)
    }

}