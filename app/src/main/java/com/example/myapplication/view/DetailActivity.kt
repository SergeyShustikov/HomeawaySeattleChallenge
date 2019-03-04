package com.example.myapplication.view

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.Html
import android.text.method.LinkMovementMethod
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MarkerOptions
import com.squareup.picasso.Picasso
import android.net.Uri
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.example.myapplication.R
import com.example.myapplication.model.data.places.details.FullPlaceInfo
import com.example.myapplication.model.storage.FavoritePlacesManager
import com.example.myapplication.viewmodel.DetailViewModel
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.support.HasSupportFragmentInjector
import kotlinx.android.synthetic.main.content_scrolling.*
import javax.inject.Inject
import com.google.android.material.appbar.AppBarLayout
import com.rhexgomez.typer.roboto.TyperRoboto
import kotlinx.android.synthetic.main.activity_details.*


class DetailActivity : AppCompatActivity(), OnMapReadyCallback, HasSupportFragmentInjector {
    @Inject
    lateinit var dispatchingAndroidInjector: DispatchingAndroidInjector<Fragment>

    @Inject
    lateinit var favoritePlacesManager: FavoritePlacesManager
    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    override fun supportFragmentInjector(): AndroidInjector<Fragment> = dispatchingAndroidInjector

    companion object {
        var PLACE_ID: String = "place_id"
        var DISTANCE_FROM_CENTER: String = "distance_from_center"
    }

    private lateinit var mMap: GoogleMap
    private lateinit var place: FullPlaceInfo
    private lateinit var detailViewModel: DetailViewModel
    private var isMapReady: Boolean = false
    private var isContentReady: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_details)
        setSupportActionBar(toolbar)
        detailViewModel =
            ViewModelProviders
                .of(this, viewModelFactory)
                .get(DetailViewModel::class.java)
        detailViewModel.getPlaceDetails(intent.getStringExtra(PLACE_ID))
        detailViewModel.placeDetailLiveData.observe(this, Observer {
            place = it
            updateUI()
        })
        detailViewModel.loadingLiveData.observe(this, Observer {
            if (it) {
                loadingIndicator.visibility = View.VISIBLE
                content_scrolling.visibility = View.GONE
            } else {
                loadingIndicator.visibility = View.GONE
                content_scrolling.visibility = View.VISIBLE
            }
        })
        detailViewModel.errorLiveData.observe(this, Observer {
            Toast.makeText(this, "Something went wrong", Toast.LENGTH_SHORT).show()
        })

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        mapFragment.view!!.isClickable = false
    }


    private fun updateUI() {
        title = place.name
        place_name.text = place.name
        if (place.categories.isNotEmpty()) {
            place_category.text = place.categories[0].name
        }
        place_distance.text = intent.getStringExtra(DISTANCE_FROM_CENTER)
        place_address.text = place.location.getAddress()
        place_rating.max = 100
        if (place.rating > 0) {
            place_rating_value.text = place.rating.toString()
        } else {
            place_rating_value.text = ""
            rating.text = ""
        }
        place_rating.progress = (place.rating * 10).toInt()
        if (place.ratingColor == null) {
            place_rating.progressDrawable.setColorFilter(
                Color.parseColor("#FFFFFF"), android.graphics.PorterDuff.Mode.SRC_IN
            )
        } else {
            place_rating.progressDrawable.setColorFilter(
                Color.parseColor("#${place.ratingColor}"), android.graphics.PorterDuff.Mode.SRC_IN
            )
        }
        place_likes.text = place.likes.count.toString()
        if (place.price != null) {
            if (place.price!!.message != null) {
                place_price_type.text = place.price!!.message
            }
            if (place.price!!.currency != null) {
                place_currency.text = place.price!!.currency
            }
        }
        if (favoritePlacesManager.isPlaceLiked(place.id)) {
            place_like.setImageResource(R.drawable.ic_liked)
            place_likes.text = (place.likes.count + 1).toString()
        } else {
            place_like.setImageResource(R.drawable.ic_not_liked)
            place_likes.text = (place.likes.count).toString()
        }
        place_like.setOnClickListener {
            if (favoritePlacesManager.isPlaceLiked(place.id)) {
                favoritePlacesManager.dislikePlace(place.id)
                place_likes.text = (place.likes.count).toString()
                place_like.setImageResource(R.drawable.ic_not_liked)
            } else {
                favoritePlacesManager.likePlace(place.id)
                place_likes.text = (place.likes.count + 1).toString()
                place_like.setImageResource(R.drawable.ic_liked)
            }
        }
        website.setLinkTextColor(Color.BLUE)
        if (place.url != null) {
            website.text = Html.fromHtml("<a href=\"${place.url}\">VISIT WEBSITE</a> ")
            website.movementMethod = LinkMovementMethod.getInstance()
            website.setOnClickListener {
                val intent = Intent(Intent.ACTION_VIEW)
                intent.data = Uri.parse(place.url)
                startActivity(intent)
            }
        }

        shortUrl.setLinkTextColor(Color.RED)
        if (place.shortUrl != null) {
            shortUrl.text = Html.fromHtml("<a href=\"${place.shortUrl}\">VISIT FOURSQUARE PAGE</a> ")
            shortUrl.movementMethod = LinkMovementMethod.getInstance()
            shortUrl.setOnClickListener {
                val intent = Intent(Intent.ACTION_VIEW)
                intent.data = Uri.parse(place.shortUrl)
                startActivity(intent)
            }
        }
        if (place.categories.isNotEmpty()) {
            val iconUrl = place.categories[0].icon.prefix + "88" + place.categories[0].icon.suffix
            Picasso.get().load(iconUrl).into(place_icon)
            place_icon.setColorFilter(
                Color.parseColor("#eeeb02"), android.graphics.PorterDuff.Mode.SRC_IN
            )
        }
        /* disable title on collapsing toolbar*/
        app_bar.addOnOffsetChangedListener(object : AppBarLayout.OnOffsetChangedListener {
            var isShow = true
            var scrollRange = -1

            override fun onOffsetChanged(appBarLayout: AppBarLayout, verticalOffset: Int) {
                if (scrollRange == -1) {
                    scrollRange = appBarLayout.totalScrollRange
                }
                if (scrollRange + verticalOffset == 0) {
                    collapsing_layout.title = place.name
                    isShow = true
                } else if (isShow) {
                    collapsing_layout.title = " "
                    isShow = false
                }
            }
        })
        isContentReady = true
        if (isMapReady) {
            fillMap()
        }
    }


    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.setOnMapLoadedCallback {
            isMapReady = true
            if (isContentReady) {
                fillMap()
            }
        }
    }

    private fun fillMap() {

        val bounds = LatLngBounds.Builder()


        val coordinates = LatLng(place.location.lat, place.location.lng)

        val placeMarker = mMap.addMarker(
            MarkerOptions().position(coordinates).title(place.name).icon(
                BitmapDescriptorFactory.defaultMarker(
                    BitmapDescriptorFactory.HUE_YELLOW
                )
            )
        )
        placeMarker.tag = 0

        val center = LatLng(47.6062, -122.3321)

        val centerMarket = mMap.addMarker(
            MarkerOptions().position(center).title(place.name).icon(
                BitmapDescriptorFactory.defaultMarker(
                    BitmapDescriptorFactory.HUE_AZURE
                )
            )
        )
        placeMarker.tag = 1
        bounds.include(placeMarker.position)
        bounds.include(centerMarket.position)

        mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds.build(), 40))
        mMap.moveCamera(CameraUpdateFactory.zoomTo(mMap.cameraPosition.zoom - 0.5f))
    }
}