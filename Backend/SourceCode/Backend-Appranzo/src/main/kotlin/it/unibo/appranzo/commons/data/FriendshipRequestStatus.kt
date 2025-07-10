package it.unibo.appranzo.commons.data

import kotlinx.serialization.Serializable

@Serializable
enum class FriendshipRequestStatus {
    PENDING,
    ACCEPTED,
    REJECTED
}