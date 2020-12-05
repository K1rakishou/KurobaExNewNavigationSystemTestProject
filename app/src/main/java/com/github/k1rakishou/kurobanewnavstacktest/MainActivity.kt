package com.github.k1rakishou.kurobanewnavstacktest

import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.bluelinelabs.conductor.Conductor
import com.bluelinelabs.conductor.Router
import com.bluelinelabs.conductor.RouterTransaction
import com.github.k1rakishou.kurobanewnavstacktest.activity.ActivityContract
import com.github.k1rakishou.kurobanewnavstacktest.base.BaseController
import com.github.k1rakishou.kurobanewnavstacktest.base.ControllerPresenterDelegate
import com.github.k1rakishou.kurobanewnavstacktest.base.ControllerPresenterDelegateImpl
import com.github.k1rakishou.kurobanewnavstacktest.controller.MainController
import com.github.k1rakishou.kurobanewnavstacktest.widget.TouchBlockingFrameLayout
import dev.chrisbanes.insetter.Insetter

class MainActivity : AppCompatActivity(), ControllerPresenterDelegate, ActivityContract {
  private lateinit var router: Router
  private lateinit var rootContainer: TouchBlockingFrameLayout
  private lateinit var controllerPresenterDelegate: ControllerPresenterDelegateImpl

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)

    Insetter.setEdgeToEdgeSystemUiFlags(window.decorView, true)
    window.statusBarColor = Color.TRANSPARENT
    window.navigationBarColor = Color.TRANSPARENT

    rootContainer = findViewById(R.id.root_container)
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
    if (!router.handleBack()) {
      super.onBackPressed()
    }
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
}