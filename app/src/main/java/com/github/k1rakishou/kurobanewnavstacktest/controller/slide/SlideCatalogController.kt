package com.github.k1rakishou.kurobanewnavstacktest.controller.slide

import android.os.Bundle
import androidx.core.view.doOnPreDraw
import com.github.k1rakishou.kurobanewnavstacktest.base.ControllerTag
import com.github.k1rakishou.kurobanewnavstacktest.controller.ControllerType
import com.github.k1rakishou.kurobanewnavstacktest.controller.FocusableController
import com.github.k1rakishou.kurobanewnavstacktest.controller.RecyclerViewProvider
import com.github.k1rakishou.kurobanewnavstacktest.controller.base.CatalogController
import com.github.k1rakishou.kurobanewnavstacktest.controller.base.UiElementsControllerCallbacks

class SlideCatalogController(args: Bundle? = null) : CatalogController(args), FocusableController {
  private lateinit var recyclerViewProvider: RecyclerViewProvider
  private lateinit var uiElementsControllerCallbacks: UiElementsControllerCallbacks

  fun recyclerViewProvider(recyclerViewProvider: RecyclerViewProvider) {
    this.recyclerViewProvider = recyclerViewProvider
  }

  fun uiElementsControllerCallbacks(uiElementsControllerCallbacks: UiElementsControllerCallbacks) {
    this.uiElementsControllerCallbacks = uiElementsControllerCallbacks
  }

  override fun onControllerShown() {
    super.onControllerShown()

    recyclerView.doOnPreDraw {
      recyclerViewProvider.provideRecyclerView(recyclerView, ControllerType.Catalog)
      uiElementsControllerCallbacks.showFab()
    }
  }

  override fun onControllerHidden() {
    super.onControllerHidden()
    recyclerViewProvider.withdrawRecyclerView(recyclerView, ControllerType.Catalog)
  }

  override fun onLostFocus() {

  }

  override fun onGainedFocus() {

  }

  override fun getControllerTag(): ControllerTag = CONTROLLER_TAG

  companion object {
    val CONTROLLER_TAG = ControllerTag("SlideCatalogControllerTag")
  }

}