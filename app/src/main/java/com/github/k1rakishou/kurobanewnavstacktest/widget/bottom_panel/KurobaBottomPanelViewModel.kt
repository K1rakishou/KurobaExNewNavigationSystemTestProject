package com.github.k1rakishou.kurobanewnavstacktest.widget.bottom_panel

import com.github.k1rakishou.kurobanewnavstacktest.core.base.BaseViewModel
import com.github.k1rakishou.kurobanewnavstacktest.controller.ControllerType

class KurobaBottomPanelViewModel : BaseViewModel() {
  private val kurobaBottomPanelViewState = mutableMapOf<ControllerType, KurobaBottomPanelViewState>()

  init {
    kurobaBottomPanelViewState[ControllerType.Catalog] = KurobaBottomPanelViewState()
    kurobaBottomPanelViewState[ControllerType.Thread] = KurobaBottomPanelViewState()
  }

  fun getBottomPanelState(controllerType: ControllerType): KurobaBottomPanelViewState {
    return kurobaBottomPanelViewState[controllerType]!!
  }

}