package com.github.k1rakishou.kurobanewnavstacktest.feature.thread

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.AttributeSet
import android.view.View
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.doOnPreDraw
import androidx.core.view.updatePadding
import com.github.k1rakishou.kurobanewnavstacktest.R
import com.github.k1rakishou.kurobanewnavstacktest.activity.ImageViewerActivity
import com.github.k1rakishou.kurobanewnavstacktest.controller.ControllerType
import com.github.k1rakishou.kurobanewnavstacktest.controller.base.UiElementsControllerCallbacks
import com.github.k1rakishou.kurobanewnavstacktest.core.base.KurobaCoroutineScope
import com.github.k1rakishou.kurobanewnavstacktest.core.test.TestHelpers
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
import com.github.k1rakishou.kurobanewnavstacktest.viewmodel.MainControllerViewModel
import com.github.k1rakishou.kurobanewnavstacktest.viewstate.ViewStateConstants
import com.github.k1rakishou.kurobanewnavstacktest.widget.fab.KurobaFloatingActionButton
import com.github.k1rakishou.kurobanewnavstacktest.widget.recycler.PaddingAwareRecyclerView
import com.github.k1rakishou.kurobanewnavstacktest.widget.toolbar.KurobaToolbarType
import com.github.k1rakishou.kurobanewnavstacktest.widget.toolbar.ToolbarAction
import com.github.k1rakishou.kurobanewnavstacktest.widget.toolbar.ToolbarContract
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collect
import timber.log.Timber

class ThreadLayout @JvmOverloads constructor(
  context: Context,
  attributeSet: AttributeSet? = null,
  attrDefStyle: Int = 0
) : CoordinatorLayout(context, attributeSet, attrDefStyle) {
  private val chanRepository = ChanRepository
  private val kurobaCoroutineScope = KurobaCoroutineScope()
  private val threadRecyclerView: PaddingAwareRecyclerView

  private var job: Job? = null
  private var boundThreadDescriptor: ThreadDescriptor? = null
  private var toolbarContract: ToolbarContract? = null
  private var threadControllerCallbacks: ThreadControllerCallbacks? = null
  private var uiElementsControllerCallbacks: UiElementsControllerCallbacks? = null
  private var threadViewModel: ThreadViewModel? = null
  private var mainControllerViewModel: MainControllerViewModel? = null
  private var testHelpers: TestHelpers? = null

  init {
    inflate(context, R.layout.controller_thread, this).apply {
      threadRecyclerView = findViewById(R.id.controller_thread_epoxy_recycler_view)

      findViewById<KurobaFloatingActionButton>(R.id.split_controller_thread_fab)
        ?.let { fab -> fab.visibility = View.GONE }
    }
  }

  fun onCreate(
    toolbarContract: ToolbarContract,
    uiElementsControllerCallbacks: UiElementsControllerCallbacks,
    threadControllerCallbacks: ThreadControllerCallbacks,
    threadViewModel: ThreadViewModel,
    mainControllerViewModel: MainControllerViewModel,
    testHelpers: TestHelpers
  ) {
    this.toolbarContract = toolbarContract
    this.uiElementsControllerCallbacks = uiElementsControllerCallbacks
    this.threadControllerCallbacks = threadControllerCallbacks
    this.threadViewModel = threadViewModel
    this.mainControllerViewModel = mainControllerViewModel
    this.testHelpers = testHelpers

    kurobaCoroutineScope.launch { reloadThread() }

    kurobaCoroutineScope.launch {
      // HACK! We want to first subscribe to flow, and then call restoreLastToolbarActions().
      // If we call restoreLastToolbarActions() after collect() flow won't have any subscribers so
      // all even emitted by the result of calling restoreLastToolbarActions() will be ignored.
      kurobaCoroutineScope.launch { toolbarContract.restoreLastToolbarActions(KurobaToolbarType.Thread) }

      toolbarContract.listenForToolbarActions(KurobaToolbarType.Thread)
        .collect { toolbarAction -> onToolbarAction(toolbarAction) }
    }

    applyInsetsForRecyclerView()

    threadRecyclerView.doOnPreDraw {
      threadControllerCallbacks.provideRecyclerView(threadRecyclerView)
    }
  }

  fun onDestroy() {
    // TODO(KurobaEx): close thread if boundThreadDescriptor != null
    threadControllerCallbacks?.withdrawRecyclerView(threadRecyclerView)

    job?.cancel()
    job = null

    boundThreadDescriptor = null
    toolbarContract = null
    threadControllerCallbacks = null
    uiElementsControllerCallbacks = null
    threadViewModel = null
    testHelpers = null

    threadRecyclerView.swapAdapter(
      adapter = null,
      removeAndRecycleExistingViews = true
    )

    kurobaCoroutineScope.cancelChildren()
  }

  fun onBackPressed(): Boolean {
    if (closeOpenedThread()) {
      return true
    }

    return false
  }

  private fun closeOpenedThread(): Boolean {
    BackgroundUtils.ensureMainThread()

    if (boundThreadDescriptor == null) {
      return false
    }

    Timber.tag(TAG).d("closeOpenedThread()")

    boundThreadDescriptor = null
    toolbarContract?.closeToolbar(KurobaToolbarType.Thread)
    mainControllerViewModel?.lastOpenedThread = null

    job?.cancel()
    job = null

    kurobaCoroutineScope.launch { reloadThread() }
    return true
  }

  fun openThread(threadDescriptor: ThreadDescriptor) {
    BackgroundUtils.ensureMainThread()

    if (boundThreadDescriptor == threadDescriptor) {
      return
    }

    Timber.tag(TAG).d("openThread($threadDescriptor)")
    boundThreadDescriptor = threadDescriptor
    mainControllerViewModel?.lastOpenedThread = threadDescriptor

    job?.cancel()
    job = null

    job = kurobaCoroutineScope.launch {
      chanRepository.listenForThreadChanges(threadDescriptor)
        .collect { updatedThreadDescriptor ->
          rebuildThread(ThreadData.Data(chanRepository.getThreadPosts(updatedThreadDescriptor)))
        }
    }

    kurobaCoroutineScope.launch { reloadThread() }
  }

  private fun onToolbarAction(toolbarAction: ToolbarAction) {
    Timber.tag(TAG).d("onToolbarAction($toolbarAction)")

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
        when (val searchAction = toolbarAction as ToolbarAction.Search) {
          is ToolbarAction.Search.SearchShown -> onToolbarSearchShown(searchAction)
          is ToolbarAction.Search.SearchHidden -> onToolbarSearchHidden(searchAction)
          is ToolbarAction.Search.QueryUpdated -> onToolbarSearchQueryUpdated(searchAction)
        }
      }
    }
  }

  private fun onToolbarSearchQueryUpdated(searchAction: ToolbarAction.Search) {
    Timber.tag(TAG).d("QueryUpdated")

    // TODO(KurobaEx):
  }

  private fun onToolbarSearchHidden(searchAction: ToolbarAction.Search) {
    Timber.tag(TAG).d("SearchHidden")
    uiElementsControllerCallbacks?.toolbarSearchVisibilityChanged(
      controllerType = ControllerType.Thread,
      toolbarSearchVisible = false
    )

    threadControllerCallbacks?.onSearchToolbarHidden()
  }

  private fun onToolbarSearchShown(searchAction: ToolbarAction.Search) {
    Timber.tag(TAG).d("SearchShown")
    uiElementsControllerCallbacks?.toolbarSearchVisibilityChanged(
      controllerType = ControllerType.Thread,
      toolbarSearchVisible = true
    )

    threadControllerCallbacks?.onSearchToolbarShown()
  }

  private fun applyInsetsForRecyclerView() {
    val toolbarHeight = context.resources.getDimension(R.dimen.toolbar_height).toInt()
    val bottomNavViewHeight =
      context.resources.getDimension(R.dimen.bottom_nav_panel_height).toInt()

    threadRecyclerView.setOnApplyWindowInsetsListenerAndDoRequest { v, insets ->
      v.updatePadding(
        top = toolbarHeight + insets.systemWindowInsetTop,
        bottom = bottomNavViewHeight + insets.systemWindowInsetBottom
      )

      return@setOnApplyWindowInsetsListenerAndDoRequest insets
    }
  }

  private suspend fun reloadThread() {
    if (threadControllerCallbacks?.waitUntilAttached() == false) {
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
        threadControllerCallbacks?.onThreadStateChanged(threadData)
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

      addOneshotModelBuildListener {
        toolbarContract?.showDefaultToolbar(KurobaToolbarType.Thread)
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
      toolbarContract?.setTitle(KurobaToolbarType.Thread, threadData.toThreadTitleString())

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

    val intent = Intent(context, ImageViewerActivity::class.java)
    intent.putExtras(bundle)

    context.startActivity(intent)
  }

  interface ThreadControllerCallbacks {
    fun provideRecyclerView(recyclerView: PaddingAwareRecyclerView)
    fun withdrawRecyclerView(recyclerView: PaddingAwareRecyclerView)

    fun onSearchToolbarShown()
    fun onSearchToolbarHidden()
    suspend fun waitUntilAttached(): Boolean
    fun onThreadStateChanged(threadData: ThreadData)
  }

  companion object {
    private const val TAG = "ThreadLayout"
  }
}