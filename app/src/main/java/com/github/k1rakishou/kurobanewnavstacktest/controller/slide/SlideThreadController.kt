package com.github.k1rakishou.kurobanewnavstacktest.controller.slide

import android.os.Bundle
import com.github.k1rakishou.kurobanewnavstacktest.base.ControllerTag
import com.github.k1rakishou.kurobanewnavstacktest.controller.FocusableController
import com.github.k1rakishou.kurobanewnavstacktest.controller.base.ThreadController

class SlideThreadController(args: Bundle? = null) : ThreadController(args), FocusableController {

  override fun onLostFocus() {

  }

  override fun onGainedFocus() {

  }

  override fun getControllerTag(): ControllerTag = CONTROLLER_TAG

  companion object {
    val CONTROLLER_TAG = ControllerTag("SlideThreadControllerTag")
  }
}