package com.github.k1rakishou.kurobanewnavstacktest.widget.bottom_panel

import com.github.k1rakishou.kurobanewnavstacktest.controller.ControllerType

interface ChildPanelContract {
  fun getCurrentHeight(): Int
  fun getBackgroundColor(): Int
  fun enableOrDisableControls(enable: Boolean)
  fun restoreState(bottomPanelViewState: KurobaBottomPanelViewState)
  fun updateCurrentControllerType(controllerType: ControllerType)
  fun handleBack(): Boolean
  fun onDestroy()
  suspend fun updateHeight(parentHeight: Int)
}