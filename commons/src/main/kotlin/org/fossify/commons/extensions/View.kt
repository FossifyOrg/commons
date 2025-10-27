package org.fossify.commons.extensions

import android.content.Context
import android.view.HapticFeedbackConstants
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import androidx.annotation.Px
import androidx.core.view.marginBottom
import androidx.core.view.marginLeft
import androidx.core.view.marginRight
import androidx.core.view.marginTop
import androidx.core.view.updateLayoutParams
import androidx.core.view.updatePadding
import androidx.core.view.updatePaddingRelative
import org.fossify.commons.R
import org.fossify.commons.helpers.SHORT_ANIMATION_DURATION

fun View.beInvisibleIf(beInvisible: Boolean) = if (beInvisible) beInvisible() else beVisible()

fun View.beVisibleIf(beVisible: Boolean) = if (beVisible) beVisible() else beGone()

fun View.beGoneIf(beGone: Boolean) = beVisibleIf(!beGone)

fun View.beInvisible() {
    visibility = View.INVISIBLE
}

fun View.beVisible() {
    visibility = View.VISIBLE
}

fun View.beGone() {
    visibility = View.GONE
}

fun View.onGlobalLayout(callback: () -> Unit) {
    viewTreeObserver?.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
        override fun onGlobalLayout() {
            if (viewTreeObserver != null) {
                viewTreeObserver.removeOnGlobalLayoutListener(this)
                callback()
            }
        }
    })
}

fun View.isVisible() = visibility == View.VISIBLE

fun View.isInvisible() = visibility == View.INVISIBLE

fun View.isGone() = visibility == View.GONE

fun View.performHapticFeedback() = performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY, HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING)

fun View.fadeIn(duration: Long = SHORT_ANIMATION_DURATION) {
    animate().alpha(1f).setDuration(duration).withStartAction { beVisible() }.start()
}

fun View.fadeOut(duration: Long = SHORT_ANIMATION_DURATION) {
    animate().alpha(0f).setDuration(duration).withEndAction { beGone() }.start()
}

fun View.setupViewBackground(context: Context) {
    background = if (context.isDynamicTheme()) {
        resources.getDrawable(R.drawable.selector_clickable_you)
    } else {
        resources.getDrawable(R.drawable.selector_clickable)
    }
}

/**
 * Sets a click listener that prevents quick repeated clicks.
 */
fun View.setDebouncedClickListener(
    debounceInterval: Long = 500,
    onClick: (View) -> Unit
) {
    var lastClickTime = 0L
    setOnClickListener {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastClickTime >= debounceInterval) {
            lastClickTime = currentTime
            onClick(it)
        }
    }
}

fun View.ensureBasePadding(): IntArray {
    val key = R.id.tag_base_padding
    val base = getTag(key) as? IntArray
    if (base != null) return base
    val arr = intArrayOf(paddingLeft, paddingTop, paddingRight, paddingBottom)
    setTag(key, arr)
    return arr
}

fun View.updatePaddingWithBase(
    @Px left: Int = 0,
    @Px top: Int = 0,
    @Px right: Int = 0,
    @Px bottom: Int = 0,
) {
    val base = ensureBasePadding()
    updatePadding(
        left = base[0] + left,
        top = base[1] + top,
        right = base[2] + right,
        bottom = base.last() + bottom
    )
}

fun View.ensureBaseMargin(): IntArray {
    val key = R.id.tag_base_margin
    val base = getTag(key) as? IntArray
    if (base != null) return base
    val arr = intArrayOf(marginLeft, marginTop, marginRight, marginBottom)
    setTag(key, arr)
    return arr
}

fun View.updateMarginWithBase(
    @Px left: Int = 0,
    @Px top: Int = 0,
    @Px right: Int = 0,
    @Px bottom: Int = 0,
) {
    val base = ensureBaseMargin()
    try {
        updateLayoutParams<ViewGroup.MarginLayoutParams> {
            leftMargin = base[0] + left
            topMargin = base[1] + top
            rightMargin = base[2] + right
            bottomMargin = base.last() + bottom
        }
    } catch (ignored: ClassCastException) {
    }
}
