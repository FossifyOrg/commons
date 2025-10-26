package org.fossify.commons.views

import android.content.Context
import android.content.res.ColorStateList
import android.util.AttributeSet
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.floatingactionbutton.FloatingActionButton
import org.fossify.commons.extensions.applyColorFilter
import org.fossify.commons.extensions.getContrastColor

open class MyFloatingActionButton : FloatingActionButton {
    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(
        context,
        attrs,
        defStyle
    )

    init {
        ViewCompat.setOnApplyWindowInsetsListener(this) { _, insets ->
            val system = insets.getInsetsIgnoringVisibility(WindowInsetsCompat.Type.systemBars())
            translationY = -system.bottom.toFloat()
            insets
        }
    }

    fun setColors(textColor: Int, accentColor: Int, backgroundColor: Int) {
        backgroundTintList = ColorStateList.valueOf(accentColor)
        applyColorFilter(accentColor.getContrastColor())
    }
}
