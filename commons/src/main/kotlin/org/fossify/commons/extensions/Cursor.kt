package org.fossify.commons.extensions

import android.database.Cursor

fun Cursor.getStringValue(key: String) = getString(getColumnIndexOrThrow(key))

fun Cursor.getStringValueOrNull(key: String): String? {
    return if (isNull(getColumnIndexOrThrow(key))) null else getString(getColumnIndexOrThrow(key))
}

fun Cursor.getIntValue(key: String) = getInt(getColumnIndexOrThrow(key))

fun Cursor.getIntValueOrNull(key: String): Int? {
    return if (isNull(getColumnIndexOrThrow(key))) null else getInt(getColumnIndexOrThrow(key))
}

fun Cursor.getLongValue(key: String) = getLong(getColumnIndexOrThrow(key))

fun Cursor.getLongValueOrNull(key: String): Long? {
    return if (isNull(getColumnIndexOrThrow(key))) null else getLong(getColumnIndexOrThrow(key))
}

fun Cursor.getBlobValue(key: String) = getBlob(getColumnIndexOrThrow(key))

fun Cursor.getStringValueOr(key: String, defaultValue: String): String {
    val index = getColumnIndex(key)
    return if (index != -1 && !isNull(index)) getString(index) else defaultValue
}

fun Cursor.getIntValueOr(key: String, defaultValue: Int): Int {
    val index = getColumnIndex(key)
    return if (index != -1 && !isNull(index)) getInt(index) else defaultValue
}

fun Cursor.getLongValueOr(key: String, defaultValue: Long): Long {
    val index = getColumnIndex(key)
    return if (index != -1 && !isNull(index)) getLong(index) else defaultValue
}
