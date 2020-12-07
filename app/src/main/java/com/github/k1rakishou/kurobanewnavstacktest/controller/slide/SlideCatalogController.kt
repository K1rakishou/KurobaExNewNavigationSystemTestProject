package com.github.k1rakishou.kurobanewnavstacktest.controller.slide

import android.os.Bundle
import androidx.core.view.doOnPreDraw
import com.github.k1rakishou.kurobanewnavstacktest.base.ControllerTag
import com.github.k1rakishou.kurobanewnavstacktest.controller.ControllerType
import com.github.k1rakishou.kurobanewnavstacktest.controller.FocusableController
import com.github.k1rakishou.kurobanewnavstacktest.controller.RecyclerViewProvider
import com.github.k1rakishou.kurobanewnavstacktest.controller.base.CatalogController
import com.github.k1rakishou.kurobanewnavstacktest.controller.base.ControllerToolbarContract
import com.github.k1rakishou.kurobanewnavstacktest.controller.base.UiElementsControllerCallbacks

class SlideCatalogController(args: Bundle? = null) : CatalogController(args), FocusableController {
  private var recyclerViewProvider: RecyclerViewProvider? = null
  private var uiElementsControllerCallbacks: UiElementsControllerCallbacks? = null
  private var controllerToolbarContract: ControllerToolbarContract? = null

  fun recyclerViewProvider(recyclerViewProvider: RecyclerViewProvider) {
    this.recyclerViewProvider = recyclerViewProvider
  }

  fun uiElementsControllerCallbacks(uiElementsControllerCallbacks: UiElementsControllerCallbacks) {
    this.uiElementsControllerCallbacks = uiElementsControllerCallbacks
  }

  fun controllerToolbarContract(controllerToolbarContract: ControllerToolbarContract) {
    this.controllerToolbarContract = controllerToolbarContract
  }

  override fun onControllerShown() {
    super.onControllerShown()

    recyclerView.doOnPreDraw {
      recyclerViewProvider?.provideRecyclerView(recyclerView, ControllerType.Catalog)
      uiElementsControllerCallbacks?.showFab()
    }
  }

  override fun onControllerHidden() {
    super.onControllerHidden()
    recyclerViewProvider?.withdrawRecyclerView(recyclerView, ControllerType.Catalog)
  }

  override fun onLostFocus() {

  }

  override fun onGainedFocus() {

  }

  override fun onControllerDestroyed() {
    super.onControllerDestroyed()

    recyclerViewProvider = null
    uiElementsControllerCallbacks = null
    controllerToolbarContract = null
  }

  override fun setToolbarTitle(title: String) {
    controllerToolbarContract?.setToolbarTitle(ControllerType.Catalog, title)
  }

  override fun getControllerTag(): ControllerTag = CONTROLLER_TAG

  companion object {
    val CONTROLLER_TAG = ControllerTag("SlideCatalogControllerTag")
  }

}