package it.unibo.appranzo.model.security

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class JwtData(
    @SerialName("secret-access")
    val secretAccess:String,
    @SerialName("secret-refresh")
    val secretRefresh:String,
    val audience:String,
    val expirationAccessTime: Long,
    val expirationRefreshTime: Long,
    val photoStoragePath:String
)