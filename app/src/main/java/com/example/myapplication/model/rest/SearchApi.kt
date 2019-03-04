package com.example.myapplication.model.rest

import com.example.myapplication.model.data.places.details.DetailsResponse
import com.example.myapplication.model.data.places.search.response.SearchResponse
import io.reactivex.Observable
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query


interface SearchApi {

    companion object {
        var CLIENT_ID = "GWHJNX0USQ1XVZ51XVGYW1ZJXIJSJRPZD1AJZVDUDOZ0M14U"
        var CLIENT_SECRET = "2NIKTL5KNQ4KCZFJPBKUHUNNPFRNIMBMAUCOOQQDESPTQKAO"
    }

    @GET("venues/search?limit=20&near=Seattle,+WA&v=20180401&ll=47.6062, -122.3321")
    fun search(
        @Query("client_id") clientId: String,
        @Query("client_secret") clientSecret: String,
        @Query("query") query: String
    ): Observable<SearchResponse>

    @GET("venues/{venue_id}")
    fun getPlaceInfo(
        @Path("venue_id") venue_id: String,
        @Query("client_id") clientId: String,
        @Query("client_secret") clientSecret: String,
        @Query("v") date: String
    ): Observable<DetailsResponse>
}