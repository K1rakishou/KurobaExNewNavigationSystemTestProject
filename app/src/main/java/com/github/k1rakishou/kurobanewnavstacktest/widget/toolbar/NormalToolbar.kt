package com.github.k1rakishou.kurobanewnavstacktest.widget.toolbar

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import androidx.core.view.updateLayoutParams
import androidx.core.view.updatePadding
import com.github.k1rakishou.kurobanewnavstacktest.R
import com.github.k1rakishou.kurobanewnavstacktest.controller.base.CollapsableView
import com.github.k1rakishou.kurobanewnavstacktest.utils.exhaustive
import kotlinx.coroutines.flow.Flow

class NormalToolbar @JvmOverloads constructor(
  context: Context,
  attributeSet: AttributeSet? = null,
  attrDefStyle: Int = 0
) : FrameLayout(context, attributeSet, attrDefStyle), ToolbarContract {
  private val actualToolbar: KurobaToolbar
  private var initialized = false
  private lateinit var toolbarType: KurobaToolbarType
  private val normalToolbarRoot: FrameLayout

  init {
    inflate(context, R.layout.widget_normal_toolbar, this)

    normalToolbarRoot = findViewById<FrameLayout>(R.id.normal_toolbar_root)
    actualToolbar = findViewById(R.id.normal_toolbar)
  }

  @Suppress("UNCHECKED_CAST")
  fun init(toolbarType: KurobaToolbarType) {
    check(!this.initialized) { "Double initialization!" }

    this.initialized = true
    this.toolbarType = toolbarType

    val kurobaToolbarType = when (toolbarType) {
      KurobaToolbarType.Catalog -> KurobaToolbarType.Catalog
      KurobaToolbarType.Thread -> KurobaToolbarType.Thread
    }

    val toolbarHeight = context.resources.getDimension(R.dimen.toolbar_height).toInt()

    normalToolbarRoot.setOnApplyWindowInsetsListener { v, insets ->
      v.updateLayoutParams<FrameLayout.LayoutParams> {
        height = toolbarHeight + insets.systemWindowInsetTop
      }

      var leftPadding = insets.systemWindowInsetLeft
      var rightPadding = insets.systemWindowInsetRight

      if (kurobaToolbarType == KurobaToolbarType.Catalog) {
        rightPadding = 0
      } else {
        leftPadding = 0
      }

      v.updatePadding(
        top = insets.systemWindowInsetTop,
        left = leftPadding,
        right = rightPadding
      )

      return@setOnApplyWindowInsetsListener insets
    }

    actualToolbar.init(kurobaToolbarType)
  }

  override fun onBackPressed(): Boolean {
    return actualToolbar.onBackPressed()
  }

  override fun collapsableView(): CollapsableView {
    return this
  }

  override fun height(): Float {
    return height.toFloat()
  }

  override fun translationY(): Float {
    return translationY
  }

  override fun translationY(newTranslationY: Float) {
    translationY = newTranslationY
  }

  override fun restoreLastToolbarActions(kurobaToolbarType: KurobaToolbarType) {
    actualToolbar.restoreLastToolbarActions(kurobaToolbarType)
  }

  override fun showSearchToolbar(toolbarType: KurobaToolbarType) {
    actualToolbar.pushNewToolbarStateClass(toolbarType, ToolbarStateClass.Search)
  }

  override fun closeSearchToolbar(toolbarType: KurobaToolbarType) {
    actualToolbar.popToolbarStateClass(toolbarType, ToolbarStateClass.Search)
  }

  override fun closeToolbar(toolbarType: KurobaToolbarType) {
    actualToolbar.closeToolbar()
  }

  override fun listenForToolbarActions(toolbarType: KurobaToolbarType): Flow<ToolbarAction> {
    return actualToolbar.listenForToolbarActions(toolbarType)
  }

  override fun showDefaultToolbar(toolbarType: KurobaToolbarType) {
    val initialToolbarClass = when (toolbarType) {
      KurobaToolbarType.Catalog -> ToolbarStateClass.Catalog
      KurobaToolbarType.Thread -> ToolbarStateClass.Thread
    }

    actualToolbar.pushNewToolbarStateClass(initialToolbarClass)
  }

  override fun setTitle(toolbarType: KurobaToolbarType, title: String) {
    if (this.toolbarType != toolbarType) {
      return
    }

    when (toolbarType) {
      KurobaToolbarType.Catalog -> {
        actualToolbar.newState(ToolbarStateUpdate.Catalog.UpdateTitle(title))
      }
      KurobaToolbarType.Thread -> {
        actualToolbar.newState(ToolbarStateUpdate.Thread.UpdateTitle(title))
      }
    }.exhaustive
  }

  override fun setSubTitle(subtitle: String) {
    if (toolbarType != KurobaToolbarType.Catalog) {
      return
    }

    actualToolbar.newState(ToolbarStateUpdate.Catalog.UpdateSubTitle(subtitle))
  }

  override fun setToolbarVisibility(visibility: Int) {
    check(visibility == View.VISIBLE || visibility == View.INVISIBLE || visibility == View.GONE) {
      "Bad visibility parameter: $visibility"
    }

    this.visibility = visibility
  }
}