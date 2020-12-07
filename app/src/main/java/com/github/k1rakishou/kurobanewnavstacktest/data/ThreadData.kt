package com.github.k1rakishou.kurobanewnavstacktest.data

sealed class ThreadData {
  object Empty : ThreadData()
  object Loading : ThreadData()

  class Data(
    val threadPosts: List<Post>,
    val error: Throwable? = null
  ) : ThreadData() {

    fun toThreadTitleString(): String {
      if (threadPosts.isEmpty()) {
        return "No posts"
      }

      val originalPost = threadPosts.first()

      val siteName = originalPost.postDescriptor.siteName()
      val boardCode = originalPost.postDescriptor.boardCode()
      val firstPostTextTrimmed = originalPost.text.take(128)

      return "$siteName/$boardCode/ - $firstPostTextTrimmed"
    }

  }
}