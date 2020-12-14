package com.github.k1rakishou.kurobanewnavstacktest.widget.bottom_panel

import android.animation.AnimatorSet
import android.animation.ValueAnimator
import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import androidx.core.animation.doOnCancel
import androidx.core.animation.doOnEnd
import androidx.core.animation.doOnStart
import androidx.core.view.*
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import com.github.k1rakishou.kurobanewnavstacktest.R
import com.github.k1rakishou.kurobanewnavstacktest.base.KurobaCoroutineScope
import com.github.k1rakishou.kurobanewnavstacktest.controller.base.CollapsableView
import com.github.k1rakishou.kurobanewnavstacktest.utils.awaitLayout
import com.github.k1rakishou.kurobanewnavstacktest.utils.setOnApplyWindowInsetsListenerAndDoRequest
import com.github.k1rakishou.kurobanewnavstacktest.widget.TouchBlockingFrameLayout
import com.github.k1rakishou.kurobanewnavstacktest.widget.fab.KurobaFloatingActionButton
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

class KurobaBottomPanel @JvmOverloads constructor(
  context: Context,
  attributeSet: AttributeSet? = null,
  attrDefStyle: Int = 0
) : TouchBlockingFrameLayout(context, attributeSet, attrDefStyle), CollapsableView {
  private val scope = KurobaCoroutineScope()
  private val insetBottomDeferred = CompletableDeferred<Int>()
  private val container: FrameLayout

  private var state = State.Uninitialized
  private var animatorSet: AnimatorSet? = null
  private val bottomPanelInitializationListener = mutableListOf<() -> Unit>()

  private val awaitableFab = CompletableDeferred<KurobaFloatingActionButton>()

  init {
    inflate(context, R.layout.kuroba_bottom_panel, this).apply {
      container = findViewById(R.id.actual_panel_container)

      container.setOnApplyWindowInsetsListenerAndDoRequest { v, insets ->
        updateContainerPaddings(v, insets.systemWindowInsetBottom)
        insetBottomDeferred.complete(insets.systemWindowInsetBottom)
        return@setOnApplyWindowInsetsListenerAndDoRequest insets
      }

      setBackgroundColor(context.resources.getColor(R.color.colorPrimaryDark))
      visibility = View.INVISIBLE
    }
  }

  private fun updateContainerPaddings(v: View, insetBottom: Int) {
    v.updateLayoutParams<FrameLayout.LayoutParams> {
      height = container.height + insetBottom
    }
    v.updatePadding(bottom = insetBottom)
  }

  override fun translationY(newTranslationY: Float) {
    translationY = newTranslationY

    if (height <= 0) {
      return
    }

    if (awaitableFab.isCompleted && state == State.BottomNavPanel) {
      val scale = 1f - (newTranslationY / height)
      val fab = awaitableFab.getCompleted()

      fab.setScale(scale, scale)
      fab.translationY = -height.toFloat()
    }
  }

  override fun height(): Float {
    return height.toFloat()
  }

  override fun translationY(): Float {
    return translationY
  }

  override fun onFinishInflate() {
    super.onFinishInflate()
    scope.launch { initState(State.BottomNavPanel) }
  }

  fun attachFab(fab: KurobaFloatingActionButton) {
    check(!awaitableFab.isCompleted) { "Already attached" }
    check(fab.isOrWillBeHidden) { "FAB must be hidden" }

    this.awaitableFab.complete(fab)
  }

  fun onBottomPanelInitialized(func: () -> Unit) {
    if (state != State.Uninitialized) {
      func()
      return
    }

    bottomPanelInitializationListener += func
  }

  suspend fun switchInto(newState: State) {
    check(state != State.Uninitialized) { "Not initialized yet" }

    if (state == newState) {
      return
    }

    container.removeAllViews()

    val kurobaBottomNavPanel = KurobaBottomNavPanel(context)
    container.addView(kurobaBottomNavPanel)
    kurobaBottomNavPanel.select(KurobaBottomNavPanel.SelectedItem.Browse)

    container.requestLayout()
    container.awaitLayout()

    val insetBottom = insetBottomDeferred.await()
    updateContainerPaddings(container, insetBottom)

    y = (container.height + container.paddingBottom).toFloat()

    val attachedFab = awaitableFab.getCompleted()
    if (newState == State.BottomNavPanel) {
      attachedFab.showFab()
    } else {
      attachedFab.hideFab()
    }
  }

  private suspend fun initState(initialState: State) {
    check(state == State.Uninitialized) { "Already initialized?!" }
    container.removeAllViews()

    val kurobaBottomNavPanel = KurobaBottomNavPanel(context)
    container.addView(kurobaBottomNavPanel)
    kurobaBottomNavPanel.select(KurobaBottomNavPanel.SelectedItem.Browse)

    container.requestLayout()
    container.awaitLayout()

    val insetBottom = insetBottomDeferred.await()
    val attachedFab = awaitableFab.await()

    updateContainerPaddings(container, insetBottom)
    val fullHeight = (container.height + container.paddingBottom).toFloat()
    y = fullHeight

    attachedFab.setScale(0f, 0f)
    attachedFab.visibility = View.VISIBLE
    attachedFab.translationY = -fullHeight

    revealAnimation(this, translationY, 0f)

    state = initialState

    bottomPanelInitializationListener.forEach { func -> func() }
    bottomPanelInitializationListener.clear()
  }

  private suspend fun revealAnimation(panel: KurobaBottomPanel, fromY: Float, toY: Float) {
    if (animatorSet != null) {
      animatorSet?.end()
      animatorSet = null
    }

    suspendCancellableCoroutine<Unit> { continuation ->
      val translationAnimation = ValueAnimator.ofFloat(fromY, toY).apply {
        addUpdateListener { animator -> panel.translationY(animator.animatedValue as Float) }
      }

      val alphaAnimation = ValueAnimator.ofFloat(0f, 1f).apply {
        addUpdateListener { animator -> panel.alpha = animator.animatedValue as Float }
      }

      animatorSet = AnimatorSet().apply {
        playTogether(translationAnimation, alphaAnimation)
        duration = 250
        interpolator = INTERPOLATOR
        doOnStart {
          panel.visibility = View.VISIBLE
          panel.alpha = 0f
        }
        doOnCancel { continuation.resume(Unit) }
        doOnEnd { continuation.resume(Unit) }

        start()
      }

      continuation.invokeOnCancellation {
        if (animatorSet != null) {
          animatorSet?.end()
          animatorSet = null
        }
      }
    }
  }

  enum class State {
    Uninitialized,
    BottomNavPanel,
    SelectionPanel,
    ReplyLayoutPanel
  }

  companion object {
    private val INTERPOLATOR = FastOutSlowInInterpolator()
  }

}