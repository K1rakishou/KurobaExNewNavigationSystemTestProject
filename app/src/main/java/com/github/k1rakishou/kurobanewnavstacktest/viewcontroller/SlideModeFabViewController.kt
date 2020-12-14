package com.github.k1rakishou.kurobanewnavstacktest.viewcontroller

import com.github.k1rakishou.kurobanewnavstacktest.controller.ControllerType
import com.github.k1rakishou.kurobanewnavstacktest.utils.ChanSettings
import com.github.k1rakishou.kurobanewnavstacktest.utils.getBehaviorExt
import com.github.k1rakishou.kurobanewnavstacktest.utils.setBehaviorExt
import com.github.k1rakishou.kurobanewnavstacktest.utils.setOnThrottlingClickListener
import com.github.k1rakishou.kurobanewnavstacktest.widget.behavior.CatalogFabBehavior
import com.github.k1rakishou.kurobanewnavstacktest.widget.fab.KurobaFloatingActionButton
import com.github.k1rakishou.kurobanewnavstacktest.widget.fab.SlideKurobaFloatingActionButton

class SlideModeFabViewController(
  private val fab: SlideKurobaFloatingActionButton,
  private val slideModeFabClickListener: SlideModeFabClickListener
) : SlideModeFabViewControllerCallbacks {
  private var initialHorizontalOffset: Float = KurobaFloatingActionButton.DEFAULT_MARGIN_RIGHT.toFloat()
  private var focusedControllerType: ControllerType? = null
  private var fabInitialized = false

  init {
    fab.setBehaviorExt(CatalogFabBehavior(fab.context, null))
  }

  private fun updateFocusedControllerType(isOpened: Boolean? = null) {
    if (isOpened == null) {
      focusedControllerType = null
      fab.onControllerFocused(null)
      return
    }

    val controllerType = if (isOpened) {
      ControllerType.Catalog
    } else {
      ControllerType.Thread
    }

    focusedControllerType = controllerType
    fab.onControllerFocused(controllerType)
  }

  fun reset() {
    fab.getBehaviorExt<CatalogFabBehavior>()?.reset()

    initialHorizontalOffset = KurobaFloatingActionButton.DEFAULT_MARGIN_RIGHT.toFloat()
    fabInitialized = false
  }

  fun onAttach() {
    fab.setOnThrottlingClickListener {
      val controllerType = focusedControllerType
        ?: return@setOnThrottlingClickListener

      if (controllerType == ControllerType.Catalog) {
        slideModeFabClickListener.onFabClicked(FabType.CatalogFab)
      } else {
        slideModeFabClickListener.onFabClicked(FabType.ThreadFab)
      }
    }
  }

  fun onDetach() {
    fab.setOnThrottlingClickListener(null)
  }

  override fun showFab(lock: Boolean) {
    if (focusedControllerType == null) {
      return
    }

    fab.showFab(lock)
  }

  override fun hideFab(lock: Boolean) {
    if (focusedControllerType == null) {
      return
    }

    fab.hideFab(lock)
  }

  override fun onSlidingPaneInitialState(isOpened: Boolean) {
    updateFocusedControllerType(isOpened)

    if (!fabInitialized) {
      fabInitialized = true

      val translationX = if (isOpened) {
        initialHorizontalOffset + ChanSettings.OVERHANG_SIZE
      } else {
        initialHorizontalOffset
      }

      fab.translationX = -translationX
    }
  }

  override fun onSlidingPaneSlidingStarted(wasOpen: Boolean) {
    fab.isEnabled = false
  }

  override fun onSlidingPaneSliding(slideOffset: Float) {
    updateFocusedControllerType(null)

    val translationDelta = ChanSettings.OVERHANG_SIZE * slideOffset
    fab.translationX = -(initialHorizontalOffset + translationDelta)
  }

  override fun onSlidingPaneSlidingEnded(becameOpen: Boolean) {
    fab.isEnabled = true

    updateFocusedControllerType(becameOpen)
  }

  enum class FabType {
    CatalogFab,
    ThreadFab
  }
}