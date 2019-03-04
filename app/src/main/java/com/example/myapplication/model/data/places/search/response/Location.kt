package com.example.myapplication.model.data.places.search.response

data class Location(
    val cc: String,

    val country: String,

    val address: String,

    val labeledLatLngs: Array<LabeledLatLngs>,

    val lng: String,

    val distance: String,

    val formattedAddress: Array<String>,

    val city: String,

    val postalCode: String,

    val state: String,

    val crossStreet: String,

    val lat: String
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Location

        if (cc != other.cc) return false
        if (country != other.country) return false
        if (address != other.address) return false
        if (!labeledLatLngs.contentEquals(other.labeledLatLngs)) return false
        if (lng != other.lng) return false
        if (distance != other.distance) return false
        if (!formattedAddress.contentEquals(other.formattedAddress)) return false
        if (city != other.city) return false
        if (postalCode != other.postalCode) return false
        if (state != other.state) return false
        if (crossStreet != other.crossStreet) return false
        if (lat != other.lat) return false

        return true
    }

    override fun hashCode(): Int {
        var result = cc.hashCode()
        result = 31 * result + country.hashCode()
        result = 31 * result + address.hashCode()
        result = 31 * result + labeledLatLngs.contentHashCode()
        result = 31 * result + lng.hashCode()
        result = 31 * result + distance.hashCode()
        result = 31 * result + formattedAddress.contentHashCode()
        result = 31 * result + city.hashCode()
        result = 31 * result + postalCode.hashCode()
        result = 31 * result + state.hashCode()
        result = 31 * result + crossStreet.hashCode()
        result = 31 * result + lat.hashCode()
        return result
    }
}
