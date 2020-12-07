package com.github.k1rakishou.kurobanewnavstacktest.controller.slide

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.recyclerview.widget.RecyclerView
import com.bluelinelabs.conductor.RouterTransaction
import com.github.k1rakishou.kurobanewnavstacktest.R
import com.github.k1rakishou.kurobanewnavstacktest.base.BaseController
import com.github.k1rakishou.kurobanewnavstacktest.base.ControllerTag
import com.github.k1rakishou.kurobanewnavstacktest.controller.ControllerType
import com.github.k1rakishou.kurobanewnavstacktest.controller.RecyclerViewProvider
import com.github.k1rakishou.kurobanewnavstacktest.controller.base.*
import com.github.k1rakishou.kurobanewnavstacktest.data.BoardDescriptor
import com.github.k1rakishou.kurobanewnavstacktest.data.ThreadDescriptor
import com.github.k1rakishou.kurobanewnavstacktest.utils.AndroidUtils
import com.github.k1rakishou.kurobanewnavstacktest.viewcontroller.CollapsingViewController
import com.github.k1rakishou.kurobanewnavstacktest.viewcontroller.SlideModeFabClickListener
import com.github.k1rakishou.kurobanewnavstacktest.viewcontroller.SlideModeFabViewController
import com.github.k1rakishou.kurobanewnavstacktest.viewcontroller.SlideModeFabViewControllerCallbacks
import com.github.k1rakishou.kurobanewnavstacktest.widget.KurobaFloatingActionButton
import com.github.k1rakishou.kurobanewnavstacktest.widget.toolbar.SlideToolbar
import timber.log.Timber

@SuppressLint("TimberTagLength")
class SlideUiElementsController(
  args: Bundle? = null
) : BaseUiElementsController(args),
  UiElementsControllerCallbacks,
  SlideNavController.SlideCatalogUiElementsControllerCallbacks,
  SlideModeFabClickListener,
  RecyclerViewProvider,
  ChanNavigationContract {
  private lateinit var createThreadButton: KurobaFloatingActionButton
  private lateinit var slideModeFabViewController: SlideModeFabViewController
  private lateinit var slideNavControllerContainer: FrameLayout

  private val collapsingViewControllerMap =
    mutableMapOf<View, MutableMap<RecyclerView, CollapsingViewController>>()

  override fun instantiateView(
    inflater: LayoutInflater,
    container: ViewGroup,
    savedViewState: Bundle?
  ): View {
    return inflater.inflateView(R.layout.controller_slide_catalog_ui_elements, container) {
      slideNavControllerContainer = findViewById(R.id.slide_nav_controller_container)
      bottomNavView = findViewById(R.id.slide_controller_bottom_nav_view)
      toolbarContainer = findViewById(R.id.slide_controller_toolbar_container)
      createThreadButton = findViewById(R.id.slide_controller_catalog_fab)

      slideModeFabViewController = SlideModeFabViewController(
        createThreadButton,
        this@SlideUiElementsController
      )
    }
  }

  override fun onControllerCreated(savedViewState: Bundle?) {
    super.onControllerCreated(savedViewState)

    slideNavControllerContainer.setupChildRouterIfNotSet(
      RouterTransaction.with(
        createSlideNavController(
          uiElementsControllerCallbacks = this,
          slideCatalogUiElementsControllerCallbacks = this,
          recyclerViewProvider = this,
          slideModeFabViewControllerCallbacks = slideModeFabViewController
        )
      )
    )

    bottomNavView.selectedItemId = R.id.action_browse

    bottomNavView.setOnNavigationItemSelectedListener { item ->
      if (bottomNavView.selectedItemId == item.itemId) {
        return@setOnNavigationItemSelectedListener true
      }

      slideNavControllerContainer.switchTo(
        controller = createControllerBySelectedItemId(
          itemId = item.itemId,
          uiElementsControllerCallbacks = this
        )
      )

      return@setOnNavigationItemSelectedListener true
    }
  }

  override fun onControllerShown() {
    super.onControllerShown()
    slideModeFabViewController.onAttach()
  }

  override fun onControllerHidden() {
    super.onControllerHidden()
    slideModeFabViewController.onDetach()
  }

  override fun onFabClicked(fabType: SlideModeFabViewController.FabType) {
    // TODO(KurobaEx): reply layout
    showToast("onFabClicked fabType=$fabType")
  }

  @SuppressLint("BinaryOperationInTimber")
  override fun openBoard(boardDescriptor: BoardDescriptor) {
    val router = getChildRouter(slideNavControllerContainer)

    val slideNavController = router.getControllerWithTag(SlideNavController.CONTROLLER_TAG.tag)
    if (slideNavController == null) {
      // TODO(KurobaEx): add ability to switch to SlideNavController in some cases
      Timber.tag(TAG).e("openBoard($boardDescriptor) " +
        "getControllerWithTag(SlideNavController) returned null")
      return
    }

    (slideNavController as CatalogNavigationContract).openBoard(boardDescriptor)
  }

  @SuppressLint("BinaryOperationInTimber")
  override fun openThread(threadDescriptor: ThreadDescriptor) {
    val router = getChildRouter(slideNavControllerContainer)

    val slideNavController = router.getControllerWithTag(SlideNavController.CONTROLLER_TAG.tag)
    if (slideNavController == null) {
      // TODO(KurobaEx): add ability to switch to SlideNavController in some cases
      Timber.tag(TAG).e("openThread($threadDescriptor) " +
        "getControllerWithTag(SlideNavController) returned null")
      return
    }

    (slideNavController as ThreadNavigationContract).openThread(threadDescriptor)
  }

  override fun provideRecyclerView(recyclerView: RecyclerView, controllerType: ControllerType) {
    slideModeFabViewController.reset()
    slideModeFabViewController.init(bottomNavView)

    initCollapsingToolbar(recyclerView, controllerType)
    initCollapsingBottomNavView(recyclerView, controllerType)

    if (AndroidUtils.showLockCollapsableViews(currentContext())) {
      collapsingViewControllerMap.values
        .flatMap { innerMap -> innerMap.values }
        .forEach { collapsingViewDelegate ->
          collapsingViewDelegate.lockUnlock(
            lock = true,
            animate = true
          )
        }
    }
  }

  override fun withdrawRecyclerView(recyclerView: RecyclerView, controllerType: ControllerType) {
    toolbarContract.collapsableView().let { collapsableView ->
      collapsingViewControllerMap[collapsableView]?.remove(recyclerView)?.detach(recyclerView)
      if (collapsingViewControllerMap[collapsableView].isNullOrEmpty()) {
        collapsingViewControllerMap.remove(collapsableView)
      }
    }

    collapsingViewControllerMap[bottomNavView]?.remove(recyclerView)?.detach(recyclerView)
    if (collapsingViewControllerMap[bottomNavView].isNullOrEmpty()) {
      collapsingViewControllerMap.remove(bottomNavView)
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

  private fun initCollapsingBottomNavView(
    recyclerView: RecyclerView,
    controllerType: ControllerType
  ) {
    if (!collapsingViewControllerMap.containsKey(bottomNavView)) {
      collapsingViewControllerMap[bottomNavView] = mutableMapOf()
    }

    if (!collapsingViewControllerMap[bottomNavView]!!.containsKey(recyclerView)) {
      collapsingViewControllerMap[bottomNavView]!![recyclerView] = CollapsingViewController(
        controllerType,
        CollapsingViewController.ViewScreenAttachPoint.AttachedToBottom
      )
    }

    collapsingViewControllerMap[bottomNavView]!![recyclerView]!!.attach(bottomNavView, recyclerView)
  }

  private fun initCollapsingToolbar(
    recyclerView: RecyclerView,
    controllerType: ControllerType
  ) {
    toolbarContract.collapsableView().let { collapsableView ->
      if (!collapsingViewControllerMap.containsKey(collapsableView)) {
        collapsingViewControllerMap[collapsableView] = mutableMapOf()
      }

      if (!collapsingViewControllerMap[collapsableView]!!.containsKey(recyclerView)) {
        collapsingViewControllerMap[collapsableView]!![recyclerView] = CollapsingViewController(
          controllerType,
          CollapsingViewController.ViewScreenAttachPoint.AttachedToTop
        )
      }

      collapsingViewControllerMap[collapsableView]!![recyclerView]!!.attach(collapsableView, recyclerView)
    }
  }

  private fun createControllerBySelectedItemId(
    itemId: Int,
    uiElementsControllerCallbacks: UiElementsControllerCallbacks
  ): BaseController {
    return when (itemId) {
      R.id.action_bookmarks -> createBookmarksController(uiElementsControllerCallbacks)
      R.id.action_browse -> {
        createSlideNavController(
          uiElementsControllerCallbacks = uiElementsControllerCallbacks,
          slideCatalogUiElementsControllerCallbacks = this,
          recyclerViewProvider = this,
          slideModeFabViewControllerCallbacks = slideModeFabViewController
        )
      }
      R.id.action_settings -> createSettingsController(uiElementsControllerCallbacks)
      else -> throw IllegalStateException("Unknown itemId: $itemId")
    }
  }

  private fun createSlideNavController(
    uiElementsControllerCallbacks: UiElementsControllerCallbacks,
    slideCatalogUiElementsControllerCallbacks: SlideNavController.SlideCatalogUiElementsControllerCallbacks,
    recyclerViewProvider: RecyclerViewProvider,
    slideModeFabViewControllerCallbacks: SlideModeFabViewControllerCallbacks
  ): SlideNavController {
    return SlideNavController().apply {
      setUiElementsControllerCallbacks(uiElementsControllerCallbacks)
      setSlideCatalogUiElementsControllerCallbacks(slideCatalogUiElementsControllerCallbacks)
      recyclerViewProvider(recyclerViewProvider)
      slideModeFabViewControllerCallbacks(slideModeFabViewControllerCallbacks)
    }
  }

  private fun getSlideToolbar() = toolbarContract.collapsableView() as SlideToolbar

  companion object {
    private const val TAG = "SlideCatalogUiElementsController"

    val CONTROLLER_TAG = ControllerTag("SlideCatalogUiElementsControllerTag")
  }
}