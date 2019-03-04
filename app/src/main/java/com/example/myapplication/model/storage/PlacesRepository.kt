package com.example.myapplication.model.storage

import com.example.myapplication.model.data.places.details.FullPlaceInfo
import com.example.myapplication.model.data.places.search.Place
import com.example.myapplication.model.data.places.search.response.SearchResponse
import com.example.myapplication.model.data.places.search.response.Venue
import com.example.myapplication.model.rest.SearchApi
import com.example.myapplication.model.utils.PlaceConverter
import com.example.myapplication.model.utils.RxResult
import com.example.myapplication.model.utils.RxSchedulers
import com.example.myapplication.view.adapter.LikeState
import io.reactivex.Observable
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

class PlacesRepository @Inject constructor(
    private val searchApi: SearchApi,
    private val rxSchedulers: RxSchedulers,
    private val favoritePlacesManager: FavoritePlacesManager
) {

    fun search(query: String): Observable<RxResult<List<Place>>> {
        return searchApi.search(SearchApi.CLIENT_ID, SearchApi.CLIENT_SECRET, query)
            .subscribeOn(rxSchedulers.network())
            .observeOn(rxSchedulers.main())
            .map {
                return@map RxResult.data(extractPlaces(it))
            }
            .onErrorReturn { RxResult.error(it) }
            .startWith(RxResult.loading())
    }

    fun getPlaceInfo(venue_id: String): Observable<RxResult<FullPlaceInfo>> {
        return searchApi.getPlaceInfo(venue_id, SearchApi.CLIENT_ID, SearchApi.CLIENT_SECRET, getCurrentDateString())
            .subscribeOn(rxSchedulers.network())
            .observeOn(rxSchedulers.main())
            .map {
                RxResult.data(it.response.venue)
            }
            .onErrorReturn { RxResult.error(it) }
            .startWith(RxResult.loading())
    }

    private fun getCurrentDateString(): String {
        val date = Date()
        return SimpleDateFormat(DATE_FORMAT, Locale.UK).format(date)
    }

    private fun extractPlaces(response: SearchResponse): List<Place> {
        val places: MutableList<Place> = mutableListOf()
        val placesConverter = PlaceConverter()
        val list = mutableListOf<Venue>()
        list.addAll(response.response.venues)
        val sorted = list.sortedWith(compareBy {
            it.location.distance.toInt()
        })
        for (venue in sorted) {
            val element = placesConverter.convert(venue)
            if (favoritePlacesManager.isPlaceLiked(element.id)) {
                element.likeState = LikeState.LIKED
            }
            places.add(element)
        }
        return places
    }

    companion object {
        private const val DATE_FORMAT: String = "YYYYddMM"
    }

}