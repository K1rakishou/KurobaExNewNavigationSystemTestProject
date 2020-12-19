package com.github.k1rakishou.kurobanewnavstacktest.utils

import android.animation.AnimatorSet
import android.animation.ValueAnimator
import android.content.res.Resources
import android.graphics.drawable.ColorDrawable
import android.text.TextWatcher
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.animation.doOnCancel
import androidx.core.animation.doOnEnd
import androidx.core.view.ViewCompat
import androidx.core.view.doOnLayout
import androidx.core.view.updateLayoutParams
import com.airbnb.epoxy.*
import com.bluelinelabs.conductor.Router
import com.github.k1rakishou.kurobanewnavstacktest.core.base.BaseController
import com.github.k1rakishou.kurobanewnavstacktest.core.base.ControllerTag
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

val Int.dp: Int
  get() = (this * Resources.getSystem().displayMetrics.density).toInt()

val Float.dp: Float
  get() = (this * Resources.getSystem().displayMetrics.density)

val <T> T.exhaustive: T
  get() = this

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

fun <T : View> View.setBehaviorExt(behavior: CoordinatorLayout.Behavior<T>) {
  (layoutParams as CoordinatorLayout.LayoutParams).behavior = behavior
}

fun <T : CoordinatorLayout.Behavior<*>> View.getBehaviorExt(): T? {
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

fun View.setOnApplyWindowInsetsListenerAndDoRequest(listener: View.OnApplyWindowInsetsListener) {
  setOnApplyWindowInsetsListener(listener)
  requestApplyInsetsWhenAttached()
}

fun View.setOnApplyWindowInsetsOneShotListenerAndDoRequest(listener: View.OnApplyWindowInsetsListener) {
  setOnApplyWindowInsetsListener { v, insets ->
    setOnApplyWindowInsetsListener(null)
    listener.onApplyWindowInsets(v, insets)
    return@setOnApplyWindowInsetsListener insets
  }

  requestApplyInsetsWhenAttached()
}

fun ViewGroup.getChildAtOrNull(index: Int): View? {
  if (index < 0 || index >= childCount) {
    return null
  }

  return getChildAt(index)
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

fun Throwable.errorMessageOrClassName(): String {
  if (message != null) {
    return message!!
  }

  return this::class.java.name
}

fun View.setVisibilityFast(newVisibility: Int) {
  if (this.visibility == newVisibility) {
    return
  }

  this.visibility = newVisibility
}

fun View.setAlphaFast(newAlpha: Float) {
  if (this.alpha == newAlpha) {
    return
  }

  this.alpha = newAlpha
}

fun TextView.setTextFast(newText: CharSequence) {
  if (text == newText) {
    return
  }

  text = newText
}

fun View.setEnabledFast(enable: Boolean) {
  if (this.isEnabled == enable) {
    return
  }

  this.isEnabled = enable
}

fun View.setBackgroundColorFast(@ColorInt newColor: Int) {
  val backgroundDrawable = background

  if (backgroundDrawable !is ColorDrawable) {
    setBackgroundColor(newColor)
    return
  }

  if (backgroundDrawable.color == newColor) {
    return
  }

  setBackgroundColor(newColor)
}

fun EditText.doIgnoringTextWatcher(textWatcher: TextWatcher, func: EditText.() -> Unit) {
  removeTextChangedListener(textWatcher)
  func(this)
  addTextChangedListener(textWatcher)
}

fun Router.findRouterWithControllerByTag(controllerTag: ControllerTag): Pair<Router, BaseController>? {
  return findRouterWithControllerByTagInternal(this, controllerTag)
}

private fun findRouterWithControllerByTagInternal(
  router: Router,
  controllerTag: ControllerTag
): Pair<Router, BaseController>? {
  for (routerTransaction in router.backstack) {
    val controller = routerTransaction.controller

    if ((controller as? BaseController)?.getControllerTag() == controllerTag) {
      return Pair(router, controller)
    }

    for (childRouter in routerTransaction.controller.childRouters) {
      val result = findRouterWithControllerByTagInternal(childRouter, controllerTag)
      if (result != null) {
        return result
      }
    }
  }

  return null
}

fun EpoxyController.addOneshotModelBuildListener(callback: () -> Unit) {
  addModelBuildListener(object : OnModelBuildFinishedListener {
    override fun onModelBuildFinished(result: DiffResult) {
      callback()

      removeModelBuildListener(this)
    }
  })
}

suspend fun View.awaitLayout() {
  suspendCoroutine<Unit> { continuation ->
    doOnLayout { continuation.resume(Unit) }
  }
}

suspend fun View.requestLayoutAndAwait() {
  requestLayout()
  awaitLayout()
}

fun <T> CancellableContinuation<T>.resumeIfActive(value: T) {
  if (isActive) {
    resume(value)
  }
}

fun <T> ValueAnimator.value(): T {
  return animatedValue as T
}

suspend fun AnimatorSet.endAndAwait() {
  if (!isStarted) {
    return
  }

  if (!isRunning) {
    return
  }

  suspendCancellableCoroutine<Unit> { continuation ->
    doOnCancel { continuation.resumeIfActive(Unit) }
    doOnEnd { continuation.resumeIfActive(Unit) }
    continuation.invokeOnCancellation { continuation.resumeIfActive(Unit) }

    end()
  }
}