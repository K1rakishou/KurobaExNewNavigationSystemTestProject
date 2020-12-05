package com.github.k1rakishou.kurobanewnavstacktest.base

import com.bluelinelabs.conductor.RouterTransaction

interface ControllerPresenterDelegate {
    fun replaceTopControllerOrPushAsNew(transaction: RouterTransaction)
    fun presentController(transaction: RouterTransaction)
    fun stopPresentingController(controller: BaseController)
    fun stopPresentingUntilRoot()
    fun isControllerPresented(predicate: (BaseController) -> Boolean): Boolean
}