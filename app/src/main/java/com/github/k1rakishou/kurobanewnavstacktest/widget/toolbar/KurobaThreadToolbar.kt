package com.github.k1rakishou.kurobanewnavstacktest.widget.toolbar

import android.annotation.SuppressLint
import android.content.Context
import androidx.appcompat.widget.AppCompatImageView
import androidx.constraintlayout.widget.ConstraintLayout
import com.github.k1rakishou.kurobanewnavstacktest.R
import com.github.k1rakishou.kurobanewnavstacktest.utils.setTextFast
import com.github.k1rakishou.kurobanewnavstacktest.widget.drawable.ArrowMenuDrawable
import com.google.android.material.textview.MaterialTextView
import timber.log.Timber

@SuppressLint("ViewConstructor")
class KurobaThreadToolbar(
  context: Context,
  private val kurobaToolbarViewModel: KurobaThreadToolbarViewModel
) : ConstraintLayout(context) {
  private val arrowMenuDrawable: ArrowMenuDrawable
  private val hamburgButton: AppCompatImageView

  private val threadTitle: MaterialTextView

  private val openGalleryButton: AppCompatImageView
  private val bookmarkThreadButton: AppCompatImageView
  private val openSubmenuButton: AppCompatImageView

  private val prevThreadToolbarState = KurobaThreadToolbarViewModel.KurobaThreadToolbarState()

  init {
    inflate(context, R.layout.kuroba_thread_toolbar, this).apply {
      arrowMenuDrawable = ArrowMenuDrawable()

      hamburgButton = findViewById(R.id.hamburg_button)
      hamburgButton.setImageDrawable(arrowMenuDrawable)

      threadTitle = findViewById(R.id.thread_title)

      openGalleryButton = findViewById(R.id.open_gallery_button)
      bookmarkThreadButton = findViewById(R.id.bookmark_thread_button)
      openSubmenuButton = findViewById(R.id.open_submenu_button)

      openGalleryButton.setOnClickListener {
        kurobaToolbarViewModel.fireAction(ToolbarAction.Thread.OpenGalleryButtonClicked)
      }
      bookmarkThreadButton.setOnClickListener {
        kurobaToolbarViewModel.fireAction(ToolbarAction.Thread.BookmarkThreadButtonClicked)
      }
      openSubmenuButton.setOnClickListener {
        kurobaToolbarViewModel.fireAction(ToolbarAction.Thread.OpenSubmenuButtonClicked)
      }
    }
  }

  fun applyStateChange() {
    val threadToolbarState = kurobaToolbarViewModel.getThreadToolbarState()
    if (threadToolbarState == prevThreadToolbarState) {
      return
    }

    Timber.tag(TAG).d("applyStateChange() threadToolbarState=$prevThreadToolbarState")

    threadToolbarState.slideProgress?.let { slideProgress ->
      if (slideProgress != arrowMenuDrawable.progress) {
        arrowMenuDrawable.progress = 1f - slideProgress
      }
    }

    threadToolbarState.threadTitle?.let { title ->
      threadTitle.setTextFast(title)
    }

    threadToolbarState.enableControls?.let { enable ->
      enableDisableControls(enable)
    }

    prevThreadToolbarState.fillFrom(threadToolbarState)
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