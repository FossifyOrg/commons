package org.fossify.commons.extensions

import android.view.Window
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import org.fossify.commons.helpers.DARK_GREY

fun Window.insetsController(): WindowInsetsControllerCompat {
    return WindowInsetsControllerCompat(this, decorView)
}

fun Window.setSystemBarsAppearance(backgroundColor: Int) {
    val isLightBackground = backgroundColor.getContrastColor() == DARK_GREY
    with(insetsController()) {
        isAppearanceLightStatusBars = isLightBackground
        isAppearanceLightNavigationBars = isLightBackground
    }
}

fun Window.showBars() {
    with(insetsController()) {
        show(WindowInsetsCompat.Type.systemBars())
        systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_DEFAULT
    }
}

fun Window.hideBarsTransient() {
    with(insetsController()) {
        hide(WindowInsetsCompat.Type.systemBars())
        systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
    }
}
