package com.github.k1rakishou.kurobanewnavstacktest.controller

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import com.bluelinelabs.conductor.RouterTransaction
import com.github.k1rakishou.kurobanewnavstacktest.R
import com.github.k1rakishou.kurobanewnavstacktest.core.base.BaseController
import com.github.k1rakishou.kurobanewnavstacktest.core.base.ControllerPresenterDelegate
import com.github.k1rakishou.kurobanewnavstacktest.core.base.ControllerTag
import com.github.k1rakishou.kurobanewnavstacktest.controller.base.CatalogNavigationContract
import com.github.k1rakishou.kurobanewnavstacktest.controller.base.ChanNavigationContract
import com.github.k1rakishou.kurobanewnavstacktest.controller.base.ThreadNavigationContract
import com.github.k1rakishou.kurobanewnavstacktest.controller.slide.SlideUiElementsController
import com.github.k1rakishou.kurobanewnavstacktest.controller.split.SplitNavController
import com.github.k1rakishou.kurobanewnavstacktest.data.BoardDescriptor
import com.github.k1rakishou.kurobanewnavstacktest.data.ThreadDescriptor
import com.github.k1rakishou.kurobanewnavstacktest.utils.ChanSettings
import timber.log.Timber

class MainController(
  args: Bundle? = null
) : BaseController(args),
  ControllerPresenterDelegate,
  ChanNavigationContract {
  private lateinit var contentContainer: FrameLayout

  private var presenterDelegate: ControllerPresenterDelegate? = null

  fun setControllerPresenterDelegate(controllerPresenterDelegate: ControllerPresenterDelegate) {
    this.presenterDelegate = controllerPresenterDelegate
  }

  override fun instantiateView(
      inflater: LayoutInflater,
      container: ViewGroup,
      savedViewState: Bundle?
  ): View {
    return inflater.inflateView(R.layout.controller_main, container) {
      contentContainer = findViewById(R.id.content_container)
    }
  }

  override fun onControllerCreated(savedViewState: Bundle?) {
    super.onControllerCreated(savedViewState)

    val controller = if (isSplitMode()) {
      SplitNavController().apply {
        setControllerPresenterDelegate(this@MainController)
      }
    } else {
      SlideUiElementsController().apply {
        setControllerPresenterDelegate(this@MainController)
      }
    }

    val transaction = RouterTransaction.with(controller)
      .tag(controller.getControllerTag().tag)

    getChildRouter(contentContainer).setRoot(transaction)
  }

  override fun openBoard(boardDescriptor: BoardDescriptor) {
    getChanNavContract<CatalogNavigationContract>()?.openBoard(boardDescriptor)
  }

  override fun openThread(threadDescriptor: ThreadDescriptor) {
    getChanNavContract<ThreadNavigationContract>()?.openThread(threadDescriptor)
  }

  override fun replaceTopControllerOrPushAsNew(transaction: RouterTransaction) {
    presenterDelegate?.replaceTopControllerOrPushAsNew(transaction)
  }

  override fun presentController(transaction: RouterTransaction) {
    presenterDelegate?.presentController(transaction)
  }

  override fun stopPresentingController(controller: BaseController) {
    presenterDelegate?.stopPresentingController(controller)
  }

  override fun stopPresentingUntilRoot() {
    presenterDelegate?.stopPresentingUntilRoot()
  }

  override fun isControllerPresented(predicate: (BaseController) -> Boolean): Boolean {
    return presenterDelegate?.isControllerPresented(predicate) ?: false
  }

  @Suppress("UNCHECKED_CAST")
  @SuppressLint("BinaryOperationInTimber")
  private fun <T> getChanNavContract(): T? {
    val router = getChildRouter(contentContainer)

    return if (isSplitMode()) {
      val splitNavController = router.getControllerWithTag(SplitNavController.CONTROLLER_TAG.tag)
      if (splitNavController == null) {
        Timber.tag(TAG).e("getCatalogNavContract() " +
          "getControllerWithTag(SplitNavController) returned null")
        return null
      }

      splitNavController as T
    } else {
      val slideUiElementsController = router.getControllerWithTag(
        SlideUiElementsController.CONTROLLER_TAG.tag
      )
      if (slideUiElementsController == null) {
        Timber.tag(TAG).e("getCatalogNavContract() " +
          "getControllerWithTag(SlideUiElementsController) returned null")
        return null
      }

      slideUiElementsController as T
    }
  }

  private fun isSplitMode(): Boolean {
    return ChanSettings.isSplitMode(currentContext())
  }

  override fun getControllerTag(): ControllerTag = CONTROLLER_TAG

  companion object {
    const val TAG = "MainController"
    val CONTROLLER_TAG = ControllerTag("MainControllerTag")
  }
}