package com.github.k1rakishou.kurobanewnavstacktest.widget.toolbar

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import androidx.core.view.updateLayoutParams
import androidx.core.view.updatePadding
import com.github.k1rakishou.kurobanewnavstacktest.R
import com.github.k1rakishou.kurobanewnavstacktest.utils.setOnApplyWindowInsetsListenerAndDoRequest
import com.google.android.material.appbar.MaterialToolbar
import timber.log.Timber

class SlideToolbar @JvmOverloads constructor(
  context: Context,
  attributeSet: AttributeSet? = null,
  attrDefStyle: Int = 0
) : FrameLayout(context, attributeSet, attrDefStyle), ToolbarContract {
  private val actualCatalogToolbar: MaterialToolbar
  private val actualThreadToolbar: MaterialToolbar

  private var prevSliding: Float? = null
  private var initialToolbarShown = false
  private var catalogToolbarVisible: Boolean = false

  init {
    inflate(context, R.layout.widget_slide_toolbar, this)

    val slideToolbarRoot = findViewById<FrameLayout>(R.id.slide_toolbar_root)
    actualCatalogToolbar = findViewById(R.id.catalog_toolbar)
    actualThreadToolbar = findViewById(R.id.thread_toolbar)

    val toolbarHeight = context.resources.getDimension(R.dimen.toolbar_height).toInt()

    slideToolbarRoot.setOnApplyWindowInsetsListenerAndDoRequest { v, insets ->
      v.updateLayoutParams<FrameLayout.LayoutParams> {
        height = toolbarHeight + insets.systemWindowInsetTop
      }
      v.updatePadding(top = insets.systemWindowInsetTop)

      return@setOnApplyWindowInsetsListenerAndDoRequest insets
    }
  }

  override fun collapsableView(): View {
    return this
  }

  override fun setTitle(title: String) {
    when (catalogToolbarVisible) {
      true -> actualCatalogToolbar.title = title
      false -> actualThreadToolbar.title = title
    }
  }

  override fun setToolbarVisibility(visibility: Int) {
    check(visibility == View.VISIBLE || visibility == View.INVISIBLE || visibility == View.GONE) {
      "Bad visibility parameter: $visibility"
    }

    this.visibility = visibility
  }

  fun onSliding(offset: Float) {
    Timber.d("onSliding offset=$offset")

    // TODO(KurobaEx): not tested
    when {
      offset >= 0.99f -> catalogToolbarVisible = true
      offset <= 0.01f -> catalogToolbarVisible = false
    }
  }

  fun onControllerGainedFocus(isCatalogController: Boolean) {
    if (!initialToolbarShown) {
      initialToolbarShown = true
      showToolbarInitial(isCatalogController)
    }
  }

  private fun showToolbarInitial(isCatalogController: Boolean) {
    if (isCatalogController) {
      actualCatalogToolbar.visibility = View.VISIBLE
      actualThreadToolbar.visibility = View.GONE
    } else {
      actualCatalogToolbar.visibility = View.GONE
      actualThreadToolbar.visibility = View.VISIBLE
    }
  }

}