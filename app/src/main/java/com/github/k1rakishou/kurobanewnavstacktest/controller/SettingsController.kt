package com.github.k1rakishou.kurobanewnavstacktest.controller

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.github.k1rakishou.kurobanewnavstacktest.R
import com.github.k1rakishou.kurobanewnavstacktest.base.BaseController
import com.github.k1rakishou.kurobanewnavstacktest.base.ControllerTag
import com.github.k1rakishou.kurobanewnavstacktest.controller.base.UiElementsControllerCallbacks

// TODO(KurobaEx): title doesn't work
class SettingsController(args: Bundle? = null) : BaseController(args) {
  private var uiElementsControllerCallbacks: UiElementsControllerCallbacks? = null

  fun setUiElementsControllerCallbacks(callbacks: UiElementsControllerCallbacks) {
    uiElementsControllerCallbacks = callbacks
  }

  override fun instantiateView(
      inflater: LayoutInflater,
      container: ViewGroup,
      savedViewState: Bundle?
  ): View {
    return inflater.inflate(R.layout.controller_settings, container, false)
  }

  override fun onControllerShown() {
    super.onControllerShown()

    uiElementsControllerCallbacks?.hideFab()
  }

  override fun onControllerDestroyed() {
    super.onControllerDestroyed()

    uiElementsControllerCallbacks = null
  }

  override fun getControllerTag(): ControllerTag = CONTROLLER_TAG

  companion object {
    val CONTROLLER_TAG = ControllerTag("SettingsControllerTag")
  }
}