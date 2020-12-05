package com.github.k1rakishou.kurobanewnavstacktest.base

import com.bluelinelabs.conductor.Router
import com.bluelinelabs.conductor.RouterTransaction
import com.bluelinelabs.conductor.changehandler.FadeChangeHandler

class ControllerPresenterDelegateImpl(
    private val router: Router
) : ControllerPresenterDelegate {
  private lateinit var rootControllerTag: ControllerTag

  fun setRootControllerTag(controllerTag: ControllerTag) {
    this.rootControllerTag = controllerTag
  }

  override fun replaceTopControllerOrPushAsNew(transaction: RouterTransaction) {
    val topController = getTopControllerOtherThanRootOrNull()
    if (topController == null) {
      router.pushController(
          transaction
              .pushChangeHandler(FadeChangeHandler(200, false))
              .popChangeHandler(FadeChangeHandler())
      )
      return
    }

    router.replaceTopController(transaction)
  }

  @Suppress("UNCHECKED_CAST")
  private fun getTopControllerOtherThanRootOrNull(): BaseController? {
    if (router.backstack.isEmpty()) {
      return null
    }

    checkRootControllerValid()

    if (router.backstack.size == 1) {
      return null
    }

    return router.backstack.last().controller as BaseController
  }

  private fun checkRootControllerValid() {
    val rootController = router.backstack.first()
    val controllerTag = (rootController.controller as BaseController).getControllerTag()

    check(controllerTag == rootControllerTag) {
      "Root controller has different tag from the one that was passed upon construction! " +
        "controllerTag='$controllerTag', rootControllerTag='$rootControllerTag'"
    }
  }

  override fun presentController(transaction: RouterTransaction) {
    router.pushController(
        transaction
            .pushChangeHandler(FadeChangeHandler(200, false))
            .popChangeHandler(FadeChangeHandler())
    )
  }

  override fun stopPresentingController(controller: BaseController) {
    router.popController(controller)
  }

  override fun stopPresentingUntilRoot() {
    router.popToRoot()
  }

  override fun isControllerPresented(predicate: (BaseController) -> Boolean): Boolean {
    return router.backstack.any { transaction ->
      return@any predicate(transaction.controller as BaseController)
    }
  }

}