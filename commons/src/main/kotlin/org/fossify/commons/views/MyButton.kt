package org.fossify.commons.views

import android.content.Context
import android.util.AttributeSet
import android.widget.Button
import org.fossify.commons.extensions.applyFontToTextView

open class MyButton : Button {
    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle) {
        init()
    }

    private fun init() {
        if (!isInEditMode) context.applyFontToTextView(this)
    }

    fun setColors(textColor: Int, accentColor: Int, backgroundColor: Int) {
        setTextColor(textColor)
    }
}
