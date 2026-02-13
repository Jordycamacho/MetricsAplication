package com.fitapp.appfit

import android.app.Application
import com.fitapp.appfit.utils.SessionManager
import timber.log.Timber

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        SessionManager.initialize(this)
        if (android.util.Log.isLoggable("AppFit", android.util.Log.DEBUG)) {
            Timber.plant(Timber.DebugTree())
        }
    }
}