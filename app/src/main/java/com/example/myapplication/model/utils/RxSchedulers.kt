package com.example.myapplication.model.utils

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.os.Process
import io.reactivex.Scheduler
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.*


/**
 * Utils class for scheduling Rx tasks*/
@Suppress("DEPRECATION")
class RxSchedulers(application: Application) {

    private var networkWiFi: Scheduler
    private var networkSlow: Scheduler
    private var database: Scheduler
    private var connectivityManager: ConnectivityManager

    companion object {
        private const val NETWORK_CORE_POOL_SIZE_WIFI = 4
        private const val NETWORK_CORE_POOL_SIZE_SLOW = 2
        private const val NET_THREAD_KEEP_ALIVE_S = 10L
    }

    init {
        networkWiFi = Schedulers.from(createNetworkExecutor("-wifi", NETWORK_CORE_POOL_SIZE_WIFI))
        networkSlow = Schedulers.from(createNetworkExecutor("-slow", NETWORK_CORE_POOL_SIZE_SLOW))
        database = Schedulers.from(
            Executors.newSingleThreadExecutor(
                PriorityThreadFactory(
                    "db-thread-pool",
                    Process.THREAD_PRIORITY_BACKGROUND
                )
            )
        )
        connectivityManager = application.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    }

    fun main(): Scheduler {
        return AndroidSchedulers.mainThread()
    }

    @SuppressLint("MissingPermission")
    fun network(): Scheduler {
        val wifi = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI)
        return if (wifi.isConnectedOrConnecting) {
            networkWiFi
        } else networkSlow

    }

    fun database(): Scheduler {
        return database
    }

    fun computation(): Scheduler {
        return Schedulers.computation()
    }

    fun mergeSchedulers(): Scheduler {
        return Schedulers.from(Executors.newSingleThreadExecutor())
    }

    private fun createNetworkExecutor(prefix: String, threadPoolSize: Int): Executor {
        val networkExecutor = ThreadPoolExecutor(
            threadPoolSize,
            threadPoolSize,
            NET_THREAD_KEEP_ALIVE_S,
            TimeUnit.SECONDS,
            LinkedBlockingQueue<Runnable>(),
            PriorityThreadFactory(
                "net-thread-pool$prefix",
                Process.THREAD_PRIORITY_BACKGROUND
            )
        )
        networkExecutor.allowCoreThreadTimeOut(true)
        return networkExecutor
    }

    @SuppressLint("MissingPermission")
    fun isNetworkConnected(): Boolean {
        val wifi = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI)
        return wifi.isConnectedOrConnecting
    }

    private class PriorityThreadFactory internal constructor(private val name: String, private val priority: Int) :
        ThreadFactory {
        private var threadId = -1

        override fun newThread(r: Runnable): Thread {
            threadId++
            return object : Thread("$name-$threadId") {
                override fun run() {
                    Process.setThreadPriority(priority)
                    r.run()
                }
            }
        }
    }
}
