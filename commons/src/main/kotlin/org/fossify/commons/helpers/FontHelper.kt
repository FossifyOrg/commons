package org.fossify.commons.helpers

import android.content.Context
import android.graphics.Typeface
import org.fossify.commons.extensions.baseConfig
import org.fossify.commons.extensions.ensureFontPresentLocally
import java.io.File

/**
 * Helper for loading and caching custom fonts.
 */
object FontHelper {
    private var cachedTypeface: Typeface? = null
    private var cachedFontType: Int = -1
    private var cachedFontFileName: String = ""

    fun getTypeface(context: Context): Typeface {
        val config = context.baseConfig
        val fontType = config.fontType
        val fontFileName = config.fontName

        if (fontType == cachedFontType && fontFileName == cachedFontFileName && cachedTypeface != null) {
            return cachedTypeface!!
        }

        cachedFontType = fontType
        cachedFontFileName = fontFileName
        cachedTypeface = when (fontType) {
            FONT_TYPE_MONOSPACE -> Typeface.MONOSPACE
            FONT_TYPE_CUSTOM -> loadCustomFont(context, fontFileName)
            else -> Typeface.DEFAULT
        }
        return cachedTypeface!!
    }

    private fun loadCustomFont(context: Context, fileName: String): Typeface {
        if (fileName.isEmpty()) return Typeface.DEFAULT
        val fontFile = File(getFontsDir(context), fileName)
        if (!fontFile.exists()) {
            context.ensureFontPresentLocally(fileName)
        }

        return try {
            if (fontFile.exists()) Typeface.createFromFile(fontFile) else Typeface.DEFAULT
        } catch (_: Exception) {
            Typeface.DEFAULT
        }
    }

    fun clearCache() {
        cachedTypeface = null
        cachedFontType = -1
        cachedFontFileName = ""
    }

    fun getFontsDir(context: Context): File {
        return File(context.filesDir, "fonts").apply {
            if (!exists()) mkdirs()
        }
    }

    fun saveFontData(context: Context, fontData: ByteArray, fileName: String): Boolean {
        return try {
            val fontFile = File(getFontsDir(context), fileName)
            fontFile.writeBytes(fontData)
            true
        } catch (_: Exception) {
            false
        }
    }

    fun getFontData(context: Context, fileName: String): ByteArray? {
        if (fileName.isEmpty()) {
            return null
        }

        return try {
            val fontFile = File(getFontsDir(context), fileName)
            if (fontFile.exists()) fontFile.readBytes() else null
        } catch (_: Exception) {
            null
        }
    }

    fun deleteFont(context: Context, fileName: String): Boolean {
        return try {
            val fontFile = File(getFontsDir(context), fileName)
            if (fontFile.exists()) fontFile.delete() else true
        } catch (_: Exception) {
            false
        }
    }
}
