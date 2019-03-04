package com.example.myapplication.model.utils

class MetersConverter {

    fun convert(meters: Int): String {
        if (meters == 1000) {
            return "1km"
        }
        return if (meters > 1000) {
            "${meters / 1000},${meters / 100}km"
        } else "${meters}m"
    }
}
