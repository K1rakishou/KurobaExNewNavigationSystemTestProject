package com.github.k1rakishou.kurobanewnavstacktest.controller.split

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
import com.github.k1rakishou.kurobanewnavstacktest.utils.getBehaviorExt
import com.github.k1rakishou.kurobanewnavstacktest.utils.setBehaviorExt
import com.github.k1rakishou.kurobanewnavstacktest.viewcontroller.ViewScreenAttachSide
import com.github.k1rakishou.kurobanewnavstacktest.widget.behavior.CatalogFabBehavior
import com.github.k1rakishou.kurobanewnavstacktest.widget.bottom_panel.KurobaBottomNavPanel
import com.github.k1rakishou.kurobanewnavstacktest.widget.bottom_panel.KurobaBottomPanel
import com.github.k1rakishou.kurobanewnavstacktest.widget.fab.KurobaFloatingActionButton
import com.github.k1rakishou.kurobanewnavstacktest.widget.layout.DrawerWidthAdjustingLayout
import timber.log.Timber

@SuppressLint("TimberTagLength")
class SplitUiElementsController(
  args: Bundle? = null
) : BaseUiElementsController(args),
  UiElementsControllerCallbacks,
  ChanNavigationContract,
  RecyclerViewProvider {
  private lateinit var drawer: DrawerWidthAdjustingLayout
  private lateinit var catalogFab: KurobaFloatingActionButton
  private lateinit var splitControllerCatalogControllerContainer: FrameLayout
  private lateinit var threadNavigationContract: ThreadNavigationContract

  private val splitFabViewController by lazy { activityContract().mainActivityOrError().splitFabViewController }
  private val collapsingViewsHolder = CollapsingViewsHolder()

  fun threadNavigationContract(threadNavigationContract: ThreadNavigationContract) {
    this.threadNavigationContract = threadNavigationContract
  }

  override fun instantiateView(
    inflater: LayoutInflater,
    container: ViewGroup,
    savedViewState: Bundle?
  ): View {
    return inflater.inflateView(R.layout.controller_split_catalog_ui_elements, container) {
      drawer = findViewById(R.id.split_controller_drawer)
      splitControllerCatalogControllerContainer = findViewById(R.id.split_controller_catalog_controller_container)
      bottomPanel = findViewById(R.id.split_controller_bottom_panel)
      toolbarContainer = findViewById(R.id.split_controller_toolbar_container)

      catalogFab = findViewById(R.id.split_controller_fab)
      catalogFab.setBehaviorExt(CatalogFabBehavior(context, null))
      catalogFab.setOnClickListener { bottomPanel.switchInto(KurobaBottomPanel.State.ReplyLayoutPanel) }
      catalogFab.hide()

      splitFabViewController.setCatalogFab(catalogFab)

      initBottomPanel()
    }
  }

  private fun initBottomPanel() {
    bottomPanel.addOnBottomPanelInitialized {
      splitFabViewController.onBottomPanelInitialized(ControllerType.Catalog)
      catalogFab.initialized()
    }
    bottomPanel.addOnBottomPanelStateChanged { newState ->
      splitFabViewController.onBottomPanelStateChanged(ControllerType.Catalog, newState)

      lockUnlockCollapsableViews(ControllerType.Catalog, newState)
    }
    bottomPanel.addOnBottomNavPanelItemSelectedListener { selectedItem ->
      splitControllerCatalogControllerContainer.switchTo(
        controller = createControllerBySelectedItemId(
          selectedItem = selectedItem,
          uiElementsControllerCallbacks = this@SplitUiElementsController
        )
      )
    }

    bottomPanel.attachFab(catalogFab)
    bottomPanel.bottomPanelPreparationsCompleted(KurobaBottomPanel.State.BottomNavPanel)
  }

  override fun onControllerCreated(savedViewState: Bundle?) {
    super.onControllerCreated(savedViewState)

    splitControllerCatalogControllerContainer.setupChildRouterIfNotSet(
      RouterTransaction.with(createSplitCatalogController(this))
    )
  }

  override fun myHandleBack(): Boolean {
    if (::drawer.isInitialized && drawer.isDrawerOpen(GravityCompat.START)) {
      drawer.closeDrawer(GravityCompat.START)
      return true
    }

    return super.myHandleBack()
  }

  override fun onControllerHidden() {
    super.onControllerHidden()

    catalogFab.getBehaviorExt<CatalogFabBehavior>()?.reset()
  }

  override fun provideRecyclerView(recyclerView: RecyclerView, controllerType: ControllerType) {
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

  override fun lockUnlockCollapsableViews(recyclerView: RecyclerView?, lock: Boolean, animate: Boolean) {
    collapsingViewsHolder.lockUnlockCollapsableViews(
      recyclerView = recyclerView,
      lock = lock,
      animate = animate
    )
  }

  override fun showFab(lock: Boolean?) {
    catalogFab.showFab(lock)
  }

  override fun hideFab(lock: Boolean?) {
    catalogFab.hideFab(lock)
  }

  override fun getControllerTag(): ControllerTag = CONTROLLER_TAG

  override fun isSplitLayout(): Boolean = true

  @SuppressLint("BinaryOperationInTimber")
  override fun openBoard(boardDescriptor: BoardDescriptor) {
    val router = getChildRouter(splitControllerCatalogControllerContainer)

    val splitCatalogController = router.getControllerWithTag(SplitCatalogController.CONTROLLER_TAG.tag)
    if (splitCatalogController == null) {
      // TODO(KurobaEx): add ability to switch to SlideNavController in some cases
      Timber.tag(TAG).e("openBoard($boardDescriptor) " +
        "getControllerWithTag(SplitCatalogController) returned null")
      return
    }

    (splitCatalogController as CatalogNavigationContract).openBoard(boardDescriptor)
  }

  override fun openThread(threadDescriptor: ThreadDescriptor) {
    threadNavigationContract.openThread(threadDescriptor)
  }

  private fun lockUnlockCollapsableViews(controllerType: ControllerType, newState: KurobaBottomPanel.State) {
    val recyclerView = collapsingViewsHolder.getRecyclerForController(controllerType)

    if (newState != KurobaBottomPanel.State.BottomNavPanel) {
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
    selectedItem: KurobaBottomNavPanel.SelectedItem,
    uiElementsControllerCallbacks: UiElementsControllerCallbacks
  ): BaseController {
    return when (selectedItem) {
      KurobaBottomNavPanel.SelectedItem.Search -> TODO()
      KurobaBottomNavPanel.SelectedItem.Bookmarks -> createBookmarksController(uiElementsControllerCallbacks)
      KurobaBottomNavPanel.SelectedItem.Browse -> createSplitCatalogController(uiElementsControllerCallbacks)
      KurobaBottomNavPanel.SelectedItem.Settings -> createSettingsController(uiElementsControllerCallbacks)
      else -> throw IllegalStateException("Unknown itemId: $selectedItem")
    }
  }

  private fun createSplitCatalogController(
    uiElementsControllerCallbacks: UiElementsControllerCallbacks
  ): SplitCatalogController {
    return SplitCatalogController().apply {
      uiElementsControllerCallbacks(uiElementsControllerCallbacks)
      recyclerViewProvider(this@SplitUiElementsController)
      threadNavigationContract(this@SplitUiElementsController)
      toolbarContract(toolbarContract)
    }
  }

  companion object {
    private const val TAG = "SplitUiElementsController"
    val CONTROLLER_TAG = ControllerTag("SplitUiElementsControllerTag")
  }
}