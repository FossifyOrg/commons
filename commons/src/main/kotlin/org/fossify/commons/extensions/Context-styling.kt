package org.fossify.commons.extensions

import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import android.content.pm.PackageManager.COMPONENT_ENABLED_STATE_DISABLED
import android.content.pm.PackageManager.COMPONENT_ENABLED_STATE_ENABLED
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.Typeface
import android.view.ViewGroup
import androidx.loader.content.CursorLoader
import com.google.android.material.color.MaterialColors
import org.fossify.commons.R
import org.fossify.commons.helpers.DARK_GREY
import org.fossify.commons.helpers.FONT_TYPE_CUSTOM
import org.fossify.commons.helpers.FONT_TYPE_SYSTEM_DEFAULT
import org.fossify.commons.helpers.FontHelper
import org.fossify.commons.helpers.MyContentProvider
import org.fossify.commons.helpers.MyContentProvider.GLOBAL_THEME_SYSTEM
import org.fossify.commons.helpers.appIconColorStrings
import org.fossify.commons.helpers.ensureBackgroundThread
import org.fossify.commons.helpers.isSPlus
import org.fossify.commons.models.GlobalConfig
import org.fossify.commons.models.isGlobalThemingEnabled
import org.fossify.commons.views.MyAppCompatCheckbox
import org.fossify.commons.views.MyAppCompatSpinner
import org.fossify.commons.views.MyAutoCompleteTextView
import org.fossify.commons.views.MyButton
import org.fossify.commons.views.MyCompatRadioButton
import org.fossify.commons.views.MyEditText
import org.fossify.commons.views.MyFloatingActionButton
import org.fossify.commons.views.MyMaterialSwitch
import org.fossify.commons.views.MySeekBar
import org.fossify.commons.views.MyTextInputLayout
import org.fossify.commons.views.MyTextView
import java.io.File

fun Context.isDynamicTheme() = isSPlus() && baseConfig.isSystemThemeEnabled

fun Context.isBlackAndWhiteTheme(): Boolean {
    return baseConfig.textColor == Color.WHITE
            && baseConfig.primaryColor == Color.BLACK
            && baseConfig.backgroundColor == Color.BLACK
}

fun Context.isWhiteTheme(): Boolean {
    return baseConfig.textColor == DARK_GREY
            && baseConfig.primaryColor == Color.WHITE
            && baseConfig.backgroundColor == Color.WHITE
}

fun Context.isSystemInDarkMode() = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_YES != 0

fun Context.isAutoTheme() = !isSPlus() && baseConfig.isSystemThemeEnabled

fun Context.getProperTextColor() = when {
    isDynamicTheme() -> resources.getColor(R.color.you_neutral_text_color, theme)
    else -> baseConfig.textColor
}

fun Context.getProperBackgroundColor() = when {
    isDynamicTheme() -> resources.getColor(R.color.you_background_color, theme)
    else -> baseConfig.backgroundColor
}

fun Context.getProperPrimaryColor() = when {
    isDynamicTheme() -> resources.getColor(R.color.you_primary_color, theme)
    isWhiteTheme() || isBlackAndWhiteTheme() -> baseConfig.accentColor
    else -> baseConfig.primaryColor
}

fun Context.getProperStatusBarColor() = when {
    isDynamicTheme() -> resources.getColor(R.color.you_status_bar_color, theme)
    else -> getProperBackgroundColor()
}

// get the color of the status bar with material activity, if the layout is scrolled down a bit
fun Context.getColoredMaterialStatusBarColor(): Int {
    return when {
        isDynamicTheme() -> resources.getColor(R.color.you_status_bar_color, theme)
        else -> getProperPrimaryColor()
    }
}

fun Context.updateTextColors(viewGroup: ViewGroup) {
    val textColor = when {
        isDynamicTheme() -> getProperTextColor()
        else -> baseConfig.textColor
    }

    val backgroundColor = baseConfig.backgroundColor
    val accentColor = when {
        isWhiteTheme() || isBlackAndWhiteTheme() -> baseConfig.accentColor
        else -> getProperPrimaryColor()
    }

    val cnt = viewGroup.childCount
    (0 until cnt).map { viewGroup.getChildAt(it) }.forEach {
        when (it) {
            is MyTextView -> it.setColors(textColor, accentColor, backgroundColor)
            is MyAppCompatSpinner -> it.setColors(textColor, accentColor, backgroundColor)
            is MyCompatRadioButton -> it.setColors(textColor, accentColor, backgroundColor)
            is MyAppCompatCheckbox -> it.setColors(textColor, accentColor, backgroundColor)
            is MyMaterialSwitch -> it.setColors(textColor, accentColor, backgroundColor)
            is MyEditText -> it.setColors(textColor, accentColor, backgroundColor)
            is MyAutoCompleteTextView -> it.setColors(textColor, accentColor, backgroundColor)
            is MyFloatingActionButton -> it.setColors(textColor, accentColor, backgroundColor)
            is MySeekBar -> it.setColors(textColor, accentColor, backgroundColor)
            is MyButton -> it.setColors(textColor, accentColor, backgroundColor)
            is MyTextInputLayout -> it.setColors(textColor, accentColor, backgroundColor)
            is ViewGroup -> updateTextColors(it)
        }
    }
}

fun Context.getTimePickerDialogTheme() = when {
    isDynamicTheme() -> if (isSystemInDarkMode()) {
        R.style.MyTimePickerMaterialTheme_Dark
    } else {
        R.style.MyDateTimePickerMaterialTheme
    }

    baseConfig.backgroundColor.getContrastColor() == Color.WHITE -> R.style.MyDialogTheme_Dark
    else -> R.style.MyDialogTheme
}

fun Context.getDatePickerDialogTheme() = when {
    isDynamicTheme() -> R.style.MyDateTimePickerMaterialTheme
    baseConfig.backgroundColor.getContrastColor() == Color.WHITE -> R.style.MyDialogTheme_Dark
    else -> R.style.MyDialogTheme
}

fun Context.getPopupMenuTheme(): Int {
    return if (isDynamicTheme()) {
        if (isSystemInDarkMode()) {
            R.style.AppTheme_YouPopupMenuStyle
        } else {
            R.style.AppTheme_YouPopupMenuStyle_Light
        }
    } else if (isWhiteTheme()) {
        R.style.AppTheme_PopupMenuLightStyle
    } else {
        R.style.AppTheme_PopupMenuDarkStyle
    }
}

fun Context.syncGlobalConfig(callback: (() -> Unit)? = null) {
    if (canAccessGlobalConfig()) {
        withGlobalConfig {
            if (it != null) {
                baseConfig.apply {
                    showCheckmarksOnSwitches = it.showCheckmarksOnSwitches
                    if (it.isGlobalThemingEnabled()) {
                        isGlobalThemeEnabled = true
                        isSystemThemeEnabled = it.themeType == GLOBAL_THEME_SYSTEM
                        textColor = it.textColor
                        backgroundColor = it.backgroundColor
                        primaryColor = it.primaryColor
                        accentColor = it.accentColor

                        if (it.fontType >= 0 && (fontType != it.fontType || fontName != it.fontName)) {
                            if (it.fontType != FONT_TYPE_CUSTOM || ensureFontPresentLocally(it.fontName)) {
                                fontType = it.fontType
                                fontName = it.fontName
                            }
                            FontHelper.clearCache()
                        }

                        if (baseConfig.appIconColor != it.appIconColor) {
                            baseConfig.appIconColor = it.appIconColor
                            checkAppIconColor()
                        }
                    }
                }
            }

            validateFontSettings()
            callback?.invoke()
        }
    } else {
        baseConfig.isGlobalThemeEnabled = false
        baseConfig.showCheckmarksOnSwitches = false
        validateFontSettings()
        callback?.invoke()
    }
}

private fun Context.validateFontSettings() {
    if (baseConfig.fontType == FONT_TYPE_CUSTOM) {
        val typeface = FontHelper.getTypeface(this)
        if (typeface == Typeface.DEFAULT) {
            baseConfig.fontType = FONT_TYPE_SYSTEM_DEFAULT
            baseConfig.fontName = ""
            FontHelper.clearCache()
        }
    }
}

fun Context.withGlobalConfig(callback: (globalConfig: GlobalConfig?) -> Unit) {
    if (!isThankYouInstalled()) {
        callback(null)
    } else {
        val cursorLoader = getMyContentProviderCursorLoader()
        ensureBackgroundThread {
            callback(getGlobalConfig(cursorLoader))
        }
    }
}

fun Context.getGlobalConfig(cursorLoader: CursorLoader): GlobalConfig? {
    val cursor = cursorLoader.loadInBackground()
    cursor?.use {
        if (cursor.moveToFirst()) {
            try {
                return GlobalConfig(
                    themeType = cursor.getIntValue(MyContentProvider.COL_THEME_TYPE),
                    textColor = cursor.getIntValue(MyContentProvider.COL_TEXT_COLOR),
                    backgroundColor = cursor.getIntValue(MyContentProvider.COL_BACKGROUND_COLOR),
                    primaryColor = cursor.getIntValue(MyContentProvider.COL_PRIMARY_COLOR),
                    accentColor = cursor.getIntValue(MyContentProvider.COL_ACCENT_COLOR),
                    appIconColor = cursor.getIntValue(MyContentProvider.COL_APP_ICON_COLOR),
                    showCheckmarksOnSwitches = cursor.getIntValue(MyContentProvider.COL_SHOW_CHECKMARKS_ON_SWITCHES) != 0,
                    lastUpdatedTS = cursor.getIntValue(MyContentProvider.COL_LAST_UPDATED_TS),
                    fontType = cursor.getIntValueOr(MyContentProvider.COL_FONT_TYPE, -1),
                    fontName = cursor.getStringValueOr(MyContentProvider.COL_FONT_NAME, "")
                )
            } catch (_: Exception) {
            }
        }
    }
    return null
}

fun Context.ensureFontPresentLocally(fontName: String): Boolean {
    if (fontName.isEmpty()) return false
    val localFile = File(FontHelper.getFontsDir(this), fontName)
    if (localFile.exists()) return true

    val fontUri = MyContentProvider.FONTS_URI.buildUpon()
        .appendPath(fontName)
        .build()

    return try {
        contentResolver.openInputStream(fontUri)?.use { input ->
            localFile.outputStream().use { output ->
                input.copyTo(output)
            }
        } != null
    } catch (_: Exception) {
        false
    }
}

fun Context.checkAppIconColor() {
    val appId = baseConfig.appId
    if (appId.isNotEmpty() && baseConfig.lastIconColor != baseConfig.appIconColor) {
        getAppIconColors().forEachIndexed { index, color ->
            toggleAppIconColor(appId, index, color, false)
        }

        getAppIconColors().forEachIndexed { index, color ->
            if (baseConfig.appIconColor == color) {
                toggleAppIconColor(appId, index, color, true)
            }
        }
    }
}

fun Context.toggleAppIconColor(appId: String, colorIndex: Int, color: Int, enable: Boolean) {
    val className = "${appId.removeSuffix(".debug")}.activities.SplashActivity${appIconColorStrings[colorIndex]}"
    val state = if (enable) COMPONENT_ENABLED_STATE_ENABLED else COMPONENT_ENABLED_STATE_DISABLED
    try {
        packageManager.setComponentEnabledSetting(ComponentName(appId, className), state, PackageManager.DONT_KILL_APP)
        if (enable) {
            baseConfig.lastIconColor = color
        }
    } catch (e: Exception) {
        showErrorToast(e)
    }
}

fun Context.getAppIconColors() = resources.getIntArray(R.array.md_app_icon_colors).toCollection(ArrayList())

@SuppressLint("NewApi")
fun Context.getBottomNavigationBackgroundColor(): Int {
    val baseColor = baseConfig.backgroundColor
    val bottomColor = when {
        isDynamicTheme() -> resources.getColor(R.color.you_status_bar_color, theme)
        baseColor == Color.WHITE -> resources.getColor(R.color.bottom_tabs_light_background)
        else -> baseConfig.backgroundColor.lightenColor(4)
    }
    return bottomColor
}

fun Context.getDialogBackgroundColor(): Int {
    return when {
        isDynamicTheme() -> MaterialColors.getColor(
            this, com.google.android.material.R.attr.colorSurfaceContainerHigh, Color.TRANSPARENT
        )

        else -> baseConfig.backgroundColor
    }
}