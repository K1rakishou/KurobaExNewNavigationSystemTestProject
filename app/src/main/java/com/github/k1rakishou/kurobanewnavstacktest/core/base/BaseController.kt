package com.github.k1rakishou.kurobanewnavstacktest.core.base

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.annotation.CallSuper
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModel
import androidx.savedstate.SavedStateRegistry
import com.bluelinelabs.conductor.Controller
import com.bluelinelabs.conductor.Router
import com.bluelinelabs.conductor.RouterTransaction
import com.github.k1rakishou.kurobanewnavstacktest.MyApplication
import com.github.k1rakishou.kurobanewnavstacktest.activity.ActivityContract
import com.github.k1rakishou.kurobanewnavstacktest.utils.viewModelStorage
import com.github.k1rakishou.kurobanewnavstacktest.widget.CancellableToast
import kotlinx.coroutines.*
import timber.log.Timber
import kotlin.coroutines.CoroutineContext
import kotlin.reflect.KClass

abstract class BaseController(
    args: Bundle? = null,
    controllerRetainViewMode: RetainViewMode = RetainViewMode.RETAIN_DETACH
) : Controller(args) {
  private var job = SupervisorJob()
  private var scope = CoroutineScope(job + Dispatchers.Main)

  private var attachCompletableDeferred = CompletableDeferred<Unit>(job)
  private val cancellableToast = CancellableToast()

  init {
    retainViewMode = controllerRetainViewMode
  }

  suspend fun waitUntilAttached(): Boolean {
    if (isAttached) {
      return true
    }

    if (isDead()) {
      return false
    }

    if (!attachCompletableDeferred.isCompleted) {
      attachCompletableDeferred.await()
    }

    return true
  }

  fun launch(context: CoroutineContext = Dispatchers.Main, block: suspend () -> Unit): Job {
    check(job.isActive) { "job is not active!" }
    check(scope.isActive) { "scope is not active!" }

    return scope.launch(context) { block() }
  }

  protected fun LayoutInflater.inflateView(
      @LayoutRes resourceId: Int,
      root: ViewGroup,
      func: View.() -> Unit
  ): View {
    return inflate(resourceId, root, false).apply(func)
  }

  final override fun onCreateView(
      inflater: LayoutInflater,
      container: ViewGroup,
      savedViewState: Bundle?
  ): View {
    val view = instantiateView(inflater, container, savedViewState)
    onControllerCreated(savedViewState)

    return view
  }

  @CallSuper
  final override fun onAttach(view: View) {
    super.onAttach(view)

    attachCompletableDeferred.complete(Unit)
    onControllerShown()
  }

  @CallSuper
  final override fun onDetach(view: View) {
    super.onDetach(view)

    onControllerHidden()
    attachCompletableDeferred = CompletableDeferred<Unit>(job)
  }

  final override fun onDestroyView(view: View) {
    super.onDestroyView(view)

    job.cancel()
    onControllerDestroyed()

    job = SupervisorJob()
    scope = CoroutineScope(job + Dispatchers.Main)
  }

  final override fun onDestroy() {
    super.onDestroy()

    cancellableToast.cancel()
  }

  protected abstract fun instantiateView(
      inflater: LayoutInflater,
      container: ViewGroup,
      savedViewState: Bundle?
  ): View

  protected open fun onControllerCreated(savedViewState: Bundle?) {
    if (LOG_CONTROLLER_STATE) {
      Timber.tag(TAG).d("${this.javaClass.simpleName} onControllerCreated()")
    }
  }

  protected open fun onControllerShown() {
    if (LOG_CONTROLLER_STATE) {
      Timber.tag(TAG).d("${this.javaClass.simpleName} onControllerShown()")
    }
  }

  protected open fun onControllerHidden() {
    if (LOG_CONTROLLER_STATE) {
      Timber.tag(TAG).d("${this.javaClass.simpleName} onControllerHidden()")
    }
  }

  protected open fun onControllerDestroyed() {
    if (LOG_CONTROLLER_STATE) {
      Timber.tag(TAG).d("${this.javaClass.simpleName} onControllerDestroyed()")
    }
  }

  open fun myHandleBack(): Boolean {
    return false
  }

  protected fun ViewGroup.setupChildRouterIfNotSet(transaction: RouterTransaction): Router {
    val router = getChildRouter(this)
    if (!router.hasRootController()) {
      val controller = transaction.controller
      if (controller is BaseController) {
        transaction.tag(controller.getControllerTag().tag)
      }

      router.setRoot(transaction)
    }

    return router
  }

  protected fun <T : Controller> ViewGroup.getControllerByTag(controllerTag: ControllerTag): T? {
    return getChildRouter(this, null, false)?.getControllerWithTag(controllerTag.tag) as? T
  }

  protected fun Router.getTopController(): Controller? {
    return backstack.lastOrNull()?.controller
  }

  protected fun currentContext(): Context {
    return requireNotNull(activity) {
      "Not attached to activity! Controller tag: ${getControllerTag()}"
    }
  }

  @Suppress("UNCHECKED_CAST")
  protected fun activityContract(): ActivityContract {
    return requireNotNull(activity) {
      "Not attached to activity! Controller tag: ${getControllerTag()}"
    } as ActivityContract
  }

  protected fun currentActivity(): AppCompatActivity {
    return activityContract().activity()
  }

  protected fun savedStateRegistry(): SavedStateRegistry {
    return currentActivity().savedStateRegistry
  }

  protected fun app(): MyApplication {
    return applicationContext!! as MyApplication
  }

  fun showToast(message: String) {
    cancellableToast.showToast(currentContext(), message, Toast.LENGTH_SHORT)
  }

  fun showToast(message: String, duration: Int) {
    cancellableToast.showToast(currentContext(), message, duration)
  }

  protected fun <VM : ViewModel> viewModels(vmClass: KClass<VM>): Lazy<VM> {
    return lazy { (activity as ComponentActivity).viewModelStorage(vmClass).value }
  }

  protected fun isDead() = isDestroyed || isBeingDestroyed

  abstract fun getControllerTag(): ControllerTag

  companion object {
    private const val TAG = "BaseController"
    private const val LOG_CONTROLLER_STATE = true
  }
}