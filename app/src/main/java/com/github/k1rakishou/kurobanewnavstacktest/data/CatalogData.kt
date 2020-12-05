package com.github.k1rakishou.kurobanewnavstacktest.data

sealed class CatalogData {
  object Empty : CatalogData()
  object Loading : CatalogData()
  data class Data(val catalog: List<Post>, val error: Throwable? = null) : CatalogData()
}