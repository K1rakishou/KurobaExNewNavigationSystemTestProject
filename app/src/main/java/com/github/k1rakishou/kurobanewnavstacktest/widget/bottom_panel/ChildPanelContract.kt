package com.github.k1rakishou.kurobanewnavstacktest.widget.bottom_panel

import com.github.k1rakishou.kurobanewnavstacktest.controller.ControllerType

interface ChildPanelContract {
  fun getCurrentHeight(): Int
  fun getBackgroundColor(): Int
  fun enableOrDisableControls(enable: Boolean)

  fun saveState(bottomPanelViewState: KurobaBottomPanelViewState)
  fun restoreState(bottomPanelViewState: KurobaBottomPanelViewState)

  fun updateCurrentControllerType(controllerType: ControllerType)
  suspend fun updateHeight(parentHeight: Int)
}