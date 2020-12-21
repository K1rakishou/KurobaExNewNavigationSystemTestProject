package com.github.k1rakishou.kurobanewnavstacktest.controller.slide

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.bluelinelabs.conductor.RouterTransaction
import com.github.k1rakishou.kurobanewnavstacktest.R
import com.github.k1rakishou.kurobanewnavstacktest.core.base.BaseController
import com.github.k1rakishou.kurobanewnavstacktest.core.base.ControllerTag
import com.github.k1rakishou.kurobanewnavstacktest.controller.ControllerType
import com.github.k1rakishou.kurobanewnavstacktest.controller.RecyclerViewProvider
import com.github.k1rakishou.kurobanewnavstacktest.controller.base.*
import com.github.k1rakishou.kurobanewnavstacktest.core.CollapsingViewsHolder
import com.github.k1rakishou.kurobanewnavstacktest.data.BoardDescriptor
import com.github.k1rakishou.kurobanewnavstacktest.data.ThreadDescriptor
import com.github.k1rakishou.kurobanewnavstacktest.viewcontroller.*
import com.github.k1rakishou.kurobanewnavstacktest.widget.bottom_panel.KurobaBottomPanelStateKind
import com.github.k1rakishou.kurobanewnavstacktest.widget.bottom_panel.KurobaBottomNavPanelSelectedItem
import com.github.k1rakishou.kurobanewnavstacktest.widget.fab.SlideKurobaFloatingActionButton
import com.github.k1rakishou.kurobanewnavstacktest.widget.layout.DrawerWidthAdjustingLayout
import com.github.k1rakishou.kurobanewnavstacktest.widget.recycler.PaddingAwareRecyclerView
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

  private val collapsingViewsHolder by lazy { CollapsingViewsHolder(currentContext()) }
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

      onBottomPanelStateChanged(controllerType, newState)
      lockUnlockDrawer(controllerType, newState)
      lockUnlockSlidePaneLayout(controllerType, newState)
    }
    bottomPanel.addOnBottomNavPanelItemSelectedListener { selectedItem ->
      slideNavControllerContainer.switchTo(
        controller = createControllerBySelectedItemId(
          selectedItem = selectedItem,
          uiElementsControllerCallbacks = this
        )
      )
    }
    bottomPanel.addOnBottomPanelHeightChangeListener { controllerType, panelHeight ->
      collapsingViewsHolder.getRecyclerForController(controllerType)?.updatePanelHeight(panelHeight)
    }
  }

  private fun lockUnlockSlidePaneLayout(
    controllerType: ControllerType,
    newState: KurobaBottomPanelStateKind
  ) {
    val lock = newState in DISABLE_SLIDING_PANE_BOTTOM_PANEL_STATE
    getSlideNavControllerOrNull()?.lockUnlockSlidingPaneLayout(lock)
  }

  private fun lockUnlockDrawer(
    controllerType: ControllerType,
    newState: KurobaBottomPanelStateKind
  ) {
    val newDrawerLockMode = if (newState in DISABLE_DRAWER_BOTTOM_PANEL_STATE) {
      DrawerLayout.LOCK_MODE_LOCKED_CLOSED
    } else {
      DrawerLayout.LOCK_MODE_UNDEFINED
    }

    val prevDrawerLockMode = drawer.getDrawerLockMode(GravityCompat.START)

    if (newDrawerLockMode == prevDrawerLockMode) {
      return
    }

    drawer.setDrawerLockMode(newDrawerLockMode)
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

  override fun provideRecyclerView(recyclerView: PaddingAwareRecyclerView, controllerType: ControllerType) {
    slideModeFabViewController.reset()
    bottomPanel.onPanelAvailableVerticalSpaceKnown(controllerType, recyclerView.height)

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

  override fun withdrawRecyclerView(recyclerView: PaddingAwareRecyclerView, controllerType: ControllerType) {
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

    bottomPanel.onControllerFocused(controllerType) {
      val panelHeight = bottomPanel.getBottomPanelHeight(controllerType)
      if (panelHeight != null) {
        collapsingViewsHolder.getRecyclerForController(controllerType)
          ?.updatePanelHeight(panelHeight)
      }
    }
  }

  override fun toolbarSearchVisibilityChanged(
    controllerType: ControllerType,
    toolbarSearchVisible: Boolean
  ) {
    collapsingViewsHolder.toolbarSearchVisibilityChanged(controllerType, toolbarSearchVisible)
  }

  override fun onBottomPanelStateChanged(
    controllerType: ControllerType,
    stateKind: KurobaBottomPanelStateKind
  ) {
    collapsingViewsHolder.onBottomPanelStateChanged(controllerType, stateKind)
  }

  override fun showFab(lock: Boolean?) {
    slideControllerFab.showFab(lock)
  }

  override fun hideFab(lock: Boolean?) {
    slideControllerFab.hideFab(lock)
  }

  override fun getControllerTag(): ControllerTag = CONTROLLER_TAG

  override fun isSplitLayout(): Boolean = false

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

  private fun getSlideNavControllerOrNull(): SlideNavController? {
    return getChildRouter(slideNavControllerContainer)
      .getControllerWithTag(SlideNavController.CONTROLLER_TAG.tag) as? SlideNavController
  }

  companion object {
    private const val TAG = "SlideCatalogUiElementsController"

    val CONTROLLER_TAG = ControllerTag("SlideCatalogUiElementsControllerTag")

    private val DISABLE_DRAWER_BOTTOM_PANEL_STATE = arrayOf(
      KurobaBottomPanelStateKind.Uninitialized,
      KurobaBottomPanelStateKind.ReplyLayoutPanel,
      KurobaBottomPanelStateKind.SelectionPanel
    )

    private val DISABLE_SLIDING_PANE_BOTTOM_PANEL_STATE = arrayOf(
      KurobaBottomPanelStateKind.Uninitialized,
      KurobaBottomPanelStateKind.ReplyLayoutPanel,
      KurobaBottomPanelStateKind.SelectionPanel
    )
  }
}