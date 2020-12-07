package com.github.k1rakishou.kurobanewnavstacktest.controller.split

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.doOnPreDraw
import androidx.recyclerview.widget.RecyclerView
import com.github.k1rakishou.kurobanewnavstacktest.R
import com.github.k1rakishou.kurobanewnavstacktest.base.ControllerTag
import com.github.k1rakishou.kurobanewnavstacktest.controller.ControllerType
import com.github.k1rakishou.kurobanewnavstacktest.controller.base.ThreadController
import com.github.k1rakishou.kurobanewnavstacktest.utils.*
import com.github.k1rakishou.kurobanewnavstacktest.viewcontroller.CollapsingViewController
import com.github.k1rakishou.kurobanewnavstacktest.widget.KurobaFloatingActionButton
import com.github.k1rakishou.kurobanewnavstacktest.widget.behavior.SplitThreadFabBehavior

class SplitThreadController(
  args: Bundle? = null
) : ThreadController(args) {
  private lateinit var threadFab: KurobaFloatingActionButton

  private val collapsingViewControllerMap =
    mutableMapOf<View, MutableMap<RecyclerView, CollapsingViewController>>()

  override fun instantiateView(
    inflater: LayoutInflater,
    container: ViewGroup,
    savedViewState: Bundle?
  ): View {
    return super.instantiateView(inflater, container, savedViewState).apply {
      threadFab = findViewById(R.id.split_controller_thread_fab)
      threadFab.visibility = View.VISIBLE
      threadFab.setBehaviorExt(SplitThreadFabBehavior(currentContext(), null))
    }
  }

  override fun onControllerCreated(savedViewState: Bundle?) {
    super.onControllerCreated(savedViewState)

    threadFab.setOnApplyWindowInsetsListenerAndRequest { v, insets ->
      threadFab.updateMargins(
        end = KurobaFloatingActionButton.DEFAULT_MARGIN_RIGHT + insets.systemWindowInsetRight,
        bottom = KurobaFloatingActionButton.DEFAULT_MARGIN_RIGHT + insets.systemWindowInsetBottom
      )

      return@setOnApplyWindowInsetsListenerAndRequest insets
    }
  }

  override fun onControllerShown() {
    super.onControllerShown()

    recyclerView.doOnPreDraw {
      initCollapsingToolbar(recyclerView)
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

    tearDownCollapsingToolbar()
  }

  private fun initCollapsingToolbar(recyclerView: RecyclerView) {
    toolbarContract.collapsableView().let { collapsableView ->
      if (!collapsingViewControllerMap.containsKey(collapsableView)) {
        collapsingViewControllerMap[collapsableView] = mutableMapOf()
      }

      if (!collapsingViewControllerMap[collapsableView]!!.containsKey(recyclerView)) {
        collapsingViewControllerMap[collapsableView]!![recyclerView] = CollapsingViewController(
          ControllerType.Thread,
          CollapsingViewController.ViewScreenAttachPoint.AttachedToTop
        )
      }

      collapsingViewControllerMap[collapsableView]!![recyclerView]!!.attach(collapsableView, recyclerView)
    }
  }

  private fun tearDownCollapsingToolbar() {
    toolbarContract.collapsableView().let { collapsableView ->
      collapsingViewControllerMap[collapsableView]?.remove(recyclerView)?.detach(recyclerView)
      if (collapsingViewControllerMap[collapsableView].isNullOrEmpty()) {
        collapsingViewControllerMap.remove(collapsableView)
      }
    }
  }

  override fun getControllerTag(): ControllerTag = CONTROLLER_TAG

  companion object {
    val CONTROLLER_TAG = ControllerTag("SplitThreadControllerTag")
  }

}