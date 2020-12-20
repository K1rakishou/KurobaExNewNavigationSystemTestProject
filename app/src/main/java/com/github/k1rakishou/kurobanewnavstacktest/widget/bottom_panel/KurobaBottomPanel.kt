package com.github.k1rakishou.kurobanewnavstacktest.widget.bottom_panel

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import androidx.core.view.*
import com.github.k1rakishou.kurobanewnavstacktest.R
import com.github.k1rakishou.kurobanewnavstacktest.activity.MainActivity
import com.github.k1rakishou.kurobanewnavstacktest.controller.ControllerType
import com.github.k1rakishou.kurobanewnavstacktest.controller.base.CollapsableView
import com.github.k1rakishou.kurobanewnavstacktest.core.base.KurobaCoroutineScope
import com.github.k1rakishou.kurobanewnavstacktest.utils.*
import com.github.k1rakishou.kurobanewnavstacktest.widget.animations.PanelAddOrRemoveChildPanelAnimation
import com.github.k1rakishou.kurobanewnavstacktest.widget.animations.PanelDisappearanceAnimation
import com.github.k1rakishou.kurobanewnavstacktest.widget.animations.PanelRemoveAnimation
import com.github.k1rakishou.kurobanewnavstacktest.widget.animations.PanelRevealAnimation
import com.github.k1rakishou.kurobanewnavstacktest.widget.fab.KurobaFloatingActionButton
import com.github.k1rakishou.kurobanewnavstacktest.widget.layout.TouchBlockingFrameLayout
import kotlinx.coroutines.CompletableDeferred
import timber.log.Timber

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
  private val panelAvailableVerticalSpace = mutableMapOf<ControllerType, CompletableDeferred<Int>>()

  private val viewModel by lazy { (context as MainActivity).viewModelStorage(KurobaBottomPanelViewModel::class).value }
  private val scope = KurobaCoroutineScope()
  private val panelContainer: FrameLayout

  private val panelRevealAnimation = PanelRevealAnimation()
  private val panelDisappearanceAnimation = PanelDisappearanceAnimation()
  private val panelRemoveAnimation = PanelRemoveAnimation()
  private val panelAddOrRemoveChildPanelAnimation = PanelAddOrRemoveChildPanelAnimation()

  private val allAnimations = arrayOf(
    panelRevealAnimation,
    panelDisappearanceAnimation,
    panelRemoveAnimation,
    panelAddOrRemoveChildPanelAnimation
  )

  private var lastInsetBottom = 0
  private var controllerType: ControllerType = ControllerType.Catalog
  private var attachedFab: KurobaFloatingActionButton? = null

  private val bottomPanelInitializationListeners = mutableListOf<(ControllerType) -> Unit>()
  private val bottomPanelHeightChangeListeners = mutableListOf<(ControllerType, Int) -> Unit>()
  private val bottomPanelStateUpdatesListeners = mutableListOf<(ControllerType, KurobaBottomPanelStateKind) -> Unit>()
  private val bottomNavPanelSelectedItemListeners = mutableListOf<(KurobaBottomNavPanelSelectedItem) -> Unit>()

  private val viewState
    get() = viewModel.getBottomPanelState(controllerType)

  init {
    log("KurobaBottomPanel init()")

    bottomPanelInitialized[ControllerType.Catalog] = CompletableDeferred()
    bottomPanelInitialized[ControllerType.Thread] = CompletableDeferred()

    panelAvailableVerticalSpace[ControllerType.Catalog] = CompletableDeferred()
    panelAvailableVerticalSpace[ControllerType.Thread] = CompletableDeferred()

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

  fun onControllerFocused(
    controllerType: ControllerType,
    controllerFocusedCallback: () -> Unit = {}
  ) {
    if (this.controllerType == controllerType) {
      controllerFocusedCallback()
      return
    }

    val controllerTypeCopy = controllerType

    scope.launch {
      if (controllerTypeCopy != controllerType) {
        controllerFocusedCallback()
        return@launch
      }

      log("onControllerFocused controllerType=$controllerType")
      allAnimations.forEach { animation -> animation.endAnimationAndAwait() }

      this@KurobaBottomPanel.controllerType = controllerType
      val currentPanelStateKind = viewState.panelCurrentStateKind

      bottomPanelInitialized[controllerType]!!.await()
      panelAvailableVerticalSpace[controllerType]!!.await()

      switchIntoInternal(currentPanelStateKind)
      controllerFocusedCallback()
    }
  }

  fun switchInto(newState: KurobaBottomPanelStateKind) {
    if (newState == viewState.panelCurrentStateKind) {
      return
    }

    val newStateCopy = newState

    scope.launch {
      if (newState != newStateCopy) {
        return@launch
      }

      log("switchInto newState=$newState")

      bottomPanelInitialized[controllerType]!!.await()
      panelAvailableVerticalSpace[controllerType]!!.await()

      if (newState == KurobaBottomPanelStateKind.Hidden) {
        hidePanel()
      } else {
        switchIntoInternal(newState)
      }
    }
  }

  fun onPanelAvailableVerticalSpaceKnown(controllerType: ControllerType, recyclerViewHeight: Int) {
    this.panelAvailableVerticalSpace[controllerType]!!.complete(recyclerViewHeight)
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
        panelAvailableVerticalSpace[controllerType]!!.await()

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

  fun addOnBottomPanelHeightChangeListener(listener: (ControllerType, Int) -> Unit) {
    bottomPanelHeightChangeListeners += listener
  }

  fun getBottomPanelHeight(controllerType: ControllerType): Int? {
    if (this.controllerType != controllerType) {
      return null
    }

    return getCurrentChildPanelOrNull()?.getCurrentHeight()
  }

  fun cleanup() {
    allAnimations.forEach { animation -> animation.endAnimation() }

    scope.cancelChildren()
    destroyChildPanel()

    bottomPanelInitializationListeners.clear()
    bottomPanelStateUpdatesListeners.clear()
    bottomNavPanelSelectedItemListeners.clear()
    bottomPanelHeightChangeListeners.clear()

    attachedFab = null
  }

  fun onBackPressed(): Boolean {
    val hasRunningAnimations = allAnimations.any { animation -> animation.isRunning() }
    if (hasRunningAnimations) {
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

    val prevHeight = childPanel.getCurrentHeight().coerceAtLeast(1)

    panelDisappearanceAnimation.disappearanceAnimation(
      this,
      panelContainer,
      lastInsetBottom,
      attachedFab,
      translationY,
      prevHeight,
      0
    )

    destroyChildPanel()

    val kurobaBottomHiddenPanel = KurobaBottomHiddenPanel(context)
    kurobaBottomHiddenPanel.initializeView()

    panelContainer.addView(kurobaBottomHiddenPanel)

    bottomPanelStateUpdatesListeners.forEach { func ->
      func(controllerType, newState)
    }
    bottomPanelHeightChangeListeners.forEach { func ->
      func(controllerType, kurobaBottomHiddenPanel.getCurrentHeight())
    }

    viewState.panelCurrentStateKind = newState

    childPanel.enableOrDisableControls(enable = true)
  }

  suspend fun switchIntoInternal(newStateKind: KurobaBottomPanelStateKind) {
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

    bottomPanelStateUpdatesListeners.forEach { func ->
      func(controllerType, newStateKind)
    }
    bottomPanelHeightChangeListeners.forEach { func ->
      val addedChildPanel = panelContainer.getChildAtOrNull(0) as ChildPanelContract
      func(controllerType, addedChildPanel.getCurrentHeight())
    }

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
    val prevHeight = getCurrentChildPanelOrNull()?.getCurrentHeight()?.coerceAtLeast(1) ?: 0

    panelRemoveAnimation.removeOldChildPanelWithAnimation(
      panelContainer,
      destroyPanelFunc = {
        destroyChildPanel()
      }
    )

    val childPanel = instantiateAndInitializeChildPanel(newStateKind)
    childPanel as ChildPanelContract
    disableControlsFunc(childPanel)

    val newColor = childPanel.getBackgroundColor()
    val newHeight = childPanel.getCurrentHeight()

    panelAddOrRemoveChildPanelAnimation.addNewChildPanelWithAnimation(
      this,
      panelContainer,
      attachedFab,
      childPanel,
      lastInsetBottom,
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

  private suspend fun instantiateAndInitializeChildPanel(stateKind: KurobaBottomPanelStateKind): View {
    val childPanel = when (stateKind) {
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
        val availableVerticalSpace = panelAvailableVerticalSpace[controllerType]!!.getCompleted()

        KurobaBottomReplyPanel(
          context,
          controllerType,
          availableVerticalSpace,
          viewModel,
          this
        )
      }
      KurobaBottomPanelStateKind.SelectionPanel -> TODO()
    }

    childPanel.initializeView()

    return childPanel
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
    bottomPanelHeightChangeListeners.forEach { func ->
      func(controllerType, childPanelContract.getCurrentHeight())
    }

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
    kurobaBottomHiddenPanel.initializeView()

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
    kurobaBottomNavPanel.initializeView()

    panelContainer.addView(kurobaBottomNavPanel)
    panelContainer.setBackgroundColorFast(kurobaBottomNavPanel.getBackgroundColor())
    setBackgroundColorFast(kurobaBottomNavPanel.getBackgroundColor())

    kurobaBottomNavPanel.enableOrDisableControls(enable = false)
    kurobaBottomNavPanel.select(KurobaBottomNavPanelSelectedItem.Browse)
    kurobaBottomNavPanel.setVisibilityFast(View.INVISIBLE)

    panelContainer.requestLayoutAndAwait()

    updateContainerPaddings(panelContainer, lastInsetBottom)
    updateFab((panelContainer.height).toFloat())

    panelRevealAnimation.bottomNavViewPanelRevealAnimation(
      this,
      kurobaBottomNavPanel,
      translationY,
      0f
    )

    return kurobaBottomNavPanel
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
  }

}