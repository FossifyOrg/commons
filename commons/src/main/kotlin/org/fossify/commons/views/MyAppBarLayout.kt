package org.fossify.commons.views

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.appbar.MaterialToolbar

open class MyAppBarLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AppBarLayout(context, attrs, defStyleAttr) {
    private var cachedToolbar: MaterialToolbar? = null

    init {
        ViewCompat.setOnApplyWindowInsetsListener(this) { view, insets ->
            val systemBarInsets = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.updatePadding(top = systemBarInsets.top)
            insets
        }
    }

    open val toolbar: MaterialToolbar?
        get() {
            for (i in 0 until childCount) {
                val child = getChildAt(i)
                if (child is MaterialToolbar) {
                    return child.also { cachedToolbar = it }
                }
            }

            return null
        }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        ViewCompat.requestApplyInsets(this)
    }

    override fun onViewAdded(child: View) {
        super.onViewAdded(child)
        cachedToolbar = null
    }

    override fun onViewRemoved(child: View) {
        super.onViewRemoved(child)
        cachedToolbar = null
    }

    fun requireToolbar(): MaterialToolbar =
        toolbar ?: error("MyAppBarLayout requires a Toolbar/MaterialToolbar child")
}
