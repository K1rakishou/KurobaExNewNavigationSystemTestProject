package com.github.k1rakishou.kurobanewnavstacktest.viewcontroller

import com.github.k1rakishou.kurobanewnavstacktest.controller.slide.SlideNavController
import com.github.k1rakishou.kurobanewnavstacktest.utils.dp
import com.github.k1rakishou.kurobanewnavstacktest.utils.getBehaviorExt
import com.github.k1rakishou.kurobanewnavstacktest.utils.setBehaviorExt
import com.github.k1rakishou.kurobanewnavstacktest.widget.FabHomeControllerBehavior
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton

class SlideModeFabViewController(
    private val fab: FloatingActionButton,
    private val slideModeFabClickListener: SlideModeFabClickListener
) : SlideModeFabViewControllerCallbacks {
  private var initialHorizontalOffset: Float = FAB_RIGHT_MARGIN_MAX.toFloat()
  private var isCatalogControllerOpened: Boolean = true
  private var fabInitialized = false

  init {
    fab.setBehaviorExt(FabHomeControllerBehavior(fab.context, null))
  }

  fun init(laidOutBottomNavigationView: BottomNavigationView) {
    fab.getBehaviorExt<FabHomeControllerBehavior>()?.init(laidOutBottomNavigationView)
  }

  fun reset() {
    fab.getBehaviorExt<FabHomeControllerBehavior>()?.reset()

    initialHorizontalOffset = FAB_RIGHT_MARGIN_MAX.toFloat()
    fabInitialized = false
  }

  fun onAttach() {
    fab.setOnClickListener {
      if (isCatalogControllerOpened) {
        slideModeFabClickListener.onFabClicked(FabType.CatalogFab)
      } else {
        slideModeFabClickListener.onFabClicked(FabType.ThreadFab)
      }
    }
  }

  fun onDetach() {
    fab.setOnClickListener(null)
  }

  override fun showFab() {
    fab.show()
  }

  override fun hideFab() {
    fab.hide()
  }

  override fun onSlidingPaneInitialState(isOpened: Boolean) {
    isCatalogControllerOpened = isOpened

    if (!fabInitialized) {
      fabInitialized = true

      val translationX = if (isOpened) {
        initialHorizontalOffset + SlideNavController.OVERHANG_SIZE
      } else {
        initialHorizontalOffset
      }

      fab.translationX = -translationX
    }
  }

  override fun onSlidingPaneSlidingStarted(wasOpen: Boolean) {
    fab.isEnabled = false
  }

  override fun onSlidingPaneSlidingEnded(becameOpen: Boolean) {
    fab.isEnabled = true
    isCatalogControllerOpened = becameOpen
  }

  override fun onSlidingPaneSliding(slideOffset: Float) {
    val translationDelta = SlideNavController.OVERHANG_SIZE * slideOffset
    fab.translationX = -(initialHorizontalOffset + translationDelta)
  }

  fun updateFabMarginEnd() {
    fab.translationX = -initialHorizontalOffset
  }

  enum class FabType {
    CatalogFab,
    ThreadFab
  }

  companion object {
    private val FAB_RIGHT_MARGIN_MAX = 16.dp
  }
}