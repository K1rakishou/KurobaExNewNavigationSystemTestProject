package com.github.k1rakishou.kurobanewnavstacktest.core.coroutine

import android.annotation.SuppressLint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * Executes all callbacks sequentially using an rendezvous channel. This means that if a callback
 * is currently running all other callbacks are ignored. This executor is really helpful when you
 * need to run a function only once at a time and the function can be executed from multiple places
 * asynchronously.
 * */
@SuppressLint("TimberTagLength")
@OptIn(ExperimentalCoroutinesApi::class)
class RendezvousCoroutineExecutor(private val scope: CoroutineScope) {
  private val channel = Channel<SerializedAction>(Channel.RENDEZVOUS)
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

  fun stop() {
    synchronized(this) {
      job?.cancel()
      job = null
    }
  }

  data class SerializedAction(
    val action: suspend () -> Unit
  )

  companion object {
    private const val TAG = "RendezvousCoroutineExecutor"
  }
}