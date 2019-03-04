package com.example.myapplication.model.utils
import timber.log.Timber

class TimberErrorLogAdapter : Logger {

    override fun log(message: String) {
        val threadName = Thread.currentThread().name
        Timber.tag("NETWORK_ERROR[$threadName]").e(message)
    }
}