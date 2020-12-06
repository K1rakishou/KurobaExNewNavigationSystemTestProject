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
import com.github.k1rakishou.kurobanewnavstacktest.base.BaseController
import com.github.k1rakishou.kurobanewnavstacktest.data.BoardDescriptor
import com.github.k1rakishou.kurobanewnavstacktest.data.CatalogData
import com.github.k1rakishou.kurobanewnavstacktest.data.PostDescriptor
import com.github.k1rakishou.kurobanewnavstacktest.data.ThreadDescriptor
import com.github.k1rakishou.kurobanewnavstacktest.epoxy.catalogThreadView
import com.github.k1rakishou.kurobanewnavstacktest.epoxy.epoxyTextView
import com.github.k1rakishou.kurobanewnavstacktest.epoxy.loadingView
import com.github.k1rakishou.kurobanewnavstacktest.repository.ChanRepository
import com.github.k1rakishou.kurobanewnavstacktest.utils.errorMessageOrClassName
import com.github.k1rakishou.kurobanewnavstacktest.utils.setOnApplyWindowInsetsListenerAndRequest
import com.github.k1rakishou.kurobanewnavstacktest.viewstate.ViewStateConstants
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collect
import timber.log.Timber

abstract class CatalogController(
  args: Bundle? = null
) : BaseController(args) {
  private val chanRepository = ChanRepository

  protected lateinit var recyclerView: EpoxyRecyclerView

  private var boardDescriptor: BoardDescriptor? = null
  private var job: Job? = null

  final override fun instantiateView(
    inflater: LayoutInflater,
    container: ViewGroup,
    savedViewState: Bundle?
  ): View {
    return inflater.inflateView(R.layout.controller_catalog, container) {
      recyclerView = findViewById(R.id.controller_catalog_epoxy_recycler_view)
    }
  }

  override fun onControllerCreated(savedViewState: Bundle?) {
    super.onControllerCreated(savedViewState)

    applyInsetsForRecyclerView()

    launch {
      chanRepository.listenForBoardOpenUpdates()
        .collect { boardDescriptor ->
          if (boardDescriptor == null) {
            return@collect
          }

          openBoard(boardDescriptor)
        }
    }

    // TODO: remove me!!! vvv
    val boardDescriptor = BoardDescriptor("test")
    chanRepository.openBoard(boardDescriptor)
    // TODO: remove me!!! ^^^
  }

  override fun onControllerDestroyed() {
    super.onControllerDestroyed()

    job?.cancel()
    job = null
  }

  private fun applyInsetsForRecyclerView() {
    val toolbarHeight = currentContext().resources.getDimension(R.dimen.toolbar_height).toInt()
    val bottomNavViewHeight =
      currentContext().resources.getDimension(R.dimen.bottom_nav_view_height).toInt()

    recyclerView.setOnApplyWindowInsetsListenerAndRequest { v, insets ->
      v.updatePadding(
        top = toolbarHeight + insets.systemWindowInsetTop,
        bottom = bottomNavViewHeight + insets.systemWindowInsetBottom
      )

      return@setOnApplyWindowInsetsListenerAndRequest insets
    }
  }

  private fun openBoard(boardDescriptor: BoardDescriptor) {
    Timber.tag(TAG).d("openBoard($boardDescriptor)")
    this.boardDescriptor = boardDescriptor

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

    val descriptor = boardDescriptor
    if (descriptor == null) {
      rebuildCatalog(CatalogData.Empty)
      return
    }

    rebuildCatalog(CatalogData.Loading)
    chanRepository.loadBoard(descriptor)
  }

  private fun rebuildCatalog(catalogData: CatalogData) {
    recyclerView.withModels {
      val isEmpty = catalogData is CatalogData.Data && catalogData.catalog.isEmpty()
      if (isEmpty) {
        loadingView {
          id("catalog_loading_view")
        }

        return@withModels
      }

      when (catalogData) {
        CatalogData.Empty -> {
          epoxyTextView {
            id("catalog_empty_view")
            message("No board opened")
          }
        }
        CatalogData.Loading -> {
          loadingView {
            id("catalog_loading_view")
          }
        }
        is CatalogData.Data -> {
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
    }
  }

  private fun openThread(threadDescriptor: ThreadDescriptor) {
    chanRepository.openThread(threadDescriptor)
  }

  private fun openImageViewer(postDescriptor: PostDescriptor) {
    val bundle = Bundle()
    bundle.putParcelable(ViewStateConstants.ImageViewController.postDescriptor, postDescriptor)

    val intent = Intent(currentContext(), ImageViewerActivity::class.java)
    intent.putExtras(bundle)

    startActivity(intent)
  }

  companion object {
    private const val TAG = "CatalogController"
  }
}