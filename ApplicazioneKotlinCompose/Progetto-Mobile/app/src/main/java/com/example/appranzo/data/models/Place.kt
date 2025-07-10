package com.example.appranzo.data.models

data class Place(
    val id:Int,
    val name:String,
    val description :String?,
    val address: String?,
    val city: String,
    val photoUrl: String?,
    val categoryName: String,
    val rating:Double,
    val distanceFromUser:Double?,
    val latitude: Double,
    val longitude: Double
)
