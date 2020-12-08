package com.github.k1rakishou.kurobanewnavstacktest.controller.base

import com.github.k1rakishou.kurobanewnavstacktest.controller.ControllerType

interface ControllerToolbarContract {
  fun setToolbarTitle(controllerType: ControllerType, title: String)
  fun setCatalogToolbarSubTitle(subtitle: String)
}