package com.github.k1rakishou.kurobanewnavstacktest.widget.bottom_panel

import android.animation.AnimatorSet
import android.animation.ValueAnimator
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
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
import com.github.k1rakishou.kurobanewnavstacktest.utils.setVisibilityFast
import com.github.k1rakishou.kurobanewnavstacktest.widget.TouchBlockingFrameLayout
import com.github.k1rakishou.kurobanewnavstacktest.widget.fab.KurobaFloatingActionButton
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.suspendCancellableCoroutine
import java.lang.IllegalStateException
import kotlin.coroutines.resume

class KurobaBottomPanel @JvmOverloads constructor(
  context: Context,
  attributeSet: AttributeSet? = null,
  attrDefStyle: Int = 0
) : TouchBlockingFrameLayout(context, attributeSet, attrDefStyle),
  CollapsableView,
  KurobaBottomNavPanel.KurobaBottomPanelCallbacks {

  private val insetBottomDeferredInitial = CompletableDeferred<Int>()
  private val bottomPanelInitialState = CompletableDeferred<State>()

  private val scope = KurobaCoroutineScope()
  private val panelContainer: FrameLayout

  private var lastInsetBottom = 0
  private var state = State.Uninitialized
  private var initialState = State.Uninitialized
  private var animatorSet: AnimatorSet? = null
  private var attachedFab: KurobaFloatingActionButton? = null

  private val bottomPanelInitializationListeners = mutableListOf<() -> Unit>()
  private val bottomPanelStateUpdatesListeners = mutableListOf<(State) -> Unit>()
  private val bottomNavPanelSelectedItemListeners = mutableListOf<(KurobaBottomNavPanel.SelectedItem) -> Unit>()

  init {
    LayoutInflater.from(context).inflate(R.layout.kuroba_bottom_panel, this, true).apply {
      panelContainer = findViewById(R.id.panel_container)

      panelContainer.setOnApplyWindowInsetsListenerAndDoRequest { v, insets ->
        updateContainerPaddings(v, insets.systemWindowInsetBottom)

        insetBottomDeferredInitial.complete(insets.systemWindowInsetBottom)
        lastInsetBottom = insets.systemWindowInsetBottom

        return@setOnApplyWindowInsetsListenerAndDoRequest insets
      }

      setBackgroundColor(context.resources.getColor(R.color.colorPrimaryDark))
      visibility = View.INVISIBLE
    }
  }

  private fun updateContainerPaddings(v: View, insetBottom: Int) {
    v.updateLayoutParams<FrameLayout.LayoutParams> {
      height = getCurrentHeightOfChild() + insetBottom
    }
    v.updatePadding(bottom = insetBottom)
  }

  override fun translationY(newTranslationY: Float) {
    translationY = newTranslationY

    if (canUpdateFab()) {
      var scaleTranslationY = newTranslationY

      if (scaleTranslationY > height) {
        scaleTranslationY = height.toFloat()
      } else if (scaleTranslationY < 0) {
        scaleTranslationY = 0f
      }

      val scale = 1f - (scaleTranslationY / height)

      attachedFab!!.setScalePreInitialized(scale, scale)
      attachedFab!!.translationY = -height.toFloat()
    }
  }

  private fun canUpdateFab(): Boolean {
    return height > 0
      && attachedFab != null
      && (state == State.BottomNavPanel || state == State.Uninitialized)
  }

  override fun height(): Float {
    return height.toFloat()
  }

  override fun translationY(): Float {
    return translationY
  }

  override fun onFinishInflate() {
    super.onFinishInflate()
    scope.launch { initState() }
  }

  override fun onItemSelected(selectedItem: KurobaBottomNavPanel.SelectedItem) {
    bottomNavPanelSelectedItemListeners.forEach { listener -> listener(selectedItem) }
  }

  fun bottomPanelPreparationsCompleted(initialState: State) {
    this.initialState = initialState
    bottomPanelInitialState.complete(initialState)
  }

  fun attachFab(fab: KurobaFloatingActionButton) {
    check(attachedFab == null) { "Already attached" }

    this.attachedFab = fab
  }

  fun addOnBottomPanelInitialized(func: () -> Unit) {
    if (state != State.Uninitialized) {
      func()
      bottomPanelStateUpdatesListeners.forEach { func -> func(state) }
      return
    }

    bottomPanelInitializationListeners += func
  }

  fun addOnBottomPanelStateChanged(func: (State) -> Unit) {
    bottomPanelStateUpdatesListeners += func
  }

  fun addOnBottomNavPanelItemSelectedListener(listener: (KurobaBottomNavPanel.SelectedItem) -> Unit) {
    bottomNavPanelSelectedItemListeners += listener
  }

  fun switchInto(newState: State) {
    scope.launch { switchIntoInternal(newState) }
  }

  fun cleanup() {
    endRevealAnimations()
    scope.cancelChildren()

    bottomPanelInitializationListeners.clear()
    bottomPanelStateUpdatesListeners.clear()
    bottomNavPanelSelectedItemListeners.clear()

    attachedFab = null
    state = State.Uninitialized
  }

  private suspend fun switchIntoInternal(newState: State) {
    check(state != State.Uninitialized) { "Not initialized yet" }

    if (state == newState) {
      return
    }

    panelContainer.removeAllViews()

    when (newState) {
      State.Uninitialized -> throw IllegalStateException("Cannot be used as newState")
      State.Hidden -> {
        val kurobaBottomHiddenPanel = KurobaBottomHiddenPanel(context)
        panelContainer.addView(kurobaBottomHiddenPanel)
      }
      State.BottomNavPanel -> {
        val kurobaBottomNavPanel = KurobaBottomNavPanel(context, this)
        panelContainer.addView(kurobaBottomNavPanel)
        kurobaBottomNavPanel.select(KurobaBottomNavPanel.SelectedItem.Browse)

        kurobaBottomNavPanel.updateLayoutParams<FrameLayout.LayoutParams> {
          height = getCurrentHeightOfChild() + lastInsetBottom
        }
      }
      State.ReplyLayoutPanel -> {
        val kurobaBottomReplyPanel = KurobaBottomReplyPanel(context)
        panelContainer.addView(kurobaBottomReplyPanel)

        kurobaBottomReplyPanel.updateLayoutParams<FrameLayout.LayoutParams> {
          height = getCurrentHeightOfChild() + lastInsetBottom
        }
      }
      State.SelectionPanel -> TODO()
    }

    panelContainer.requestLayout()
    panelContainer.awaitLayout()

    if (newState != State.Hidden) {
      this.setVisibilityFast(View.VISIBLE)
      panelContainer.updateLayoutParams<FrameLayout.LayoutParams> {
        height = getCurrentHeightOfChild() + lastInsetBottom
      }
      panelContainer.updatePadding(bottom = lastInsetBottom)
    } else {
      this.setVisibilityFast(View.GONE)
      panelContainer.updateLayoutParams<FrameLayout.LayoutParams> {
        height = 0
      }
      panelContainer.updatePadding(bottom = 0)
    }

    bottomPanelStateUpdatesListeners.forEach { func -> func(newState) }
    state = newState
  }

  private suspend fun initState() {
    val initialState = bottomPanelInitialState.await()
    check(state == State.Uninitialized) { "Already initialized?!" }

    when (initialState) {
      State.BottomNavPanel -> initBottomNavViewPanel()
      State.Hidden -> initBottomHiddenPanel()
      else -> throw NotImplementedError("Not implemented for state: $initialState")
    }

    bottomPanelInitializationListeners.forEach { func -> func() }
    bottomPanelInitializationListeners.clear()

    bottomPanelStateUpdatesListeners.forEach { func -> func(initialState) }

    state = initialState
  }

  private suspend fun initBottomHiddenPanel() {
    panelContainer.removeAllViews()

    val kurobaBottomHiddenPanel = KurobaBottomHiddenPanel(context)
    panelContainer.addView(kurobaBottomHiddenPanel)

    panelContainer.requestLayout()
    panelContainer.awaitLayout()

    insetBottomDeferredInitial.await()
  }

  private suspend fun initBottomNavViewPanel() {
    panelContainer.removeAllViews()

    val kurobaBottomNavPanel = KurobaBottomNavPanel(context, this)
    panelContainer.addView(kurobaBottomNavPanel)
    kurobaBottomNavPanel.select(KurobaBottomNavPanel.SelectedItem.Browse)

    panelContainer.requestLayout()
    panelContainer.awaitLayout()

    val insetBottom = insetBottomDeferredInitial.await()

    updateContainerPaddings(panelContainer, insetBottom)
    val fullHeight = (panelContainer.height + panelContainer.paddingBottom).toFloat()
    translationY(fullHeight)

    revealAnimation(this, translationY, 0f)
  }

  private suspend fun revealAnimation(panel: KurobaBottomPanel, fromY: Float, toY: Float) {
    endRevealAnimations()

    suspendCancellableCoroutine<Unit> { continuation ->
      val translationAnimation = ValueAnimator.ofFloat(fromY, toY).apply {
        addUpdateListener { animator -> panel.translationY(animator.animatedValue as Float) }
      }

      val alphaAnimation = ValueAnimator.ofFloat(0f, 1f).apply {
        addUpdateListener { animator -> panel.alpha = animator.animatedValue as Float }
      }

      animatorSet = AnimatorSet().apply {
        playTogether(translationAnimation, alphaAnimation)
        duration = ANIMATION_DURATION
        interpolator = INTERPOLATOR
        doOnStart {
          panel.visibility = View.VISIBLE
          panel.alpha = 0f
        }

        fun resume() {
          if (continuation.isActive) {
            continuation.resume(Unit)
          }
        }

        doOnCancel { resume() }
        doOnEnd { resume() }

        start()
      }

      continuation.invokeOnCancellation {
        endRevealAnimations()
      }
    }
  }

  private fun endRevealAnimations() {
    if (animatorSet != null) {
      animatorSet?.end()
      animatorSet = null
    }
  }

  private fun getCurrentHeightOfChild(): Int {
    check(panelContainer.childCount <= 1) { "Bad children count: ${panelContainer.childCount}" }

    if (panelContainer.childCount <= 0) {
      return 0
    }

    return when (val child = panelContainer.getChildAt(0)) {
      is KurobaBottomNavPanel -> child.getCurrentHeight()
      is KurobaBottomReplyPanel -> child.getCurrentHeight()
      is KurobaBottomHiddenPanel -> child.getCurrentHeight()
      else -> throw IllegalStateException("Unknown view: ${panelContainer.javaClass.simpleName}")
    }
  }

  fun onBackPressed(): Boolean {
    if (state == State.Uninitialized || initialState == State.Uninitialized || state == initialState) {
      return false
    }

    if (initialState != State.Uninitialized) {
      switchInto(initialState)
      return true
    }

    return false
  }

  enum class State {
    Uninitialized,
    Hidden,
    BottomNavPanel,
    SelectionPanel,
    ReplyLayoutPanel
  }

  companion object {
    private val INTERPOLATOR = FastOutSlowInInterpolator()
    private const val ANIMATION_DURATION = 250L
  }

}