package com.github.k1rakishou.kurobanewnavstacktest.widget.toolbar.catalog

import android.annotation.SuppressLint
import android.content.Context
import androidx.appcompat.widget.AppCompatImageView
import androidx.constraintlayout.widget.ConstraintLayout
import com.github.k1rakishou.kurobanewnavstacktest.R
import com.github.k1rakishou.kurobanewnavstacktest.utils.setEnabledFast
import com.github.k1rakishou.kurobanewnavstacktest.utils.setOnThrottlingClickListener
import com.github.k1rakishou.kurobanewnavstacktest.utils.setTextFast
import com.github.k1rakishou.kurobanewnavstacktest.widget.drawable.ArrowMenuDrawable
import com.github.k1rakishou.kurobanewnavstacktest.widget.toolbar.*
import com.google.android.material.textview.MaterialTextView
import timber.log.Timber

@SuppressLint("ViewConstructor")
class KurobaCatalogToolbar(
  context: Context,
  private val toolbarType: KurobaToolbarType,
  private val kurobaToolbarViewModel: KurobaToolbarViewModel,
  private val kurobaToolbarCallbacks: KurobaToolbarCallbacks
) : ConstraintLayout(context), KurobaToolbarDelegateContract<KurobaCatalogToolbarState> {
  private val arrowMenuDrawable: ArrowMenuDrawable
  private val hamburgButton: AppCompatImageView
  private val boardSelectionMenuButton: ConstraintLayout
  private val catalogTitle: MaterialTextView
  private val catalogSubtitle: MaterialTextView

  private val openSearchButton: AppCompatImageView
  private val refreshCatalogButton: AppCompatImageView
  private val openSubMenuButton: AppCompatImageView

  override val parentToolbarType: KurobaToolbarType
    get() = toolbarType

  override val toolbarStateClass: ToolbarStateClass
    get() = ToolbarStateClass.Catalog

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

      boardSelectionMenuButton.setOnThrottlingClickListener {
        kurobaToolbarViewModel.fireAction(ToolbarAction.Catalog.BoardSelectionMenuButtonClicked)
      }
      openSearchButton.setOnThrottlingClickListener {
        kurobaToolbarCallbacks.pushNewToolbarStateClass(toolbarType, ToolbarStateClass.Search)
      }
      refreshCatalogButton.setOnThrottlingClickListener {
        kurobaToolbarViewModel.fireAction(ToolbarAction.Catalog.RefreshCatalogButtonClicked)
      }
      openSubMenuButton.setOnThrottlingClickListener {
        kurobaToolbarViewModel.fireAction(ToolbarAction.Catalog.OpenSubMenuButtonClicked)
      }
    }
  }

  override fun applyStateToUi(toolbarState: KurobaCatalogToolbarState) {
    toolbarState.slideProgress?.let { slideProgress ->
      if (slideProgress != arrowMenuDrawable.progress) {
        arrowMenuDrawable.progress = 1f - slideProgress
      }
    }

    toolbarState.title?.let { title ->
      catalogTitle.setTextFast(title)
    }

    toolbarState.subtitle?.let { subtitle ->
      catalogSubtitle.setTextFast(subtitle)
    }

    toolbarState.enableControls?.let { enable ->
      enableDisableControls(enable)
    }
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