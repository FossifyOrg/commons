package org.fossify.commons.extensions

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.media.ExifInterface
import android.os.Handler
import android.os.Looper
import androidx.core.graphics.ColorUtils
import androidx.core.os.postDelayed
import org.fossify.commons.helpers.DARK_GREY
import org.fossify.commons.helpers.WCAG_AA_NORMAL
import java.text.DecimalFormat
import java.util.Locale
import java.util.Random

fun Int.getContrastColor(): Int {
    val luminance = ColorUtils.calculateLuminance(this)
    return if (luminance > 0.5) DARK_GREY else Color.WHITE
}

fun Int.toHex() = String.format("#%06X", 0xFFFFFF and this).uppercase(Locale.getDefault())

fun Int.adjustAlpha(factor: Float): Int {
    val alpha = Math.round(Color.alpha(this) * factor)
    val red = Color.red(this)
    val green = Color.green(this)
    val blue = Color.blue(this)
    return Color.argb(alpha, red, green, blue)
}

fun Int.getFormattedDuration(forceShowHours: Boolean = false): String {
    val sb = StringBuilder(8)
    val hours = this / 3600
    val minutes = this % 3600 / 60
    val seconds = this % 60

    if (this >= 3600) {
        sb.append(String.format(Locale.getDefault(), "%02d", hours)).append(":")
    } else if (forceShowHours) {
        sb.append("0:")
    }

    sb.append(String.format(Locale.getDefault(), "%02d", minutes))
    sb.append(":").append(String.format(Locale.getDefault(), "%02d", seconds))
    return sb.toString()
}

fun Int.formatSize(): String {
    if (this <= 0) {
        return "0 B"
    }

    val units = arrayOf("B", "kB", "MB", "GB", "TB")
    val digitGroups = (Math.log10(toDouble()) / Math.log10(1024.0)).toInt()
    return "${DecimalFormat("#,##0.#").format(this / Math.pow(1024.0, digitGroups.toDouble()))} ${units[digitGroups]}"
}

@Deprecated(
    message = "Broken due to the Year 2038 problem. Use Long.formatDate() instead (but note that it uses milliseconds, not seconds).",
    replaceWith = ReplaceWith("(this * 1000L).formatDate(context, dateFormat, timeFormat)")
)
fun Int.formatDate(context: Context, dateFormat: String? = null, timeFormat: String? = null): String {
    return (this * 1000L).formatDate(context, dateFormat, timeFormat)
}

@Deprecated(
    message = "Broken due to the Year 2038 problem. Use Long.formatDateOrTime() instead (but note that it uses milliseconds, not seconds).",
    replaceWith = ReplaceWith("(this * 1000L).formatDateOrTime(context, hideTimeAtOtherDays, showYearEvenIfCurrent)")
)
fun Int.formatDateOrTime(context: Context, hideTimeAtOtherDays: Boolean, showYearEvenIfCurrent: Boolean): String {
    return (this * 1000L).formatDateOrTime(context, hideTimeAtOtherDays, showYearEvenIfCurrent)
}

@Deprecated(
    message = "Broken due to the Year 2038 problem. Use Long.isThisYear() instead (but note that it uses milliseconds, not seconds).",
    replaceWith = ReplaceWith("(this * 1000L).isThisYear()")
)
fun Int.isThisYear(): Boolean {
    return (this * 1000L).isThisYear()
}

fun Int.addBitIf(add: Boolean, bit: Int) =
    if (add) {
        addBit(bit)
    } else {
        removeBit(bit)
    }

// TODO: how to do "bits & ~bit" in kotlin?
fun Int.removeBit(bit: Int) = addBit(bit) - bit

fun Int.addBit(bit: Int) = this or bit

fun Int.flipBit(bit: Int) = if (this and bit == 0) addBit(bit) else removeBit(bit)

fun ClosedRange<Int>.random() = Random().nextInt(endInclusive - start) + start

// taken from https://stackoverflow.com/a/40964456/1967672
fun Int.darkenColor(factor: Int = 8): Int {
    if (this == Color.WHITE || this == Color.BLACK) {
        return this
    }

    val DARK_FACTOR = factor
    var hsv = FloatArray(3)
    Color.colorToHSV(this, hsv)
    val hsl = hsv2hsl(hsv)
    hsl[2] -= DARK_FACTOR / 100f
    if (hsl[2] < 0)
        hsl[2] = 0f
    hsv = hsl2hsv(hsl)
    return Color.HSVToColor(hsv)
}

fun Int.lightenColor(factor: Int = 8): Int {
    if (this == Color.WHITE || this == Color.BLACK) {
        return this
    }

    val LIGHT_FACTOR = factor
    var hsv = FloatArray(3)
    Color.colorToHSV(this, hsv)
    val hsl = hsv2hsl(hsv)
    hsl[2] += LIGHT_FACTOR / 100f
    if (hsl[2] < 0)
        hsl[2] = 0f
    hsv = hsl2hsv(hsl)
    return Color.HSVToColor(hsv)
}

private fun hsl2hsv(hsl: FloatArray): FloatArray {
    val hue = hsl[0]
    var sat = hsl[1]
    val light = hsl[2]
    sat *= if (light < .5) light else 1 - light
    return floatArrayOf(hue, 2f * sat / (light + sat), light + sat)
}

private fun hsv2hsl(hsv: FloatArray): FloatArray {
    val hue = hsv[0]
    val sat = hsv[1]
    val value = hsv[2]

    val newHue = (2f - sat) * value
    var newSat = sat * value / if (newHue < 1f) newHue else 2f - newHue
    if (newSat > 1f)
        newSat = 1f

    return floatArrayOf(hue, newSat, newHue / 2f)
}

fun Int.orientationFromDegrees() = when (this) {
    270 -> ExifInterface.ORIENTATION_ROTATE_270
    180 -> ExifInterface.ORIENTATION_ROTATE_180
    90 -> ExifInterface.ORIENTATION_ROTATE_90
    else -> ExifInterface.ORIENTATION_NORMAL
}.toString()

fun Int.degreesFromOrientation() = when (this) {
    ExifInterface.ORIENTATION_ROTATE_270 -> 270
    ExifInterface.ORIENTATION_ROTATE_180 -> 180
    ExifInterface.ORIENTATION_ROTATE_90 -> 90
    else -> 0
}

fun Int.ensureTwoDigits(): String {
    return if (toString().length == 1) {
        "0$this"
    } else {
        toString()
    }
}

fun Int.getColorStateList(): ColorStateList {
    val states = arrayOf(
        intArrayOf(android.R.attr.state_enabled),
        intArrayOf(-android.R.attr.state_enabled),
        intArrayOf(-android.R.attr.state_checked),
        intArrayOf(android.R.attr.state_pressed)
    )
    val colors = intArrayOf(this, this, this, this)
    return ColorStateList(states, colors)
}

fun Int.countdown(intervalMillis: Long, callback: (count: Int) -> Unit) {
    callback(this)
    if (this == 0) {
        return
    }

    Handler(Looper.getMainLooper()).postDelayed(intervalMillis) {
        (this - 1).countdown(intervalMillis, callback)
    }
}

fun Int.adjustForContrast(
    background: Int,
    minContrast: Double = WCAG_AA_NORMAL,
): Int {
    var color = this
    var alpha = 0f
    val target = if (ColorUtils.calculateLuminance(background) < 0.5) {
        Color.WHITE
    } else {
        Color.BLACK
    }

    while (ColorUtils.calculateContrast(color, background) < minContrast && alpha < 1f) {
        alpha += 0.05f
        color = ColorUtils.blendARGB(this, target, alpha)
    }

    return color
}
