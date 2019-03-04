package com.example.myapplication.model.data.places.search.response

data class Venue(
    var venuePage: VenuePage,
    var name: String,
    var location: Location,
    var id: String,
    var categories: Array<Category>,
    var url: String?
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Venue

        if (venuePage != other.venuePage) return false
        if (name != other.name) return false
        if (url != other.url) return false
        if (location != other.location) return false
        if (id != other.id) return false
        if (!categories.contentEquals(other.categories)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = venuePage.hashCode()
        result = 31 * result + name.hashCode()
        result = 31 * result + url.hashCode()
        result = 31 * result + location.hashCode()
        result = 31 * result + id.hashCode()
        result = 31 * result + categories.contentHashCode()
        return result
    }
}