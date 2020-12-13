package com.github.k1rakishou.kurobanewnavstacktest.controller.base

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.updatePadding
import com.airbnb.epoxy.EpoxyRecyclerView
import com.github.k1rakishou.kurobanewnavstacktest.R
import com.github.k1rakishou.kurobanewnavstacktest.activity.ImageViewerActivity
import com.github.k1rakishou.kurobanewnavstacktest.activity.MainActivity
import com.github.k1rakishou.kurobanewnavstacktest.base.BaseController
import com.github.k1rakishou.kurobanewnavstacktest.controller.ControllerType
import com.github.k1rakishou.kurobanewnavstacktest.data.*
import com.github.k1rakishou.kurobanewnavstacktest.epoxy.catalogThreadView
import com.github.k1rakishou.kurobanewnavstacktest.epoxy.epoxyTextView
import com.github.k1rakishou.kurobanewnavstacktest.epoxy.loadingView
import com.github.k1rakishou.kurobanewnavstacktest.repository.ChanRepository
import com.github.k1rakishou.kurobanewnavstacktest.utils.BackgroundUtils
import com.github.k1rakishou.kurobanewnavstacktest.utils.addOneshotModelBuildListener
import com.github.k1rakishou.kurobanewnavstacktest.utils.errorMessageOrClassName
import com.github.k1rakishou.kurobanewnavstacktest.utils.setOnApplyWindowInsetsListenerAndDoRequest
import com.github.k1rakishou.kurobanewnavstacktest.viewstate.ViewStateConstants
import com.github.k1rakishou.kurobanewnavstacktest.widget.toolbar.KurobaToolbarType
import com.github.k1rakishou.kurobanewnavstacktest.widget.toolbar.ToolbarAction
import com.github.k1rakishou.kurobanewnavstacktest.widget.toolbar.ToolbarContract
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collect
import timber.log.Timber
import java.lang.IllegalStateException

abstract class CatalogController(
  args: Bundle? = null
) : BaseController(args),
  CatalogNavigationContract {

  private val chanRepository = ChanRepository
  private val testHelpers by lazy { (activity as MainActivity).testHelpers }

  protected lateinit var catalogRecyclerView: EpoxyRecyclerView
  protected lateinit var uiElementsControllerCallbacks: UiElementsControllerCallbacks

  protected val controllerType = ControllerType.Catalog

  private var toolbarContract: ToolbarContract? = null
  private var threadNavigationContract: ThreadNavigationContract? = null
  private var boundBoardDescriptor: BoardDescriptor? = null
  private var job: Job? = null

  fun uiElementsControllerCallbacks(uiElementsControllerCallbacks: UiElementsControllerCallbacks) {
    this.uiElementsControllerCallbacks = uiElementsControllerCallbacks
  }

  fun threadNavigationContract(threadNavigationContract: ThreadNavigationContract) {
    this.threadNavigationContract = threadNavigationContract
  }

  fun toolbarContract(toolbarContract: ToolbarContract) {
    this.toolbarContract = toolbarContract
  }

  final override fun instantiateView(
    inflater: LayoutInflater,
    container: ViewGroup,
    savedViewState: Bundle?
  ): View {
    return inflater.inflateView(R.layout.controller_catalog, container) {
      catalogRecyclerView = findViewById(R.id.controller_catalog_epoxy_recycler_view)
    }
  }

  override fun onControllerCreated(savedViewState: Bundle?) {
    super.onControllerCreated(savedViewState)
    checkNotNull(toolbarContract) { "toolbarContract is null" }

    launch {
      toolbarContract!!.listenForToolbarActions(KurobaToolbarType.Catalog)
        .collect { toolbarAction -> onToolbarAction(toolbarAction) }
    }

    applyInsetsForRecyclerView()

    // TODO: remove me!!! vvv
    val boardDescriptor = BoardDescriptor(SiteDescriptor("4chan.org"), "g")
    openBoard(boardDescriptor)
    // TODO: remove me!!! ^^^
  }

  override fun onControllerDestroyed() {
    super.onControllerDestroyed()

    threadNavigationContract = null

    job?.cancel()
    job = null
  }

  private fun onToolbarAction(toolbarAction: ToolbarAction) {
    check(toolbarAction.toolbarType == KurobaToolbarType.Catalog) {
      "Bad toolbarType: ${toolbarAction.toolbarType}"
    }

    when (toolbarAction) {
      is ToolbarAction.Catalog -> {
        // no-op
      }
      is ToolbarAction.Thread -> {
        throw IllegalStateException("ToolbarAction.Thread does not belong to CatalogController")
      }
      is ToolbarAction.Search -> {
        val searchAction = toolbarAction as ToolbarAction.Search

        when (searchAction) {
          is ToolbarAction.Search.SearchShown -> {
            Timber.tag(TAG).d("SearchShown")
            uiElementsControllerCallbacks.lockUnlockCollapsableViews(
              recyclerView = catalogRecyclerView,
              lock = true,
              animate = true
            )

            onSearchToolbarShown()
          }
          is ToolbarAction.Search.SearchHidden -> {
            Timber.tag(TAG).d("SearchHidden")
            uiElementsControllerCallbacks.lockUnlockCollapsableViews(
              recyclerView = catalogRecyclerView,
              lock = false,
              animate = true
            )

            onSearchToolbarHidden()
          }
          is ToolbarAction.Search.QueryUpdated -> {
            Timber.tag(TAG).d("QueryUpdated")
          }
        }
      }
    }
  }

  private fun applyInsetsForRecyclerView() {
    val toolbarHeight = currentContext().resources.getDimension(R.dimen.toolbar_height).toInt()
    val bottomNavViewHeight =
      currentContext().resources.getDimension(R.dimen.bottom_nav_view_height).toInt()

    catalogRecyclerView.setOnApplyWindowInsetsListenerAndDoRequest { v, insets ->
      v.updatePadding(
        top = toolbarHeight + insets.systemWindowInsetTop,
        bottom = bottomNavViewHeight + insets.systemWindowInsetBottom
      )

      return@setOnApplyWindowInsetsListenerAndDoRequest insets
    }
  }

  override fun openBoard(boardDescriptor: BoardDescriptor) {
    Timber.tag(TAG).d("openBoard($boardDescriptor)")
    this.boundBoardDescriptor = boardDescriptor

    job?.cancel()
    job = null

    job = launch {
      waitUntilAttached()

      chanRepository.listenForCatalogChanges(boardDescriptor)
        .collect { updatedBoardDescriptor ->
          rebuildCatalog(CatalogData.Data(chanRepository.getCatalogThreads(updatedBoardDescriptor)))
        }
    }

    launch { reloadCatalog() }
  }

  private suspend fun reloadCatalog() {
    if (!waitUntilAttached()) {
      return
    }

    val descriptor = boundBoardDescriptor
    if (descriptor == null) {
      rebuildCatalog(CatalogData.Empty)
      return
    }

    rebuildCatalog(CatalogData.Loading)

    chanRepository.loadBoard(descriptor)
  }

  private fun rebuildCatalog(catalogData: CatalogData) {
    BackgroundUtils.ensureMainThread()

    catalogRecyclerView.withModels {
      addOneshotModelBuildListener {
        toolbarContract!!.showDefaultToolbar(KurobaToolbarType.Catalog)
        onCatalogStateChanged(catalogData)
      }

      if (catalogData is CatalogData.Empty) {
        epoxyTextView {
          id("catalog_empty_view")
          message("No board opened")
        }
        return@withModels
      }

      if (catalogData is CatalogData.Loading) {
        loadingView {
          id("catalog_loading_view")
        }
        return@withModels
      }

      val noThreads = catalogData is CatalogData.Data && catalogData.catalog.isEmpty()
      if (noThreads) {
        epoxyTextView {
          id("catalog_no_posts_view")
          message("Board has no threads")
        }

        return@withModels
      }

      catalogData as CatalogData.Data

      toolbarContract!!.setTitle(KurobaToolbarType.Catalog, catalogData.toCatalogTitleString())
      toolbarContract!!.setSubTitle(currentContext().getString(R.string.lorem_ipsum))

      addOneshotModelBuildListener {
        testHelpers.catalogLoadedLatch.countDown()
      }

      val error = catalogData.error
      if (error != null) {
        showToast(error.errorMessageOrClassName())
      }

      catalogData.catalog.forEach { post ->
        catalogThreadView {
          id("catalog_thread_${post.postDescriptor.postNo}")
          color(post.color)
          comment(post.text)
          clickListener { openThread(post.postDescriptor.threadDescriptor) }
          imageClickListener { openImageViewer(post.postDescriptor) }
        }
      }
    }
  }

  private fun openThread(threadDescriptor: ThreadDescriptor) {
    threadNavigationContract?.openThread(threadDescriptor)
  }

  private fun openImageViewer(postDescriptor: PostDescriptor) {
    val bundle = Bundle()
    bundle.putParcelable(ViewStateConstants.ImageViewController.postDescriptor, postDescriptor)

    val intent = Intent(currentContext(), ImageViewerActivity::class.java)
    intent.putExtras(bundle)

    startActivity(intent)
  }

  protected open fun onSearchToolbarShown() {

  }

  protected open fun onSearchToolbarHidden() {

  }

  protected open fun onCatalogStateChanged(catalogData: CatalogData) {

  }

  companion object {
    private const val TAG = "CatalogController"
  }
}