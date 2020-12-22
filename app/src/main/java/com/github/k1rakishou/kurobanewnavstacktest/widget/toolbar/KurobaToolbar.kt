package com.github.k1rakishou.kurobanewnavstacktest.widget.toolbar

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import androidx.activity.ComponentActivity
import androidx.annotation.VisibleForTesting
import com.github.k1rakishou.kurobanewnavstacktest.R
import com.github.k1rakishou.kurobanewnavstacktest.utils.BackgroundUtils
import com.github.k1rakishou.kurobanewnavstacktest.utils.viewModelStorage
import com.github.k1rakishou.kurobanewnavstacktest.widget.toolbar.catalog.KurobaCatalogToolbar
import com.github.k1rakishou.kurobanewnavstacktest.widget.toolbar.catalog.KurobaCatalogToolbarState
import com.github.k1rakishou.kurobanewnavstacktest.widget.toolbar.search.KurobaSearchToolbar
import com.github.k1rakishou.kurobanewnavstacktest.widget.toolbar.search.KurobaSearchToolbarState
import com.github.k1rakishou.kurobanewnavstacktest.widget.toolbar.thread.KurobaThreadToolbar
import com.github.k1rakishou.kurobanewnavstacktest.widget.toolbar.thread.KurobaThreadToolbarState
import com.github.k1rakishou.kurobanewnavstacktest.widget.toolbar.uninitialized.KurobaUninitializedToolbar
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
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

  private lateinit var kurobaToolbarType: KurobaToolbarType
  private var initialized = false

  init {
    inflate(context, R.layout.kuroba_toolbar, this).apply {
      kurobaToolbarViewContainer = findViewById(R.id.kuroba_toolbar_view_container)
    }
  }

  fun init(newKurobaToolbarType: KurobaToolbarType) {
    BackgroundUtils.ensureMainThread()
    check(!initialized) { "Double initialization!" }

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

      pushNewToolbarStateClassInternal(
        kurobaToolbarType = kurobaToolbarType,
        toolbarStateClass = prevPrevToolbarStateClass,
        isInitialization = true
      )
      return
    }

    Timber.tag(TAG).d("Fresh initialization kurobaToolbarType=$kurobaToolbarType, " +
      "prevPrevToolbarStateClass=$prevPrevToolbarStateClass")

    pushNewToolbarStateClassInternal(
      kurobaToolbarType = kurobaToolbarType,
      toolbarStateClass = ToolbarStateClass.Uninitialized,
      isInitialization = true
    )
  }

  @VisibleForTesting
  fun getViewModel(): KurobaToolbarViewModel = toolbarViewModel

  fun newState(toolbarStateUpdate: ToolbarStateUpdate) {
    BackgroundUtils.ensureMainThread()
    toolbarViewModel.newState(kurobaToolbarType, toolbarStateUpdate)
  }

  override fun onDetachedFromWindow() {
    super.onDetachedFromWindow()

    job.cancelChildren()
  }

  override fun popCurrentToolbarStateClass() {
    BackgroundUtils.ensureMainThread()

    val toolbarStateClass = toolbarViewModel.getToolbarStateStack(kurobaToolbarType)
      .popCurrentStateIfPossibleOrNull()
      ?: return

    onToolbarStateChanged(
      newToolbarStateClass = toolbarStateClass,
      toolbarStateChangeType = ToolbarStateChangeType.Pop
    )
  }

  fun pushNewToolbarStateClass(toolbarStateClass: ToolbarStateClass) {
    BackgroundUtils.ensureMainThread()

    pushNewToolbarStateClass(kurobaToolbarType, toolbarStateClass)
  }

  fun closeToolbar() {
    BackgroundUtils.ensureMainThread()

    if (!toolbarViewModel.getToolbarStateStack(kurobaToolbarType).clearState()) {
      return
    }

    onToolbarStateChanged(
      newToolbarStateClass = ToolbarStateClass.Uninitialized,
      toolbarStateChangeType = ToolbarStateChangeType.Pop
    )
  }

  fun restoreLastToolbarActions(toolbarType: KurobaToolbarType) {
    val toolbarStateClass = toolbarViewModel.getToolbarStateStack(toolbarType).getPrevToolbarStateClass()
    if (toolbarStateClass == ToolbarStateClass.Uninitialized) {
      return
    }

    val toolbarStateContract = toolbarViewModel.getToolbarState<ToolbarStateContract>(
      kurobaToolbarType,
      toolbarStateClass
    )

    val lastToolbarActions = toolbarStateContract.restoreLastToolbarActions(toolbarType)
    if (lastToolbarActions.isEmpty()) {
      return
    }

    lastToolbarActions.forEach { toolbarAction -> toolbarViewModel.fireAction(toolbarAction) }
  }

  override fun popToolbarStateClass(
    kurobaToolbarType: KurobaToolbarType,
    toolbarStateClass: ToolbarStateClass
  ) {
    BackgroundUtils.ensureMainThread()

    val newStateClass = toolbarViewModel.getToolbarStateStack(kurobaToolbarType)
      .popIfStateOnTop(toolbarStateClass)

    checkNotNull(newStateClass) { "Something went wrong: newStateClass==null" }

    onToolbarStateChanged(
      newToolbarStateClass = newStateClass,
      toolbarStateChangeType = ToolbarStateChangeType.Pop
    )
  }

  override fun pushNewToolbarStateClass(
    kurobaToolbarType: KurobaToolbarType,
    toolbarStateClass: ToolbarStateClass
  ) {
    pushNewToolbarStateClassInternal(
      kurobaToolbarType = kurobaToolbarType,
      toolbarStateClass = toolbarStateClass,
      isInitialization = false
    )
  }

  private fun pushNewToolbarStateClassInternal(
    kurobaToolbarType: KurobaToolbarType,
    toolbarStateClass: ToolbarStateClass,
    isInitialization: Boolean
  ) {
    BackgroundUtils.ensureMainThread()

    check(this.kurobaToolbarType == kurobaToolbarType) {
      "Bad kurobaToolbarType, current=${this.kurobaToolbarType}, input=$kurobaToolbarType"
    }

    val stateStack = toolbarViewModel.getToolbarStateStack(kurobaToolbarType)
    val addedToStack = stateStack.pushToolbarStateClass(toolbarStateClass)

    if (!addedToStack) {
      // Already in stack
      if (!stateStack.isTop(toolbarStateClass)) {
        // Already in stack and is not at the top, do nothing
        return
      }

      if (!isInitialization) {
        ensureViewMatchesState(toolbarStateClass)
      }
    }

    onToolbarStateChanged(
      newToolbarStateClass = toolbarStateClass,
      toolbarStateChangeType = ToolbarStateChangeType.Push
    )
  }

  private fun ensureViewMatchesState(topToolbarStateClass: ToolbarStateClass) {
    if (kurobaToolbarViewContainer.childCount != 1) {
      throw IllegalStateException("Bad children count: ${kurobaToolbarViewContainer.childCount}")
    }

    val toolbarView = kurobaToolbarViewContainer.getChildAt(0)

    when (topToolbarStateClass) {
      ToolbarStateClass.Uninitialized -> {
        check(toolbarView is KurobaUninitializedToolbar) {
          "Unexpected toolbar view: ${toolbarView.javaClass.simpleName}"
        }
      }
      ToolbarStateClass.Catalog -> {
        check(toolbarView is KurobaCatalogToolbar) {
          "Unexpected toolbar view: ${toolbarView.javaClass.simpleName}"
        }
      }
      ToolbarStateClass.Thread -> {
        check(toolbarView is KurobaThreadToolbar) {
          "Unexpected toolbar view: ${toolbarView.javaClass.simpleName}"
        }
      }
      ToolbarStateClass.Search -> {
        check(toolbarView is KurobaSearchToolbar) {
          "Unexpected toolbar view: ${toolbarView.javaClass.simpleName}"
        }
      }
      ToolbarStateClass.SimpleTitle -> TODO()
      ToolbarStateClass.Selection -> TODO()
    }
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
        // no-op
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
    val newToolbar = when (newToolbarStateClass) {
      ToolbarStateClass.Uninitialized -> {
        KurobaUninitializedToolbar(
          context = context,
          toolbarType = kurobaToolbarType
        )
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

        onToolbarDestroyed(contract.toolbarStateClass)
      }

      kurobaToolbarViewContainer.removeAllViews()
    }

    Timber.tag(TAG).d("($kurobaToolbarType) replaceStateView() newToolbar=${newToolbar.javaClass.simpleName}")

    kurobaToolbarViewContainer.addView(newToolbar)
    onToolbarCreated(newToolbar.toolbarStateClass)

    check(kurobaToolbarViewContainer.childCount <= 1) {
      "($kurobaToolbarType) Bad child count: ${kurobaToolbarViewContainer.childCount}"
    }
  }

  private fun onToolbarCreated(toolbarStateClass: ToolbarStateClass) {
    when (toolbarStateClass) {
      ToolbarStateClass.Search -> {
        toolbarViewModel.fireAction(ToolbarAction.Search.SearchShown(kurobaToolbarType))
      }
      ToolbarStateClass.Uninitialized,
      ToolbarStateClass.Catalog,
      ToolbarStateClass.Thread,
      ToolbarStateClass.SimpleTitle,
      ToolbarStateClass.Selection -> {
        // no-op
      }
    }
  }

  private fun onToolbarDestroyed(toolbarStateClass: ToolbarStateClass) {
    when (toolbarStateClass) {
      ToolbarStateClass.Search -> {
        toolbarViewModel.fireAction(ToolbarAction.Search.SearchHidden(kurobaToolbarType))
      }
      ToolbarStateClass.Uninitialized,
      ToolbarStateClass.Catalog,
      ToolbarStateClass.Thread,
      ToolbarStateClass.SimpleTitle,
      ToolbarStateClass.Selection -> {
        // no-op
      }
    }
  }

  private fun ensureInitialized() {
    check(initialized) { "($kurobaToolbarType) Must initialize first!" }
    check(::kurobaToolbarType.isInitialized) { "kurobaToolbarType is not initialized!" }
  }

  fun listenForToolbarActions(toolbarType: KurobaToolbarType): Flow<ToolbarAction> {
    return toolbarViewModel.listenForToolbarActions(toolbarType)
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