package com.example.appranzo.communication.remote.loginDtos


import kotlinx.serialization.Serializable

@Serializable
data class CategoryDto(
    val id: Int,
    val name: String
)