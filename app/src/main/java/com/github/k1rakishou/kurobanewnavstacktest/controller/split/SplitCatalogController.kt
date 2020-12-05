package com.github.k1rakishou.kurobanewnavstacktest.controller.split

import android.os.Bundle
import com.github.k1rakishou.kurobanewnavstacktest.base.ControllerTag
import com.github.k1rakishou.kurobanewnavstacktest.controller.base.CatalogController
import com.github.k1rakishou.kurobanewnavstacktest.controller.base.UiElementsControllerCallbacks

class SplitCatalogController(args: Bundle? = null) : CatalogController(args) {
  private var uiElementsControllerCallbacks: UiElementsControllerCallbacks? = null

  fun setUiElementsControllerCallbacks(callbacks: UiElementsControllerCallbacks) {
    uiElementsControllerCallbacks = callbacks
  }

  override fun onControllerShown() {
    super.onControllerShown()

    uiElementsControllerCallbacks?.showFab()
  }

  override fun onControllerDestroyed() {
    super.onControllerDestroyed()

    uiElementsControllerCallbacks = null
  }

  override fun getControllerTag(): ControllerTag = CONTROLLER_TAG

  companion object {
    val CONTROLLER_TAG = ControllerTag("SplitCatalogControllerTag")
  }

}