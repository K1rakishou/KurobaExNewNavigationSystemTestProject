package com.github.k1rakishou.kurobanewnavstacktest.viewcontroller.fab

import com.github.k1rakishou.kurobanewnavstacktest.controller.ControllerType
import com.github.k1rakishou.kurobanewnavstacktest.utils.BackgroundUtils
import com.github.k1rakishou.kurobanewnavstacktest.widget.bottom_panel.KurobaBottomPanelStateKind
import com.github.k1rakishou.kurobanewnavstacktest.widget.fab.KurobaFloatingActionButton

class SplitFabViewController : FabViewController {
  private lateinit var catalogFab: KurobaFloatingActionButton
  private lateinit var threadFab: KurobaFloatingActionButton
  private var state = State()

  fun setCatalogFab(fab: KurobaFloatingActionButton) {
    this.catalogFab = fab
  }

  fun setThreadFab(fab: KurobaFloatingActionButton) {
    this.threadFab = fab
  }

  override fun onBottomPanelInitialized(controllerType: ControllerType) {
    BackgroundUtils.ensureMainThread()

    val prev = state.bottomPanelInitialized[controllerType] ?: false
    if (prev) {
      return
    }

    state.bottomPanelInitialized[controllerType] = true
    onStateChanged(controllerType)
  }

  override fun onBottomPanelStateChanged(
    controllerType: ControllerType,
    newState: KurobaBottomPanelStateKind
  ) {
    BackgroundUtils.ensureMainThread()

    val prevState = state.bottomPanelState[controllerType]
    if (prevState == newState) {
      return
    }

    state.bottomPanelState[controllerType] = newState
    onStateChanged(controllerType)
  }

  override fun onControllerStateChanged(controllerType: ControllerType, fullyLoaded: Boolean) {
    BackgroundUtils.ensureMainThread()

    val prev = state.controllerFullyLoaded[controllerType]
    if (prev == fullyLoaded) {
      return
    }

    state.controllerFullyLoaded[controllerType] = fullyLoaded
    onStateChanged(controllerType)
  }

  override fun onSearchToolbarShownOrHidden(controllerType: ControllerType, shown: Boolean) {
    BackgroundUtils.ensureMainThread()

    val prev = state.searchToolbarShown[controllerType]
    if (prev == shown) {
      return
    }

    state.searchToolbarShown[controllerType] = shown
    onStateChanged(controllerType)
  }

  private fun onStateChanged(controllerType: ControllerType) {
    BackgroundUtils.ensureMainThread()

    val bottomPanelInitialized = state.bottomPanelInitialized[controllerType]
      ?: false
    val controllerFullyLoaded = state.controllerFullyLoaded[controllerType]
      ?: false
    val bottomPanelState = state.bottomPanelState[controllerType]
      ?: KurobaBottomPanelStateKind.Uninitialized

    val fab = when (controllerType) {
      ControllerType.Catalog -> {
        check(::catalogFab.isInitialized) { "catalogFab is not initialized" }
        catalogFab
      }
      ControllerType.Thread -> {
        check(::threadFab.isInitialized) { "catalogFab is not initialized" }
        threadFab
      }
    }

    if (!bottomPanelInitialized || !controllerFullyLoaded) {
      fab.hideFab(lock = true)
      return
    }

    val searchToolbarShown = state.searchToolbarShown[controllerType]
      ?: false
    val isBottomPanelStateNotOk = bottomPanelState == KurobaBottomPanelStateKind.Uninitialized
      || bottomPanelState == KurobaBottomPanelStateKind.SelectionPanel
      || bottomPanelState == KurobaBottomPanelStateKind.ReplyLayoutPanel

    if (searchToolbarShown || isBottomPanelStateNotOk) {
      fab.hideFab(lock = true)
      return
    }

    fab.showFab(lock = false)
  }

  class State(
    val searchToolbarShown: MutableMap<ControllerType, Boolean> = mutableMapOf(),
    val controllerFullyLoaded: MutableMap<ControllerType, Boolean> = mutableMapOf(),
    val bottomPanelInitialized: MutableMap<ControllerType, Boolean> = mutableMapOf(),
    val bottomPanelState: MutableMap<ControllerType, KurobaBottomPanelStateKind> = mutableMapOf()
  )
}