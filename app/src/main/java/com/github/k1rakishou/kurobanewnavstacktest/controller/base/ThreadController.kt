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
import com.github.k1rakishou.kurobanewnavstacktest.utils.setOnApplyWindowInsetsListenerAndDoRequest
import com.github.k1rakishou.kurobanewnavstacktest.viewstate.ViewStateConstants
import com.github.k1rakishou.kurobanewnavstacktest.widget.KurobaFloatingActionButton
import com.github.k1rakishou.kurobanewnavstacktest.widget.toolbar.ToolbarContract
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collect
import timber.log.Timber

abstract class ThreadController(args: Bundle? = null) : BaseController(args), ThreadNavigationContract {
  private val chanRepository = ChanRepository

  protected lateinit var recyclerView: EpoxyRecyclerView
  protected lateinit var toolbarContract: ToolbarContract

  private var threadDescriptor: ThreadDescriptor? = null
  private var job: Job? = null

  override fun instantiateView(
    inflater: LayoutInflater,
    container: ViewGroup,
    savedViewState: Bundle?
  ): View {
    return inflater.inflateView(R.layout.controller_thread, container) {
      recyclerView = findViewById(R.id.controller_thread_epoxy_recycler_view)
      toolbarContract = findViewById(R.id.thread_controller_toolbar)

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
    this.threadDescriptor = threadDescriptor

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

  private fun applyInsetsForRecyclerView() {
    val toolbarHeight = currentContext().resources.getDimension(R.dimen.toolbar_height).toInt()
    val bottomNavViewHeight =
      currentContext().resources.getDimension(R.dimen.bottom_nav_view_height).toInt()

    recyclerView.setOnApplyWindowInsetsListenerAndDoRequest { v, insets ->
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

    val descriptor = threadDescriptor
    if (descriptor == null) {
      rebuildThread(ThreadData.Empty)
      return
    }

    rebuildThread(ThreadData.Loading)
    chanRepository.loadThread(descriptor)
  }

  private fun rebuildThread(threadData: ThreadData) {
    recyclerView.withModels {
      val isEmpty = threadData is ThreadData.Data && threadData.thread.isEmpty()
      if (isEmpty) {
        loadingView {
          id("thread_loading_view")
        }

        return@withModels
      }

      when (threadData) {
        ThreadData.Empty -> {
          epoxyTextView {
            id("thread_empty_view")
            message("No thread opened")
          }
        }
        ThreadData.Loading -> {
          loadingView {
            id("thread_loading_view")
          }
        }
        is ThreadData.Data -> {
          // TODO(KurobaEx):
//          scrollbarMarksChildDecoration.setPosts(threadData.thread)

          threadData.thread.forEach { post ->
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
    }
  }

  private fun openImageViewer(postDescriptor: PostDescriptor) {
    val bundle = Bundle()
    bundle.putParcelable(ViewStateConstants.ImageViewController.postDescriptor, postDescriptor)

    val intent = Intent(currentContext(), ImageViewerActivity::class.java)
    intent.putExtras(bundle)

    startActivity(intent)
  }

  companion object {
    private const val TAG = "ThreadController"
  }
}