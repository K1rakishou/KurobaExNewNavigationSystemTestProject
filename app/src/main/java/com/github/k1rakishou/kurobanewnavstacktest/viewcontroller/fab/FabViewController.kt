package com.github.k1rakishou.kurobanewnavstacktest.viewcontroller.fab

import com.github.k1rakishou.kurobanewnavstacktest.controller.ControllerType

interface FabViewController {
  fun onBottomPanelInitialized(controllerType: ControllerType)
  fun onControllerStateChanged(controllerType: ControllerType, fullyLoaded: Boolean)
  fun onSearchToolbarShownOrHidden(controllerType: ControllerType, shown: Boolean)
}