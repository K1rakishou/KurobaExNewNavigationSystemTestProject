package com.github.k1rakishou.kurobanewnavstacktest.widget.toolbar

import android.annotation.SuppressLint
import android.content.Context
import androidx.appcompat.widget.AppCompatImageView
import androidx.constraintlayout.widget.ConstraintLayout
import com.github.k1rakishou.kurobanewnavstacktest.R
import com.github.k1rakishou.kurobanewnavstacktest.utils.setEnabledFast
import com.github.k1rakishou.kurobanewnavstacktest.utils.setTextFast
import com.github.k1rakishou.kurobanewnavstacktest.widget.drawable.ArrowMenuDrawable
import com.google.android.material.textview.MaterialTextView
import timber.log.Timber

@SuppressLint("ViewConstructor")
class KurobaCatalogToolbar(
  context: Context,
  private val kurobaToolbarViewModel: KurobaCatalogToolbarViewModel
) : ConstraintLayout(context) {
  private val arrowMenuDrawable: ArrowMenuDrawable
  private val hamburgButton: AppCompatImageView
  private val boardSelectionMenuButton: ConstraintLayout
  private val catalogTitle: MaterialTextView
  private val catalogSubtitle: MaterialTextView

  private val openSearchButton: AppCompatImageView
  private val refreshCatalogButton: AppCompatImageView
  private val openSubMenuButton: AppCompatImageView

  private val prevCatalogToolbarState = KurobaCatalogToolbarViewModel.KurobaCatalogToolbarState()

  init {
    inflate(context, R.layout.kuroba_catalog_toolbar, this).apply {
      arrowMenuDrawable = ArrowMenuDrawable()

      hamburgButton = findViewById(R.id.hamburg_button)
      hamburgButton.setImageDrawable(arrowMenuDrawable)

      boardSelectionMenuButton = findViewById(R.id.board_selection_menu_button)
      catalogTitle = findViewById(R.id.catalog_title)
      catalogSubtitle = findViewById(R.id.catalog_subtitle)

      openSearchButton = findViewById(R.id.open_search_button)
      refreshCatalogButton = findViewById(R.id.refresh_catalog_button)
      openSubMenuButton = findViewById(R.id.open_submenu_button)

      boardSelectionMenuButton.setOnClickListener {
        kurobaToolbarViewModel.fireAction(ToolbarAction.Catalog.BoardSelectionMenuButtonClicked)
      }
      openSearchButton.setOnClickListener {
        kurobaToolbarViewModel.fireAction(ToolbarAction.Catalog.OpenSearchButtonClicked)
      }
      refreshCatalogButton.setOnClickListener {
        kurobaToolbarViewModel.fireAction(ToolbarAction.Catalog.RefreshCatalogButtonClicked)
      }
      openSubMenuButton.setOnClickListener {
        kurobaToolbarViewModel.fireAction(ToolbarAction.Catalog.OpenSubMenuButtonClicked)
      }
    }
  }

  override fun onAttachedToWindow() {
    super.onAttachedToWindow()

    prevCatalogToolbarState.reset()
  }

  fun applyStateChange() {
    val catalogToolbarState = kurobaToolbarViewModel.getCatalogToolbarState()
    if (catalogToolbarState == prevCatalogToolbarState) {
      return
    }

    Timber.tag(TAG).d("applyStateChange() catalogToolbarState=$catalogToolbarState")

    catalogToolbarState.slideProgress?.let { slideProgress ->
      if (slideProgress != arrowMenuDrawable.progress) {
        arrowMenuDrawable.progress = 1f - slideProgress
      }
    }

    catalogToolbarState.title?.let { title ->
      catalogTitle.setTextFast(title)
    }

    catalogToolbarState.subtitle?.let { subtitle ->
      catalogSubtitle.setTextFast(subtitle)
    }

    catalogToolbarState.enableControls?.let { enable ->
      enableDisableControls(enable)
    }

    prevCatalogToolbarState.fillFrom(catalogToolbarState)
  }

  private fun enableDisableControls(enable: Boolean) {
    boardSelectionMenuButton.setEnabledFast(enable)
    openSearchButton.setEnabledFast(enable)
    refreshCatalogButton.setEnabledFast(enable)
    openSubMenuButton.setEnabledFast(enable)
  }

  companion object {
    private const val TAG = "KurobaCatalogToolbar"
  }

}