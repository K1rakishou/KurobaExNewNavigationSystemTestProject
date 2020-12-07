package com.github.k1rakishou.kurobanewnavstacktest.viewcontroller

import com.github.k1rakishou.kurobanewnavstacktest.controller.slide.SlideNavController
import com.github.k1rakishou.kurobanewnavstacktest.utils.getBehaviorExt
import com.github.k1rakishou.kurobanewnavstacktest.utils.setBehaviorExt
import com.github.k1rakishou.kurobanewnavstacktest.widget.KurobaFloatingActionButton
import com.github.k1rakishou.kurobanewnavstacktest.widget.behavior.CatalogFabBehavior
import com.google.android.material.bottomnavigation.BottomNavigationView

class SlideModeFabViewController(
    private val fab: KurobaFloatingActionButton,
    private val slideModeFabClickListener: SlideModeFabClickListener
) : SlideModeFabViewControllerCallbacks {
  private var initialHorizontalOffset: Float = KurobaFloatingActionButton.DEFAULT_MARGIN_RIGHT.toFloat()
  private var isCatalogControllerOpened: Boolean = true
  private var fabInitialized = false

  init {
    fab.setBehaviorExt(CatalogFabBehavior(fab.context, null))
  }

  fun init(laidOutBottomNavigationView: BottomNavigationView) {
    fab.getBehaviorExt<CatalogFabBehavior>()?.init(laidOutBottomNavigationView)
  }

  fun reset() {
    fab.getBehaviorExt<CatalogFabBehavior>()?.reset()

    initialHorizontalOffset = KurobaFloatingActionButton.DEFAULT_MARGIN_RIGHT.toFloat()
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
}