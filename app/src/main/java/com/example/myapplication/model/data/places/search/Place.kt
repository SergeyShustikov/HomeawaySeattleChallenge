package com.example.myapplication.model.data.places.search

import android.os.Parcel
import android.os.Parcelable
import com.example.myapplication.view.adapter.LikeState
import com.google.android.gms.maps.model.LatLng

@Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
class Place : Parcelable {


    lateinit var id: String
    lateinit var name: String
    lateinit var category: String
    lateinit var iconUrl: String
    lateinit var address: String
    lateinit var distanceFromCenter: String
    lateinit var website: String
    lateinit var likeState: LikeState
    lateinit var coordinates: LatLng

    /*Default constructor*/
    constructor()

    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readInt(),
        parcel.readDouble(),
        parcel.readDouble()
    )

    constructor(
        id: String,
        name: String,
        category: String,
        iconUrl: String,
        distance: String,
        address: String,
        website: String,
        likeState: Int,
        lat: Double,
        long: Double
    ) {
        this.id = id
        this.name = name
        this.category = category
        this.iconUrl = iconUrl
        this.distanceFromCenter = distance
        this.address = address
        this.website = website
        when (likeState) {
            0 -> this.likeState = LikeState.LIKED
            1 -> this.likeState = LikeState.NOT_LIKED
        }
        this.coordinates = LatLng(lat, long)
    }


    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(name)
        parcel.writeString(category)
        parcel.writeString(iconUrl)
        parcel.writeString(distanceFromCenter)
        parcel.writeString(address)
        parcel.writeString(website)
        parcel.writeInt(likeState.ordinal)
        parcel.writeDouble(coordinates.latitude)
        parcel.writeDouble(coordinates.longitude)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Place> {
        override fun createFromParcel(parcel: Parcel): Place {
            return Place(parcel)
        }

        override fun newArray(size: Int): Array<Place?> {
            return arrayOfNulls(size)
        }
    }


}