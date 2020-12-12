package com.github.k1rakishou.kurobanewnavstacktest.viewcontroller.fab

import com.github.k1rakishou.kurobanewnavstacktest.controller.ControllerType
import com.github.k1rakishou.kurobanewnavstacktest.utils.BackgroundUtils
import com.github.k1rakishou.kurobanewnavstacktest.widget.fab.KurobaFloatingActionButton

class SplitFabViewController : FabViewController {
  private lateinit var catalogFab: KurobaFloatingActionButton
  private lateinit var threadFab: KurobaFloatingActionButton
  private var state = State()

  fun initCatalogFab(fab: KurobaFloatingActionButton) {
    this.catalogFab = fab
  }

  fun initThreadFab(fab: KurobaFloatingActionButton) {
    this.threadFab = fab
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

    val searchToolbarShown = state.searchToolbarShown[controllerType]
      ?: false

    val fab = when (controllerType) {
      ControllerType.Catalog -> catalogFab
      ControllerType.Thread -> threadFab
    }

    if (searchToolbarShown) {
      fab.hideFab(lock = true)
      return
    }

    fab.showFab(lock = false)
  }

  class State(
    var searchToolbarShown: MutableMap<ControllerType, Boolean> = mutableMapOf(),
    var controllerFullyLoaded: MutableMap<ControllerType, Boolean> = mutableMapOf()
  )
}