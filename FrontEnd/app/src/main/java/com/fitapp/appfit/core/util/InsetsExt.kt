package com.fitapp.appfit.core.util

import android.view.View
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding

class SystemBarInsetConfig(
    var applyTop: Boolean = true,
    var applyBottom: Boolean = false,
)

fun View.applySystemBarInsets(
    applyTop: Boolean = true,
    applyBottom: Boolean = false,
): SystemBarInsetConfig {
    val config = SystemBarInsetConfig(applyTop, applyBottom)
    ViewCompat.setOnApplyWindowInsetsListener(this) { view, insets ->
        val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
        view.updatePadding(
            top = if (config.applyTop) systemBars.top else 0,
            bottom = if (config.applyBottom) systemBars.bottom else 0,
        )
        insets
    }
    ViewCompat.requestApplyInsets(this)
    return config
}

fun View.requestSystemBarInsets() {
    ViewCompat.requestApplyInsets(this)
}
