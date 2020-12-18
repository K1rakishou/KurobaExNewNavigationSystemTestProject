package com.github.k1rakishou.kurobanewnavstacktest.viewcontroller.fab

import com.github.k1rakishou.kurobanewnavstacktest.controller.ControllerType
import com.github.k1rakishou.kurobanewnavstacktest.widget.KurobaBottomPanelStateKind

interface FabViewController {
  fun onBottomPanelInitialized(controllerType: ControllerType)
  fun onBottomPanelStateChanged(controllerType: ControllerType, newState: KurobaBottomPanelStateKind)
  fun onControllerStateChanged(controllerType: ControllerType, fullyLoaded: Boolean)
  fun onSearchToolbarShownOrHidden(controllerType: ControllerType, shown: Boolean)
}