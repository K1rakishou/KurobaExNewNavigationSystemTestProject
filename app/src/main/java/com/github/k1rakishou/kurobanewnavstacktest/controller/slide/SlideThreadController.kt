package com.github.k1rakishou.kurobanewnavstacktest.controller.slide

import android.os.Bundle
import android.view.View
import androidx.core.view.doOnPreDraw
import com.github.k1rakishou.kurobanewnavstacktest.base.ControllerTag
import com.github.k1rakishou.kurobanewnavstacktest.controller.ControllerType
import com.github.k1rakishou.kurobanewnavstacktest.controller.FocusableController
import com.github.k1rakishou.kurobanewnavstacktest.controller.RecyclerViewProvider
import com.github.k1rakishou.kurobanewnavstacktest.controller.base.ThreadController

class SlideThreadController(
  args: Bundle? = null
) : ThreadController(args),
  FocusableController {
  private var recyclerViewProvider: RecyclerViewProvider? = null

  fun recyclerViewProvider(recyclerViewProvider: RecyclerViewProvider) {
    this.recyclerViewProvider = recyclerViewProvider
  }

  override fun onControllerCreated(savedViewState: Bundle?) {
    super.onControllerCreated(savedViewState)

    toolbarContract.setToolbarVisibility(View.GONE)
  }

  override fun onControllerShown() {
    super.onControllerShown()

    recyclerView.doOnPreDraw {
      recyclerViewProvider?.provideRecyclerView(recyclerView, ControllerType.Thread)
    }
  }

  override fun onControllerHidden() {
    super.onControllerHidden()

    recyclerViewProvider?.withdrawRecyclerView(recyclerView, ControllerType.Thread)
  }

  override fun onLostFocus() {

  }

  override fun onGainedFocus() {

  }

  override fun onControllerDestroyed() {
    super.onControllerDestroyed()

    recyclerViewProvider = null
  }

  override fun getControllerTag(): ControllerTag = CONTROLLER_TAG

  companion object {
    val CONTROLLER_TAG = ControllerTag("SlideThreadControllerTag")
  }
}