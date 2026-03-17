package com.fitapp.appfit

import android.app.Application
import com.fitapp.appfit.utils.SessionManager
import timber.log.Timber

class App : Application() {

    override fun onCreate() {
        super.onCreate()
        SessionManager.initialize(this)
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
    }
}