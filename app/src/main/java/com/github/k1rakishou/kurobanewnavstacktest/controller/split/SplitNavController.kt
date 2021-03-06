package com.github.k1rakishou.kurobanewnavstacktest.controller.split

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import com.bluelinelabs.conductor.RouterTransaction
import com.github.k1rakishou.kurobanewnavstacktest.R
import com.github.k1rakishou.kurobanewnavstacktest.core.base.BaseController
import com.github.k1rakishou.kurobanewnavstacktest.core.base.ControllerTag
import com.github.k1rakishou.kurobanewnavstacktest.controller.base.CatalogNavigationContract
import com.github.k1rakishou.kurobanewnavstacktest.controller.base.ChanNavigationContract
import com.github.k1rakishou.kurobanewnavstacktest.controller.base.ThreadNavigationContract
import com.github.k1rakishou.kurobanewnavstacktest.data.BoardDescriptor
import com.github.k1rakishou.kurobanewnavstacktest.data.ThreadDescriptor
import com.github.k1rakishou.kurobanewnavstacktest.viewstate.*
import timber.log.Timber

class SplitNavController(
  args: Bundle? = null
) : BaseController(args),
  ChanNavigationContract {
  private lateinit var leftControllerContainer: FrameLayout
  private lateinit var rightControllerContainer: FrameLayout

  override fun instantiateView(
      inflater: LayoutInflater,
      container: ViewGroup,
      savedViewState: Bundle?
  ): View {
    return inflater.inflateView(R.layout.controller_split_navigation, container) {
      leftControllerContainer = findViewById(R.id.left_controller_container)
      rightControllerContainer = findViewById(R.id.right_controller_container)
    }
  }

  override fun onControllerCreated(savedViewState: Bundle?) {
    super.onControllerCreated(savedViewState)

    val boardDescriptor = args.getBoardDescriptorOrNull()
    val threadDescriptor = args.getThreadDescriptorOrNull()

    leftControllerContainer.setupChildRouterIfNotSet(
      RouterTransaction.with(createSplitUiElementsController(boardDescriptor))
    )
    rightControllerContainer.setupChildRouterIfNotSet(
      RouterTransaction.with(createSplitThreadController(threadDescriptor))
    )
  }

  override fun handleBack(): Boolean {
    val splitThreadController = getChildRouter(rightControllerContainer).getControllerWithTag(
      SplitThreadController.CONTROLLER_TAG.tag
    ) as SplitThreadController

    if (splitThreadController.myHandleBack()) {
      return true
    }

    val splitCatalogUiElementsController = getChildRouter(leftControllerContainer).getControllerWithTag(
      SplitUiElementsController.CONTROLLER_TAG.tag
    ) as SplitUiElementsController

    if (splitCatalogUiElementsController.myHandleBack()) {
      return true
    }

    return false
  }

  @SuppressLint("BinaryOperationInTimber")
  override fun openBoard(boardDescriptor: BoardDescriptor) {
    val splitCatalogUiElementsController = getChildRouter(leftControllerContainer).getControllerWithTag(
      SplitUiElementsController.CONTROLLER_TAG.tag
    )

    if (splitCatalogUiElementsController == null) {
      Timber.tag(TAG).e("openBoard($boardDescriptor) " +
        "getControllerWithTag(SplitCatalogUiElementsController) returned null")
      return
    }

    (splitCatalogUiElementsController as CatalogNavigationContract).openBoard(boardDescriptor)
  }

  @SuppressLint("BinaryOperationInTimber")
  override fun openThread(threadDescriptor: ThreadDescriptor) {
    val splitThreadController = getChildRouter(rightControllerContainer).getControllerWithTag(
      SplitThreadController.CONTROLLER_TAG.tag
    )

    if (splitThreadController == null) {
      Timber.tag(TAG).e("openThread($threadDescriptor) " +
        "getControllerWithTag(splitThreadController) returned null")
      return
    }

    (splitThreadController as ThreadNavigationContract).openThread(threadDescriptor)
  }

  private fun createSplitUiElementsController(
    boardDescriptor: BoardDescriptor?
  ): SplitUiElementsController {
    return SplitUiElementsController.create(boardDescriptor).apply {
      threadNavigationContract(this@SplitNavController)
    }
  }

  private fun createSplitThreadController(
    threadDescriptor: ThreadDescriptor?
  ): SplitThreadController {
    return SplitThreadController.create(threadDescriptor)
  }

  override fun getControllerTag(): ControllerTag = CONTROLLER_TAG

  companion object {
    private const val TAG = "SplitNavController"
    val CONTROLLER_TAG = ControllerTag("SplitNavControllerTag")

    fun create(
      boardDescriptor: BoardDescriptor?,
      threadDescriptor: ThreadDescriptor?
    ): SplitNavController {
      val args = Bundle()
      args.putBoardDescriptor(boardDescriptor)
      args.putThreadDescriptor(threadDescriptor)

      return SplitNavController(args)
    }
  }

}