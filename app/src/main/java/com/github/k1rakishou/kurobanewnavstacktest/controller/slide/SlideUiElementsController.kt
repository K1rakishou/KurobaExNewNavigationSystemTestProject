package com.github.k1rakishou.kurobanewnavstacktest.controller.slide

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.core.view.GravityCompat
import androidx.recyclerview.widget.RecyclerView
import com.bluelinelabs.conductor.RouterTransaction
import com.github.k1rakishou.kurobanewnavstacktest.R
import com.github.k1rakishou.kurobanewnavstacktest.base.BaseController
import com.github.k1rakishou.kurobanewnavstacktest.base.ControllerTag
import com.github.k1rakishou.kurobanewnavstacktest.controller.ControllerType
import com.github.k1rakishou.kurobanewnavstacktest.controller.RecyclerViewProvider
import com.github.k1rakishou.kurobanewnavstacktest.controller.base.*
import com.github.k1rakishou.kurobanewnavstacktest.core.CollapsingViewsHolder
import com.github.k1rakishou.kurobanewnavstacktest.data.BoardDescriptor
import com.github.k1rakishou.kurobanewnavstacktest.data.ThreadDescriptor
import com.github.k1rakishou.kurobanewnavstacktest.viewcontroller.*
import com.github.k1rakishou.kurobanewnavstacktest.widget.KurobaBottomPanelStateKind
import com.github.k1rakishou.kurobanewnavstacktest.widget.bottom_panel.KurobaBottomNavPanelSelectedItem
import com.github.k1rakishou.kurobanewnavstacktest.widget.fab.SlideKurobaFloatingActionButton
import com.github.k1rakishou.kurobanewnavstacktest.widget.layout.DrawerWidthAdjustingLayout
import com.github.k1rakishou.kurobanewnavstacktest.widget.toolbar.SlideToolbar
import com.github.k1rakishou.kurobanewnavstacktest.widget.toolbar.ToolbarContract
import timber.log.Timber

@SuppressLint("TimberTagLength")
class SlideUiElementsController(
  args: Bundle? = null
) : BaseUiElementsController(args),
  UiElementsControllerCallbacks,
  SlideNavController.SlideCatalogUiElementsControllerCallbacks,
  FabClickListener,
  RecyclerViewProvider,
  ChanNavigationContract {
  private lateinit var drawer: DrawerWidthAdjustingLayout
  private lateinit var slideControllerFab: SlideKurobaFloatingActionButton
  private lateinit var slideModeFabViewController: SlideModeFabViewController
  private lateinit var slideNavControllerContainer: FrameLayout

  private val collapsingViewsHolder = CollapsingViewsHolder()
  private val slideFabViewController by lazy { activityContract().mainActivityOrError().slideFabViewController }

  override fun instantiateView(
    inflater: LayoutInflater,
    container: ViewGroup,
    savedViewState: Bundle?
  ): View {
    return inflater.inflateView(R.layout.controller_slide_catalog_ui_elements, container) {
      drawer = findViewById(R.id.slide_controller_drawer)
      slideNavControllerContainer = findViewById(R.id.slide_nav_controller_container)
      bottomPanel = findViewById(R.id.slide_controller_bottom_panel)
      toolbarContainer = findViewById(R.id.slide_controller_toolbar_container)

      slideControllerFab = findViewById(R.id.slide_controller_fab)
      slideControllerFab.hide()

      slideModeFabViewController = SlideModeFabViewController(
        slideControllerFab,
        this@SlideUiElementsController
      )

      slideFabViewController.setFab(slideControllerFab)
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
          slideModeFabViewControllerCallbacks = slideModeFabViewController,
          toolbarContract = toolbarContract
        )
      )
    )

    bottomPanel.attachFab(slideControllerFab)

    bottomPanel.addOnBottomPanelInitialized { controllerType ->
      // Doesn't matter what we use here since Slide layout has only one bottom panel
      slideFabViewController.onBottomPanelInitialized(controllerType)
      slideControllerFab.initialized()
    }
    bottomPanel.addOnBottomPanelStateChanged { controllerType, newState ->
      slideFabViewController.onBottomPanelStateChanged(controllerType, newState)
      lockUnlockCollapsableViews(controllerType, newState)
    }
    bottomPanel.addOnBottomNavPanelItemSelectedListener { selectedItem ->
      slideNavControllerContainer.switchTo(
        controller = createControllerBySelectedItemId(
          selectedItem = selectedItem,
          uiElementsControllerCallbacks = this
        )
      )
    }
    bottomPanel.addOnBottomPanelHeightChangeListener { controllerType, height, isFullScreen ->
      collapsingViewsHolder.getRecyclerForController(controllerType)?.let { recyclerView ->
        // TODO(KurobaEx): subclass recycler
      }
    }
  }

  override fun handleBack(): Boolean {
    if (::drawer.isInitialized && drawer.isDrawerOpen(GravityCompat.START)) {
      drawer.closeDrawer(GravityCompat.START)
      return true
    }

    if (super.myHandleBack()) {
      return true
    }

    val topController = getChildRouter(slideNavControllerContainer).getTopController()
      as? BaseController
      ?: return false

    if (topController.myHandleBack()) {
      return true
    }

    return super.handleBack()
  }

  override fun onControllerShown() {
    super.onControllerShown()
    slideModeFabViewController.onAttach()
  }

  override fun onControllerHidden() {
    super.onControllerHidden()
    slideModeFabViewController.onDetach()
  }

  override fun onControllerDestroyed() {
    super.onControllerDestroyed()

    bottomPanel.cleanup()
  }

  override fun onFabClicked(fabType: SlideModeFabViewController.FabType) {
    bottomPanel.switchInto(KurobaBottomPanelStateKind.ReplyLayoutPanel)
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
    bottomPanel.onRecyclerViewHeightKnown(controllerType, recyclerView.height)

    collapsingViewsHolder.attach(
      recyclerView = recyclerView,
      collapsableView = toolbarContract.collapsableView(),
      viewAttachSide = ViewScreenAttachSide.Top,
      controllerType = controllerType
    )

    collapsingViewsHolder.attach(
      recyclerView = recyclerView,
      collapsableView = bottomPanel,
      viewAttachSide = ViewScreenAttachSide.Bottom,
      controllerType = controllerType
    )
  }

  override fun withdrawRecyclerView(recyclerView: RecyclerView, controllerType: ControllerType) {
    collapsingViewsHolder.detach(
      recyclerView = recyclerView,
      collapsableView = toolbarContract.collapsableView(),
      controllerType = controllerType
    )
    collapsingViewsHolder.detach(
      recyclerView = recyclerView,
      collapsableView = bottomPanel,
      controllerType = controllerType
    )
  }

  override fun onBeforeSliding(transitioningIntoCatalogToolbar: Boolean) {
    (toolbarContract.collapsableView() as SlideToolbar).onBeforeSliding(transitioningIntoCatalogToolbar)
  }

  override fun onSliding(offset: Float) {
    (toolbarContract.collapsableView() as SlideToolbar).onSliding(offset)
  }

  override fun onAfterSliding(becameCatalogToolbar: Boolean) {
    (toolbarContract.collapsableView() as SlideToolbar).onAfterSliding(becameCatalogToolbar)
  }

  override fun onControllerGainedFocus(isCatalogController: Boolean) {
    (toolbarContract.collapsableView() as SlideToolbar).onControllerGainedFocus(isCatalogController)

    val controllerType = if (isCatalogController) {
      ControllerType.Catalog
    } else {
      ControllerType.Thread
    }

    if (!bottomPanel.isBottomPanelInitialized(controllerType)) {
      bottomPanel.bottomPanelPreparationsCompleted(
        controllerType,
        KurobaBottomPanelStateKind.BottomNavPanel
      )
    }

    bottomPanel.onControllerFocused(controllerType)
  }

  override fun lockUnlockCollapsableViews(recyclerView: RecyclerView?, lock: Boolean, animate: Boolean) {
    collapsingViewsHolder.lockUnlockCollapsableViews(
      recyclerView = recyclerView,
      lock = lock,
      animate = animate
    )
  }

  override fun showFab(lock: Boolean?) {
    slideControllerFab.showFab(lock)
  }

  override fun hideFab(lock: Boolean?) {
    slideControllerFab.hideFab(lock)
  }

  override fun getControllerTag(): ControllerTag = CONTROLLER_TAG

  override fun isSplitLayout(): Boolean = false

  private fun lockUnlockCollapsableViews(
    controllerType: ControllerType,
    newState: KurobaBottomPanelStateKind
  ) {
    val recyclerView = collapsingViewsHolder.getRecyclerForController(controllerType)

    if (newState != KurobaBottomPanelStateKind.BottomNavPanel) {
      collapsingViewsHolder.lockUnlockCollapsableViews(
        recyclerView = recyclerView,
        lock = true,
        animate = true
      )
    } else {
      collapsingViewsHolder.lockUnlockCollapsableViews(
        recyclerView = recyclerView,
        lock = false,
        animate = true
      )
    }
  }

  private fun createControllerBySelectedItemId(
    selectedItem: KurobaBottomNavPanelSelectedItem,
    uiElementsControllerCallbacks: UiElementsControllerCallbacks
  ): BaseController {
    return when (selectedItem) {
      KurobaBottomNavPanelSelectedItem.Search -> TODO()
      KurobaBottomNavPanelSelectedItem.Bookmarks -> createBookmarksController(uiElementsControllerCallbacks)
      KurobaBottomNavPanelSelectedItem.Browse -> {
        createSlideNavController(
          uiElementsControllerCallbacks = uiElementsControllerCallbacks,
          slideCatalogUiElementsControllerCallbacks = this,
          recyclerViewProvider = this,
          slideModeFabViewControllerCallbacks = slideModeFabViewController,
          toolbarContract = toolbarContract
        )
      }
      KurobaBottomNavPanelSelectedItem.Settings -> createSettingsController(uiElementsControllerCallbacks)
      else -> throw IllegalStateException("Unknown itemId: $selectedItem")
    }
  }

  private fun createSlideNavController(
    uiElementsControllerCallbacks: UiElementsControllerCallbacks,
    slideCatalogUiElementsControllerCallbacks: SlideNavController.SlideCatalogUiElementsControllerCallbacks,
    recyclerViewProvider: RecyclerViewProvider,
    slideModeFabViewControllerCallbacks: SlideModeFabViewControllerCallbacks,
    toolbarContract: ToolbarContract
  ): SlideNavController {
    return SlideNavController().apply {
      setUiElementsControllerCallbacks(uiElementsControllerCallbacks)
      setSlideCatalogUiElementsControllerCallbacks(slideCatalogUiElementsControllerCallbacks)
      recyclerViewProvider(recyclerViewProvider)
      slideModeFabViewControllerCallbacks(slideModeFabViewControllerCallbacks)
      toolbarContract(toolbarContract)
    }
  }

  companion object {
    private const val TAG = "SlideCatalogUiElementsController"

    val CONTROLLER_TAG = ControllerTag("SlideCatalogUiElementsControllerTag")
  }
}