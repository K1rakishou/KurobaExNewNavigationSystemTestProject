package com.github.k1rakishou.kurobanewnavstacktest.widget.bottom_panel

import android.animation.AnimatorSet
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Color
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
import com.github.k1rakishou.kurobanewnavstacktest.utils.*
import com.github.k1rakishou.kurobanewnavstacktest.widget.layout.TouchBlockingFrameLayout
import com.github.k1rakishou.kurobanewnavstacktest.widget.fab.KurobaFloatingActionButton
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.suspendCancellableCoroutine
import java.lang.IllegalStateException

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
  private var revealAnimatorSet: AnimatorSet? = null
  private var stateChangeAnimatorSet: AnimatorSet? = null
  private var disappearanceAnimatorSet: AnimatorSet? = null
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

      setVisibilityFast(View.INVISIBLE)
    }
  }

  private fun updateContainerPaddings(v: View, insetBottom: Int) {
    v.updateLayoutParams<FrameLayout.LayoutParams> {
      height = getChildPanelHeight() + insetBottom
    }
    v.updatePadding(bottom = insetBottom)
  }

  override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
    super.onLayout(changed, left, top, right, bottom)

    if (changed && canUpdateFab()) {
      updateFab(translationY)
    }
  }

  override fun translationY(newTranslationY: Float) {
    translationY = newTranslationY

    if (canUpdateFab()) {
      updateFab(newTranslationY)
    }
  }

  private fun updateFab(newTranslationY: Float) {
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
    scope.launch { initBottomPanel() }
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
    scope.launch {
      if (newState == State.Hidden) {
        hidePanel()
      } else {
        switchIntoInternal(newState)
      }
    }
  }

  fun cleanup() {
    endRevealAnimations()
    endStateChangeAnimations()
    endDisappearanceAnimations()

    scope.cancelChildren()

    panelContainer.removeAllViews()
    bottomPanelInitializationListeners.clear()
    bottomPanelStateUpdatesListeners.clear()
    bottomNavPanelSelectedItemListeners.clear()

    attachedFab = null

    state = State.Uninitialized
    initialState = State.Uninitialized
  }

  fun onBackPressed(): Boolean {
    if (revealAnimatorSet != null || stateChangeAnimatorSet != null || disappearanceAnimatorSet != null) {
      // If any of the animations is in progress, consume the click and do nothing
      return true
    }

    if (state == State.Uninitialized || initialState == State.Uninitialized || state == initialState) {
      return false
    }

    if (initialState != State.Uninitialized) {
      switchInto(initialState)
      return true
    }

    return false
  }

  private suspend fun hidePanel() {
    val newState = State.Hidden

    val childPanel = panelContainer.getChildAt(0)
    childPanel as ChildPanelContract
    childPanel.enableOrDisableControls(enable = false)

    var prevHeight = getChildPanelHeight()
    if (prevHeight == 0) {
      prevHeight = 1
    }

    disappearanceAnimation(
      panelContainer,
      prevHeight,
      0
    )

    panelContainer.removeAllViews()
    val kurobaBottomHiddenPanel = KurobaBottomHiddenPanel(context)
    panelContainer.addView(kurobaBottomHiddenPanel)

    bottomPanelStateUpdatesListeners.forEach { func -> func(newState) }
    state = newState

    childPanel.enableOrDisableControls(enable = true)
  }

  private suspend fun switchIntoInternal(newState: State) {
    check(state != State.Uninitialized) { "Not initialized yet" }
    check(panelContainer.childCount == 1) { "Bad childrenCount: ${panelContainer.childCount}" }

    if (state == newState) {
      return
    }

    val prevColor = getChildPanelColor()
    var prevHeight = getChildPanelHeight()
    if (prevHeight == 0) {
      prevHeight = 1
    }

    panelContainer.removeAllViews()

    val childPanel = when (newState) {
      State.Uninitialized -> throw IllegalStateException("Cannot be used as newState")
      State.Hidden -> throw IllegalStateException("Cannot be used in switchIntoInternal, use hidePanel instead")
      State.BottomNavPanel -> {
        val kurobaBottomNavPanel = KurobaBottomNavPanel(context, this)
        kurobaBottomNavPanel.setVisibilityFast(View.INVISIBLE)
        panelContainer.addView(kurobaBottomNavPanel)
        kurobaBottomNavPanel.select(KurobaBottomNavPanel.SelectedItem.Browse)

        kurobaBottomNavPanel
      }
      State.ReplyLayoutPanel -> {
        val kurobaBottomReplyPanel = KurobaBottomReplyPanel(context)
        kurobaBottomReplyPanel.setVisibilityFast(View.INVISIBLE)
        panelContainer.addView(kurobaBottomReplyPanel)

        kurobaBottomReplyPanel
      }
      State.SelectionPanel -> TODO()
    }

    childPanel.enableOrDisableControls(enable = false)

    panelContainer.requestLayout()
    panelContainer.awaitLayout()

    val newColor = getChildPanelColor()
    val newHeight = getChildPanelHeight()

    stateChangeAnimation(
      panelContainer,
      childPanel,
      prevColor,
      newColor,
      prevHeight,
      newHeight
    )

    bottomPanelStateUpdatesListeners.forEach { func -> func(newState) }
    state = newState

    childPanel.enableOrDisableControls(enable = true)
  }

  private suspend fun initBottomPanel() {
    val initialState = bottomPanelInitialState.await()
    check(state == State.Uninitialized) { "Already initialized?!" }

    val childPanelContract = when (initialState) {
      State.BottomNavPanel -> initBottomNavViewPanel()
      State.Hidden -> initBottomHiddenPanel()
      else -> throw NotImplementedError("Not implemented for state: $initialState")
    }

    setBackgroundColorFast(getChildPanelColor())

    bottomPanelInitializationListeners.forEach { func -> func() }
    bottomPanelInitializationListeners.clear()

    bottomPanelStateUpdatesListeners.forEach { func -> func(initialState) }

    state = initialState
    childPanelContract.enableOrDisableControls(enable = true)
  }

  private suspend fun initBottomHiddenPanel(): ChildPanelContract {
    panelContainer.removeAllViews()

    val kurobaBottomHiddenPanel = KurobaBottomHiddenPanel(context)
    panelContainer.addView(kurobaBottomHiddenPanel)
    panelContainer.setBackgroundColorFast(getChildPanelColor())
    kurobaBottomHiddenPanel.enableOrDisableControls(enable = false)

    panelContainer.requestLayout()
    panelContainer.awaitLayout()

    val insetBottom = insetBottomDeferredInitial.await()
    updateContainerPaddings(panelContainer, insetBottom)

    val fullHeight = (panelContainer.height).toFloat()
    translationY(fullHeight)

    return kurobaBottomHiddenPanel
  }

  private suspend fun initBottomNavViewPanel(): ChildPanelContract {
    panelContainer.removeAllViews()

    val kurobaBottomNavPanel = KurobaBottomNavPanel(context, this)
    panelContainer.addView(kurobaBottomNavPanel)
    panelContainer.setBackgroundColorFast(getChildPanelColor())
    kurobaBottomNavPanel.enableOrDisableControls(enable = false)
    kurobaBottomNavPanel.select(KurobaBottomNavPanel.SelectedItem.Browse)
    kurobaBottomNavPanel.setVisibilityFast(View.INVISIBLE)

    panelContainer.requestLayout()
    panelContainer.awaitLayout()

    val insetBottom = insetBottomDeferredInitial.await()
    updateContainerPaddings(panelContainer, insetBottom)

    val fullHeight = (panelContainer.height).toFloat()
    translationY(fullHeight)

    bottomNavViewPanelRevealAnimation(this, kurobaBottomNavPanel, translationY, 0f)
    return kurobaBottomNavPanel
  }

  private fun getChildPanelHeight(): Int {
    check(panelContainer.childCount <= 1) { "Bad children count: ${panelContainer.childCount}" }

    if (panelContainer.childCount <= 0) {
      return 0
    }

    val child = panelContainer.getChildAt(0) as ChildPanelContract
    return child.getCurrentHeight()
  }

  private fun getChildPanelColor(): Int {
    check(panelContainer.childCount <= 1) { "Bad children count: ${panelContainer.childCount}" }

    if (panelContainer.childCount <= 0) {
      return Color.MAGENTA
    }

    val child = panelContainer.getChildAt(0) as ChildPanelContract
    return child.getBackgroundColor()
  }

  private suspend fun disappearanceAnimation(
    panelContainer: FrameLayout,
    prevHeight: Int,
    newHeight: Int
  ) {
    require(newHeight < prevHeight) { "Bad height: prevHeight=$prevHeight, newHeight=$newHeight" }
    endDisappearanceAnimations()

    val prevTranslationY = translationY
    val newTranslationY = translationY + (prevHeight - newHeight)
    val initialFabTranslationY = attachedFab?.translationY

    suspendCancellableCoroutine<Unit> { continuation ->
      val translationAnimation = ValueAnimator.ofFloat(prevTranslationY, newTranslationY).apply {
        addUpdateListener { animator ->
          this@KurobaBottomPanel.translationY = animator.animatedValue as Float

          attachedFab?.let { fab ->
            initialFabTranslationY?.let { initialY ->
              val deltaTranslationY = animator.animatedValue as Float
              fab.translationY = initialY + deltaTranslationY
            }
          }
        }
      }

      val alphaAnimation = ValueAnimator.ofFloat(1f, 0f).apply {
        addUpdateListener { animator ->
          this@KurobaBottomPanel.setAlphaFast(animator.animatedValue as Float)
        }
      }

      fun onAnimationEnd() {
        disappearanceAnimatorSet = null
        continuation.resumeIfActive(Unit)
      }

      disappearanceAnimatorSet = AnimatorSet().apply {
        playTogether(translationAnimation, alphaAnimation)

        interpolator = INTERPOLATOR
        duration = GENERIC_ANIMATION_DURATION
        doOnCancel { onAnimationEnd() }

        doOnEnd {
          translationY = prevTranslationY

          panelContainer.updateLayoutParams<FrameLayout.LayoutParams> { height = 0 }
          panelContainer.updatePadding(bottom = lastInsetBottom)
          this@KurobaBottomPanel.setVisibilityFast(View.INVISIBLE)

          onAnimationEnd()
        }

        start()
      }
    }
  }

  private suspend fun stateChangeAnimation(
    panelContainer: FrameLayout,
    childPanel: View,
    prevColor: Int,
    newColor: Int,
    prevHeight: Int,
    newHeight: Int
  ) {
    endStateChangeAnimations()

    val prevScale = panelContainer.scaleY
    val newScale = when {
      newHeight > prevHeight -> (newHeight / prevHeight).toFloat()
      newHeight < prevHeight -> (prevHeight / newHeight).toFloat()
      else -> 1f
    }

    val prevPivotY = panelContainer.pivotY
    val prevTranslationY = panelContainer.translationY
    val newTranslationY = prevTranslationY - (newHeight - prevHeight)
    val initialFabTranslationY = attachedFab?.translationY

    childPanel.setAlphaFast(0f)
    childPanel.setVisibilityFast(View.VISIBLE)
    panelContainer.setVisibilityFast(View.VISIBLE)
    this@KurobaBottomPanel.setAlphaFast(1f)
    this@KurobaBottomPanel.setVisibilityFast(View.VISIBLE)
    this@KurobaBottomPanel.setBackgroundColorFast(newColor)

    suspendCancellableCoroutine<Unit> { continuation ->
      val colorAnimation = ValueAnimator.ofArgb(prevColor, newColor).apply {
        addUpdateListener { animator ->
          panelContainer.setBackgroundColorFast(animator.animatedValue as Int)
          childPanel.setBackgroundColorFast(animator.animatedValue as Int)
        }
      }

      val translationAnimation = ValueAnimator.ofFloat(prevTranslationY, newTranslationY).apply {
        addUpdateListener { animator ->
          this@KurobaBottomPanel.translationY = animator.animatedValue as Float

          attachedFab?.let { fab ->
            initialFabTranslationY?.let { initialY ->
              val deltaTranslationY = animator.animatedValue as Float
              fab.translationY = initialY + deltaTranslationY
            }
          }
        }
      }

      val scaleAnimation = ValueAnimator.ofFloat(prevScale, newScale).apply {
        addUpdateListener { animator ->
          this@KurobaBottomPanel.scaleY = animator.animatedValue as Float
        }
      }

      val boundsChangeAnimatorSet = AnimatorSet().apply {
        playTogether(colorAnimation, translationAnimation, scaleAnimation)
        duration = BOUNDS_CHANGE_ANIMATION_DURATION
        doOnStart {
          this@KurobaBottomPanel.pivotY = 0f
        }
        doOnEnd {
          this@KurobaBottomPanel.scaleY = prevScale
          this@KurobaBottomPanel.pivotY = prevPivotY
          this@KurobaBottomPanel.translationY = prevTranslationY

          panelContainer.updateLayoutParams<FrameLayout.LayoutParams> {
            height = getChildPanelHeight() + lastInsetBottom
          }
          panelContainer.updatePadding(bottom = lastInsetBottom)
        }
      }

      val alphaAnimation = ValueAnimator.ofFloat(0f, 1f).apply {
        duration = ALPHA_ANIMATION_DURATION
        addUpdateListener { animator ->
          childPanel.setAlphaFast(animator.animatedFraction as Float)
        }
      }

      fun onAnimationEnd() {
        stateChangeAnimatorSet = null
        continuation.resumeIfActive(Unit)
      }

      stateChangeAnimatorSet = AnimatorSet().apply {
        playSequentially(boundsChangeAnimatorSet, alphaAnimation)

        interpolator = INTERPOLATOR
        doOnCancel { onAnimationEnd() }
        doOnEnd { onAnimationEnd() }

        start()
      }

      continuation.invokeOnCancellation {
        endStateChangeAnimations()
      }
    }
  }

  private suspend fun bottomNavViewPanelRevealAnimation(
    parentPanel: KurobaBottomPanel,
    childPanel: View,
    fromY: Float,
    toY: Float
  ) {
    endRevealAnimations()

    suspendCancellableCoroutine<Unit> { continuation ->
      val translationAnimation = ValueAnimator.ofFloat(fromY, toY).apply {
        addUpdateListener { animator ->
          parentPanel.translationY(animator.animatedValue as Float)
        }
      }

      val alphaAnimation = ValueAnimator.ofFloat(0f, 1f).apply {
        addUpdateListener { animator ->
          parentPanel.setAlphaFast(animator.animatedValue as Float)
          childPanel.setAlphaFast(animator.animatedValue as Float)
        }
      }

      fun onAnimationEnd() {
        revealAnimatorSet = null
        continuation.resumeIfActive(Unit)
      }

      revealAnimatorSet = AnimatorSet().apply {
        playTogether(translationAnimation, alphaAnimation)
        duration = GENERIC_ANIMATION_DURATION
        interpolator = INTERPOLATOR
        doOnStart {
          childPanel.setVisibilityFast(View.VISIBLE)
          parentPanel.setVisibilityFast(View.VISIBLE)
          childPanel.setAlphaFast(0f)
          parentPanel.setAlphaFast(0f)
        }

        doOnCancel { onAnimationEnd() }
        doOnEnd { onAnimationEnd() }

        start()
      }

      continuation.invokeOnCancellation {
        endRevealAnimations()
      }
    }
  }

  private fun endRevealAnimations() {
    if (revealAnimatorSet != null) {
      revealAnimatorSet?.end()
      revealAnimatorSet = null
    }
  }

  private fun endStateChangeAnimations() {
    if (stateChangeAnimatorSet != null) {
      stateChangeAnimatorSet?.end()
      stateChangeAnimatorSet = null
    }
  }

  private fun endDisappearanceAnimations() {
    if (disappearanceAnimatorSet != null) {
      disappearanceAnimatorSet?.end()
      disappearanceAnimatorSet = null
    }
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
    private const val GENERIC_ANIMATION_DURATION = 250L

    private const val BOUNDS_CHANGE_ANIMATION_DURATION = 175L
    private const val ALPHA_ANIMATION_DURATION = 75L
  }

}