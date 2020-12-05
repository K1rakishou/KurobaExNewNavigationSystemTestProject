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

class SplitCatalogUiElementsController(args: Bundle? = null) : BaseController(args) {
  private lateinit var catalogControllerContainer: FrameLayout

  override fun instantiateView(
    inflater: LayoutInflater,
    container: ViewGroup,
    savedViewState: Bundle?
  ): View {
    return inflater.inflateView(R.layout.controller_split_catalog_ui_elements, container) {
      catalogControllerContainer = findViewById(R.id.split_controller_catalog_controller_container)
    }
  }

  override fun onControllerCreated(savedViewState: Bundle?) {
    super.onControllerCreated(savedViewState)

    catalogControllerContainer.setupChildRouterIfNotSet(
      RouterTransaction.with(SplitCatalogController())
    )
  }

  override fun getControllerTag(): ControllerTag = CONTROLLER_TAG

  companion object {
    val CONTROLLER_TAG = ControllerTag("SplitUiElementsControllerTag")
  }
}