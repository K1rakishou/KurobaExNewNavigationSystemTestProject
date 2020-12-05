package com.github.k1rakishou.kurobanewnavstacktest.controller

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import com.bluelinelabs.conductor.RouterTransaction
import com.github.k1rakishou.kurobanewnavstacktest.R
import com.github.k1rakishou.kurobanewnavstacktest.base.BaseController
import com.github.k1rakishou.kurobanewnavstacktest.base.ControllerPresenterDelegate
import com.github.k1rakishou.kurobanewnavstacktest.base.ControllerTag
import com.github.k1rakishou.kurobanewnavstacktest.controller.slide.SlideCatalogUiElementsController
import com.github.k1rakishou.kurobanewnavstacktest.controller.split.SplitNavController
import com.github.k1rakishou.kurobanewnavstacktest.utils.AndroidUtils

class MainController(args: Bundle? = null) : BaseController(args) {
  private lateinit var contentContainer: FrameLayout

  private lateinit var presenterDelegate: ControllerPresenterDelegate

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

    val controller = if (AndroidUtils.isSplitMode(currentContext())) {
      SplitNavController().apply {
        setControllerPresenterDelegate(presenterDelegate)
      }
    } else {
      SlideCatalogUiElementsController().apply {
        setControllerPresenterDelegate(presenterDelegate)
      }
    }

    val transaction = RouterTransaction.with(controller)
      .tag(controller.getControllerTag().tag)

    getChildRouter(contentContainer).setRoot(transaction)
  }

  override fun getControllerTag(): ControllerTag = CONTROLLER_TAG

  companion object {
    const val TAG = "MainController"
    val CONTROLLER_TAG = ControllerTag("MainControllerTag")
  }
}