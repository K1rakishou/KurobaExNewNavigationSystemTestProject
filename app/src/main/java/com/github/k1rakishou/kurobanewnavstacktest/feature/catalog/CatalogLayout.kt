package com.github.k1rakishou.kurobanewnavstacktest.feature.catalog

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.AttributeSet
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.doOnPreDraw
import androidx.core.view.updatePadding
import com.airbnb.epoxy.EpoxyRecyclerView
import com.github.k1rakishou.kurobanewnavstacktest.R
import com.github.k1rakishou.kurobanewnavstacktest.activity.ImageViewerActivity
import com.github.k1rakishou.kurobanewnavstacktest.core.base.KurobaCoroutineScope
import com.github.k1rakishou.kurobanewnavstacktest.controller.base.UiElementsControllerCallbacks
import com.github.k1rakishou.kurobanewnavstacktest.core.test.TestHelpers
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

class CatalogLayout @JvmOverloads constructor(
  context: Context,
  attributeSet: AttributeSet? = null,
  attrDefStyle: Int = 0
) : ConstraintLayout(context, attributeSet, attrDefStyle) {
  private val catalogRecyclerView: EpoxyRecyclerView
  private val chanRepository = ChanRepository
  private val kurobaCoroutineScope = KurobaCoroutineScope()

  private var job: Job? = null
  private var boundBoardDescriptor: BoardDescriptor? = null
  private var toolbarContract: ToolbarContract? = null
  private var catalogControllerCallbacks: CatalogControllerCallbacks? = null
  private var uiElementsControllerCallbacks: UiElementsControllerCallbacks? = null
  private var testHelpers: TestHelpers? = null
  private var catalogViewModel: CatalogViewModel? = null

  init {
    inflate(context, R.layout.controller_catalog, this).apply {
      catalogRecyclerView = findViewById(R.id.controller_catalog_epoxy_recycler_view)
    }
  }

  fun onCreate(
    toolbarContract: ToolbarContract,
    uiElementsControllerCallbacks: UiElementsControllerCallbacks,
    catalogControllerCallbacks: CatalogControllerCallbacks,
    catalogViewModel: CatalogViewModel,
    testHelpers: TestHelpers
  ) {
    this.toolbarContract = toolbarContract
    this.uiElementsControllerCallbacks = uiElementsControllerCallbacks
    this.catalogControllerCallbacks = catalogControllerCallbacks
    this.testHelpers = testHelpers
    this.catalogViewModel = catalogViewModel

    kurobaCoroutineScope.launch {
      toolbarContract.listenForToolbarActions(KurobaToolbarType.Catalog)
        .collect { toolbarAction -> onToolbarAction(toolbarAction) }
    }

    applyInsetsForRecyclerView()

    catalogRecyclerView.doOnPreDraw {
      catalogControllerCallbacks.provideRecyclerView(catalogRecyclerView)
    }

    // TODO: remove me!!! vvv
    val boardDescriptor = BoardDescriptor(SiteDescriptor("4chan.org"), "g")
    openBoard(boardDescriptor)
    // TODO: remove me!!! ^^^
  }

  fun onDestroy() {
    // TODO(KurobaEx): close catalog if boundBoardDescriptor != null
    catalogControllerCallbacks?.withdrawRecyclerView(catalogRecyclerView)

    boundBoardDescriptor = null
    toolbarContract = null
    catalogControllerCallbacks = null
    uiElementsControllerCallbacks = null
    testHelpers = null
    catalogViewModel = null

    catalogRecyclerView.swapAdapter(
      adapter = null,
      removeAndRecycleExistingViews = true
    )

    kurobaCoroutineScope.cancelChildren()
  }

  fun onBackPressed(): Boolean {
    return false
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
            uiElementsControllerCallbacks?.lockUnlockCollapsableViews(
              recyclerView = catalogRecyclerView,
              lock = true,
              animate = true
            )

            catalogControllerCallbacks?.onSearchToolbarShown()
          }
          is ToolbarAction.Search.SearchHidden -> {
            Timber.tag(TAG).d("SearchHidden")
            uiElementsControllerCallbacks?.lockUnlockCollapsableViews(
              recyclerView = catalogRecyclerView,
              lock = false,
              animate = true
            )

            catalogControllerCallbacks?.onSearchToolbarHidden()
          }
          is ToolbarAction.Search.QueryUpdated -> {
            Timber.tag(TAG).d("QueryUpdated")
          }
        }
      }
    }
  }

  fun openBoard(boardDescriptor: BoardDescriptor) {
    Timber.tag(TAG).d("openBoard($boardDescriptor)")
    this.boundBoardDescriptor = boardDescriptor

    job?.cancel()
    job = null

    job = kurobaCoroutineScope.launch {
      catalogControllerCallbacks?.waitUntilAttached()
        ?: return@launch

      chanRepository.listenForCatalogChanges(boardDescriptor)
        .collect { updatedBoardDescriptor ->
          rebuildCatalog(CatalogData.Data(chanRepository.getCatalogThreads(updatedBoardDescriptor)))
        }
    }

    kurobaCoroutineScope.launch { reloadCatalog() }
  }

  private suspend fun reloadCatalog() {
    if (catalogControllerCallbacks?.waitUntilAttached() == false) {
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
        toolbarContract?.showDefaultToolbar(KurobaToolbarType.Catalog)
        catalogControllerCallbacks?.onCatalogStateChanged(catalogData)
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

      toolbarContract?.setTitle(KurobaToolbarType.Catalog, catalogData.toCatalogTitleString())
      toolbarContract?.setSubTitle(context.getString(R.string.lorem_ipsum))

      addOneshotModelBuildListener {
        testHelpers?.catalogLoadedLatch?.countDown()
      }

      val error = catalogData.error
      if (error != null) {
        catalogControllerCallbacks?.showToast(error.errorMessageOrClassName())
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
    catalogControllerCallbacks?.openThread(threadDescriptor)
  }

  private fun openImageViewer(postDescriptor: PostDescriptor) {
    val bundle = Bundle()
    bundle.putParcelable(ViewStateConstants.ImageViewController.postDescriptor, postDescriptor)

    val intent = Intent(context, ImageViewerActivity::class.java)
    intent.putExtras(bundle)

    context.startActivity(intent)
  }

  private fun applyInsetsForRecyclerView() {
    val toolbarHeight = context.resources.getDimension(R.dimen.toolbar_height).toInt()
    val bottomNavViewHeight =
      context.resources.getDimension(R.dimen.bottom_nav_panel_height).toInt()

    catalogRecyclerView.setOnApplyWindowInsetsListenerAndDoRequest { v, insets ->
      v.updatePadding(
        top = toolbarHeight + insets.systemWindowInsetTop,
        bottom = bottomNavViewHeight + insets.systemWindowInsetBottom
      )

      return@setOnApplyWindowInsetsListenerAndDoRequest insets
    }
  }

  interface CatalogControllerCallbacks {
    suspend fun waitUntilAttached(): Boolean

    fun provideRecyclerView(recyclerView: EpoxyRecyclerView)
    fun withdrawRecyclerView(recyclerView: EpoxyRecyclerView)

    fun onSearchToolbarShown()
    fun onSearchToolbarHidden()
    fun onCatalogStateChanged(catalogData: CatalogData)
    fun showToast(message: String)
    fun openThread(threadDescriptor: ThreadDescriptor)
  }

  companion object {
    private const val TAG = "CatalogLayout"
  }

}