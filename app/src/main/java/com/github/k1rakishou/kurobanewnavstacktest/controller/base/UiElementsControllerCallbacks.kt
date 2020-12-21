package com.github.k1rakishou.kurobanewnavstacktest.controller.base

import com.github.k1rakishou.kurobanewnavstacktest.controller.ControllerType
import com.github.k1rakishou.kurobanewnavstacktest.widget.bottom_panel.KurobaBottomPanelStateKind

interface UiElementsControllerCallbacks {
  fun toolbarSearchVisibilityChanged(controllerType: ControllerType, toolbarSearchVisible: Boolean)
  fun onBottomPanelStateChanged(controllerType: ControllerType, stateKind: KurobaBottomPanelStateKind)

  fun showFab(lock: Boolean? = null)
  fun hideFab(lock: Boolean? = null)
}