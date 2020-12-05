package com.github.k1rakishou.kurobanewnavstacktest.base

import androidx.lifecycle.ViewModel

abstract class BaseViewModel : ViewModel() {

  final override fun onCleared() {
    super.onCleared()

    onDestroy()
  }

  abstract fun onDestroy()

}