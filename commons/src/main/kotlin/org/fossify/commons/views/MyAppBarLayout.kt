package org.fossify.commons.views

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.core.content.withStyledAttributes
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat.Type
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.appbar.MaterialToolbar
import org.fossify.commons.R
import org.fossify.commons.extensions.updatePaddingWithBase

open class MyAppBarLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AppBarLayout(context, attrs, defStyleAttr) {
    private var cachedToolbar: MaterialToolbar? = null
    private var applyWindowInsets = true

    init {
        elevation = 0f
        ViewCompat.setElevation(this, 0f)
        stateListAnimator = null
        isLiftOnScroll = false
        isLifted = false

        context.withStyledAttributes(attrs, R.styleable.MyAppBarLayout) {
            applyWindowInsets = getBoolean(R.styleable.MyAppBarLayout_applyWindowInsets, true)
        }

        ViewCompat.setOnApplyWindowInsetsListener(this) { view, insets ->
            if (applyWindowInsets) {
                val system = insets.getInsetsIgnoringVisibility(Type.systemBars())
                view.updatePaddingWithBase(
                    top = system.top,
                    left = system.left,
                    right = system.right
                )
            }
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

    fun setApplyWindowInsets(apply: Boolean) {
        applyWindowInsets = apply
        if (apply) {
            ViewCompat.requestApplyInsets(this)
        } else {
            updatePaddingWithBase(top = 0, left = 0, right = 0)
        }
    }

    fun isApplyWindowInsetsEnabled(): Boolean = applyWindowInsets

    fun requireToolbar(): MaterialToolbar =
        toolbar ?: error("MyAppBarLayout requires a Toolbar/MaterialToolbar child")
}
