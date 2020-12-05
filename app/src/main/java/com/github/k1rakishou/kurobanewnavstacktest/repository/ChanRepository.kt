package com.github.k1rakishou.kurobanewnavstacktest.repository

import com.github.k1rakishou.kurobanewnavstacktest.data.BoardDescriptor
import com.github.k1rakishou.kurobanewnavstacktest.data.Post
import com.github.k1rakishou.kurobanewnavstacktest.data.PostDescriptor
import com.github.k1rakishou.kurobanewnavstacktest.data.ThreadDescriptor
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.concurrent.atomic.AtomicLong
import kotlin.random.Random

object ChanRepository {
  private val random = Random(System.currentTimeMillis())
  private val threadIndex = AtomicLong(0)
  private val cache =
    mutableMapOf<BoardDescriptor, MutableMap<ThreadDescriptor, MutableList<Post>>>()

  private val mutex = Mutex()

  private val catalogUpdatesFlow = MutableSharedFlow<BoardDescriptor>(
    extraBufferCapacity = 1,
    onBufferOverflow = BufferOverflow.DROP_OLDEST
  )
  private val threadUpdatesFlow = MutableSharedFlow<ThreadDescriptor>(
    extraBufferCapacity = 1,
    onBufferOverflow = BufferOverflow.DROP_OLDEST
  )

  private val job = SupervisorJob()
  private val scope = CoroutineScope(job + Dispatchers.Default)

  private val currentOpenedBoardFlow = MutableStateFlow<BoardDescriptor?>(null)
  private val currentOpenedThreadFlow = MutableStateFlow<ThreadDescriptor?>(null)

  fun openBoard(boardDescriptor: BoardDescriptor) {
    currentOpenedBoardFlow.value = boardDescriptor
  }

  fun openThread(threadDescriptor: ThreadDescriptor) {
    currentOpenedThreadFlow.value = threadDescriptor
  }

  fun listenForBoardOpenUpdates(): Flow<BoardDescriptor?> {
    return currentOpenedBoardFlow
      .distinctUntilChanged(areEquivalent = { bd1, bd2 -> bd1 == bd2 })
  }

  fun listenForThreadOpenUpdates(): Flow<ThreadDescriptor?> {
    return currentOpenedThreadFlow
      .distinctUntilChanged(areEquivalent = { td1, td2 -> td1 == td2 })
  }

  fun listenForCatalogChanges(boardDescriptor: BoardDescriptor): Flow<BoardDescriptor> {
    return catalogUpdatesFlow
      .filter { updatedBoardDescriptor -> updatedBoardDescriptor == boardDescriptor }
  }

  fun listenForThreadChanges(threadDescriptor: ThreadDescriptor): Flow<ThreadDescriptor> {
    return threadUpdatesFlow
      .filter { updatedThreadDescriptor -> updatedThreadDescriptor == threadDescriptor }
  }

  suspend fun getCatalogThreads(boardDescriptor: BoardDescriptor): List<Post> {
    return mutex.withLock {
      return@withLock cache[boardDescriptor]?.values
        ?.flatten()
        ?.filter { post -> post.postDescriptor.isOP }
        ?: emptyList()
    }
  }

  suspend fun getThreadPosts(threadDescriptor: ThreadDescriptor): List<Post> {
    return mutex.withLock {
      return@withLock cache[threadDescriptor.boardDescriptor]
        ?.get(threadDescriptor)
        ?.sortedBy { post -> post.postDescriptor.postNo }
        ?: emptyList()
    }
  }

  suspend fun contains(boardDescriptor: BoardDescriptor): Boolean {
    return mutex.withLock { cache[boardDescriptor]?.isNotEmpty() ?: false }
  }

  suspend fun loadBoard(boardDescriptor: BoardDescriptor) {
    scope.launch {
      val contains = mutex.withLock { cache.containsKey(boardDescriptor) }
      if (!contains) {
        delay(1000)
      }

      mutex.withLock {
        if (!cache.containsKey(boardDescriptor)) {
          val map = mutableMapOf<ThreadDescriptor, MutableList<Post>>()

          repeat(5) {
            val color = 0xFF000000.toInt() or random.nextInt(0x300000, 0xAAAAAA)
            val threadDescriptor = ThreadDescriptor(boardDescriptor, threadIndex.incrementAndGet())

            if (!map.containsKey(threadDescriptor)) {
              map[threadDescriptor] = mutableListOf()
            }

            val postDescriptor = PostDescriptor(threadDescriptor, threadDescriptor.threadNo)

            val comment = buildString {
              appendLine("Thread ${threadDescriptor.threadNo}")
              val count = random.nextInt(3, 35)

              repeat(count) {
                appendLine(it)
              }
            }

            map[threadDescriptor]!! += Post(
                postDescriptor,
                color,
                comment,
                false
            )
          }

          cache[boardDescriptor] = map
        }
      }

      catalogUpdatesFlow.emit(boardDescriptor)
    }
  }

  suspend fun loadThread(threadDescriptor: ThreadDescriptor) {
    scope.launch {
      delay(100)

      mutex.withLock {
        if (cache.containsKey(threadDescriptor.boardDescriptor)) {
          val prevCatalogThreads = cache[threadDescriptor.boardDescriptor]!!.get(threadDescriptor)
          requireNotNull(prevCatalogThreads) { "Thread has no OP!" }

          repeat(random.nextInt(1, 10)) {
            val color = 0xFF000000.toInt() or random.nextInt(0x300000, 0xAAAAAA)
            val postId = threadIndex.incrementAndGet()
            val postDescriptor = PostDescriptor(threadDescriptor, postId)

            val comment = buildString {
              appendLine("Post ${postId}")
              val count = random.nextInt(3, 35)

              repeat(count) {
                appendLine(it)
              }
            }

            prevCatalogThreads += Post(
                postDescriptor,
                color,
                comment,
                false
            )
          }
        }
      }

      threadUpdatesFlow.emit(threadDescriptor)
    }
  }

  fun selectUnSelectPost(postDescriptor: PostDescriptor) {
    scope.launch {
      val threadDescriptor = postDescriptor.threadDescriptor
      val boardDescriptor = threadDescriptor.boardDescriptor

      val updated = mutex.withLock {
        val threads = cache.get(boardDescriptor)
          ?: return@withLock false

        val entry = threads.entries.firstOrNull { (td, _) -> threadDescriptor == td }
          ?: return@withLock false

        val post = entry.value.firstOrNull { post -> post.postDescriptor == postDescriptor }
          ?: return@withLock false

        post.selected = !post.selected
        return@withLock true
      }

      if (updated) {
        threadUpdatesFlow.emit(threadDescriptor)
      }
    }
  }

}