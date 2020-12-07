package com.github.k1rakishou.kurobanewnavstacktest.controller.split

import android.os.Bundle
import androidx.core.view.doOnPreDraw
import com.github.k1rakishou.kurobanewnavstacktest.base.ControllerTag
import com.github.k1rakishou.kurobanewnavstacktest.controller.ControllerType
import com.github.k1rakishou.kurobanewnavstacktest.controller.RecyclerViewProvider
import com.github.k1rakishou.kurobanewnavstacktest.controller.base.CatalogController
import com.github.k1rakishou.kurobanewnavstacktest.controller.base.ControllerToolbarContract
import com.github.k1rakishou.kurobanewnavstacktest.controller.base.UiElementsControllerCallbacks

class SplitCatalogController(args: Bundle? = null) : CatalogController(args) {
  private var uiElementsControllerCallbacks: UiElementsControllerCallbacks? = null
  private var recyclerViewProvider: RecyclerViewProvider? = null
  private var controllerToolbarContract: ControllerToolbarContract? = null

  fun uiElementsControllerCallbacks(uiElementsControllerCallbacks: UiElementsControllerCallbacks) {
    this.uiElementsControllerCallbacks = uiElementsControllerCallbacks
  }

  fun recyclerViewProvider(recyclerViewProvider: RecyclerViewProvider) {
    this.recyclerViewProvider = recyclerViewProvider
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

  override fun onControllerDestroyed() {
    super.onControllerDestroyed()

    uiElementsControllerCallbacks = null
    recyclerViewProvider = null
  }

  override fun setToolbarTitle(title: String) {
    controllerToolbarContract?.setToolbarTitle(ControllerType.Catalog, title)
  }

  override fun getControllerTag(): ControllerTag = CONTROLLER_TAG

  companion object {
    val CONTROLLER_TAG = ControllerTag("SplitCatalogControllerTag")
  }

}