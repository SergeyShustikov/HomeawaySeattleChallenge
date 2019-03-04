package com.example.myapplication.model.utils

import com.example.myapplication.model.data.places.search.Place
import com.example.myapplication.model.data.places.search.response.Venue
import com.example.myapplication.view.adapter.LikeState
import com.google.android.gms.maps.model.LatLng
import java.lang.StringBuilder

class PlaceConverter {


    fun convert(venue: Venue): Place {
        val place = Place()
        place.id = venue.id
        place.name = venue.name
        if (!venue.url.isNullOrEmpty()) {
            place.website = venue.url!!
        } else {
            place.website = ""
        }
        val categoryStringBuilder = StringBuilder()
        val addressStringBuilder = StringBuilder()
        for (formattedAddress in venue.location.formattedAddress) {
            addressStringBuilder.append(formattedAddress)
            addressStringBuilder.append(',')
        }
        if (addressStringBuilder.isNotEmpty()) {
            place.address = addressStringBuilder.substring(0, addressStringBuilder.length - 1)
        } else {
            // prevent crashing
            place.address = ""
        }      //keep variable names here to make more clear understanding
        for ((pluralName, name, icon, id, shortName, primary) in venue.categories) {
            categoryStringBuilder.append(name)
            categoryStringBuilder.append(',')
        }
        if (categoryStringBuilder.isNotEmpty()) {
            place.category = categoryStringBuilder.substring(0, categoryStringBuilder.length - 1)
        } else {
            // prevent crashing
            place.category = ""
        }
        place.distanceFromCenter = MetersConverter().convert(venue.location.distance.toInt())
        if (venue.categories.isNotEmpty()) {
            place.iconUrl = venue.categories[0].icon.prefix + "88" + venue.categories[0].icon.suffix
        } else {
            // prevent crashing
            place.iconUrl = ""
        }
        place.likeState = LikeState.NOT_LIKED // TODO: load favorite
        place.coordinates =
            LatLng(venue.location.lat.toDouble(), venue.location.lng.toDouble())
        return place
    }
}