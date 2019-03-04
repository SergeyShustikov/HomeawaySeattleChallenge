package com.example.myapplication.model.utils
import timber.log.Timber

class TimberLogAdapter : Logger {

    override fun log(message: String) {
        val threadName = Thread.currentThread().name
        Timber.tag("NETWORK[$threadName]").i(message)
    }
}