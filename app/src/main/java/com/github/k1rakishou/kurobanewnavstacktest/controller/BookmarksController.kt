package com.github.k1rakishou.kurobanewnavstacktest.controller

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.github.k1rakishou.kurobanewnavstacktest.R
import com.github.k1rakishou.kurobanewnavstacktest.base.BaseController
import com.github.k1rakishou.kurobanewnavstacktest.base.ControllerTag
import com.github.k1rakishou.kurobanewnavstacktest.controller.base.ControllerToolbarContract
import com.github.k1rakishou.kurobanewnavstacktest.controller.base.UiElementsControllerCallbacks

class BookmarksController(args: Bundle? = null) : BaseController(args), ControllerToolbarContract {
  private var uiElementsControllerCallbacks: UiElementsControllerCallbacks? = null

  fun setUiElementsControllerCallbacks(callbacks: UiElementsControllerCallbacks) {
    uiElementsControllerCallbacks = callbacks
  }

  override fun instantiateView(
      inflater: LayoutInflater,
      container: ViewGroup,
      savedViewState: Bundle?
  ): View {
    return inflater.inflate(R.layout.controller_bookmarks, container, false)
  }

  override fun onControllerShown() {
    super.onControllerShown()
    uiElementsControllerCallbacks?.hideFab()
  }

  override fun onControllerDestroyed() {
    super.onControllerDestroyed()
    uiElementsControllerCallbacks = null
  }

  override fun getTitle(): String = "Bookmarks Controller"

  override fun getControllerTag(): ControllerTag = ControllerTag(CONTROLLER_TAG)

  companion object {
    const val CONTROLLER_TAG = "BookmarksControllerTag"
  }
}