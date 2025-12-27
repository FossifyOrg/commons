package org.fossify.commons.models

import org.fossify.commons.helpers.FONT_TYPE_SYSTEM_DEFAULT
import org.fossify.commons.helpers.MyContentProvider

data class GlobalConfig(
    val themeType: Int,
    val textColor: Int,
    val backgroundColor: Int,
    val primaryColor: Int,
    val accentColor: Int,
    val appIconColor: Int,
    val showCheckmarksOnSwitches: Boolean,
    val lastUpdatedTS: Int = 0,
    val fontType: Int = FONT_TYPE_SYSTEM_DEFAULT,
    val fontName: String = "",
    val fontData: ByteArray? = null,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as GlobalConfig

        if (themeType != other.themeType) return false
        if (textColor != other.textColor) return false
        if (backgroundColor != other.backgroundColor) return false
        if (primaryColor != other.primaryColor) return false
        if (accentColor != other.accentColor) return false
        if (appIconColor != other.appIconColor) return false
        if (showCheckmarksOnSwitches != other.showCheckmarksOnSwitches) return false
        if (lastUpdatedTS != other.lastUpdatedTS) return false
        if (fontType != other.fontType) return false
        if (fontName != other.fontName) return false
        if (!fontData.contentEquals(other.fontData)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = themeType
        result = 31 * result + textColor
        result = 31 * result + backgroundColor
        result = 31 * result + primaryColor
        result = 31 * result + accentColor
        result = 31 * result + appIconColor
        result = 31 * result + showCheckmarksOnSwitches.hashCode()
        result = 31 * result + lastUpdatedTS
        result = 31 * result + fontType
        result = 31 * result + fontName.hashCode()
        result = 31 * result + (fontData?.contentHashCode() ?: 0)
        return result
    }
}

fun GlobalConfig?.isGlobalThemingEnabled(): Boolean {
    return this != null && themeType != MyContentProvider.GLOBAL_THEME_DISABLED
}
