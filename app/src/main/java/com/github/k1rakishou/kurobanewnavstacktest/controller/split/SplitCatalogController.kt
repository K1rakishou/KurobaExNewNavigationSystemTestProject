package com.github.k1rakishou.kurobanewnavstacktest.controller.split

import android.os.Bundle
import androidx.core.view.doOnPreDraw
import com.github.k1rakishou.kurobanewnavstacktest.core.base.ControllerTag
import com.github.k1rakishou.kurobanewnavstacktest.controller.RecyclerViewProvider
import com.github.k1rakishou.kurobanewnavstacktest.data.BoardDescriptor
import com.github.k1rakishou.kurobanewnavstacktest.feature.catalog.CatalogController
import com.github.k1rakishou.kurobanewnavstacktest.data.CatalogData
import com.github.k1rakishou.kurobanewnavstacktest.viewstate.getBoardDescriptorOrNull
import com.github.k1rakishou.kurobanewnavstacktest.viewstate.putBoardDescriptor
import com.github.k1rakishou.kurobanewnavstacktest.widget.recycler.PaddingAwareRecyclerView

class SplitCatalogController(args: Bundle? = null) : CatalogController(args) {
  private lateinit var recyclerViewProvider: RecyclerViewProvider
  private val splitFabViewController by lazy {
    activityContract().mainActivityOrError().splitFabViewController
  }

  fun recyclerViewProvider(recyclerViewProvider: RecyclerViewProvider) {
    this.recyclerViewProvider = recyclerViewProvider
  }

  override fun onControllerCreated(savedViewState: Bundle?) {
    super.onControllerCreated(savedViewState)

    args.getBoardDescriptorOrNull()?.let { boardDescriptor -> openBoard(boardDescriptor) }
  }

  override fun provideRecyclerView(recyclerView: PaddingAwareRecyclerView) {
    recyclerView.doOnPreDraw {
      recyclerViewProvider.provideRecyclerView(recyclerView, controllerType)
      uiElementsControllerCallbacks.showFab()
    }
  }

  override fun withdrawRecyclerView(recyclerView: PaddingAwareRecyclerView) {
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

    fun create(boardDescriptor: BoardDescriptor?): SplitCatalogController {
      val args = Bundle()
      args.putBoardDescriptor(boardDescriptor)

      return SplitCatalogController(args)
    }
  }

}