package com.github.k1rakishou.kurobanewnavstacktest.viewcontroller

import com.github.k1rakishou.kurobanewnavstacktest.controller.ControllerType
import com.github.k1rakishou.kurobanewnavstacktest.utils.BackgroundUtils
import com.github.k1rakishou.kurobanewnavstacktest.widget.fab.SlideKurobaFloatingActionButton

class SlideFabViewController {
  private lateinit var fab: SlideKurobaFloatingActionButton
  private var state = State()

  init {
    state.searchToolbarShown[ControllerType.Catalog] = false
    state.searchToolbarShown[ControllerType.Thread] = false

    state.controllerFullyLoaded[ControllerType.Catalog] = false
    state.controllerFullyLoaded[ControllerType.Thread] = false
  }

  fun init(fab: SlideKurobaFloatingActionButton) {
    this.fab = fab
  }

  fun onControllerStateChanged(controllerType: ControllerType, fullyLoaded: Boolean) {
    BackgroundUtils.ensureMainThread()

    val prev = state.controllerFullyLoaded[controllerType]
    if (prev == fullyLoaded) {
      return
    }

    state.controllerFullyLoaded[controllerType] = fullyLoaded
    onStateChanged()
  }

  fun onSearchToolbarShownOrHidden(controllerType: ControllerType, shown: Boolean) {
    BackgroundUtils.ensureMainThread()

    val prev = state.searchToolbarShown[controllerType]
    if (prev == shown) {
      return
    }

    state.searchToolbarShown[controllerType] = shown
    onStateChanged()
  }

  fun onControllerFocused(controllerType: ControllerType) {
    BackgroundUtils.ensureMainThread()

    if (state.currentControllerType == controllerType) {
      return
    }

    state.currentControllerType = controllerType
    onStateChanged()
  }

  private fun onStateChanged() {
    BackgroundUtils.ensureMainThread()

    val currentControllerType = state.currentControllerType
      ?: return

    val searchToolbarShown = state.searchToolbarShown[currentControllerType]
      ?: false

    if (searchToolbarShown) {
      fab.hideFab(lock = true)
      return
    }

    fab.showFab(lock = false)
  }

  class State(
    var searchToolbarShown: MutableMap<ControllerType, Boolean> = mutableMapOf(),
    var controllerFullyLoaded: MutableMap<ControllerType, Boolean> = mutableMapOf(),
    var currentControllerType: ControllerType? = null
  )

}