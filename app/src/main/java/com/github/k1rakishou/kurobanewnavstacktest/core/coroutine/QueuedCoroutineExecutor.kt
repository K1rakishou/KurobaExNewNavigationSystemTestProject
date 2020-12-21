package com.github.k1rakishou.kurobanewnavstacktest.core.coroutine

import android.annotation.SuppressLint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * Executes all callbacks sequentially using an unlimited channel. This means that there won't
 * be two callbacks running at the same, they will be queued and executed sequentially instead.
 * */
@SuppressLint("TimberTagLength")
@OptIn(ExperimentalCoroutinesApi::class)
class QueuedCoroutineExecutor(private val scope: CoroutineScope) {
  private val channel = Channel<SerializedAction>(Channel.UNLIMITED)
  private var job: Job? = null

  init {
    job = scope.launch {
      channel.consumeEach { serializedAction ->
        try {
          serializedAction.action()
        } catch (error: Throwable) {
          Timber.tag(TAG).e(error, "serializedAction unhandled exception")
        }
      }
    }
  }

  fun post(func: suspend () -> Unit) {
    if (channel.isClosedForSend) {
      throw IllegalStateException("Channel is closed!")
    }

    val serializedAction = SerializedAction(func)
    channel.offer(serializedAction)
  }

  fun cancelChildren() {
    synchronized(this) {
      job?.cancelChildren()
    }
  }

  fun stop() {
    synchronized(this) {
      job?.cancel()
      job = null
    }
  }

  class SerializedAction(
    val action: suspend () -> Unit
  )

  companion object {
    private const val TAG = "SerializedCoroutineExecutor"
  }
}