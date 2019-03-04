package com.example.myapplication.view.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.model.data.places.search.Place
import com.example.myapplication.model.storage.FavoritePlacesManager
import com.squareup.picasso.Picasso
import com.squareup.picasso.Transformation
import kotlinx.android.synthetic.main.item_search.view.*

class SearchViewAdapter(var places: List<Place>, private var favoritePlacesManager: FavoritePlacesManager) :
    RecyclerView.Adapter<SearchViewAdapter.ViewHolder>() {

    lateinit var onClickListener: View.OnClickListener

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_search, parent, false) as LinearLayout
        itemView.setOnClickListener(onClickListener)
        return ViewHolder(itemView, favoritePlacesManager)
    }

    override fun getItemCount(): Int {
        return places.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.fillView(places[position])
    }

    class ViewHolder(itemView: View, private var favoritePlacesManager: FavoritePlacesManager) :
        RecyclerView.ViewHolder(itemView) {

        private var name: TextView = itemView.name
        private var category: TextView = itemView.category
        private var icon: ImageView = itemView.icon
        private var distance: TextView = itemView.distance
        private var isFavorite: ImageView = itemView.favorite


        fun fillView(place: Place) {
            name.text = place.name
            category.text = place.category
            if (place.iconUrl.isNotEmpty()) {
                Picasso.get().load(place.iconUrl).into(icon)
                icon.setColorFilter(
                    Color.parseColor("#eeeb02"), android.graphics.PorterDuff.Mode.SRC_IN
                )
            }
            distance.text = place.distanceFromCenter
            isFavorite.visibility = View.VISIBLE
            if (favoritePlacesManager.isPlaceLiked(place.id)) {
                isFavorite.setImageResource(R.drawable.ic_liked)
            } else {
                isFavorite.setImageResource(R.drawable.ic_not_liked)
            }
            isFavorite.setOnClickListener {
                if (favoritePlacesManager.isPlaceLiked(place.id)) {
                    favoritePlacesManager.dislikePlace(place.id)
                    isFavorite.setImageResource(R.drawable.ic_not_liked)
                } else {
                    favoritePlacesManager.likePlace(place.id)
                    isFavorite.setImageResource(R.drawable.ic_liked)
                }
            }
        }
    }
}