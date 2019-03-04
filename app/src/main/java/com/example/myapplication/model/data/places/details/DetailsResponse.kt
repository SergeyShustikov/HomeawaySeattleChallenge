package com.example.myapplication.model.data.places.details

import com.example.myapplication.model.data.places.search.response.Meta

data class DetailsResponse(
    var meta: Meta,

    var response: VenueList
)