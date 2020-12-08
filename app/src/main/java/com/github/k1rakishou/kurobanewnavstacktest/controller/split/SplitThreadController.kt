package com.github.k1rakishou.kurobanewnavstacktest.controller.split

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.doOnPreDraw
import com.github.k1rakishou.kurobanewnavstacktest.R
import com.github.k1rakishou.kurobanewnavstacktest.base.ControllerTag
import com.github.k1rakishou.kurobanewnavstacktest.controller.ControllerType
import com.github.k1rakishou.kurobanewnavstacktest.controller.base.ThreadController
import com.github.k1rakishou.kurobanewnavstacktest.core.CollapsingViewsHolder
import com.github.k1rakishou.kurobanewnavstacktest.utils.*
import com.github.k1rakishou.kurobanewnavstacktest.viewcontroller.ViewScreenAttachSide
import com.github.k1rakishou.kurobanewnavstacktest.widget.KurobaFloatingActionButton
import com.github.k1rakishou.kurobanewnavstacktest.widget.behavior.SplitThreadFabBehavior
import com.github.k1rakishou.kurobanewnavstacktest.widget.toolbar.NormalToolbar
import com.github.k1rakishou.kurobanewnavstacktest.widget.toolbar.ToolbarContract

class SplitThreadController(
  args: Bundle? = null
) : ThreadController(args) {

  private lateinit var threadFab: KurobaFloatingActionButton
  private lateinit var toolbarContract: ToolbarContract

  private val collapsingViewsHolder = CollapsingViewsHolder()

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
      normalToolbar.init(ControllerType.Thread)
      normalToolbar.visibility = View.VISIBLE
      toolbarContract = normalToolbar
    }
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

    recyclerView.doOnPreDraw {
      collapsingViewsHolder.attach(
        recyclerView = recyclerView,
        collapsableView = toolbarContract.collapsableView(),
        controllerType = ControllerType.Thread,
        viewAttachSide = ViewScreenAttachSide.Top
      )

      collapsingViewsHolder.lockUnlockCollapsableViews(
        lock = ChanSettings.showLockCollapsableViews(currentContext()),
        animate = true
      )
    }

    threadFab.doOnPreDraw {
      threadFab.getBehaviorExt<SplitThreadFabBehavior>()?.apply {
        reset()
        init(threadFab)
      }
    }
  }

  override fun onControllerHidden() {
    super.onControllerHidden()

    collapsingViewsHolder.detach(recyclerView, toolbarContract.collapsableView())
  }

  override fun setToolbarTitle(title: String) {
    toolbarContract.setTitle(ControllerType.Thread, title)
  }

  override fun getControllerTag(): ControllerTag = CONTROLLER_TAG

  companion object {
    val CONTROLLER_TAG = ControllerTag("SplitThreadControllerTag")
  }

}