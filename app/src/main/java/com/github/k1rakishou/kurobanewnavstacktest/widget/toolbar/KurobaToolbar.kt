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

  private val toolbarViewModel by lazy {
    (context as ComponentActivity).viewModelStorage(KurobaToolbarViewModel::class).value
  }

  private var kurobaToolbarType: KurobaToolbarType = KurobaToolbarType.Uninitialized
  private var initialized = false

  init {
    inflate(context, R.layout.kuroba_toolbar, this).apply {
      kurobaToolbarViewContainer = findViewById(R.id.kuroba_toolbar_view_container)
    }
  }

  fun init(newKurobaToolbarType: KurobaToolbarType) {
    check(!initialized) { "Double initialization!" }
    check(newKurobaToolbarType != KurobaToolbarType.Uninitialized) {
      "Unexpected kurobaToolbarType: ${newKurobaToolbarType}"
    }

    this.kurobaToolbarType = newKurobaToolbarType
    this.initialized = true

    ensureInitialized()
    toolbarViewModel.initStateStackForToolbar(kurobaToolbarType)

    toolbarScope.launch {
      toolbarViewModel.listenForToolbarStateChanges()
        .filter { (toolbarType, _) -> toolbarType == kurobaToolbarType }
        .collect { (toolbarType, toolbarStateClass) ->
          onToolbarStateChanged(
            newToolbarStateClass = toolbarStateClass,
            toolbarStateChangeType = ToolbarStateChangeType.Update
          )
        }
    }

    val prevPrevToolbarStateClass = toolbarViewModel.getToolbarStateStack(kurobaToolbarType)
      .getPrevToolbarStateClass()

    if (prevPrevToolbarStateClass != ToolbarStateClass.Uninitialized) {
      Timber.tag(TAG).d("Restoring from previous state kurobaToolbarType=$kurobaToolbarType, " +
        "prevPrevToolbarStateClass=$prevPrevToolbarStateClass")

      pushNewToolbarStateClass(kurobaToolbarType, prevPrevToolbarStateClass)
      return
    }

    val initialToolbarStateClass = when (kurobaToolbarType) {
      KurobaToolbarType.Uninitialized -> throw IllegalStateException("Bad kurobaToolbarType: $kurobaToolbarType")
      KurobaToolbarType.Catalog -> ToolbarStateClass.Catalog
      KurobaToolbarType.Thread -> ToolbarStateClass.Thread
    }

    Timber.tag(TAG).d("Fresh initialization kurobaToolbarType=$kurobaToolbarType, " +
      "prevPrevToolbarStateClass=$prevPrevToolbarStateClass")

    pushNewToolbarStateClass(kurobaToolbarType, initialToolbarStateClass)
  }

  fun newState(toolbarStateUpdate: ToolbarStateUpdate) {
    toolbarViewModel.newState(kurobaToolbarType, toolbarStateUpdate)
  }

  override fun onDetachedFromWindow() {
    super.onDetachedFromWindow()

    job.cancelChildren()
  }

  override fun popCurrentToolbarStateClass() {
    val toolbarStateClass = toolbarViewModel.getToolbarStateStack(kurobaToolbarType).pop()
      ?: return

    onToolbarStateChanged(
      newToolbarStateClass = toolbarStateClass,
      toolbarStateChangeType = ToolbarStateChangeType.Pop
    )
  }

  override fun pushNewToolbarStateClass(
    kurobaToolbarType: KurobaToolbarType,
    toolbarStateClass: ToolbarStateClass
  ) {
    toolbarViewModel.getToolbarStateStack(kurobaToolbarType)
      .pushToolbarStateClass(toolbarStateClass)

    onToolbarStateChanged(
      newToolbarStateClass = toolbarStateClass,
      toolbarStateChangeType = ToolbarStateChangeType.Push
    )
  }

  fun onBackPressed(): Boolean {
    ensureInitialized()

    val toolbarStateClass = toolbarViewModel.getToolbarStateStack(kurobaToolbarType)
      .popCurrentStateIfPossibleOrNull()
      ?: return false

    onToolbarStateChanged(
      newToolbarStateClass = toolbarStateClass,
      toolbarStateChangeType = ToolbarStateChangeType.Pop
    )

    return true
  }

  private fun onToolbarStateChanged(
    newToolbarStateClass: ToolbarStateClass,
    toolbarStateChangeType: ToolbarStateChangeType
  ) {
    BackgroundUtils.ensureMainThread()
    ensureInitialized()

    if (newToolbarStateClass == ToolbarStateClass.Uninitialized) {
      return
    }

    val needAddOrReplaceToolbarView = toolbarStateChangeType == ToolbarStateChangeType.Push
      || toolbarStateChangeType == ToolbarStateChangeType.Pop
      || kurobaToolbarViewContainer.childCount < 1

    if (needAddOrReplaceToolbarView) {
      Timber.tag(TAG).d("($kurobaToolbarType) onToolbarStateChanged() " +
        "newToolbarStateClass ($newToolbarStateClass)")

      addOrReplaceToolbarView(
        newToolbarStateClass = newToolbarStateClass,
        toolbarStateChangeType = toolbarStateChangeType
      )
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
        throw IllegalStateException("($kurobaToolbarType) Must not be called")
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
    val isSuitableState = toolbarContract.toolbarStateClass == newToolbarStateClass
      && toolbarContract.parentToolbarType == kurobaToolbarType

    if (isSuitableState) {
      val toolbarState = toolbarViewModel.getToolbarState<T>(
        kurobaToolbarType,
        newToolbarStateClass
      )

      toolbarContract.applyStateToUi(toolbarState)
    }
  }

  private fun addOrReplaceToolbarView(
    newToolbarStateClass: ToolbarStateClass,
    toolbarStateChangeType: ToolbarStateChangeType
  ) {
    val newView = when (newToolbarStateClass) {
      ToolbarStateClass.Uninitialized -> {
        throw IllegalStateException("($toolbarStateChangeType) Must not be called")
      }
      ToolbarStateClass.Catalog -> {
        KurobaCatalogToolbar(
          context = context,
          toolbarType = kurobaToolbarType,
          kurobaToolbarViewModel = toolbarViewModel,
          kurobaToolbarCallbacks = this
        )
      }
      ToolbarStateClass.Thread -> {
        KurobaThreadToolbar(
          context = context,
          toolbarType = kurobaToolbarType,
          kurobaToolbarViewModel = toolbarViewModel,
          kurobaToolbarCallbacks = this
        )
      }
      ToolbarStateClass.Search -> {
        KurobaSearchToolbar(
          context = context,
          toolbarType = kurobaToolbarType,
          kurobaToolbarViewModel = toolbarViewModel,
          kurobaToolbarCallbacks = this
        )
      }
      ToolbarStateClass.SimpleTitle -> TODO()
      ToolbarStateClass.Selection -> TODO()
    }

    check(kurobaToolbarViewContainer.childCount <= 1) {
      "($kurobaToolbarType) Bad child count: ${kurobaToolbarViewContainer.childCount}"
    }

    if (kurobaToolbarViewContainer.childCount > 0) {
      val currentToolbar = kurobaToolbarViewContainer.getChildAt(0)

      Timber.tag(TAG).d("($kurobaToolbarType) replaceStateView() oldView=" +
        currentToolbar.javaClass.simpleName
      )

      if (toolbarStateChangeType == ToolbarStateChangeType.Pop) {
        val contract = (currentToolbar as KurobaToolbarDelegateContract<*>)
        toolbarViewModel.resetState(contract.parentToolbarType, contract.toolbarStateClass)
      }

      kurobaToolbarViewContainer.removeAllViews()
    }

    Timber.tag(TAG).d("($kurobaToolbarType) replaceStateView() " +
      "newView = ${newView.javaClass.simpleName}")
    kurobaToolbarViewContainer.addView(newView)

    check(kurobaToolbarViewContainer.childCount <= 1) {
      "($kurobaToolbarType) Bad child count: ${kurobaToolbarViewContainer.childCount}"
    }
  }

  private fun ensureInitialized() {
    check(initialized) { "($kurobaToolbarType) Must initialize first!" }
    check(kurobaToolbarType != KurobaToolbarType.Uninitialized) { "kurobaToolbarType is Uninitialized!" }
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