package com.github.k1rakishou.kurobanewnavstacktest.viewcontroller

interface SlideModeFabViewControllerCallbacks {
  fun showFab(lock: Boolean)
  fun hideFab(lock: Boolean)

  fun onSlidingPaneInitialState(isOpened: Boolean)
  fun onSlidingPaneSlidingStarted(wasOpen: Boolean)
  fun onSlidingPaneSlidingEnded(becameOpen: Boolean)
  fun onSlidingPaneSliding(slideOffset: Float)
}