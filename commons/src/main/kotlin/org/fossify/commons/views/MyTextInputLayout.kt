package org.fossify.commons.views

import android.content.Context
import android.content.res.ColorStateList
import android.util.AttributeSet
import com.google.android.material.textfield.TextInputLayout
import org.fossify.commons.extensions.adjustAlpha
import org.fossify.commons.extensions.value
import org.fossify.commons.helpers.FontHelper
import org.fossify.commons.helpers.HIGHER_ALPHA
import org.fossify.commons.helpers.MEDIUM_ALPHA

open class MyTextInputLayout : TextInputLayout {
    constructor(context: Context) : super(context) {
        applyCustomFont()
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        applyCustomFont()
    }

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle) {
        applyCustomFont()
    }

    private fun applyCustomFont() {
        if (isInEditMode) return
        val customTypeface = FontHelper.getTypeface(context)
        typeface = customTypeface
    }

    // we need to use reflection to make some colors work well
    fun setColors(textColor: Int, accentColor: Int, backgroundColor: Int) {
        try {
            editText!!.setTextColor(textColor)
            editText!!.backgroundTintList = ColorStateList.valueOf(accentColor)

            val hintColor = if (editText!!.value.isEmpty()) textColor.adjustAlpha(HIGHER_ALPHA) else textColor
            val defaultTextColor = TextInputLayout::class.java.getDeclaredField("defaultHintTextColor")
            defaultTextColor.isAccessible = true
            defaultTextColor.set(this, ColorStateList(arrayOf(intArrayOf(0)), intArrayOf(hintColor)))

            val focusedTextColor = TextInputLayout::class.java.getDeclaredField("focusedTextColor")
            focusedTextColor.isAccessible = true
            focusedTextColor.set(this, ColorStateList(arrayOf(intArrayOf(0)), intArrayOf(accentColor)))

            val defaultHintTextColor = textColor.adjustAlpha(MEDIUM_ALPHA)
            val boxColorState = ColorStateList(
                arrayOf(
                    intArrayOf(android.R.attr.state_active),
                    intArrayOf(android.R.attr.state_focused)
                ),
                intArrayOf(
                    defaultHintTextColor,
                    accentColor
                )
            )

            setEndIconTintList(ColorStateList(arrayOf(intArrayOf(0)), intArrayOf(hintColor)))
            setBoxStrokeColorStateList(boxColorState)
            defaultTextColor.set(this, ColorStateList(arrayOf(intArrayOf(0)), intArrayOf(defaultHintTextColor)))
            setHelperTextColor(ColorStateList.valueOf(textColor))
        } catch (e: Exception) {
        }
    }
}
