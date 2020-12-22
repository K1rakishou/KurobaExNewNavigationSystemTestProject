package com.github.k1rakishou.kurobanewnavstacktest.controller.slide

import android.os.Bundle
import com.github.k1rakishou.kurobanewnavstacktest.core.base.ControllerTag
import com.github.k1rakishou.kurobanewnavstacktest.controller.FocusableController
import com.github.k1rakishou.kurobanewnavstacktest.controller.RecyclerViewProvider
import com.github.k1rakishou.kurobanewnavstacktest.data.BoardDescriptor
import com.github.k1rakishou.kurobanewnavstacktest.feature.catalog.CatalogController
import com.github.k1rakishou.kurobanewnavstacktest.data.CatalogData
import com.github.k1rakishou.kurobanewnavstacktest.viewstate.getBoardDescriptorOrNull
import com.github.k1rakishou.kurobanewnavstacktest.viewstate.putBoardDescriptor
import com.github.k1rakishou.kurobanewnavstacktest.widget.recycler.PaddingAwareRecyclerView

class SlideCatalogController(
  args: Bundle? = null
) : CatalogController(args), FocusableController {
  private lateinit var recyclerViewProvider: RecyclerViewProvider
  private val slideFabViewController by lazy {
    activityContract().mainActivityOrError().slideFabViewController
  }

  fun recyclerViewProvider(recyclerViewProvider: RecyclerViewProvider) {
    this.recyclerViewProvider = recyclerViewProvider
  }

  override fun onControllerCreated(savedViewState: Bundle?) {
    super.onControllerCreated(savedViewState)

    args.getBoardDescriptorOrNull()?.let { boardDescriptor -> openBoard(boardDescriptor) }
  }

  override fun provideRecyclerView(recyclerView: PaddingAwareRecyclerView) {
    recyclerViewProvider.provideRecyclerView(recyclerView, controllerType)
    uiElementsControllerCallbacks.showFab()
  }

  override fun withdrawRecyclerView(recyclerView: PaddingAwareRecyclerView) {
    recyclerViewProvider.withdrawRecyclerView(recyclerView, controllerType)
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

    fun create(boardDescriptor: BoardDescriptor?): SlideCatalogController {
      val args = Bundle()
      args.putBoardDescriptor(boardDescriptor)

      return SlideCatalogController(args)
    }
  }

}