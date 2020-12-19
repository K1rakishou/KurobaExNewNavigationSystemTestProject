package com.github.k1rakishou.kurobanewnavstacktest.controller.split

import android.os.Bundle
import androidx.core.view.doOnPreDraw
import com.airbnb.epoxy.EpoxyRecyclerView
import com.github.k1rakishou.kurobanewnavstacktest.core.base.ControllerTag
import com.github.k1rakishou.kurobanewnavstacktest.controller.RecyclerViewProvider
import com.github.k1rakishou.kurobanewnavstacktest.feature.catalog.CatalogController
import com.github.k1rakishou.kurobanewnavstacktest.data.CatalogData

class SplitCatalogController(args: Bundle? = null) : CatalogController(args) {
  private lateinit var recyclerViewProvider: RecyclerViewProvider
  private val splitFabViewController by lazy { activityContract().mainActivityOrError().splitFabViewController }

  fun recyclerViewProvider(recyclerViewProvider: RecyclerViewProvider) {
    this.recyclerViewProvider = recyclerViewProvider
  }

  override fun provideRecyclerView(recyclerView: EpoxyRecyclerView) {
    recyclerView.doOnPreDraw {
      recyclerViewProvider.provideRecyclerView(recyclerView, controllerType)
      uiElementsControllerCallbacks.showFab()
    }
  }

  override fun withdrawRecyclerView(recyclerView: EpoxyRecyclerView) {
    recyclerViewProvider.withdrawRecyclerView(recyclerView, controllerType)
  }

  override fun onSearchToolbarShown() {
    splitFabViewController.onSearchToolbarShownOrHidden(controllerType, true)
  }

  override fun onSearchToolbarHidden() {
    splitFabViewController.onSearchToolbarShownOrHidden(controllerType, false)
  }

  override fun onCatalogStateChanged(catalogData: CatalogData) {
    splitFabViewController.onControllerStateChanged(
      controllerType,
      catalogData is CatalogData.Data
    )
  }

  override fun getControllerTag(): ControllerTag = CONTROLLER_TAG

  companion object {
    val CONTROLLER_TAG = ControllerTag("SplitCatalogControllerTag")
  }

}