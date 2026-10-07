package org.fossify.commons.helpers

import android.content.Context
import android.content.SharedPreferences
import org.fossify.commons.extensions.getSharedPrefs
import java.text.DecimalFormat
import kotlin.math.log10
import kotlin.math.pow

private const val BINARY_UNIT_BASE = 1024.0

internal object FileSizeFormatter {
    private var preferences: SharedPreferences? = null
    private val decimalUnits = arrayOf("B", "kB", "MB", "GB", "TB", "PB", "EB")
    private val binaryUnits = arrayOf("B", "KiB", "MiB", "GiB", "TiB", "PiB", "EiB")

    fun initialize(context: Context) {
        preferences = context.getSharedPrefs()
    }

    fun format(bytes: Long): String {
        if (bytes <= 0) return "0 B"

        val useBinaryUnits = preferences?.getBoolean(USE_BINARY_STORAGE_UNITS, false) == true
        val divisor = if (useBinaryUnits) BINARY_UNIT_BASE else 1000.0
        val units = if (useBinaryUnits) binaryUnits else decimalUnits
        val digitGroups = (log10(bytes.toDouble()) / log10(divisor)).toInt()
        val value = bytes / divisor.pow(digitGroups.toDouble())
        return "${DecimalFormat("#,##0.#").format(value)} ${units[digitGroups]}"
    }
}
