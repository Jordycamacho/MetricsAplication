package com.fitapp.appfit.feature.feedback.util

import android.content.Context
import android.os.Build
import com.fitapp.appfit.BuildConfig
import java.util.Locale

object FeedbackTechnicalContext {

    fun collect(context: Context): Map<String, String> {
        return mapOf(
            "appVersion" to BuildConfig.VERSION_NAME,
            "versionCode" to BuildConfig.VERSION_CODE.toString(),
            "buildType" to BuildConfig.BUILD_TYPE,
            "flavor" to BuildConfig.FLAVOR,
            "manufacturer" to Build.MANUFACTURER,
            "model" to Build.MODEL,
            "androidSdk" to Build.VERSION.SDK_INT.toString(),
            "androidRelease" to (Build.VERSION.RELEASE ?: ""),
            "locale" to Locale.getDefault().toLanguageTag(),
            "platform" to "ANDROID"
        )
    }
}
