package com.github.k1rakishou.kurobanewnavstacktest.controller.slide

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import com.bluelinelabs.conductor.RouterTransaction
import com.github.k1rakishou.kurobanewnavstacktest.R
import com.github.k1rakishou.kurobanewnavstacktest.base.BaseController
import com.github.k1rakishou.kurobanewnavstacktest.base.ControllerTag
import com.github.k1rakishou.kurobanewnavstacktest.controller.base.CatalogUiElementsController
import com.github.k1rakishou.kurobanewnavstacktest.controller.base.UiElementsControllerCallbacks
import com.github.k1rakishou.kurobanewnavstacktest.widget.toolbar.SlideToolbar
import com.google.android.material.floatingactionbutton.FloatingActionButton

class SlideCatalogUiElementsController(
  args: Bundle? = null
) : CatalogUiElementsController(args),
  UiElementsControllerCallbacks,
  SlideNavController.SlideCatalogUiElementsControllerCallbacks {
  private lateinit var catalogControllerContainer: FrameLayout
  private lateinit var createThreadButton: FloatingActionButton

  override fun instantiateView(
    inflater: LayoutInflater,
    container: ViewGroup,
    savedViewState: Bundle?
  ): View {
    return inflater.inflateView(R.layout.controller_slide_catalog_ui_elements, container) {
      catalogControllerContainer = findViewById(R.id.slide_controller_catalog_controller_container)
      bottomNavView = findViewById(R.id.slide_controller_bottom_nav_view)
      toolbarContainer = findViewById(R.id.slide_controller_toolbar_container)
      createThreadButton = findViewById(R.id.slide_controller_catalog_fab)
    }
  }

  override fun onControllerCreated(savedViewState: Bundle?) {
    super.onControllerCreated(savedViewState)

    catalogControllerContainer.setupChildRouterIfNotSet(
      RouterTransaction.with(createSlideNavController(this, this))
    )

    bottomNavView.selectedItemId = R.id.action_browse

    bottomNavView.setOnNavigationItemSelectedListener { item ->
      if (bottomNavView.selectedItemId == item.itemId) {
        return@setOnNavigationItemSelectedListener true
      }

      catalogControllerContainer.switchTo(
        controller = createControllerBySelectedItemId(
          itemId = item.itemId,
          uiElementsControllerCallbacks = this
        )
      )

      return@setOnNavigationItemSelectedListener true
    }
  }

  override fun onSliding(offset: Float) {
    (toolbarContract.collapsableView() as SlideToolbar).onSliding(offset)
  }

  override fun onControllerGainedFocus(isCatalogController: Boolean) {
    (toolbarContract.collapsableView() as SlideToolbar).onControllerGainedFocus(isCatalogController)
  }

  override fun showFab() {
    createThreadButton.show()
  }

  override fun hideFab() {
    createThreadButton.hide()
  }

  override fun getControllerTag(): ControllerTag = CONTROLLER_TAG

  override fun isSplitLayout(): Boolean = false

  private fun createControllerBySelectedItemId(
    itemId: Int,
    uiElementsControllerCallbacks: UiElementsControllerCallbacks
  ): BaseController {
    return when (itemId) {
      R.id.action_bookmarks -> createBookmarksController(uiElementsControllerCallbacks)
      R.id.action_browse -> createSlideNavController(uiElementsControllerCallbacks, this)
      R.id.action_settings -> createSettingsController(uiElementsControllerCallbacks)
      else -> throw IllegalStateException("Unknown itemId: $itemId")
    }
  }

  private fun getSlideToolbar() = toolbarContract.collapsableView() as SlideToolbar

  companion object {
    val CONTROLLER_TAG = ControllerTag("SlideCatalogUiElementsControllerTag")
  }
}