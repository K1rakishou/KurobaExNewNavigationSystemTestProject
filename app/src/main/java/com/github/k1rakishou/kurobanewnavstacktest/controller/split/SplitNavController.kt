package com.github.k1rakishou.kurobanewnavstacktest.controller.split

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import com.bluelinelabs.conductor.RouterTransaction
import com.github.k1rakishou.kurobanewnavstacktest.R
import com.github.k1rakishou.kurobanewnavstacktest.base.BaseController
import com.github.k1rakishou.kurobanewnavstacktest.base.ControllerTag
import com.github.k1rakishou.kurobanewnavstacktest.data.BoardDescriptor
import com.github.k1rakishou.kurobanewnavstacktest.data.ThreadDescriptor

class SplitNavController(args: Bundle? = null) : BaseController(args) {
  private lateinit var leftControllerContainer: FrameLayout
  private lateinit var rightControllerContainer: FrameLayout

  override fun instantiateView(
      inflater: LayoutInflater,
      container: ViewGroup,
      savedViewState: Bundle?
  ): View {
    return inflater.inflateView(R.layout.controller_split_navigation, container) {
      leftControllerContainer = findViewById(R.id.left_controller_container)
      rightControllerContainer = findViewById(R.id.right_controller_container)
    }
  }

  override fun onControllerCreated(savedViewState: Bundle?) {
    super.onControllerCreated(savedViewState)

    leftControllerContainer.setupChildRouterIfNotSet(
      RouterTransaction.with(SplitCatalogUiElementsController())
    )
    rightControllerContainer.setupChildRouterIfNotSet(
      RouterTransaction.with(SplitThreadController())
    )
  }

  override fun getControllerTag(): ControllerTag = CONTROLLER_TAG

  companion object {
    val CONTROLLER_TAG = ControllerTag("SplitNavControllerTag")
  }

}