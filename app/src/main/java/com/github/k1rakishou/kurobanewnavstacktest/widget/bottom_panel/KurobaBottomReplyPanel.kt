package com.github.k1rakishou.kurobanewnavstacktest.widget.bottom_panel

import android.content.Context
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import com.github.k1rakishou.kurobanewnavstacktest.R

class KurobaBottomReplyPanel(
  context: Context,
) : ConstraintLayout(context, null, 0) {

  init {
    LayoutInflater.from(context).inflate(R.layout.kuroba_bottom_reply_panel, this, true)
  }

  fun getCurrentHeight(): Int {
    return context.resources.getDimension(R.dimen.bottom_reply_panel_height).toInt()
  }

}