package com.example.myapplication.model.utils

import okhttp3.internal.platform.Platform

import okhttp3.internal.platform.Platform.INFO

interface Logger {
    fun log(message: String)

    companion object {

        /**
         * A [Logger] defaults output appropriate for the current platform.
         */
        val DEFAULT: Logger = object : Logger {
            override fun log(message: String) {
                Platform.get().log(INFO, message, null)
            }
        }
    }
}
