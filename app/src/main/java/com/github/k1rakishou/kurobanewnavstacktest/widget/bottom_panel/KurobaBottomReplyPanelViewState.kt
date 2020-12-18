package com.github.k1rakishou.kurobanewnavstacktest.widget.bottom_panel

data class KurobaBottomReplyPanelViewState(
  var text: String? = null,
  var selectionStart: Int? = null,
  var selectionEnd: Int? = null
) {

  fun fillFromOther(other: KurobaBottomReplyPanelViewState?) {
    if (other == null) {
      return
    }

    this.text = other.text
    this.selectionStart = other.selectionStart
    this.selectionEnd = other.selectionEnd
  }

}