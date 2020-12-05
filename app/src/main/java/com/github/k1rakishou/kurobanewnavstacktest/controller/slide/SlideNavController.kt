package com.github.k1rakishou.kurobanewnavstacktest.controller.slide

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import com.bluelinelabs.conductor.RouterTransaction
import com.github.k1rakishou.kurobanewnavstacktest.R
import com.github.k1rakishou.kurobanewnavstacktest.base.BaseController
import com.github.k1rakishou.kurobanewnavstacktest.base.ControllerTag

class SlideNavController(args: Bundle? = null) : BaseController(args) {
  private lateinit var uiElementsControllerContainer: FrameLayout

  override fun instantiateView(
      inflater: LayoutInflater,
      container: ViewGroup,
      savedViewState: Bundle?
  ): View {
    return inflater.inflateView(R.layout.controller_slide_navigation, container) {
      uiElementsControllerContainer = findViewById(R.id.ui_elements_controller_container)
    }
  }

  override fun onControllerCreated(savedViewState: Bundle?) {
    super.onControllerCreated(savedViewState)

    uiElementsControllerContainer.setupChildRouterIfNotSet(
      RouterTransaction.with(SlideCatalogUiElementsController())
    )
  }

  override fun getControllerTag(): ControllerTag = CONTROLLER_TAG

  companion object {
    val CONTROLLER_TAG = ControllerTag("SlideNavControllerTag")
  }

}