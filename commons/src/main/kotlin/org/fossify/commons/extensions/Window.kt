package org.fossify.commons.extensions

import android.view.View
import android.view.Window
import android.view.WindowManager
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import org.fossify.commons.helpers.DARK_GREY

fun Window.insetsController(view: View? = null): WindowInsetsControllerCompat {
    return WindowInsetsControllerCompat(this, view ?: decorView)
}

fun Window.setSystemBarsAppearance(backgroundColor: Int) {
    val isLightBackground = backgroundColor.getContrastColor() == DARK_GREY
    insetsController().apply {
        isAppearanceLightStatusBars = isLightBackground
        isAppearanceLightNavigationBars = isLightBackground
    }
}

fun Window.setStatusBarAppearance(backgroundColor: Int) {
    val isLightBackground = backgroundColor.getContrastColor() == DARK_GREY
    insetsController().isAppearanceLightStatusBars = isLightBackground
}

fun Window.setNavigationBarAppearance(backgroundColor: Int) {
    val isLightBackground = backgroundColor.getContrastColor() == DARK_GREY
    insetsController().isAppearanceLightNavigationBars = isLightBackground
}

fun Window.showBars() = insetsController().apply {
    systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_DEFAULT
    show(WindowInsetsCompat.Type.systemBars())
}

fun Window.hideBars(transient: Boolean = true) = insetsController().apply {
    systemBarsBehavior = if (transient) {
        WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
    } else {
        WindowInsetsControllerCompat.BEHAVIOR_DEFAULT
    }

    hide(WindowInsetsCompat.Type.systemBars())
}

fun Window.updateBrightness(maxBrightness: Boolean, originalBrightness: Float?): Float? {
    val layoutParams = attributes
    return if (maxBrightness) {
        val original = originalBrightness ?: layoutParams.screenBrightness
        layoutParams.screenBrightness = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_FULL
        attributes = layoutParams
        original
    } else {
        if (originalBrightness == null) return null
        layoutParams.screenBrightness = originalBrightness
        attributes = layoutParams
        null
    }
}
