package com.github.k1rakishou.kurobanewnavstacktest.controller.split

import android.os.Bundle
import com.github.k1rakishou.kurobanewnavstacktest.base.ControllerTag
import com.github.k1rakishou.kurobanewnavstacktest.controller.base.ThreadController

class SplitThreadController(
  args: Bundle? = null
) : ThreadController(args) {

  override fun getControllerTag(): ControllerTag = CONTROLLER_TAG

  companion object {
    val CONTROLLER_TAG = ControllerTag("SplitThreadControllerTag")
  }

}