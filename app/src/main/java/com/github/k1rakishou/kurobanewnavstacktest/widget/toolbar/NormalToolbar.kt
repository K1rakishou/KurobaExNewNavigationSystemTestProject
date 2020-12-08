package com.github.k1rakishou.kurobanewnavstacktest.widget.toolbar

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import androidx.core.view.updateLayoutParams
import androidx.core.view.updatePadding
import com.github.k1rakishou.kurobanewnavstacktest.R
import com.github.k1rakishou.kurobanewnavstacktest.controller.ControllerType
import kotlin.reflect.KClass

class NormalToolbar @JvmOverloads constructor(
  context: Context,
  attributeSet: AttributeSet? = null,
  attrDefStyle: Int = 0
) : FrameLayout(context, attributeSet, attrDefStyle), ToolbarContract {
  private val actualToolbar: KurobaToolbar<KurobaToolbarViewModel>
  private lateinit var controllerType: ControllerType
  private var initialized = false

  init {
    inflate(context, R.layout.widget_normal_toolbar, this)
    val toolbarHeight = context.resources.getDimension(R.dimen.toolbar_height).toInt()

    val normalToolbarRoot = findViewById<FrameLayout>(R.id.normal_toolbar_root)
    actualToolbar = findViewById(R.id.normal_toolbar)

    normalToolbarRoot.setOnApplyWindowInsetsListener { v, insets ->
      v.updateLayoutParams<FrameLayout.LayoutParams> {
        height = toolbarHeight + insets.systemWindowInsetTop
      }
      v.updatePadding(top = insets.systemWindowInsetTop)

      return@setOnApplyWindowInsetsListener insets
    }
  }

  @Suppress("UNCHECKED_CAST")
  fun init(controllerType: ControllerType) {
    check(!this.initialized) { "Double initialization!" }

    this.initialized = true
    this.controllerType = controllerType

    val debugTag = when (controllerType) {
      ControllerType.Catalog -> KurobaToolbar.DebugTag.CatalogToolbar
      ControllerType.Thread -> KurobaToolbar.DebugTag.ThreadToolbar
    }

    val viewModelClass = when (controllerType) {
      ControllerType.Catalog -> KurobaCatalogToolbarViewModel::class
      ControllerType.Thread -> KurobaThreadToolbarViewModel::class
    }

    actualToolbar.init(debugTag, viewModelClass as KClass<KurobaToolbarViewModel>)
  }

  override fun collapsableView(): View {
    return this
  }

  override fun setTitle(controllerType: ControllerType, title: String) {
    if (this.controllerType != controllerType) {
      return
    }

    when (controllerType) {
      ControllerType.Catalog -> {
        actualToolbar.toolbarViewModel.newState(ToolbarStateUpdate.Catalog.UpdateTitle(title))
      }
      ControllerType.Thread -> {
        actualToolbar.toolbarViewModel.newState(ToolbarStateUpdate.Thread.UpdateTitle(title))
      }
    }
  }

  override fun setSubTitle(subtitle: String) {
    if (controllerType != ControllerType.Catalog) {
      return
    }

    actualToolbar.toolbarViewModel.newState(ToolbarStateUpdate.Catalog.UpdateSubTitle(subtitle))
  }

  override fun setToolbarVisibility(visibility: Int) {
    check(visibility == View.VISIBLE || visibility == View.INVISIBLE || visibility == View.GONE) {
      "Bad visibility parameter: $visibility"
    }

    this.visibility = visibility
  }
}