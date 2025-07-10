package com.example.appranzo.data.repository

import com.example.appranzo.communication.remote.loginDtos.ResearchDto
import com.example.appranzo.data.models.Category
import com.example.appranzo.data.models.Coordinates
import com.example.appranzo.data.models.Place

interface PlacesRepository {
    fun getCategories(): List<Category>
    fun getNearRestaurants(coordinates: Coordinates): List<Place>
    fun getRestaurantsWithoutPosition(researchDto: ResearchDto): List<Place>
}