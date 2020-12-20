package com.github.k1rakishou.kurobanewnavstacktest.epoxy

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.view.updateLayoutParams
import com.airbnb.epoxy.ModelProp
import com.airbnb.epoxy.ModelView
import com.github.k1rakishou.kurobanewnavstacktest.R

@ModelView(autoLayout = ModelView.Size.MATCH_WIDTH_WRAP_HEIGHT, fullSpan = false)
class EpoxyReplyAttachmentFileView  @JvmOverloads constructor(
  context: Context,
  attrs: AttributeSet? = null,
  defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {
  private val attachmentImage: AppCompatImageView

  init {
    inflate(context, R.layout.epoxy_reply_attachment_file_view, this)
    attachmentImage = findViewById(R.id.attachment_image)
  }

  @ModelProp
  fun setExpandedCollapsedMode(expanded: Boolean) {
    val newHeight = if (expanded) {
      context.resources.getDimension(R.dimen.attach_new_file_button_expanded_height)
    } else {
      context.resources.getDimension(R.dimen.attach_new_file_button_collapsed_height)
    }

    attachmentImage.updateLayoutParams<FrameLayout.LayoutParams> {
      height = newHeight.toInt()
    }
  }

  @ModelProp
  fun setColor(color: Int) {
    attachmentImage.setBackgroundColor(color)
  }

}