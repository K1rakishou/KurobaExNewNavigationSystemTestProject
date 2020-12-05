package com.github.k1rakishou.kurobanewnavstacktest.utils

import android.content.res.Resources
import android.view.View
import android.view.ViewGroup
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.ViewCompat
import androidx.core.view.updateLayoutParams
import com.airbnb.epoxy.AsyncEpoxyController
import com.airbnb.epoxy.EpoxyController
import com.airbnb.epoxy.EpoxyRecyclerView

val Int.dp: Int
    get() = (this * Resources.getSystem().displayMetrics.density).toInt()

fun EpoxyRecyclerView.withModelsAsync(buildModels: EpoxyController.() -> Unit) {
    val controller = SimpleAsyncEpoxyController(buildModels)
        .also { setController(it) }

    controller.requestModelBuild()
}

class SimpleAsyncEpoxyController(
    val builder: EpoxyController.() -> Unit
) : AsyncEpoxyController() {
    override fun buildModels() {
        builder()
    }
}

fun <T : View> View.setBehavior(behavior: CoordinatorLayout.Behavior<T>) {
    (layoutParams as CoordinatorLayout.LayoutParams).behavior = behavior
}

fun <T : CoordinatorLayout.Behavior<*>> View.getBehaviorEx(): T? {
    return (layoutParams as? CoordinatorLayout.LayoutParams)?.behavior as? T
}

fun findChildView(viewGroup: ViewGroup, predicate: (View) -> Boolean): View? {
    if (predicate(viewGroup)) {
        return viewGroup
    }

    for (childIndex in 0 until viewGroup.childCount) {
        val child = viewGroup.getChildAt(childIndex)
        if (predicate(child)) {
            return child
        }

        if (child is ViewGroup) {
            val result = findChildView(child, predicate)
            if (result != null) {
                return result
            }
        }
    }

    return null
}

private fun View.requestApplyInsetsWhenAttached() {
    if (ViewCompat.isAttachedToWindow(this)) {
        ViewCompat.requestApplyInsets(this)
        return
    }

    this.addOnAttachStateChangeListener(object : View.OnAttachStateChangeListener {
        override fun onViewAttachedToWindow(v: View) {
            v.removeOnAttachStateChangeListener(this)
            ViewCompat.requestApplyInsets(v)
        }

        override fun onViewDetachedFromWindow(v: View?) {
        }
    })
}

fun View.setOnApplyWindowInsetsListenerAndRequest(listener: View.OnApplyWindowInsetsListener) {
    setOnApplyWindowInsetsListener(listener)
    requestApplyInsetsWhenAttached()
}

fun View.updateMargins(
    left: Int? = null,
    start: Int? = null,
    right: Int? = null,
    end: Int? = null,
    top: Int? = null,
    bottom: Int? = null
) {
    updateLayoutParams<ViewGroup.MarginLayoutParams> {
        leftMargin = left ?: leftMargin
        marginStart = start ?: marginStart
        rightMargin = right ?: rightMargin
        marginEnd = end ?: marginEnd
        topMargin = top ?: topMargin
        bottomMargin = bottom ?: bottomMargin
    }
}