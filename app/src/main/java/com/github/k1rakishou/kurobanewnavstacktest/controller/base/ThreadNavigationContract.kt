package com.github.k1rakishou.kurobanewnavstacktest.controller.base

import com.github.k1rakishou.kurobanewnavstacktest.data.ThreadDescriptor

interface ThreadNavigationContract {
  fun openThread(threadDescriptor: ThreadDescriptor)
}