package com.github.k1rakishou.kurobanewnavstacktest.controller.slide

import android.os.Bundle
import androidx.core.view.doOnPreDraw
import com.github.k1rakishou.kurobanewnavstacktest.base.ControllerTag
import com.github.k1rakishou.kurobanewnavstacktest.controller.ControllerType
import com.github.k1rakishou.kurobanewnavstacktest.controller.FocusableController
import com.github.k1rakishou.kurobanewnavstacktest.controller.RecyclerViewProvider
import com.github.k1rakishou.kurobanewnavstacktest.controller.base.CatalogController
import com.github.k1rakishou.kurobanewnavstacktest.data.CatalogData

class SlideCatalogController(args: Bundle? = null) : CatalogController(args), FocusableController {
  private lateinit var recyclerViewProvider: RecyclerViewProvider
  private val slideFabViewController by lazy { activityContract().mainActivityOrError().slideFabViewController }

  fun recyclerViewProvider(recyclerViewProvider: RecyclerViewProvider) {
    this.recyclerViewProvider = recyclerViewProvider
  }

  override fun onControllerShown() {
    super.onControllerShown()

    catalogRecyclerView.doOnPreDraw {
      recyclerViewProvider.provideRecyclerView(catalogRecyclerView, controllerType)
      uiElementsControllerCallbacks.showFab()
    }
  }

  override fun onControllerHidden() {
    super.onControllerHidden()
    recyclerViewProvider.withdrawRecyclerView(catalogRecyclerView, controllerType)
  }

  override fun onLostFocus() {

  }

  override fun onGainedFocus() {
    slideFabViewController.onControllerFocused(controllerType)
  }

  override fun onSearchToolbarShown() {
    slideFabViewController.onSearchToolbarShownOrHidden(controllerType, true)
  }

  override fun onSearchToolbarHidden() {
    slideFabViewController.onSearchToolbarShownOrHidden(controllerType, false)
  }

  override fun onCatalogStateChanged(catalogData: CatalogData) {
    slideFabViewController.onControllerStateChanged(
      controllerType,
      catalogData is CatalogData.Data
    )
  }

  override fun getControllerTag(): ControllerTag = CONTROLLER_TAG

  companion object {
    val CONTROLLER_TAG = ControllerTag("SlideCatalogControllerTag")
  }

}