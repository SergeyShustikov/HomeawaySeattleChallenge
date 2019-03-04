package com.example.myapplication

import android.app.Activity
import android.app.Application
import android.os.Bundle

abstract class SimpleActivityLifecycleCallback : Application.ActivityLifecycleCallbacks {
    override fun onActivityPaused(a: Activity?) {
    }

    override fun onActivityResumed(a: Activity?) {
    }

    override fun onActivityStarted(a: Activity?) {
    }

    override fun onActivityDestroyed(a: Activity?) {
    }

    override fun onActivitySaveInstanceState(a: Activity?, b: Bundle?) {
    }

    override fun onActivityStopped(a: Activity?) {
    }
}