package com.github.k1rakishou.kurobanewnavstacktest.core.test

import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

class KurobaRealCountDownLatch(
  counter: Int
) : KurobaCountDownLatch {
  private val latch: CountDownLatch

  init {
    require(counter > 0) { "Bad counter: $counter" }
    latch = CountDownLatch(counter)
  }

  override fun countDown() {
    latch.countDown()
  }

  override fun await() {
    latch.await(1, TimeUnit.MINUTES)
  }

}