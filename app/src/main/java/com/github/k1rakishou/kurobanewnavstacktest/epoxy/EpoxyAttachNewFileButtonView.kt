package com.github.k1rakishou.kurobanewnavstacktest.epoxy

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import androidx.core.view.updateLayoutParams
import com.airbnb.epoxy.CallbackProp
import com.airbnb.epoxy.ModelProp
import com.airbnb.epoxy.ModelView
import com.github.k1rakishou.kurobanewnavstacktest.R
import com.github.k1rakishou.kurobanewnavstacktest.widget.button.AttachNewFileButton
import java.util.*

@ModelView(autoLayout = ModelView.Size.MATCH_WIDTH_WRAP_HEIGHT, fullSpan = false)
class EpoxyAttachNewFileButtonView @JvmOverloads constructor(
  context: Context,
  attrs: AttributeSet? = null,
  defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

  private val newAttachableButtonHolder: FrameLayout
  private val attachButton: AttachNewFileButton

  init {
    inflate(context, R.layout.epoxy_attach_new_file_button_view, this)
    newAttachableButtonHolder = findViewById(R.id.reply_new_attachable_button)
    attachButton = findViewById(R.id.attach_button)
  }

  @ModelProp
  fun setSize(size: Size) {
    val expanded = size.expanded
    val isEmpty = size.empty

    if (isEmpty) {
      val newHeight = if (expanded) {
        context.resources.getDimension(R.dimen.attach_new_file_button_expanded_height)
      } else {
        context.resources.getDimension(R.dimen.attach_new_file_button_collapsed_height)
      }

      attachButton.updateLayoutParams<FrameLayout.LayoutParams> {
        height = newHeight.toInt()
      }
    } else {
      val newHeight = if (expanded) {
        context.resources.getDimension(R.dimen.attach_new_file_button_expanded_height)
      } else {
        context.resources.getDimension(R.dimen.attach_new_file_button_collapsed_height)
      }

      attachButton.updateLayoutParams<FrameLayout.LayoutParams> {
        height = newHeight.toInt()
      }
    }
  }

  @CallbackProp
  fun setOnClickListener(listener: (() -> Unit)?) {
    if (listener == null) {
      newAttachableButtonHolder.setOnClickListener(null)
      return
    }

    newAttachableButtonHolder.setOnClickListener {
      listener.invoke()
    }
  }

  @CallbackProp
  fun setOnLongClickListener(listener: (() -> Unit)?) {
    if (listener == null) {
      newAttachableButtonHolder.setOnLongClickListener(null)
      return
    }

    newAttachableButtonHolder.setOnLongClickListener {
      listener.invoke()
      return@setOnLongClickListener true
    }
  }

  data class Size(
    val expanded: Boolean,
    val empty: Boolean
  )

}