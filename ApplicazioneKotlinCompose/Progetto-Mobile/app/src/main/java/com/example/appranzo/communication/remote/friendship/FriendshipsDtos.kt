package com.example.appranzo.communication.remote.friendship

import kotlinx.serialization.Serializable

@Serializable
enum class FriendshipRequestStatus {
    PENDING,
    ACCEPTED,
    REJECTED
}

@Serializable
data class FriendshipRequestDto(
    val id: Int,
    val senderId: Int,
    val receiverId: Int,
    val status: FriendshipRequestStatus
)
