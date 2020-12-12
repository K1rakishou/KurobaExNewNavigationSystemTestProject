package com.github.k1rakishou.kurobanewnavstacktest.controller.split

import android.os.Bundle
import androidx.core.view.doOnPreDraw
import com.github.k1rakishou.kurobanewnavstacktest.base.ControllerTag
import com.github.k1rakishou.kurobanewnavstacktest.controller.ControllerType
import com.github.k1rakishou.kurobanewnavstacktest.controller.RecyclerViewProvider
import com.github.k1rakishou.kurobanewnavstacktest.controller.base.CatalogController

class SplitCatalogController(args: Bundle? = null) : CatalogController(args) {
  private lateinit var recyclerViewProvider: RecyclerViewProvider

  fun recyclerViewProvider(recyclerViewProvider: RecyclerViewProvider) {
    this.recyclerViewProvider = recyclerViewProvider
  }

  override fun onControllerShown() {
    super.onControllerShown()

    catalogRecyclerView.doOnPreDraw {
      recyclerViewProvider.provideRecyclerView(catalogRecyclerView, ControllerType.Catalog)
      uiElementsControllerCallbacks.showFab()
    }
  }

  override fun onControllerHidden() {
    super.onControllerHidden()

    recyclerViewProvider.withdrawRecyclerView(catalogRecyclerView, ControllerType.Catalog)
  }

  override fun getControllerTag(): ControllerTag = CONTROLLER_TAG

  companion object {
    val CONTROLLER_TAG = ControllerTag("SplitCatalogControllerTag")
  }

}