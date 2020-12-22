package com.github.k1rakishou.kurobanewnavstacktest.viewmodel

import com.github.k1rakishou.kurobanewnavstacktest.core.base.BaseViewModel
import com.github.k1rakishou.kurobanewnavstacktest.data.BoardDescriptor
import com.github.k1rakishou.kurobanewnavstacktest.data.ThreadDescriptor

class MainControllerViewModel : BaseViewModel() {
  var lastOpenedBoard: BoardDescriptor? = null
  var lastOpenedThread: ThreadDescriptor? = null

}