package com.github.k1rakishou.kurobanewnavstacktest.widget.toolbar

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import androidx.annotation.VisibleForTesting
import androidx.core.view.updateLayoutParams
import androidx.core.view.updatePadding
import com.github.k1rakishou.kurobanewnavstacktest.R
import com.github.k1rakishou.kurobanewnavstacktest.controller.ControllerType
import com.github.k1rakishou.kurobanewnavstacktest.utils.exhaustive
import com.github.k1rakishou.kurobanewnavstacktest.utils.setAlphaFast
import com.github.k1rakishou.kurobanewnavstacktest.utils.setOnApplyWindowInsetsListenerAndDoRequest
import com.github.k1rakishou.kurobanewnavstacktest.utils.setVisibilityFast
import kotlinx.coroutines.flow.Flow

class SlideToolbar @JvmOverloads constructor(
  context: Context,
  attributeSet: AttributeSet? = null,
  attrDefStyle: Int = 0
) : FrameLayout(context, attributeSet, attrDefStyle), ToolbarContract {
  private val actualCatalogToolbar: KurobaToolbar
  private val actualThreadToolbar: KurobaToolbar

  private var transitioningIntoCatalogToolbar: Boolean? = null
  private var initialToolbarShown = false
  private var catalogToolbarVisible: Boolean? = false
  private var initialized: Boolean = false

  init {
    inflate(context, R.layout.widget_slide_toolbar, this)
    id = R.id.slide_toolbar_id

    val slideToolbarRoot = findViewById<FrameLayout>(R.id.slide_toolbar_root)
    actualCatalogToolbar = findViewById(R.id.catalog_toolbar)
    actualThreadToolbar = findViewById(R.id.thread_toolbar)

    val toolbarHeight = context.resources.getDimension(R.dimen.toolbar_height).toInt()

    slideToolbarRoot.setOnApplyWindowInsetsListenerAndDoRequest { v, insets ->
      v.updateLayoutParams<FrameLayout.LayoutParams> {
        height = toolbarHeight + insets.systemWindowInsetTop
      }

      v.updatePadding(
        top = insets.systemWindowInsetTop,
        left = insets.systemWindowInsetLeft,
        right = insets.systemWindowInsetRight
      )

      return@setOnApplyWindowInsetsListenerAndDoRequest insets
    }
  }

  fun getCatalogToolbar(): KurobaToolbar {
    return actualCatalogToolbar
  }

  fun getThreadToolbar(): KurobaToolbar {
    return actualThreadToolbar
  }

  fun init() {
    check(!this.initialized) { "Double initialization!" }
    this.initialized = true

    actualCatalogToolbar.init(KurobaToolbarType.Catalog)
    actualThreadToolbar.init(KurobaToolbarType.Thread)
  }

  @Suppress("FoldInitializerAndIfToElvis")
  override fun onBackPressed(): Boolean {
    val catalogVisible = catalogToolbarVisible
    if (catalogVisible == null) {
      // Consume backpresses and do nothing while the sliding pane layout is sliding
      return true
    }

    if (catalogVisible) {
      return actualCatalogToolbar.onBackPressed()
    }

    return actualThreadToolbar.onBackPressed()
  }

  override fun collapsableView(): View {
    return this
  }

  override fun showSearchToolbar(toolbarType: KurobaToolbarType) {
    when (toolbarType) {
      KurobaToolbarType.Catalog -> {
        actualCatalogToolbar.pushNewToolbarStateClass(toolbarType, ToolbarStateClass.Search)
      }
      KurobaToolbarType.Thread -> {
        actualThreadToolbar.pushNewToolbarStateClass(toolbarType, ToolbarStateClass.Search)
      }
    }.exhaustive
  }

  override fun closeSearchToolbar(toolbarType: KurobaToolbarType) {
    when (toolbarType) {
      KurobaToolbarType.Catalog -> {
        actualCatalogToolbar.popToolbarStateClass(toolbarType, ToolbarStateClass.Search)
      }
      KurobaToolbarType.Thread -> {
        actualThreadToolbar.popToolbarStateClass(toolbarType, ToolbarStateClass.Search)
      }
    }.exhaustive
  }

  override fun listenForToolbarActions(toolbarType: KurobaToolbarType): Flow<ToolbarAction> {
    return when (toolbarType) {
      KurobaToolbarType.Catalog -> actualCatalogToolbar.listenForToolbarActions(toolbarType)
      KurobaToolbarType.Thread -> actualThreadToolbar.listenForToolbarActions(toolbarType)
    }
  }

  override fun showDefaultToolbar(toolbarType: KurobaToolbarType) {
    when (toolbarType) {
      KurobaToolbarType.Catalog -> {
        actualCatalogToolbar.pushNewToolbarStateClass(ToolbarStateClass.Catalog)
      }
      KurobaToolbarType.Thread -> {
        actualThreadToolbar.pushNewToolbarStateClass(ToolbarStateClass.Thread)
      }
    }.exhaustive
  }

  override fun setTitle(toolbarType: KurobaToolbarType, title: String) {
    when (toolbarType) {
      KurobaToolbarType.Catalog -> {
        actualCatalogToolbar.newState(ToolbarStateUpdate.Catalog.UpdateTitle(title))
      }
      KurobaToolbarType.Thread -> {
        actualThreadToolbar.newState(ToolbarStateUpdate.Thread.UpdateTitle(title))
      }
    }.exhaustive
  }

  override fun setSubTitle(subtitle: String) {
    actualCatalogToolbar.newState(ToolbarStateUpdate.Catalog.UpdateSubTitle(subtitle))
  }

  override fun setToolbarVisibility(visibility: Int) {
    check(visibility == View.VISIBLE || visibility == View.INVISIBLE || visibility == View.GONE) {
      "Bad visibility parameter: $visibility"
    }

    this.visibility = visibility
  }

  fun onBeforeSliding(transitioningIntoCatalogToolbar: Boolean) {
    checkNotNull(catalogToolbarVisible) { "The sliding is already in progress, wtf?" }
    check(this.transitioningIntoCatalogToolbar == null) { "transitioningIntoCatalogToolbar != null" }
    this.transitioningIntoCatalogToolbar = transitioningIntoCatalogToolbar

    actualCatalogToolbar.setVisibilityFast(View.VISIBLE)
    actualThreadToolbar.setVisibilityFast(View.VISIBLE)

    if (transitioningIntoCatalogToolbar) {
      actualCatalogToolbar.setAlphaFast(0f)
      actualThreadToolbar.setAlphaFast(1f)
    } else {
      actualCatalogToolbar.setAlphaFast(1f)
      actualThreadToolbar.setAlphaFast(0f)
    }
  }

  fun onSliding(offset: Float) {
    catalogToolbarVisible = null

    if (transitioningIntoCatalogToolbar == null) {
      return
    }

    actualCatalogToolbar.setAlphaFast(offset)
    actualThreadToolbar.setAlphaFast(1f - offset)

    actualCatalogToolbar.newState(ToolbarStateUpdate.Catalog.UpdateSlideProgress(offset))
    actualThreadToolbar.newState(ToolbarStateUpdate.Thread.UpdateSlideProgress(offset))
  }

  fun onAfterSliding(becameCatalogToolbar: Boolean) {
    catalogToolbarVisible = becameCatalogToolbar

    if (transitioningIntoCatalogToolbar == null) {
      return
    }

    if (transitioningIntoCatalogToolbar != becameCatalogToolbar) {
      // The sliding was canceled, we need to revert everything back
      if (becameCatalogToolbar) {
        actualCatalogToolbar.setVisibilityFast(View.VISIBLE)
        actualCatalogToolbar.setAlphaFast(1f)

        actualThreadToolbar.setVisibilityFast(View.GONE)
        actualThreadToolbar.setAlphaFast(0f)
      } else {
        actualCatalogToolbar.setVisibilityFast(View.GONE)
        actualCatalogToolbar.setAlphaFast(0f)

        actualThreadToolbar.setVisibilityFast(View.VISIBLE)
        actualThreadToolbar.setAlphaFast(1f)
      }

      transitioningIntoCatalogToolbar = null
      return
    }

    if (becameCatalogToolbar) {
      actualCatalogToolbar.setAlphaFast(1f)
      actualCatalogToolbar.setVisibilityFast(View.VISIBLE)

      actualThreadToolbar.setAlphaFast(0f)
      actualThreadToolbar.setVisibilityFast(View.GONE)
    } else {
      actualCatalogToolbar.setAlphaFast(0f)
      actualCatalogToolbar.setVisibilityFast(View.GONE)

      actualThreadToolbar.setAlphaFast(1f)
      actualThreadToolbar.setVisibilityFast(View.VISIBLE)
    }

    transitioningIntoCatalogToolbar = null
  }

  fun onControllerGainedFocus(isCatalogController: Boolean) {
    if (!initialToolbarShown) {
      initialToolbarShown = true
      catalogToolbarVisible = isCatalogController

      showToolbarInitial(isCatalogController)
      return
    }
  }

  private fun showToolbarInitial(isCatalogController: Boolean) {
    if (isCatalogController) {
      actualCatalogToolbar.setVisibilityFast(View.VISIBLE)
      actualThreadToolbar.setVisibilityFast(View.GONE)
    } else {
      actualCatalogToolbar.setVisibilityFast(View.GONE)
      actualThreadToolbar.setVisibilityFast(View.VISIBLE)
    }
  }

}