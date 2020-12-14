package com.github.k1rakishou.kurobanewnavstacktest.controller.split

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.doOnPreDraw
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.epoxy.EpoxyRecyclerView
import com.github.k1rakishou.kurobanewnavstacktest.R
import com.github.k1rakishou.kurobanewnavstacktest.base.ControllerTag
import com.github.k1rakishou.kurobanewnavstacktest.feature.thread.ThreadController
import com.github.k1rakishou.kurobanewnavstacktest.controller.base.UiElementsControllerCallbacks
import com.github.k1rakishou.kurobanewnavstacktest.core.CollapsingViewsHolder
import com.github.k1rakishou.kurobanewnavstacktest.data.ThreadData
import com.github.k1rakishou.kurobanewnavstacktest.utils.*
import com.github.k1rakishou.kurobanewnavstacktest.viewcontroller.ViewScreenAttachSide
import com.github.k1rakishou.kurobanewnavstacktest.widget.behavior.SplitThreadFabBehavior
import com.github.k1rakishou.kurobanewnavstacktest.widget.fab.KurobaFloatingActionButton
import com.github.k1rakishou.kurobanewnavstacktest.widget.toolbar.KurobaToolbarType
import com.github.k1rakishou.kurobanewnavstacktest.widget.toolbar.NormalToolbar

class SplitThreadController(
  args: Bundle? = null
) : ThreadController(args), UiElementsControllerCallbacks {
  private lateinit var threadFab: KurobaFloatingActionButton

  private val collapsingViewsHolder = CollapsingViewsHolder()
  private val splitFabViewController by lazy { activityContract().mainActivityOrError().splitFabViewController }

  override fun instantiateView(
    inflater: LayoutInflater,
    container: ViewGroup,
    savedViewState: Bundle?
  ): View {
    return super.instantiateView(inflater, container, savedViewState).apply {
      threadFab = findViewById(R.id.split_controller_thread_fab)
      threadFab.visibility = View.VISIBLE
      threadFab.setBehaviorExt(SplitThreadFabBehavior(currentContext(), null))

      val normalToolbar = findViewById<NormalToolbar>(R.id.thread_controller_toolbar)
      normalToolbar.init(KurobaToolbarType.Thread)
      normalToolbar.visibility = View.VISIBLE

      super.toolbarContract(normalToolbar)
      super.uiElementsControllerCallbacks(this@SplitThreadController)

      splitFabViewController.initThreadFab(threadFab)
    }
  }

  override fun myHandleBack(): Boolean {
    if (toolbarContract.onBackPressed()) {
      return true
    }

    return super.myHandleBack()
  }

  override fun onControllerCreated(savedViewState: Bundle?) {
    super.onControllerCreated(savedViewState)

    threadFab.setOnApplyWindowInsetsListenerAndDoRequest { v, insets ->
      threadFab.updateMargins(
        end = KurobaFloatingActionButton.DEFAULT_MARGIN_RIGHT + insets.systemWindowInsetRight,
        bottom = KurobaFloatingActionButton.DEFAULT_MARGIN_RIGHT + insets.systemWindowInsetBottom
      )

      return@setOnApplyWindowInsetsListenerAndDoRequest insets
    }
  }

  override fun onControllerShown() {
    super.onControllerShown()

    threadFab.doOnPreDraw {
      threadFab.getBehaviorExt<SplitThreadFabBehavior>()?.apply {
        reset()
        init(threadFab)
      }
    }
  }

  override fun provideRecyclerView(recyclerView: EpoxyRecyclerView) {
    recyclerView.doOnPreDraw {
      collapsingViewsHolder.attach(
        recyclerView = recyclerView,
        collapsableView = toolbarContract.collapsableView(),
        viewAttachSide = ViewScreenAttachSide.Top
      )
    }
  }

  override fun withdrawRecyclerView(recyclerView: EpoxyRecyclerView) {
    collapsingViewsHolder.detach(recyclerView, toolbarContract.collapsableView())
  }

  override fun lockUnlockCollapsableViews(
    recyclerView: RecyclerView?,
    lock: Boolean,
    animate: Boolean
  ) {
    collapsingViewsHolder.lockUnlockCollapsableViews(
      recyclerView = recyclerView,
      lock = lock,
      animate = animate
    )
  }

  override fun showFab(lock: Boolean?) {
    threadFab.showFab(lock)
  }

  override fun hideFab(lock: Boolean?) {
    threadFab.hideFab(lock)
  }

  override fun onSearchToolbarShown() {
    splitFabViewController.onSearchToolbarShownOrHidden(controllerType, true)
  }

  override fun onSearchToolbarHidden() {
    splitFabViewController.onSearchToolbarShownOrHidden(controllerType, false)
  }

  override fun onThreadStateChanged(threadData: ThreadData) {
    splitFabViewController.onControllerStateChanged(
      controllerType,
      threadData is ThreadData.Data
    )
  }

  override fun getControllerTag(): ControllerTag = CONTROLLER_TAG

  companion object {
    val CONTROLLER_TAG = ControllerTag("SplitThreadControllerTag")
  }

}