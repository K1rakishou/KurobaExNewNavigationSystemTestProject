package com.github.k1rakishou.kurobanewnavstacktest.widget.bottom_panel

import android.content.Context
import android.view.LayoutInflater
import androidx.appcompat.widget.AppCompatEditText
import androidx.constraintlayout.widget.ConstraintLayout
import com.github.k1rakishou.kurobanewnavstacktest.R
import com.github.k1rakishou.kurobanewnavstacktest.utils.setEnabledFast

class KurobaBottomReplyPanel(
  context: Context,
) : ConstraintLayout(context, null, 0), ChildPanelContract {
  private val replyInputEditText: AppCompatEditText

  init {
    LayoutInflater.from(context).inflate(R.layout.kuroba_bottom_reply_panel, this, true)
    replyInputEditText = findViewById(R.id.reply_input_edit_text)
  }

  override fun getCurrentHeight(): Int {
    return context.resources.getDimension(R.dimen.bottom_reply_panel_height).toInt()
  }

  override fun getBackgroundColor(): Int {
    return context.resources.getColor(R.color.backColorDark)
  }

  override fun enableOrDisableControls(enable: Boolean) {
    replyInputEditText.setEnabledFast(enable)
  }
}