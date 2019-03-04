package com.example.myapplication.model.utils

import timber.log.Timber

/**
 * A generic class that holds a value with its loading status.
 * @param <T>
</T> */
data class RxResult<out T>(val data: T?, val throwable: Throwable, val isLoading: Boolean, val progress: Double) {


    companion object {
        fun <T> data(data: T?): RxResult<T> {
            return RxResult(data, NoException(), false, 0.0)
        }

        fun <T> error(t: Throwable): RxResult<T> {
            Timber.e(t)
            return RxResult(null, t, false, 0.0)
        }

        fun <T> error(t: Exception): RxResult<T> {
            Timber.e(t)
            return RxResult(null, t, false, 0.0)
        }

        fun <T> loading(): RxResult<T> {
            return RxResult(null, NoException(), true, 0.0)
        }

        fun <T> progress(progress: Double): RxResult<T> {
            return RxResult(null, NoException(), false, progress)
        }
    }

    fun hasData(): Boolean {
        return data != null
    }

    fun isError(): Boolean {
        return throwable !is NoException
    }

    fun loading(): Boolean {
        return isLoading
    }

    fun progress(): Boolean {
        return progress != 0.0
    }

}