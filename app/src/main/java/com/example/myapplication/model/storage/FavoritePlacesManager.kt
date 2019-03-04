package com.example.myapplication.model.storage

import android.content.Context
import android.content.SharedPreferences
import javax.inject.Inject

class FavoritePlacesManager @Inject constructor(context: Context) {

    private var sharedPref: SharedPreferences = context.getSharedPreferences("favorites", Context.MODE_PRIVATE)

    fun likePlace(id: String) {
        with(sharedPref.edit()) {
            putBoolean(id, true)
            apply()
        }
    }

    fun dislikePlace(id: String) {
        with(sharedPref.edit()) {
            putBoolean(id, false)
            apply()
        }
    }

    fun isPlaceLiked(id: String): Boolean {
        return sharedPref.getBoolean(id, false)
    }
}