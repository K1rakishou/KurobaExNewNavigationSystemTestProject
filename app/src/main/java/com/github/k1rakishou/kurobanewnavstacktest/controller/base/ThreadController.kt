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
import com.github.k1rakishou.kurobanewnavstacktest.data.PostDescriptor
import com.github.k1rakishou.kurobanewnavstacktest.data.ThreadData
import com.github.k1rakishou.kurobanewnavstacktest.data.ThreadDescriptor
import com.github.k1rakishou.kurobanewnavstacktest.epoxy.epoxyTextView
import com.github.k1rakishou.kurobanewnavstacktest.epoxy.loadingView
import com.github.k1rakishou.kurobanewnavstacktest.epoxy.threadPostView
import com.github.k1rakishou.kurobanewnavstacktest.repository.ChanRepository
import com.github.k1rakishou.kurobanewnavstacktest.utils.BackgroundUtils
import com.github.k1rakishou.kurobanewnavstacktest.utils.addOneshotModelBuildListener
import com.github.k1rakishou.kurobanewnavstacktest.utils.setOnApplyWindowInsetsListenerAndDoRequest
import com.github.k1rakishou.kurobanewnavstacktest.viewstate.ViewStateConstants
import com.github.k1rakishou.kurobanewnavstacktest.widget.fab.KurobaFloatingActionButton
import com.github.k1rakishou.kurobanewnavstacktest.widget.toolbar.KurobaToolbarType
import com.github.k1rakishou.kurobanewnavstacktest.widget.toolbar.ToolbarAction
import com.github.k1rakishou.kurobanewnavstacktest.widget.toolbar.ToolbarContract
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collect
import timber.log.Timber
import java.lang.IllegalStateException

abstract class ThreadController(
  args: Bundle? = null
) : BaseController(args),
  ThreadNavigationContract {

  private val chanRepository = ChanRepository

  protected lateinit var threadRecyclerView: EpoxyRecyclerView
  protected lateinit var toolbarContract: ToolbarContract
  protected lateinit var uiElementsControllerCallbacks: UiElementsControllerCallbacks

  private var boundThreadDescriptor: ThreadDescriptor? = null
  private var job: Job? = null

  fun uiElementsControllerCallbacks(uiElementsControllerCallbacks: UiElementsControllerCallbacks) {
    this.uiElementsControllerCallbacks = uiElementsControllerCallbacks
  }

  fun toolbarContract(toolbarContract: ToolbarContract) {
    this.toolbarContract = toolbarContract

    launch {
      toolbarContract.listenForToolbarActions(KurobaToolbarType.Thread)
        .collect { toolbarAction -> onToolbarAction(toolbarAction) }
    }
  }

  override fun instantiateView(
    inflater: LayoutInflater,
    container: ViewGroup,
    savedViewState: Bundle?
  ): View {
    return inflater.inflateView(R.layout.controller_thread, container) {
      threadRecyclerView = findViewById(R.id.controller_thread_epoxy_recycler_view)

      findViewById<KurobaFloatingActionButton>(R.id.split_controller_thread_fab)!!.visibility = View.GONE
    }
  }

  override fun onControllerCreated(savedViewState: Bundle?) {
    super.onControllerCreated(savedViewState)

    applyInsetsForRecyclerView()
  }

  override fun onControllerDestroyed() {
    super.onControllerDestroyed()

    job?.cancel()
    job = null
  }

  override fun openThread(threadDescriptor: ThreadDescriptor) {
    Timber.tag(TAG).d("openThread($threadDescriptor)")
    this.boundThreadDescriptor = threadDescriptor

    job?.cancel()
    job = null

    job = launch {
      chanRepository.listenForThreadChanges(threadDescriptor)
        .collect { updatedThreadDescriptor ->
          rebuildThread(ThreadData.Data(chanRepository.getThreadPosts(updatedThreadDescriptor)))
        }
    }

    launch { reloadThread() }
  }

  private fun onToolbarAction(toolbarAction: ToolbarAction) {
    check(toolbarAction.toolbarType == KurobaToolbarType.Thread) {
      "Bad toolbarType: ${toolbarAction.toolbarType}"
    }

    when (toolbarAction) {
      is ToolbarAction.Catalog -> {
        throw IllegalStateException("ToolbarAction.Catalog does not belong to ThreadController")
      }
      is ToolbarAction.Thread -> {
        // no-op
      }
      is ToolbarAction.Search -> {
        val searchAction = toolbarAction as ToolbarAction.Search

        when (searchAction) {
          is ToolbarAction.Search.SearchShown -> {
            Timber.tag(TAG).d("SearchShown")
            uiElementsControllerCallbacks.lockUnlockCollapsableViews(
              recyclerView = threadRecyclerView,
              lock = true,
              animate = true
            )

            onSearchToolbarShown()
          }
          is ToolbarAction.Search.SearchHidden -> {
            Timber.tag(TAG).d("SearchHidden")
            uiElementsControllerCallbacks.lockUnlockCollapsableViews(
              recyclerView = threadRecyclerView,
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

    threadRecyclerView.setOnApplyWindowInsetsListenerAndDoRequest { v, insets ->
      v.updatePadding(
        top = toolbarHeight + insets.systemWindowInsetTop,
        bottom = bottomNavViewHeight + insets.systemWindowInsetBottom
      )

      return@setOnApplyWindowInsetsListenerAndDoRequest insets
    }
  }

  private suspend fun reloadThread() {
    if (!waitUntilAttached()) {
      return
    }

    val descriptor = boundThreadDescriptor
    if (descriptor == null) {
      rebuildThread(ThreadData.Empty)
      return
    }

    rebuildThread(ThreadData.Loading)

    chanRepository.loadThread(descriptor)
  }

  private fun rebuildThread(threadData: ThreadData) {
    BackgroundUtils.ensureMainThread()

    threadRecyclerView.withModels {
      addOneshotModelBuildListener {
        toolbarContract.showDefaultToolbar(KurobaToolbarType.Thread)
        onThreadStateChanged(threadData)
      }

      if (threadData is ThreadData.Empty) {
        epoxyTextView {
          id("thread_empty_view")
          message("No thread opened")
        }
        return@withModels
      }

      if (threadData is ThreadData.Loading) {
        loadingView {
          id("thread_loading_view")
        }
        return@withModels
      }

      val noPosts = threadData is ThreadData.Data && threadData.threadPosts.isEmpty()
      if (noPosts) {
        epoxyTextView {
          id("thread_empty_view")
          message("No posts in thread")
        }

        return@withModels
      }

      threadData as ThreadData.Data
      toolbarContract.setTitle(KurobaToolbarType.Thread, threadData.toThreadTitleString())

      // TODO(KurobaEx):
//          scrollbarMarksChildDecoration.setPosts(threadData.thread)

      threadData.threadPosts.forEach { post ->
        threadPostView {
          id("thread_thread_${post.postDescriptor}")
          color(post.color)
          postSelected(post.selected)
          comment(post.text)
          clickListener { chanRepository.selectUnSelectPost(post.postDescriptor) }
          imageClickListener { openImageViewer(post.postDescriptor) }
        }
      }
    }
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

  protected open fun onThreadStateChanged(threadData: ThreadData) {

  }

  companion object {
    private const val TAG = "ThreadController"
  }
}