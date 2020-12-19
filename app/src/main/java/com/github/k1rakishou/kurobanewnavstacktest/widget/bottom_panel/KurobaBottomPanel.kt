package com.github.k1rakishou.kurobanewnavstacktest.widget.bottom_panel

import android.animation.AnimatorSet
import android.animation.ValueAnimator
import android.annotation.SuppressLint
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
import com.github.k1rakishou.kurobanewnavstacktest.activity.MainActivity
import com.github.k1rakishou.kurobanewnavstacktest.base.KurobaCoroutineScope
import com.github.k1rakishou.kurobanewnavstacktest.controller.ControllerType
import com.github.k1rakishou.kurobanewnavstacktest.controller.base.CollapsableView
import com.github.k1rakishou.kurobanewnavstacktest.utils.*
import com.github.k1rakishou.kurobanewnavstacktest.widget.KurobaBottomPanelStateKind
import com.github.k1rakishou.kurobanewnavstacktest.widget.layout.TouchBlockingFrameLayout
import com.github.k1rakishou.kurobanewnavstacktest.widget.fab.KurobaFloatingActionButton
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.suspendCancellableCoroutine
import timber.log.Timber
import java.lang.IllegalStateException

@SuppressLint("BinaryOperationInTimber")
class KurobaBottomPanel @JvmOverloads constructor(
  context: Context,
  attributeSet: AttributeSet? = null,
  attrDefStyle: Int = 0
) : TouchBlockingFrameLayout(context, attributeSet, attrDefStyle),
  CollapsableView,
  KurobaBottomNavPanel.KurobaBottomPanelCallbacks,
  KurobaBottomReplyPanel.KurobaBottomPanelCallbacks {

  private val insetBottomDeferredInitial = CompletableDeferred<Int>()
  private val bottomPanelInitialized = mutableMapOf<ControllerType, CompletableDeferred<Unit>>()
  private val recyclerViewHeight = mutableMapOf<ControllerType, CompletableDeferred<Int>>()

  private val viewModel by lazy { (context as MainActivity).viewModelStorage(KurobaBottomPanelViewModel::class).value }
  private val scope = KurobaCoroutineScope()
  private val panelContainer: FrameLayout

  private var lastInsetBottom = 0
  private var revealAnimatorSet: AnimatorSet? = null
  private var stateChangeAnimatorSet: AnimatorSet? = null
  private var disappearanceAnimatorSet: AnimatorSet? = null
  private var attachedFab: KurobaFloatingActionButton? = null
  private var controllerType: ControllerType = ControllerType.Catalog

  private val bottomPanelInitializationListeners = mutableListOf<(ControllerType) -> Unit>()
  private val bottomPanelHeightChangeListeners = mutableListOf<(ControllerType, Int, Boolean) -> Unit>() // TODO(KurobaEx):
  private val bottomPanelStateUpdatesListeners = mutableListOf<(ControllerType, KurobaBottomPanelStateKind) -> Unit>()
  private val bottomNavPanelSelectedItemListeners = mutableListOf<(KurobaBottomNavPanelSelectedItem) -> Unit>()

  private val viewState
    get() = viewModel.getBottomPanelState(controllerType)

  init {
    log("KurobaBottomPanel init()")

    bottomPanelInitialized[ControllerType.Catalog] = CompletableDeferred()
    bottomPanelInitialized[ControllerType.Thread] = CompletableDeferred()

    recyclerViewHeight[ControllerType.Catalog] = CompletableDeferred()
    recyclerViewHeight[ControllerType.Thread] = CompletableDeferred()

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
    val childPanel = getCurrentChildPanelOrNull()
      ?: return

    v.updateLayoutParams<FrameLayout.LayoutParams> {
      height = childPanel.getCurrentHeight() + insetBottom
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
    if (height == 0 || attachedFab == null) {
      return
    }

    var scaleTranslationY = newTranslationY

    if (scaleTranslationY > height) {
      scaleTranslationY = height.toFloat()
    } else if (scaleTranslationY < 0) {
      scaleTranslationY = 0f
    }

    val scale = 1f - (scaleTranslationY / height)

    attachedFab?.setScalePreInitialized(scale, scale)
    attachedFab?.translationY = -height.toFloat()
  }

  private fun canUpdateFab(): Boolean {
    return height > 0
      && attachedFab != null
      && viewState.currentStateAllowsFabUpdate()
  }

  override fun height(): Float {
    return height.toFloat()
  }

  override fun translationY(): Float {
    return translationY
  }

  override fun onItemSelected(selectedItem: KurobaBottomNavPanelSelectedItem) {
    bottomNavPanelSelectedItemListeners.forEach { listener -> listener(selectedItem) }
  }

  override suspend fun updateParentPanelHeight(newHeight: Int) {
    panelContainer.updateLayoutParams<FrameLayout.LayoutParams> {
      height = newHeight + lastInsetBottom
    }
    panelContainer.updatePadding(bottom = lastInsetBottom)
    panelContainer.requestLayoutAndAwait()
  }

  fun isBottomPanelInitialized(controllerType: ControllerType): Boolean {
    return bottomPanelInitialized[controllerType]!!.isCompleted
  }

  fun onControllerFocused(controllerType: ControllerType) {
    if (this.controllerType == controllerType) {
      return
    }

    log("onControllerFocused controllerType=$controllerType")

    this.controllerType = controllerType
    val currentPanelStateKind = viewState.panelCurrentStateKind

    scope.launch {
      bottomPanelInitialized[controllerType]!!.await()
      recyclerViewHeight[controllerType]!!.await()

      switchIntoInternal(currentPanelStateKind)
    }
  }

  fun switchInto(newState: KurobaBottomPanelStateKind) {
    if (newState == viewState.panelCurrentStateKind) {
      return
    }

    log("switchInto newState=$newState")

    scope.launch {
      bottomPanelInitialized[controllerType]!!.await()
      recyclerViewHeight[controllerType]!!.await()

      if (newState == KurobaBottomPanelStateKind.Hidden) {
        hidePanel()
      } else {
        switchIntoInternal(newState)
      }
    }
  }

  fun onRecyclerViewHeightKnown(controllerType: ControllerType, recyclerViewHeight: Int) {
    this.recyclerViewHeight[controllerType]!!.complete(recyclerViewHeight)
  }

  fun bottomPanelPreparationsCompleted(
    controllerType: ControllerType,
    initialState: KurobaBottomPanelStateKind
  ) {
    this.controllerType = controllerType
    check(bottomPanelInitialized[controllerType]!!.isCompleted.not()) { "Already initialized" }

    log("bottomPanelPreparationsCompleted controllerType=$controllerType, initialState=$initialState")

    scope.launch {
      viewState.panelInitialStateKind = initialState
      val prevState = viewState.panelCurrentStateKind

      insetBottomDeferredInitial.await()

      if (prevState == KurobaBottomPanelStateKind.Uninitialized) {
        initBottomPanel(controllerType, initialState)
        bottomPanelInitialized[controllerType]!!.complete(Unit)
      } else {
        recyclerViewHeight[controllerType]!!.await()

        if (viewState.panelCurrentStateKind != KurobaBottomPanelStateKind.Hidden) {
          switchIntoInternal(viewState.panelCurrentStateKind)
        }
      }

      bottomPanelInitialized[controllerType]!!.complete(Unit)
    }
  }

  fun attachFab(fab: KurobaFloatingActionButton) {
    check(attachedFab == null) { "Already attached" }

    this.attachedFab = fab
  }

  fun addOnBottomPanelInitialized(func: (ControllerType) -> Unit) {
    if (viewState.panelCurrentStateKind != KurobaBottomPanelStateKind.Uninitialized) {
      func(controllerType)

      bottomPanelStateUpdatesListeners.forEach { listener ->
        listener(controllerType, viewState.panelCurrentStateKind)
      }
      return
    }

    bottomPanelInitializationListeners += func
  }

  fun addOnBottomPanelStateChanged(func: (ControllerType, KurobaBottomPanelStateKind) -> Unit) {
    bottomPanelStateUpdatesListeners += func
  }

  fun addOnBottomNavPanelItemSelectedListener(listener: (KurobaBottomNavPanelSelectedItem) -> Unit) {
    bottomNavPanelSelectedItemListeners += listener
  }

  fun addOnBottomPanelHeightChangeListener(listener: (ControllerType, Int, Boolean) -> Unit) {
    bottomPanelHeightChangeListeners += listener
  }

  fun cleanup() {
    endRevealAnimations()
    endStateChangeAnimations()
    endDisappearanceAnimations()

    scope.cancelChildren()
    destroyChildPanel()

    bottomPanelInitializationListeners.clear()
    bottomPanelStateUpdatesListeners.clear()
    bottomNavPanelSelectedItemListeners.clear()
    bottomPanelHeightChangeListeners.clear()

    attachedFab = null
  }

  fun onBackPressed(): Boolean {
    if (revealAnimatorSet != null
      || stateChangeAnimatorSet != null
      || disappearanceAnimatorSet != null) {
      // If any of the animations is in progress, consume the click and do nothing
      return true
    }

    val state = viewState.panelCurrentStateKind
    val initialState = viewState.panelInitialStateKind

    if (state == KurobaBottomPanelStateKind.Uninitialized
      || initialState == KurobaBottomPanelStateKind.Uninitialized
      || state == initialState) {
      return false
    }

    val childPanel = panelContainer.getChildAtOrNull(0) as? ChildPanelContract
    if (childPanel != null && childPanel.handleBack()) {
      return true
    }

    if (initialState != KurobaBottomPanelStateKind.Uninitialized) {
      switchInto(initialState)
      return true
    }

    return false
  }

  private fun destroyChildPanel() {
    (panelContainer.getChildAtOrNull(0) as? ChildPanelContract)?.onDestroy()
    panelContainer.removeAllViews()
  }

  private suspend fun hidePanel() {
    val newState = KurobaBottomPanelStateKind.Hidden

    if (viewState.panelCurrentStateKind == newState) {
      return
    }

    val childPanel = panelContainer.getChildAtOrNull(0)
      ?: return

    childPanel as ChildPanelContract
    childPanel.enableOrDisableControls(enable = false)

    var prevHeight = childPanel.getCurrentHeight()
    if (prevHeight == 0) {
      prevHeight = 1
    }

    disappearanceAnimation(
      panelContainer,
      prevHeight,
      0
    )

    destroyChildPanel()

    val kurobaBottomHiddenPanel = KurobaBottomHiddenPanel(context)
    panelContainer.addView(kurobaBottomHiddenPanel)

    bottomPanelStateUpdatesListeners.forEach { func -> func(controllerType, newState) }
    viewState.panelCurrentStateKind = newState

    childPanel.enableOrDisableControls(enable = true)
  }

  private suspend fun switchIntoInternal(newStateKind: KurobaBottomPanelStateKind) {
    val currentChildPanel = panelContainer.getChildAtOrNull(0) as? ChildPanelContract
    val isTheSamePanel = isTheSamePanel(newStateKind, currentChildPanel)

    log("switchIntoInternal newStateKind=$newStateKind, " +
      "currentChildPanel=${currentChildPanel?.javaClass?.simpleName}, " +
      "isTheSamePanel=${isTheSamePanel}")

    if (currentChildPanel == null || !isTheSamePanel) {
      addOrReplaceChildPanel(
        newStateKind,
        disableControlsFunc = { childPanelContract ->
          childPanelContract.enableOrDisableControls(enable = false)
        },
        restoreChildPanelStateFunc = { childPanelContract ->
          childPanelContract.restoreState(viewState)
          childPanelContract.updateCurrentControllerType(controllerType)
        },
        enableControlsFunc = { childPanelContract ->
          childPanelContract.enableOrDisableControls(enable = true)
        })
    } else {
      currentChildPanel.restoreState(viewState)
      currentChildPanel.updateCurrentControllerType(controllerType)
    }

    bottomPanelStateUpdatesListeners.forEach { func -> func(controllerType, newStateKind) }
    viewState.panelCurrentStateKind = newStateKind
  }

  private fun isTheSamePanel(
    newStateKind: KurobaBottomPanelStateKind,
    currentChildPanel: ChildPanelContract?
  ): Boolean {
    return when (newStateKind) {
      KurobaBottomPanelStateKind.Uninitialized -> {
        throw IllegalStateException("Cannot be used as newStateKind")
      }
      KurobaBottomPanelStateKind.Hidden -> {
        throw IllegalStateException("Cannot be used in switchIntoInternal, use hidePanel instead")
      }
      KurobaBottomPanelStateKind.BottomNavPanel -> currentChildPanel is KurobaBottomNavPanel
      KurobaBottomPanelStateKind.ReplyLayoutPanel -> currentChildPanel is KurobaBottomReplyPanel
      KurobaBottomPanelStateKind.SelectionPanel -> TODO()
    }
  }

  private suspend fun addOrReplaceChildPanel(
    newStateKind: KurobaBottomPanelStateKind,
    disableControlsFunc: (ChildPanelContract) -> Unit,
    restoreChildPanelStateFunc: (ChildPanelContract) -> Unit,
    enableControlsFunc: (ChildPanelContract) -> Unit,
  ) {
    log("addOrReplaceChildPanel newStateKind=${newStateKind}")

    val prevColor = getCurrentChildPanelOrNull()?.getBackgroundColor()
    var prevHeight = getCurrentChildPanelOrNull()?.getCurrentHeight() ?: 0

    if (prevHeight == 0) {
      prevHeight = 1
    }

    destroyChildPanel()

    val childPanel = instantiateChildPanel(newStateKind)
    childPanel as ChildPanelContract

    disableControlsFunc(childPanel)

    val newColor = childPanel.getBackgroundColor()
    val newHeight = childPanel.getCurrentHeight()

    stateChangeAnimation(
      panelContainer,
      childPanel,
      prevColor ?: newColor,
      newColor,
      prevHeight,
      newHeight,
      doBeforeViewVisible = { panel ->
        panel as ChildPanelContract

        initializeChildPanel(newStateKind, panel)
        panel.updateHeight(panelContainer.height)
        updateFab((panelContainer.height).toFloat())

        restoreChildPanelStateFunc(panel)
      })

    enableControlsFunc(childPanel)
  }

  private fun initializeChildPanel(stateKind: KurobaBottomPanelStateKind, childPanel: View) {
    when (stateKind) {
      KurobaBottomPanelStateKind.Uninitialized -> {
        throw IllegalStateException("Cannot be used as newStateKind")
      }
      KurobaBottomPanelStateKind.Hidden -> {
        throw IllegalStateException("Cannot be used in switchIntoInternal, use hidePanel instead")
      }
      KurobaBottomPanelStateKind.BottomNavPanel -> {
        childPanel as KurobaBottomNavPanel

        childPanel.setVisibilityFast(INVISIBLE)
        panelContainer.addView(childPanel)
        childPanel.select(KurobaBottomNavPanelSelectedItem.Browse)
      }
      KurobaBottomPanelStateKind.ReplyLayoutPanel -> {
        childPanel as KurobaBottomReplyPanel

        childPanel.setVisibilityFast(INVISIBLE)
        panelContainer.addView(childPanel)
      }
      KurobaBottomPanelStateKind.SelectionPanel -> TODO()
    }
  }

  private fun instantiateChildPanel(stateKind: KurobaBottomPanelStateKind): View {
    return when (stateKind) {
      KurobaBottomPanelStateKind.Uninitialized -> {
        throw IllegalStateException("Cannot be used as newStateKind")
      }
      KurobaBottomPanelStateKind.Hidden -> {
        throw IllegalStateException("Cannot be used in switchIntoInternal, use hidePanel instead")
      }
      KurobaBottomPanelStateKind.BottomNavPanel -> {
        KurobaBottomNavPanel(
          context,
          controllerType,
          viewModel,
          this
        )
      }
      KurobaBottomPanelStateKind.ReplyLayoutPanel -> {
        val recyclerViewHeight = recyclerViewHeight[controllerType]!!.getCompleted()

        return KurobaBottomReplyPanel(
          context,
          controllerType,
          recyclerViewHeight,
          viewModel,
          this
        )
      }
      KurobaBottomPanelStateKind.SelectionPanel -> TODO()
    }
  }

  private suspend fun initBottomPanel(
    controllerType: ControllerType,
    initialStateKind: KurobaBottomPanelStateKind
  ) {
    if (viewState.panelCurrentStateKind == initialStateKind) {
      return
    }

    log("initBottomPanel controllerType=$controllerType, initialStateKind=$initialStateKind")

    val childPanelContract = when (initialStateKind) {
      KurobaBottomPanelStateKind.BottomNavPanel -> initBottomNavViewPanel()
      KurobaBottomPanelStateKind.Hidden -> initBottomHiddenPanel()
      else -> throw NotImplementedError("Not implemented for state: $initialStateKind")
    }

    bottomPanelInitializationListeners.forEach { func -> func(controllerType) }
    bottomPanelInitializationListeners.clear()

    bottomPanelStateUpdatesListeners.forEach { func -> func(controllerType, initialStateKind) }

    viewState.panelCurrentStateKind = initialStateKind
    childPanelContract.enableOrDisableControls(enable = true)
  }

  private suspend fun initBottomHiddenPanel(): ChildPanelContract {
    val currentChildPanel = panelContainer.getChildAtOrNull(0)
    if (currentChildPanel is KurobaBottomHiddenPanel) {
      return currentChildPanel
    }

    log("initBottomHiddenPanel")
    destroyChildPanel()

    val kurobaBottomHiddenPanel = KurobaBottomHiddenPanel(context)
    panelContainer.addView(kurobaBottomHiddenPanel)
    panelContainer.setBackgroundColorFast(kurobaBottomHiddenPanel.getBackgroundColor())
    setBackgroundColorFast(kurobaBottomHiddenPanel.getBackgroundColor())

    kurobaBottomHiddenPanel.enableOrDisableControls(enable = false)

    panelContainer.requestLayoutAndAwait()

    updateContainerPaddings(panelContainer, lastInsetBottom)
    updateFab((panelContainer.height).toFloat())

    return kurobaBottomHiddenPanel
  }

  private suspend fun initBottomNavViewPanel(): ChildPanelContract {
    val currentChildPanel = panelContainer.getChildAtOrNull(0)
    if (currentChildPanel is KurobaBottomNavPanel) {
      return currentChildPanel
    }

    log("initBottomNavViewPanel")
    destroyChildPanel()

    val kurobaBottomNavPanel = KurobaBottomNavPanel(context, controllerType, viewModel, this)
    panelContainer.addView(kurobaBottomNavPanel)
    panelContainer.setBackgroundColorFast(kurobaBottomNavPanel.getBackgroundColor())
    setBackgroundColorFast(kurobaBottomNavPanel.getBackgroundColor())

    kurobaBottomNavPanel.enableOrDisableControls(enable = false)
    kurobaBottomNavPanel.select(KurobaBottomNavPanelSelectedItem.Browse)
    kurobaBottomNavPanel.setVisibilityFast(View.INVISIBLE)

    panelContainer.requestLayoutAndAwait()

    updateContainerPaddings(panelContainer, lastInsetBottom)
    updateFab((panelContainer.height).toFloat())

    bottomNavViewPanelRevealAnimation(this, kurobaBottomNavPanel, translationY, 0f)
    return kurobaBottomNavPanel
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
    newHeight: Int,
    doBeforeViewVisible: suspend (View) -> Unit
  ) {
    endStateChangeAnimations()
    childPanel as ChildPanelContract

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
            height = childPanel.getCurrentHeight() + lastInsetBottom
          }
          panelContainer.updatePadding(bottom = lastInsetBottom)
        }
      }

      fun onAnimationEnd() {
        stateChangeAnimatorSet = null
        continuation.resumeIfActive(Unit)
      }

      stateChangeAnimatorSet = AnimatorSet().apply {
        play(boundsChangeAnimatorSet)

        interpolator = INTERPOLATOR
        doOnCancel { onAnimationEnd() }
        doOnEnd { onAnimationEnd() }

        start()
      }

      continuation.invokeOnCancellation {
        endStateChangeAnimations()
      }
    }

    doBeforeViewVisible(childPanel)

    suspendCancellableCoroutine<Unit> { continuation ->
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
        play(alphaAnimation)

        interpolator = INTERPOLATOR
        doOnStart {
          childPanel.setAlphaFast(0f)
          childPanel.setVisibilityFast(View.VISIBLE)
        }
        doOnCancel { onAnimationEnd() }
        doOnEnd { onAnimationEnd() }

        start()
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

  private fun getCurrentChildPanelOrNull(): ChildPanelContract? {
    val childView = panelContainer.getChildAtOrNull(0)
      ?: return null

    return childView as ChildPanelContract
  }

  private fun log(message: String) {
    Timber.tag(TAG).d(message)
  }

  companion object {
    private const val TAG = "KurobaBottomPanel"

    private val INTERPOLATOR = FastOutSlowInInterpolator()
    private const val GENERIC_ANIMATION_DURATION = 250L

    private const val BOUNDS_CHANGE_ANIMATION_DURATION = 175L
    private const val ALPHA_ANIMATION_DURATION = 75L
  }

}