package com.github.k1rakishou.kurobanewnavstacktest.widget.toolbar

import android.view.View
import com.github.k1rakishou.kurobanewnavstacktest.controller.ControllerType

interface ToolbarContract {
  fun collapsableView(): View
  fun setToolbarVisibility(visibility: Int)

  fun setTitle(controllerType: ControllerType, title: String)
}