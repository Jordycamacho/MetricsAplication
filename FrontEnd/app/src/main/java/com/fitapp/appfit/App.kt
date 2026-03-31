package com.fitapp.appfit

import android.app.Application
import com.fitapp.appfit.core.session.SessionManager
import timber.log.Timber

class App : Application() {

    override fun onCreate() {
        super.onCreate()
        SessionManager.initialize(this)
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }

        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            Timber.e(throwable, "UNCAUGHT_EXCEPTION en thread: ${thread.name}")
            throwable.printStackTrace()
        }
    }
}