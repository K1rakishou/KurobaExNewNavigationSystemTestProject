package com.github.k1rakishou.kurobanewnavstacktest.activity

import android.app.Activity
import android.graphics.Color
import android.os.Bundle
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import com.bluelinelabs.conductor.Conductor
import com.bluelinelabs.conductor.Router
import com.bluelinelabs.conductor.RouterTransaction
import com.bluelinelabs.conductor.internal.LifecycleHandler
import com.github.k1rakishou.kurobanewnavstacktest.R
import com.github.k1rakishou.kurobanewnavstacktest.base.BaseController
import com.github.k1rakishou.kurobanewnavstacktest.base.ControllerPresenterDelegate
import com.github.k1rakishou.kurobanewnavstacktest.base.ControllerPresenterDelegateImpl
import com.github.k1rakishou.kurobanewnavstacktest.controller.MainController
import com.github.k1rakishou.kurobanewnavstacktest.controller.slide.SlideUiElementsController
import com.github.k1rakishou.kurobanewnavstacktest.controller.split.SplitNavController
import com.github.k1rakishou.kurobanewnavstacktest.core.test.TestHelpers
import com.github.k1rakishou.kurobanewnavstacktest.utils.BackgroundUtils
import com.github.k1rakishou.kurobanewnavstacktest.utils.ChanSettings.isSplitMode
import com.github.k1rakishou.kurobanewnavstacktest.utils.findRouterWithControllerByTag
import com.github.k1rakishou.kurobanewnavstacktest.viewstate.ViewStateConstants
import com.github.k1rakishou.kurobanewnavstacktest.widget.TouchBlockingFrameLayout
import dev.chrisbanes.insetter.Insetter

class MainActivity : AppCompatActivity(), ControllerPresenterDelegate, ActivityContract {
  lateinit var testHelpers: TestHelpers

  private lateinit var router: Router
  private lateinit var rootContainer: TouchBlockingFrameLayout
  private lateinit var controllerPresenterDelegate: ControllerPresenterDelegateImpl

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)

    testHelpers = TestHelpers()
    testHelpers.init(intent.getBooleanExtra(ViewStateConstants.MainActivity.testModeKey, false))

    Insetter.setEdgeToEdgeSystemUiFlags(window.decorView, true)
    window.statusBarColor = Color.TRANSPARENT
    window.navigationBarColor = Color.TRANSPARENT

    rootContainer = findViewById(R.id.root_container)
//    router = attachRouterHacky(this, rootContainer, savedInstanceState)
    router = Conductor.attachRouter(this, rootContainer, savedInstanceState)
    controllerPresenterDelegate = ControllerPresenterDelegateImpl(router)

    if (!router.hasRootController()) {
      val controller = MainController()
      controller.setControllerPresenterDelegate(this)

      controllerPresenterDelegate.setRootControllerTag(controller.getControllerTag())
      router.setRoot(RouterTransaction.with(controller))
    }
  }

  override fun onBackPressed() {
    if (router.handleBack()) {
      return
    }

    super.onBackPressed()
  }

  override fun replaceTopControllerOrPushAsNew(transaction: RouterTransaction) {
    controllerPresenterDelegate.replaceTopControllerOrPushAsNew(transaction)
  }

  override fun presentController(transaction: RouterTransaction) {
    controllerPresenterDelegate.presentController(transaction)
  }

  override fun stopPresentingController(controller: BaseController) {
    controllerPresenterDelegate.stopPresentingController(controller)
  }

  override fun stopPresentingUntilRoot() {
    controllerPresenterDelegate.stopPresentingUntilRoot()
  }

  override fun isControllerPresented(predicate: (BaseController) -> Boolean): Boolean {
    return controllerPresenterDelegate.isControllerPresented(predicate)
  }

  override fun activity(): AppCompatActivity {
    return this
  }

  // TODO(KurobaEx): this doesn't work. Because of this not working there is a problem with
  //  Split/Slide root controller getting rebound upon config change which leads to nasty bugs
  //  (one of them is causing toolbars to get initialized with wrong state). This cannot be fixed
  //  right now because I have no idea how to properly remove controller from backstack upon config
  //  change.
  private fun attachRouterHacky(
    activity: Activity,
    container: ViewGroup,
    savedInstanceState: Bundle?
  ): Router {
    BackgroundUtils.ensureMainThread()
    val isSplitMode = isSplitMode(activity)
    val router = LifecycleHandler.install(activity)
      .getRouter(container, savedInstanceState)

    if (savedInstanceState != null) {
      // This is a hack to remove previously added controllers to avoid rebinding them just to pop
      // them again afterwards when screen orientation change occurs. Basically, what happens, after
      // the phone is rotated, and lets say we were in Portrait orientation, the Slide controller
      // stack is present in the router backstack and it is rebound. Then we get to the point where
      // we decide what layout mode to use (slide or split) and replace the previous controller
      // (slide in this case) with new controller (split in this case). This may lead to all kind of
      // funny bugs so we want to avoid that controller getting rebound. So we want to pop it before
      // calling "router.rebindIfNeeded()"
      val controllerTag = if (isSplitMode) {
        SlideUiElementsController.CONTROLLER_TAG
      } else {
        SplitNavController.CONTROLLER_TAG
      }

      val result = router.findRouterWithControllerByTag(controllerTag)
      if (result != null) {
        val (foundRouter, foundController) = result
        val backstackCopy = foundRouter.backstack

        val index = backstackCopy.indexOfFirst { routerTransaction ->
          (routerTransaction.controller as? BaseController)?.getControllerTag() == controllerTag
        }

        if (index >= 0) {
          backstackCopy.removeAt(index)
          foundRouter.setBackstack(backstackCopy, null)
        }
      }
    }

    router.rebindIfNeeded()
    return router
  }

}