package com.github.k1rakishou.kurobanewnavstacktest.controller.split

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.core.view.doOnPreDraw
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
import com.github.k1rakishou.kurobanewnavstacktest.utils.getBehaviorExt
import com.github.k1rakishou.kurobanewnavstacktest.utils.setBehaviorExt
import com.github.k1rakishou.kurobanewnavstacktest.viewcontroller.CollapsingViewController
import com.github.k1rakishou.kurobanewnavstacktest.widget.KurobaFloatingActionButton
import com.github.k1rakishou.kurobanewnavstacktest.widget.behavior.CatalogFabBehavior
import timber.log.Timber

@SuppressLint("TimberTagLength")
class SplitUiElementsController(
  args: Bundle? = null
) : BaseUiElementsController(args),
  UiElementsControllerCallbacks,
  ChanNavigationContract,
  RecyclerViewProvider {
  private lateinit var catalogFab: KurobaFloatingActionButton
  private lateinit var splitControllerCatalogControllerContainer: FrameLayout

  private val collapsingViewControllerMap =
    mutableMapOf<View, MutableMap<RecyclerView, CollapsingViewController>>()

  private var threadNavigationContract: ThreadNavigationContract? = null

  fun threadNavigationContract(threadNavigationContract: ThreadNavigationContract) {
    this.threadNavigationContract = threadNavigationContract
  }

  override fun instantiateView(
    inflater: LayoutInflater,
    container: ViewGroup,
    savedViewState: Bundle?
  ): View {
    return inflater.inflateView(R.layout.controller_split_catalog_ui_elements, container) {
      splitControllerCatalogControllerContainer = findViewById(R.id.split_controller_catalog_controller_container)
      bottomNavView = findViewById(R.id.split_controller_bottom_nav_view)
      toolbarContainer = findViewById(R.id.split_controller_toolbar_container)

      catalogFab = findViewById(R.id.split_controller_fab)
      catalogFab.setBehaviorExt(CatalogFabBehavior(context, null))
    }
  }

  override fun onControllerCreated(savedViewState: Bundle?) {
    super.onControllerCreated(savedViewState)

    bottomNavView.selectedItemId = R.id.action_browse

    bottomNavView.setOnNavigationItemSelectedListener { item ->
      if (bottomNavView.selectedItemId == item.itemId) {
        return@setOnNavigationItemSelectedListener true
      }

      splitControllerCatalogControllerContainer.switchTo(
        controller = createControllerBySelectedItemId(
          itemId = item.itemId,
          uiElementsControllerCallbacks = this
        )
      )

      return@setOnNavigationItemSelectedListener true
    }

    splitControllerCatalogControllerContainer.setupChildRouterIfNotSet(
      RouterTransaction.with(createSplitCatalogController(this))
    )
  }

  override fun onControllerShown() {
    super.onControllerShown()

    bottomNavView.doOnPreDraw {
      catalogFab.getBehaviorExt<CatalogFabBehavior>()?.init(bottomNavView)
      catalogFab.translationX = -KurobaFloatingActionButton.DEFAULT_MARGIN_RIGHT.toFloat()
    }
  }

  override fun onControllerHidden() {
    super.onControllerHidden()

    catalogFab.getBehaviorExt<CatalogFabBehavior>()?.reset()
  }

  override fun onControllerDestroyed() {
    super.onControllerDestroyed()

    threadNavigationContract = null
  }

  override fun provideRecyclerView(recyclerView: RecyclerView, controllerType: ControllerType) {
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

  override fun showFab() {
    catalogFab.show()
  }

  override fun hideFab() {
    catalogFab.hide()
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
    threadNavigationContract?.openThread(threadDescriptor)
  }

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
      R.id.action_browse -> createSplitCatalogController(uiElementsControllerCallbacks)
      R.id.action_settings -> createSettingsController(uiElementsControllerCallbacks)
      else -> throw IllegalStateException("Unknown itemId: $itemId")
    }
  }

  private fun createSplitCatalogController(
    uiElementsControllerCallbacks: UiElementsControllerCallbacks
  ): SplitCatalogController {
    return SplitCatalogController().apply {
      setUiElementsControllerCallbacks(uiElementsControllerCallbacks)
      recyclerViewProvider(this@SplitUiElementsController)
      threadNavigationContract(this@SplitUiElementsController)
    }
  }

  companion object {
    private const val TAG = "SplitUiElementsController"
    val CONTROLLER_TAG = ControllerTag("SplitUiElementsControllerTag")
  }
}