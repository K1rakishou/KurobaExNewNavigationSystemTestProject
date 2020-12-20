package com.github.k1rakishou.kurobanewnavstacktest.widget.bottom_panel

import com.github.k1rakishou.kurobanewnavstacktest.data.ReplyAttachmentFile

data class KurobaBottomReplyPanelViewState(
  var text: String? = null,
  var selectionStart: Int? = null,
  var selectionEnd: Int? = null,
  var expanded: Boolean = false,
  val replyAttachments: MutableList<ReplyAttachmentFile> = mutableListOf()
)