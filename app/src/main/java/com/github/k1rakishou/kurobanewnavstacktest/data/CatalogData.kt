package com.github.k1rakishou.kurobanewnavstacktest.data

sealed class CatalogData {
  object Empty : CatalogData()
  object Loading : CatalogData()

  data class Data(
    val catalog: List<Post>,
    val error: Throwable? = null
  ) : CatalogData() {

    fun toCatalogTitleString(): String {
      if (catalog.isEmpty()) {
        return ""
      }

      val boardDescriptor = catalog.first().postDescriptor.threadDescriptor.boardDescriptor
      return "/${boardDescriptor.boardCode}/"
    }

  }
}