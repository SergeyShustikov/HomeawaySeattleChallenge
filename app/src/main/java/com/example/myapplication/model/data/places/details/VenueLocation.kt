package com.example.myapplication.model.data.places.details

import java.lang.StringBuilder

data class VenueLocation(
    val lat: Double,
    val lng: Double,

    val formattedAddress: List<String>
) {
    fun getAddress(): String {
        val builder = StringBuilder()
        for (item in formattedAddress) {
            builder.append(item).append(',')
        }
        return if (builder.isNotEmpty()) {
            builder.substring(0, builder.length - 1)
        } else {
            ""
        }
    }
}
