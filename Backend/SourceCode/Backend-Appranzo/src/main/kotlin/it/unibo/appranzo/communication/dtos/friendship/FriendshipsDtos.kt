package it.unibo.appranzo.communication.dtos.friendship

import it.unibo.appranzo.commons.data.FriendshipRequestStatus
import kotlinx.serialization.Serializable

@Serializable
data class FriendshipRequestDto(
    val id: Int,
    val senderId: Int,
    val receiverId: Int,
    val status: FriendshipRequestStatus
)