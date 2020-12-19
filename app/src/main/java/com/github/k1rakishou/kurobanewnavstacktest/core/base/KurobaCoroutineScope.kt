package com.github.k1rakishou.kurobanewnavstacktest.core.base

import kotlinx.coroutines.*

class KurobaCoroutineScope(private val dispatcher: CoroutineDispatcher = Dispatchers.Main) {
  private val job = SupervisorJob()

  val scope = CoroutineScope(job + dispatcher)

  fun launch(block: suspend CoroutineScope.() -> Unit): Job {
    return scope.launch { block() }
  }

  fun cancelChildren() {
    scope.launch {  }

    job.cancelChildren()
  }

}