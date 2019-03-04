package com.example.myapplication.model.data.places.search.response

data class Response(var venues: Array<Venue>) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Response

        if (!venues.contentEquals(other.venues)) return false

        return true
    }

    override fun hashCode(): Int {
        return venues.contentHashCode()
    }
}