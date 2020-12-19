package com.github.k1rakishou.kurobanewnavstacktest.activity

import android.app.Activity
import android.graphics.Color
import android.os.Bundle
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import com.bluelinelabs.conductor.Router
import com.bluelinelabs.conductor.RouterTransaction
import com.bluelinelabs.conductor.internal.LifecycleHandler
import com.github.k1rakishou.kurobanewnavstacktest.R
import com.github.k1rakishou.kurobanewnavstacktest.core.base.BaseController
import com.github.k1rakishou.kurobanewnavstacktest.core.base.ControllerPresenterDelegate
import com.github.k1rakishou.kurobanewnavstacktest.core.base.ControllerPresenterDelegateImpl
import com.github.k1rakishou.kurobanewnavstacktest.controller.MainController
import com.github.k1rakishou.kurobanewnavstacktest.controller.slide.SlideUiElementsController
import com.github.k1rakishou.kurobanewnavstacktest.controller.split.SplitNavController
import com.github.k1rakishou.kurobanewnavstacktest.core.GlobalWindowInsetsManager
import com.github.k1rakishou.kurobanewnavstacktest.core.test.TestHelpers
import com.github.k1rakishou.kurobanewnavstacktest.utils.BackgroundUtils
import com.github.k1rakishou.kurobanewnavstacktest.utils.ChanSettings.isSplitMode
import com.github.k1rakishou.kurobanewnavstacktest.utils.FullScreenUtils
import com.github.k1rakishou.kurobanewnavstacktest.utils.findRouterWithControllerByTag
import com.github.k1rakishou.kurobanewnavstacktest.viewcontroller.fab.SlideFabViewController
import com.github.k1rakishou.kurobanewnavstacktest.viewcontroller.fab.SplitFabViewController
import com.github.k1rakishou.kurobanewnavstacktest.viewstate.ViewStateConstants
import com.github.k1rakishou.kurobanewnavstacktest.widget.layout.TouchBlockingFrameLayout
import dev.chrisbanes.insetter.Insetter

class MainActivity : AppCompatActivity(), ControllerPresenterDelegate, ActivityContract {
  // TODO(KurobaEx): DI vvv
  lateinit var testHelpers: TestHelpers
  lateinit var slideFabViewController: SlideFabViewController
  lateinit var splitFabViewController: SplitFabViewController
  // TODO(KurobaEx): DI ^^^

  private lateinit var router: Router
  private lateinit var rootContainer: TouchBlockingFrameLayout
  private lateinit var controllerPresenterDelegate: ControllerPresenterDelegateImpl
  private lateinit var globalWindowInsetsManager: GlobalWindowInsetsManager

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)

    testHelpers = TestHelpers()
    testHelpers.init(intent.getBooleanExtra(ViewStateConstants.MainActivity.testModeKey, false))

    Insetter.setEdgeToEdgeSystemUiFlags(window.decorView, true)
    window.statusBarColor = Color.TRANSPARENT
    window.navigationBarColor = Color.TRANSPARENT

    rootContainer = findViewById(R.id.root_container)
    router = attachRouterHacky(this, rootContainer, savedInstanceState)
    controllerPresenterDelegate = ControllerPresenterDelegateImpl(router)
    globalWindowInsetsManager = GlobalWindowInsetsManager()

    slideFabViewController = SlideFabViewController()
    splitFabViewController = SplitFabViewController()

    if (!router.hasRootController()) {
      val controller = MainController()
      controller.setControllerPresenterDelegate(this)

      controllerPresenterDelegate.setRootControllerTag(controller.getControllerTag())
      router.setRoot(RouterTransaction.with(controller))
    }

    listenForInsetsUpdates()
  }

  private fun listenForInsetsUpdates() {
    ViewCompat.setOnApplyWindowInsetsListener(window.decorView) { view, insets ->
      val isKeyboardOpen = FullScreenUtils.isKeyboardShown(view, insets.systemWindowInsetBottom)

      globalWindowInsetsManager.updateKeyboardHeight(
        FullScreenUtils.calculateDesiredRealBottomInset(view, insets.systemWindowInsetBottom)
      )

      globalWindowInsetsManager.updateIsKeyboardOpened(isKeyboardOpen)

      return@setOnApplyWindowInsetsListener insets
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

  override fun mainActivityOrError(): MainActivity {
    return this
  }

  private fun attachRouterHacky(
    activity: Activity,
    container: ViewGroup,
    savedInstanceState: Bundle?
  ): Router {
    BackgroundUtils.ensureMainThread()
    val router = LifecycleHandler.install(activity)
      .getRouter(container, savedInstanceState)

    if (savedInstanceState != null) {
      // Unfortunately, we have to destroy the whole backstack (staring with the MainController and
      // going deeper in the hierarchy) because otherwise a nasty bug will happen. In this app we
      // use different kind of controllers for Portrait/Landscape (or for Tablets) orientations: for
      // Portrait there is Slide***Controller and for Landscape there is Split***Controller. When
      // the user rotates the phone we want to switch from Slide controllers to Split controllers
      // or vice versa. But Conductor preserves the controller backstack across orientation changes
      // and then even rebinds them (meaning they will receive OnCreate/OnDestroy/etc lifecycle
      // events), so after the phone rotates the following will happen:
      // Let's say we were in Portrait orientation (Slide mode) and the phone is rotated 90 degrees.
      // After activity recreation Router.rebindIfNeeded() is called and all the old controllers
      // (Slide mode) will be rebound (event though we are already in the Split mode). Then in the
      // MainController we will figure out that we are in Split mode and Slide controllers will be
      // replaced with Split controllers, meaning that Slide controllers will be destroyed then
      // created and then immediately destroyed again. This is bad because there is a lot of stuff
      // that will be recreated during the rebind phase (like toolbars or bottom panels),
      // and they will be recreated with the wrong states which will lead to all kind of bugs.
      // To fix that (or more like hack around) we are deleting the whole backstack starting from the
      // very root. This is not good for us since we will have to manually recreate the backstack
      // back (with the help of viewmodels) but I don't know how to do this properly, there seems
      // to be no other way in Conductor. The official example (MasterDetailController) doesn't
      // work for this app.

      val controllerTag = if (isSplitMode(this)) {
        SlideUiElementsController.CONTROLLER_TAG
      } else {
        SplitNavController.CONTROLLER_TAG
      }

      val result = router.findRouterWithControllerByTag(controllerTag)
      if (result != null) {
        router.setBackstack(emptyList(), null)
      }
    }

    router.rebindIfNeeded()
    return router
  }

}