package com.github.k1rakishou.kurobanewnavstacktest.core.test

class TestHelpers {
  lateinit var catalogLoadedLatch: KurobaCountDownLatch
  var disableMainActivityBackButton = false

  fun init(isTestMode: Boolean) {
    if (isTestMode) {
      initTestMode()
    } else {
      initNoOpMode()
    }
  }

  private fun initNoOpMode() {
    catalogLoadedLatch = KurobaNoOpCountDownLatch()
  }

  private fun initTestMode() {
    catalogLoadedLatch = KurobaRealCountDownLatch(1)
    disableMainActivityBackButton = true
  }

}