package com.github.k1rakishou.kurobanewnavstacktest.controller.slide

import android.os.Bundle
import com.airbnb.epoxy.EpoxyRecyclerView
import com.github.k1rakishou.kurobanewnavstacktest.core.base.ControllerTag
import com.github.k1rakishou.kurobanewnavstacktest.controller.ControllerType
import com.github.k1rakishou.kurobanewnavstacktest.controller.FocusableController
import com.github.k1rakishou.kurobanewnavstacktest.controller.RecyclerViewProvider
import com.github.k1rakishou.kurobanewnavstacktest.feature.thread.ThreadController
import com.github.k1rakishou.kurobanewnavstacktest.data.ThreadData
import com.github.k1rakishou.kurobanewnavstacktest.data.ThreadDescriptor
import com.github.k1rakishou.kurobanewnavstacktest.viewstate.getThreadDescriptorOrNull
import com.github.k1rakishou.kurobanewnavstacktest.viewstate.putThreadDescriptor
import com.github.k1rakishou.kurobanewnavstacktest.widget.recycler.PaddingAwareRecyclerView

class SlideThreadController(
  args: Bundle? = null
) : ThreadController(args),
  FocusableController {
  private lateinit var recyclerViewProvider: RecyclerViewProvider
  private val slideFabViewController by lazy { activityContract().mainActivityOrError().slideFabViewController }

  fun recyclerViewProvider(recyclerViewProvider: RecyclerViewProvider) {
    this.recyclerViewProvider = recyclerViewProvider
  }

  override fun onControllerCreated(savedViewState: Bundle?) {
    super.onControllerCreated(savedViewState)

    args.getThreadDescriptorOrNull()?.let { threadDescriptor -> openThread(threadDescriptor) }
  }

  override fun provideRecyclerView(recyclerView: PaddingAwareRecyclerView) {
    recyclerViewProvider.provideRecyclerView(recyclerView, CONTROLLER_TYPE)
  }

  override fun withdrawRecyclerView(recyclerView: PaddingAwareRecyclerView) {
    recyclerViewProvider.withdrawRecyclerView(recyclerView, CONTROLLER_TYPE)
  }

  override fun onLostFocus() {

  }

  override fun onGainedFocus() {
    slideFabViewController.onControllerFocused(CONTROLLER_TYPE)
  }

  override fun onSearchToolbarShown() {
    slideFabViewController.onSearchToolbarShownOrHidden(CONTROLLER_TYPE, true)
  }

  override fun onSearchToolbarHidden() {
    slideFabViewController.onSearchToolbarShownOrHidden(CONTROLLER_TYPE, false)
  }

  override fun onThreadStateChanged(threadData: ThreadData) {
    slideFabViewController.onControllerStateChanged(
      CONTROLLER_TYPE,
      threadData is ThreadData.Data
    )
  }

  override fun getControllerTag(): ControllerTag = CONTROLLER_TAG

  companion object {
    val CONTROLLER_TAG = ControllerTag("SlideThreadControllerTag")

    private val CONTROLLER_TYPE = ControllerType.Thread

    fun create(threadDescriptor: ThreadDescriptor?): SlideThreadController {
      val args = Bundle()
      args.putThreadDescriptor(threadDescriptor)

      return SlideThreadController(args)
    }
  }
}