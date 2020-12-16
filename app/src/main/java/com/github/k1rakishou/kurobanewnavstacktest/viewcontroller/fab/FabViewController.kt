package com.github.k1rakishou.kurobanewnavstacktest.viewcontroller.fab

import com.github.k1rakishou.kurobanewnavstacktest.controller.ControllerType
import com.github.k1rakishou.kurobanewnavstacktest.widget.bottom_panel.KurobaBottomPanel

interface FabViewController {
  fun onBottomPanelInitialized(controllerType: ControllerType)
  fun onBottomPanelStateChanged(controllerType: ControllerType, newState: KurobaBottomPanel.State)
  fun onControllerStateChanged(controllerType: ControllerType, fullyLoaded: Boolean)
  fun onSearchToolbarShownOrHidden(controllerType: ControllerType, shown: Boolean)
}