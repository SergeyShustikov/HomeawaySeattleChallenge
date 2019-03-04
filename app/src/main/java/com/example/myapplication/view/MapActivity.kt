package com.example.myapplication.view

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.fragment.app.FragmentActivity
import com.example.myapplication.R
import com.example.myapplication.model.data.places.search.Place
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.android.synthetic.main.activity_maps.*


class MapActivity : FragmentActivity(), OnMapReadyCallback {

    companion object {
        var OBJECTS_KEY: String = ".objects_key"
    }

    private lateinit var mMap: GoogleMap
    private lateinit var placesList: ArrayList<Place>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        loadingIndicator.visibility = View.VISIBLE
        placesList = intent.getParcelableArrayListExtra(OBJECTS_KEY)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        mMap.setOnMapLoadedCallback {
            fillMap()
        }
    }

    private fun fillMap() {

        val bounds = LatLngBounds.Builder()
        val center = LatLng(47.6062, -122.3321)

        val centerMarket = mMap.addMarker(
            MarkerOptions().position(center).title("Seattle center").icon(
                BitmapDescriptorFactory.defaultMarker(
                    BitmapDescriptorFactory.HUE_AZURE
                )
            )
        )
        centerMarket.tag = 1
        bounds.include(centerMarket.position)

        for (i in 0 until placesList.size) {
            val currentPlace = placesList[i]

            val coordinates = currentPlace.coordinates

            val mMarker = mMap.addMarker(
                MarkerOptions().position(coordinates).title(currentPlace.name).icon(
                    BitmapDescriptorFactory.defaultMarker(
                        BitmapDescriptorFactory.HUE_YELLOW
                    )
                )
            )
            mMarker.tag = i

            bounds.include(mMarker.position)
        }

        mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds.build(), 40))
        mMap.setOnInfoWindowClickListener { marker ->
            if (marker.tag != null) {

                val intent = Intent(applicationContext, DetailActivity::class.java)
                intent.putExtra(DetailActivity.PLACE_ID, placesList[marker.tag as Int].id)
                intent.putExtra(DetailActivity.DISTANCE_FROM_CENTER, placesList[marker.tag as Int].distanceFromCenter)

                startActivity(intent)

            }
        }
        loadingIndicator.visibility = View.GONE
    }
}