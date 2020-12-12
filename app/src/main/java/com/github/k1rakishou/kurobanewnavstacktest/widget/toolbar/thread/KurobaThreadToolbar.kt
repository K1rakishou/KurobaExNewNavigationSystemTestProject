package com.github.k1rakishou.kurobanewnavstacktest.widget.toolbar.thread

import android.annotation.SuppressLint
import android.content.Context
import androidx.appcompat.widget.AppCompatImageView
import androidx.constraintlayout.widget.ConstraintLayout
import com.github.k1rakishou.kurobanewnavstacktest.R
import com.github.k1rakishou.kurobanewnavstacktest.utils.setOnThrottlingClickListener
import com.github.k1rakishou.kurobanewnavstacktest.utils.setTextFast
import com.github.k1rakishou.kurobanewnavstacktest.widget.drawable.ArrowMenuDrawable
import com.github.k1rakishou.kurobanewnavstacktest.widget.toolbar.*
import com.google.android.material.textview.MaterialTextView
import timber.log.Timber

@SuppressLint("ViewConstructor")
class KurobaThreadToolbar(
  context: Context,
  private val toolbarType: KurobaToolbarType,
  private val kurobaToolbarViewModel: KurobaToolbarViewModel,
  private val kurobaToolbarCallbacks: KurobaToolbarCallbacks
) : ConstraintLayout(context), KurobaToolbarDelegateContract<KurobaThreadToolbarState> {
  private val arrowMenuDrawable: ArrowMenuDrawable
  private val hamburgButton: AppCompatImageView

  private val threadTitle: MaterialTextView

  private val openSearchButton: AppCompatImageView
  private val openGalleryButton: AppCompatImageView
  private val bookmarkThreadButton: AppCompatImageView
  private val openSubmenuButton: AppCompatImageView

  override val parentToolbarType: KurobaToolbarType
    get() = toolbarType
  override val toolbarStateClass: ToolbarStateClass
    get() = ToolbarStateClass.Thread

  init {
    inflate(context, R.layout.kuroba_thread_toolbar, this).apply {
      arrowMenuDrawable = ArrowMenuDrawable()

      hamburgButton = findViewById(R.id.hamburg_button)
      hamburgButton.setImageDrawable(arrowMenuDrawable)

      threadTitle = findViewById(R.id.thread_title)

      openSearchButton = findViewById(R.id.open_search_button)
      openGalleryButton = findViewById(R.id.open_gallery_button)
      bookmarkThreadButton = findViewById(R.id.bookmark_thread_button)
      openSubmenuButton = findViewById(R.id.open_submenu_button)

      openSearchButton.setOnThrottlingClickListener {
        kurobaToolbarCallbacks.pushNewToolbarStateClass(toolbarType, ToolbarStateClass.Search)
      }
      openGalleryButton.setOnThrottlingClickListener {
        kurobaToolbarViewModel.fireAction(ToolbarAction.Thread.OpenGalleryButtonClicked(toolbarType))
      }
      bookmarkThreadButton.setOnThrottlingClickListener {
        kurobaToolbarViewModel.fireAction(ToolbarAction.Thread.BookmarkThreadButtonClicked(toolbarType))
      }
      openSubmenuButton.setOnThrottlingClickListener {
        kurobaToolbarViewModel.fireAction(ToolbarAction.Thread.OpenSubmenuButtonClicked(toolbarType))
      }
    }
  }

  override fun applyStateToUi(toolbarState: KurobaThreadToolbarState) {
    toolbarState.slideProgress?.let { slideProgress ->
      val progress = 1f - slideProgress
      if (progress != arrowMenuDrawable.progress) {
        arrowMenuDrawable.progress = progress
      }
    }

    toolbarState.threadTitle?.let { title ->
      threadTitle.setTextFast(title)
    }

    toolbarState.enableControls?.let { enable ->
      enableDisableControls(enable)
    }
  }

  private fun enableDisableControls(enable: Boolean) {
    openGalleryButton.isEnabled = enable
    bookmarkThreadButton.isEnabled = enable
    openSubmenuButton.isEnabled = enable
  }

  companion object {
    private const val TAG = "KurobaThreadToolbar"
  }

}