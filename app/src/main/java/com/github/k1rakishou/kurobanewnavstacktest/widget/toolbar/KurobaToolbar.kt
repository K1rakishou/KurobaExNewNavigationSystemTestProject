package com.github.k1rakishou.kurobanewnavstacktest.widget.toolbar

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import androidx.activity.ComponentActivity
import com.github.k1rakishou.kurobanewnavstacktest.R
import com.github.k1rakishou.kurobanewnavstacktest.utils.BackgroundUtils
import com.github.k1rakishou.kurobanewnavstacktest.utils.exhaustive
import com.github.k1rakishou.kurobanewnavstacktest.utils.myViewModels
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect
import timber.log.Timber
import java.lang.IllegalStateException
import kotlin.reflect.KClass

@SuppressLint("BinaryOperationInTimber")
class KurobaToolbar <T : KurobaToolbarViewModel> @JvmOverloads constructor(
  context: Context,
  attributeSet: AttributeSet? = null,
  defAttrStyle: Int = 0
) : FrameLayout(context, attributeSet, defAttrStyle) {
  private val job = SupervisorJob()
  private val toolbarScope = CoroutineScope(job + Dispatchers.Main)

  private val kurobaToolbarViewContainer: FrameLayout

  lateinit var toolbarViewModel: T
  private var debugTag: DebugTag = DebugTag.Uninitialized
  private var initialized = false

  init {
    inflate(context, R.layout.kuroba_toolbar, this).apply {
      kurobaToolbarViewContainer = findViewById(R.id.kuroba_toolbar_view_container)
    }
  }

  fun init(debugTag: DebugTag, viewModelClass: KClass<T>) {
    check(!this.initialized) { "Double initialization!" }

    toolbarViewModel = (context as ComponentActivity).myViewModels(viewModelClass).value

    this.debugTag = debugTag
    this.initialized = true

    toolbarScope.launch {
      toolbarViewModel.listenForToolbarStateChanges()
        .collect { toolbarStateClass -> onToolbarStateChanged(toolbarStateClass) }
    }
  }

  override fun onDetachedFromWindow() {
    super.onDetachedFromWindow()

    job.cancelChildren()
  }

  private fun onToolbarStateChanged(newToolbarStateClass: ToolbarStateClass) {
    BackgroundUtils.ensureMainThread()
    ensureInitialized()

    Timber.tag(TAG).d("($debugTag) onToolbarStateChanged() newToolbarStateClass=$newToolbarStateClass")

    if (newToolbarStateClass == ToolbarStateClass.Uninitialized) {
      return
    }

    val prevToolbarStateClass = toolbarViewModel.getPrevToolbarStateClass()
    if (prevToolbarStateClass != newToolbarStateClass || kurobaToolbarViewContainer.childCount < 1) {
      Timber.tag(TAG).d("($debugTag) onToolbarStateChanged() prevToolbarStateClass ($prevToolbarStateClass) " +
        "!= newToolbarStateClass ($newToolbarStateClass)")

      replaceStateView(newToolbarStateClass)
    }

    // TODO(KurobaEx): animations and stuff

    applyStateChange(newToolbarStateClass)
    toolbarViewModel.updatePrevToolbarStateClass(newToolbarStateClass)
  }

  private fun applyStateChange(newToolbarStateClass: ToolbarStateClass) {
    if (kurobaToolbarViewContainer.childCount != 1) {
      throw IllegalStateException("Bad children count: ${kurobaToolbarViewContainer.childCount}")
    }

    when (newToolbarStateClass) {
      ToolbarStateClass.Uninitialized -> {
        throw IllegalStateException("($debugTag) Must not be called")
      }
      ToolbarStateClass.Catalog -> {
        val child = kurobaToolbarViewContainer.getChildAt(0)
        check(child is KurobaCatalogToolbar) {
          "($debugTag) Unexpected child view: ${child.javaClass.simpleName}"
        }

        child.applyStateChange()
      }
      ToolbarStateClass.Thread -> {
        val child = kurobaToolbarViewContainer.getChildAt(0)
        check(child is KurobaThreadToolbar) {
          "($debugTag) Unexpected child view: ${child.javaClass.simpleName}"
        }

        child.applyStateChange()
      }
      ToolbarStateClass.SimpleTitle -> TODO()
      ToolbarStateClass.Search -> TODO()
      ToolbarStateClass.Selection -> TODO()
    }.exhaustive
  }

  private fun replaceStateView(newToolbarStateClass: ToolbarStateClass) {
    val newView = when (newToolbarStateClass) {
      ToolbarStateClass.Uninitialized -> {
        throw IllegalStateException("($debugTag) Must not be called")
      }
      ToolbarStateClass.Catalog -> {
        KurobaCatalogToolbar(context, toolbarViewModel as KurobaCatalogToolbarViewModel)
      }
      ToolbarStateClass.Thread -> {
        KurobaThreadToolbar(context, toolbarViewModel as KurobaThreadToolbarViewModel)
      }
      ToolbarStateClass.SimpleTitle -> TODO()
      ToolbarStateClass.Search -> TODO()
      ToolbarStateClass.Selection -> TODO()
    }

    check(kurobaToolbarViewContainer.childCount <= 1) {
      "($debugTag) Bad child count: ${kurobaToolbarViewContainer.childCount}"
    }

    if (kurobaToolbarViewContainer.childCount > 0) {
      Timber.tag(TAG).d("($debugTag) replaceStateView() oldView=" +
        kurobaToolbarViewContainer.getChildAt(0).javaClass.simpleName
      )

      kurobaToolbarViewContainer.removeAllViews()
    }

    Timber.tag(TAG).d("($debugTag) replaceStateView() newView = ${newView.javaClass.simpleName}")
    kurobaToolbarViewContainer.addView(newView)

    check(kurobaToolbarViewContainer.childCount <= 1) {
      "($debugTag) Bad child count: ${kurobaToolbarViewContainer.childCount}"
    }
  }

  private fun ensureInitialized() {
    check(initialized) { "Must initialize first!" }
  }

  enum class DebugTag {
    Uninitialized,
    CatalogToolbar,
    ThreadToolbar
  }

  companion object {
    private const val TAG = "KurobaToolbar"
  }

}