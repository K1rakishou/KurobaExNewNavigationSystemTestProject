package com.github.k1rakishou.kurobanewnavstacktest.widget.toolbar

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import androidx.activity.ComponentActivity
import com.github.k1rakishou.kurobanewnavstacktest.R
import com.github.k1rakishou.kurobanewnavstacktest.utils.BackgroundUtils
import com.github.k1rakishou.kurobanewnavstacktest.utils.viewModelStorage
import com.github.k1rakishou.kurobanewnavstacktest.widget.toolbar.catalog.KurobaCatalogToolbar
import com.github.k1rakishou.kurobanewnavstacktest.widget.toolbar.catalog.KurobaCatalogToolbarState
import com.github.k1rakishou.kurobanewnavstacktest.widget.toolbar.search.KurobaSearchToolbar
import com.github.k1rakishou.kurobanewnavstacktest.widget.toolbar.search.KurobaSearchToolbarState
import com.github.k1rakishou.kurobanewnavstacktest.widget.toolbar.thread.KurobaThreadToolbar
import com.github.k1rakishou.kurobanewnavstacktest.widget.toolbar.thread.KurobaThreadToolbarState
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.filter
import timber.log.Timber

@SuppressLint("BinaryOperationInTimber")
class KurobaToolbar @JvmOverloads constructor(
  context: Context,
  attributeSet: AttributeSet? = null,
  defAttrStyle: Int = 0
) : FrameLayout(context, attributeSet, defAttrStyle), KurobaToolbarCallbacks {
  private val job = SupervisorJob()
  private val toolbarScope = CoroutineScope(job + Dispatchers.Main)

  private val kurobaToolbarViewContainer: FrameLayout

  val toolbarViewModel by lazy {
    (context as ComponentActivity).viewModelStorage(KurobaToolbarViewModel::class).value
  }

  private var initialToolbarStateClass: ToolbarStateClass = ToolbarStateClass.Uninitialized
  private var initialized = false

  init {
    inflate(context, R.layout.kuroba_toolbar, this).apply {
      kurobaToolbarViewContainer = findViewById(R.id.kuroba_toolbar_view_container)
    }
  }

  fun init(initialToolbarStateClass: ToolbarStateClass) {
    check(!this.initialized) { "Double initialization!" }
    check(initialToolbarStateClass.canBeInitialState) {
      "Unexpected ToolbarStateClass: ${initialToolbarStateClass}"
    }

    this.initialToolbarStateClass = initialToolbarStateClass
    this.initialized = true

    toolbarViewModel.initStateStackForToolbar(initialToolbarStateClass)

    toolbarScope.launch {
      toolbarViewModel.listenForToolbarStateChanges()
        .filter { toolbarStateClass -> filterToolbarStateClassIfNeeded(toolbarStateClass) }
        .collect { toolbarStateClass ->
          onToolbarStateChanged(
            newToolbarStateClass = toolbarStateClass,
            type = ToolbarStateChangeType.Update
          )
        }
    }

    ensureInitialized()

    val prevPrevToolbarStateClass = toolbarViewModel.getToolbarStateStack(initialToolbarStateClass)
      .getPrevToolbarStateClass()

    if (prevPrevToolbarStateClass != ToolbarStateClass.Uninitialized) {
      pushNewToolbarStateClass(prevPrevToolbarStateClass)
      return
    }

    pushNewToolbarStateClass(initialToolbarStateClass)
  }

  override fun onDetachedFromWindow() {
    super.onDetachedFromWindow()

    job.cancelChildren()
  }

  /**
   * If toolbarStateClass is either [ToolbarStateClass.Thread] or [ToolbarStateClass.Catalog] then
   * we only want to allow toolbarStateClass of the same class as initialToolbarStateClass. Basically,
   * we don't want to consume state of catalog toolbar in thread toolbar and vice versa. It is okay
   * to consume other state classes (like SimpleTitle/Selection etc) in catalog/thread toolbars.
   * */
  private fun filterToolbarStateClassIfNeeded(toolbarStateClass: ToolbarStateClass): Boolean {
    if (!toolbarStateClass.canBeInitialState) {
      return true
    }

    return toolbarStateClass == initialToolbarStateClass
  }

  override fun popCurrentToolbarStateClass() {
    val toolbarStateClass = toolbarViewModel.getToolbarStateStack(initialToolbarStateClass).pop()
      ?: return

    onToolbarStateChanged(
      newToolbarStateClass = toolbarStateClass,
      type = ToolbarStateChangeType.Pop
    )
  }

  override fun pushNewToolbarStateClass(toolbarStateClass: ToolbarStateClass) {
    toolbarViewModel.getToolbarStateStack(initialToolbarStateClass)
      .pushToolbarStateClass(toolbarStateClass)

    onToolbarStateChanged(
      newToolbarStateClass = toolbarStateClass,
      type = ToolbarStateChangeType.Push
    )
  }

  fun onBackPressed(): Boolean {
    ensureInitialized()

    val toolbarStateClass = toolbarViewModel.getToolbarStateStack(initialToolbarStateClass)
      .popCurrentStateIfPossibleOrNull()
      ?: return false

    onToolbarStateChanged(
      newToolbarStateClass = toolbarStateClass,
      type = ToolbarStateChangeType.Pop
    )

    return true
  }

  private fun onToolbarStateChanged(
    newToolbarStateClass: ToolbarStateClass,
    type: ToolbarStateChangeType
  ) {
    BackgroundUtils.ensureMainThread()
    ensureInitialized()

    Timber.tag(TAG).d("($initialToolbarStateClass) onToolbarStateChanged() " +
      "newToolbarStateClass=$newToolbarStateClass")

    if (newToolbarStateClass == ToolbarStateClass.Uninitialized) {
      return
    }

    val needAddOrReplaceToolbarView = type == ToolbarStateChangeType.Push
      || type == ToolbarStateChangeType.Pop
      || kurobaToolbarViewContainer.childCount < 1

    if (needAddOrReplaceToolbarView) {
      Timber.tag(TAG).d("($initialToolbarStateClass) onToolbarStateChanged() " +
        "newToolbarStateClass ($newToolbarStateClass)")

      addOrReplaceToolbarView(newToolbarStateClass = newToolbarStateClass, type = type)
    }

    // TODO(KurobaEx): animations and stuff

    applyStateChangeToUi(newToolbarStateClass)
  }

  private fun applyStateChangeToUi(newToolbarStateClass: ToolbarStateClass) {
    if (kurobaToolbarViewContainer.childCount != 1) {
      throw IllegalStateException("Bad children count: ${kurobaToolbarViewContainer.childCount}")
    }

    when (newToolbarStateClass) {
      ToolbarStateClass.Uninitialized -> {
        throw IllegalStateException("($initialToolbarStateClass) Must not be called")
      }
      ToolbarStateClass.Catalog -> {
        val child = kurobaToolbarViewContainer.getChildAt(0)
        if (child is KurobaCatalogToolbar) {
          applyStateChangeToUiInternal<KurobaCatalogToolbarState>(child, newToolbarStateClass)
        }
      }
      ToolbarStateClass.Thread -> {
        val child = kurobaToolbarViewContainer.getChildAt(0)
        if (child is KurobaThreadToolbar) {
          applyStateChangeToUiInternal<KurobaThreadToolbarState>(child, newToolbarStateClass)
        }
      }
      ToolbarStateClass.Search -> {
        val child = kurobaToolbarViewContainer.getChildAt(0)
        if (child is KurobaSearchToolbar) {
          applyStateChangeToUiInternal<KurobaSearchToolbarState>(child, newToolbarStateClass)
        }
      }
      ToolbarStateClass.SimpleTitle -> TODO()
      ToolbarStateClass.Selection -> TODO()
    }
  }

  private inline fun <reified T : ToolbarStateContract> applyStateChangeToUiInternal(
    currentToolbarView: View,
    newToolbarStateClass: ToolbarStateClass
  ) {
    val toolbarContract = (currentToolbarView as KurobaToolbarDelegateContract<T>)
    if (toolbarContract.toolbarStateClass == newToolbarStateClass) {
      val toolbarState = toolbarViewModel.getToolbarState<T>(newToolbarStateClass)

      toolbarContract.applyStateToUi(toolbarState)
    }
  }

  private fun addOrReplaceToolbarView(
    newToolbarStateClass: ToolbarStateClass,
    type: ToolbarStateChangeType
  ) {
    val newView = when (newToolbarStateClass) {
      ToolbarStateClass.Uninitialized -> {
        throw IllegalStateException("($initialToolbarStateClass) Must not be called")
      }
      ToolbarStateClass.Catalog -> {
        KurobaCatalogToolbar(
          context = context,
          kurobaToolbarViewModel = toolbarViewModel,
          kurobaToolbarCallbacks = this
        )
      }
      ToolbarStateClass.Thread -> {
        KurobaThreadToolbar(
          context = context,
          kurobaToolbarViewModel = toolbarViewModel,
          kurobaToolbarCallbacks = this
        )
      }
      ToolbarStateClass.Search -> {
        KurobaSearchToolbar(
          context = context,
          kurobaToolbarViewModel = toolbarViewModel,
          kurobaToolbarCallbacks = this
        )
      }
      ToolbarStateClass.SimpleTitle -> TODO()
      ToolbarStateClass.Selection -> TODO()
    }

    check(kurobaToolbarViewContainer.childCount <= 1) {
      "($initialToolbarStateClass) Bad child count: ${kurobaToolbarViewContainer.childCount}"
    }

    if (kurobaToolbarViewContainer.childCount > 0) {
      val currentToolbar = kurobaToolbarViewContainer.getChildAt(0)

      Timber.tag(TAG).d("($initialToolbarStateClass) replaceStateView() oldView=" +
        currentToolbar.javaClass.simpleName
      )

      if (type == ToolbarStateChangeType.Pop) {
        val contract = (currentToolbar as KurobaToolbarDelegateContract<*>)
        toolbarViewModel.resetState(contract.toolbarStateClass)
      }

      kurobaToolbarViewContainer.removeAllViews()
    }

    Timber.tag(TAG).d("($initialToolbarStateClass) replaceStateView() " +
      "newView = ${newView.javaClass.simpleName}")
    kurobaToolbarViewContainer.addView(newView)

    check(kurobaToolbarViewContainer.childCount <= 1) {
      "($initialToolbarStateClass) Bad child count: ${kurobaToolbarViewContainer.childCount}"
    }
  }

  private fun ensureInitialized() {
    check(initialized) {
      "($initialToolbarStateClass) Must initialize first!"
    }
    check(initialToolbarStateClass != ToolbarStateClass.Uninitialized) {
      "initialToolbarStateClass is Uninitialized!"
    }
  }

  enum class ToolbarStateChangeType {
    Push,
    Pop,
    Update
  }

  companion object {
    private const val TAG = "KurobaToolbar"
  }

}