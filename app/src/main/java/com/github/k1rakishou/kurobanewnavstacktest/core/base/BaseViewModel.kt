package com.github.k1rakishou.kurobanewnavstacktest.core.base

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelChildren

abstract class BaseViewModel : ViewModel() {
  private val job = SupervisorJob()
  protected val scope = CoroutineScope(job + Dispatchers.Main)

  override fun onCleared() {
    super.onCleared()

    job.cancelChildren()
  }

}