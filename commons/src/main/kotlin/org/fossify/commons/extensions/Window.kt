package org.fossify.commons.extensions

import android.view.Window
import androidx.core.view.WindowInsetsControllerCompat
import org.fossify.commons.helpers.DARK_GREY

fun Window.setSystemBarsAppearance(backgroundColor: Int) {
    val isLightBackground = backgroundColor.getContrastColor() == DARK_GREY
    with(WindowInsetsControllerCompat(this, decorView)) {
        isAppearanceLightStatusBars = isLightBackground
        isAppearanceLightNavigationBars = isLightBackground
    }
}
