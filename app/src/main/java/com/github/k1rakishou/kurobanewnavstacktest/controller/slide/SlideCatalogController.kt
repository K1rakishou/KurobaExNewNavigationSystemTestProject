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
      recyclerViewProvider.provideRecyclerView(catalogRecyclerView, CONTROLLER_TYPE)
      uiElementsControllerCallbacks.showFab()
    }
  }

  override fun onControllerHidden() {
    super.onControllerHidden()
    recyclerViewProvider.withdrawRecyclerView(catalogRecyclerView, CONTROLLER_TYPE)
  }

  override fun onLostFocus() {

  }

  override fun onGainedFocus() {
    slideFabViewController.onControllerFocused(CONTROLLER_TYPE)
  }

  override fun onSearchToolbarShown() {
    slideFabViewController.onSearchToolbarShownOrHidden(CONTROLLER_TYPE, true)
  }

  override fun onSearchToolbarHidden() {
    slideFabViewController.onSearchToolbarShownOrHidden(CONTROLLER_TYPE, false)
  }

  override fun onCatalogStateChanged(catalogData: CatalogData) {
    slideFabViewController.onControllerStateChanged(
      CONTROLLER_TYPE,
      catalogData is CatalogData.Data
    )
  }

  override fun getControllerTag(): ControllerTag = CONTROLLER_TAG

  companion object {
    val CONTROLLER_TAG = ControllerTag("SlideCatalogControllerTag")

    private val CONTROLLER_TYPE = ControllerType.Catalog
  }

}