package com.example.myapplication.model.data.places.details

import com.example.myapplication.model.data.places.search.response.Category

data class FullPlaceInfo (
    val id: String,
    val name: String,
    val categories: List<Category>,
    val contact: VenueContact,
    val location: VenueLocation,
    val url: String?,
    val rating: Double,
    val price: VenuePrice?,
    val likes: VenueLikes,
    val ratingColor: String?,
    val shortUrl: String?
)
