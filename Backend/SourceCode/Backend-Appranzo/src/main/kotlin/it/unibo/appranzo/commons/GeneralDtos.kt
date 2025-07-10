package it.unibo.appranzo.commons

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class GeneralDtos{
    @SerialName("INVALID_VALUE")
    INVALID_VALUE,
    @SerialName("MISSING_VALUE")
    MISSING_VALUE,
    @SerialName("VOID_RESULT")
    VOID_RESULT,
    @SerialName("IMPOSSIBLE_TO_ADD")
    IMPOSSIBLE_TO_ADD,
    @SerialName("IMPOSSIBLE_TO_REMOVE")
    IMPOSSIBLE_TO_REMOVE,
    @SerialName("SUCCESSFULL")
    SUCCESSFULL
}