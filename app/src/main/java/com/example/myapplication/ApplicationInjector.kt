package com.example.myapplication

import android.app.Activity

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import com.google.gson.JsonParseException
import dagger.android.AndroidInjection
import dagger.android.support.AndroidSupportInjection
import dagger.android.support.HasSupportFragmentInjector
import io.reactivex.exceptions.UndeliverableException
import io.reactivex.plugins.RxJavaPlugins
import timber.log.Timber

object ApplicationInjector {

    fun initialize(application: CommonApplication) {
        DaggerCoreComponent
            .builder()
            .application(application)
            .build()
            .inject(application)
        application.registerActivityLifecycleCallbacks(object : SimpleActivityLifecycleCallback() {
            override fun onActivityCreated(activity: Activity?, savedInstanceState: Bundle?) {
                processActivity(activity)
            }
        })

        Timber.plant(Timber.DebugTree())
        RxJavaPlugins.setErrorHandler { throwable ->
            var realException = throwable
            if (throwable is UndeliverableException) {
                Timber.w(throwable.cause, "RxJavaPlugins.Error")
                realException = throwable.cause
            }

            if (realException is JsonParseException) {
                Timber.w(throwable, "RxJavaPlugins.Error JsonParseException!")
            }

            Thread.currentThread().uncaughtExceptionHandler.uncaughtException(Thread.currentThread(), throwable)
        }
    }

    private fun processActivity(activity: Activity?) {
        if (activity is HasSupportFragmentInjector) {
            AndroidInjection.inject(activity)
        }

        if (activity is FragmentActivity) {
            activity.supportFragmentManager
                .registerFragmentLifecycleCallbacks(object : FragmentManager.FragmentLifecycleCallbacks() {

                    override fun onFragmentPreAttached(fm: FragmentManager, f: Fragment, context: Context) {
                        if (f is Injectable) {
                            AndroidSupportInjection.inject(f)
                        }
                    }
                }, true)
        }
    }

}