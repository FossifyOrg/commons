package org.fossify.commons.activities

import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ScrollingView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsAnimationCompat
import androidx.core.view.WindowInsetsAnimationCompat.Callback.DISPATCH_MODE_CONTINUE_ON_SUBTREE
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsCompat.Type
import androidx.core.view.get
import androidx.core.view.size
import androidx.core.view.updatePadding
import androidx.core.widget.NestedScrollView
import androidx.recyclerview.widget.RecyclerView
import org.fossify.commons.R
import org.fossify.commons.extensions.applyColorFilter
import org.fossify.commons.extensions.getColoredDrawableWithColor
import org.fossify.commons.extensions.getColoredMaterialStatusBarColor
import org.fossify.commons.extensions.getContrastColor
import org.fossify.commons.extensions.getProperBackgroundColor
import org.fossify.commons.extensions.onApplyWindowInsets
import org.fossify.commons.extensions.setSystemBarsAppearance
import org.fossify.commons.extensions.updateMarginWithBase
import org.fossify.commons.extensions.updatePaddingWithBase
import org.fossify.commons.views.MyAppBarLayout

abstract class EdgeToEdgeActivity : AppCompatActivity() {
    open var isSearchBarEnabled = false
    open val padCutout: Boolean
        get() = true

    private var topAppBar: MyAppBarLayout? = null
    private var scrollingView: ScrollingView? = null
    private var materialScrollColorAnimation: ValueAnimator? = null
    private var currentScrollY = 0

    private val contentRoot by lazy { findViewById<View>(android.R.id.content) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.enableEdgeToEdge(window)
    }

    /**
     * Helper for views that need to be edge to edge compatible.
     */
    fun setupEdgeToEdge(
        padTopSystem: List<View> = emptyList(),
        padBottomSystem: List<View> = emptyList(),
        padBottomImeAndSystem: List<View> = emptyList(),
        moveBottomSystem: List<View> = emptyList(),
        animateIme: Boolean = false,
    ) {
        onApplyWindowInsets { insets ->
            val system = insets.getInsetsIgnoringVisibility(Type.systemBars())
            val imeAndSystem = insets.getInsets(Type.ime() or Type.systemBars())

            padTopSystem.forEach { it.updatePaddingWithBase(top = system.top) }
            padBottomSystem.forEach { it.updatePaddingWithBase(bottom = system.bottom) }
            padBottomImeAndSystem.forEach { it.updatePaddingWithBase(bottom = imeAndSystem.bottom) }
            moveBottomSystem.forEach { it.updateMarginWithBase(bottom = system.bottom) }

            if (padCutout) {
                val cutout = insets.getInsets(Type.displayCutout())
                val sideLeft = maxOf(system.left, cutout.left)
                val sideRight = maxOf(system.right, cutout.right)
                contentRoot.updatePaddingWithBase(left = sideLeft, right = sideRight)
            }

            if (animateIme) {
                ViewCompat.setWindowInsetsAnimationCallback(
                    contentRoot,
                    object : WindowInsetsAnimationCompat.Callback(DISPATCH_MODE_CONTINUE_ON_SUBTREE) {
                        override fun onProgress(
                            insets: WindowInsetsCompat,
                            runningAnimations: MutableList<WindowInsetsAnimationCompat>
                        ): WindowInsetsCompat {
                            val bottom = insets.getInsets(Type.systemBars() or Type.ime()).bottom
                            padBottomImeAndSystem.forEach {
                                it.updatePaddingWithBase(bottom = bottom)
                            }
                            return insets
                        }
                    }
                )
            }
        }
    }

    fun setupMaterialScrollListener(scrollingView: ScrollingView?, topAppBar: MyAppBarLayout) {
        this.scrollingView = scrollingView
        this.topAppBar = topAppBar
        if (scrollingView is RecyclerView) {
            scrollingView.setOnScrollChangeListener { v, scrollX, scrollY, oldScrollX, oldScrollY ->
                val newScrollY = scrollingView.computeVerticalScrollOffset()
                scrollingChanged(newScrollY, currentScrollY)
                currentScrollY = newScrollY
            }
        } else if (scrollingView is NestedScrollView) {
            scrollingView.setOnScrollChangeListener { v, scrollX, scrollY, oldScrollX, oldScrollY ->
                scrollingChanged(scrollY, oldScrollY)
            }
        }
    }

    private fun scrollingChanged(newScrollY: Int, oldScrollY: Int) {
        if (newScrollY > 0 && oldScrollY == 0) {
            animateTopBarColors(getProperBackgroundColor(), getColoredMaterialStatusBarColor())
        } else if (newScrollY == 0 && oldScrollY > 0) {
            animateTopBarColors(getColoredMaterialStatusBarColor(), getRequiredTopBarColor())
        }
    }

    fun animateTopBarColors(colorFrom: Int, colorTo: Int) {
        if (topAppBar == null) {
            return
        }

        materialScrollColorAnimation?.end()
        materialScrollColorAnimation = ValueAnimator.ofObject(ArgbEvaluator(), colorFrom, colorTo)
        materialScrollColorAnimation!!.addUpdateListener { animator ->
            val color = animator.animatedValue as Int
            if (topAppBar != null) {
                updateTopBarColors(topAppBar!!, color)
            }
        }

        materialScrollColorAnimation!!.start()
    }

    fun getRequiredTopBarColor(): Int {
        return if (
            (scrollingView is RecyclerView || scrollingView is NestedScrollView)
            && scrollingView?.computeVerticalScrollOffset() == 0
        ) {
            getProperBackgroundColor()
        } else {
            getColoredMaterialStatusBarColor()
        }
    }

    fun updateTopBarColors(topAppBar: MyAppBarLayout, color: Int) {
        val contrastColor = if (isSearchBarEnabled) {
            getProperBackgroundColor().getContrastColor()
        } else {
            color.getContrastColor()
        }

        window.setSystemBarsAppearance(color)
        if (!isSearchBarEnabled) {
            topAppBar.setBackgroundColor(color)
            topAppBar.toolbar?.setBackgroundColor(color)
            topAppBar.toolbar?.setTitleTextColor(contrastColor)
            topAppBar.toolbar?.navigationIcon?.applyColorFilter(contrastColor)
            topAppBar.toolbar?.collapseIcon = resources.getColoredDrawableWithColor(
                drawableId = R.drawable.ic_arrow_left_vector,
                color = contrastColor
            )
        }

        topAppBar.toolbar?.overflowIcon =
            resources.getColoredDrawableWithColor(R.drawable.ic_three_dots_vector, contrastColor)

        val menu = topAppBar.toolbar?.menu ?: return
        for (i in 0 until menu.size) {
            try {
                menu[i].icon?.setTint(contrastColor)
            } catch (ignored: Exception) {
            }
        }
    }

    override fun onDestroy() {
        materialScrollColorAnimation?.cancel()
        materialScrollColorAnimation = null
        super.onDestroy()
    }
}
