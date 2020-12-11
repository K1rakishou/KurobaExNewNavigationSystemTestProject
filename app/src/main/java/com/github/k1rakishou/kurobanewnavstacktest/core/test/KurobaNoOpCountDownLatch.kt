package com.github.k1rakishou.kurobanewnavstacktest.core.test

import java.lang.IllegalStateException

class KurobaNoOpCountDownLatch : KurobaCountDownLatch {

  override fun countDown() {

  }

  override fun await() {
    throw IllegalStateException("Must not be called")
  }

}