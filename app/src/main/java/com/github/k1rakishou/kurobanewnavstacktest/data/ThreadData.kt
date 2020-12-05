package com.github.k1rakishou.kurobanewnavstacktest.data

sealed class ThreadData {
  object Empty : ThreadData()
  object Loading : ThreadData()
  class Data(val thread: List<Post>, val error: Throwable? = null) : ThreadData()
}