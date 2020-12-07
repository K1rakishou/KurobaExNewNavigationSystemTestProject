package com.github.k1rakishou.kurobanewnavstacktest.data

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class PostDescriptor(
    val threadDescriptor: ThreadDescriptor,
    val postNo: Long
) : Parcelable {

  fun boardDescriptor(): BoardDescriptor = threadDescriptor.boardDescriptor
  fun siteDescriptor(): SiteDescriptor = boardDescriptor().siteDescriptor
  fun boardCode(): String = boardDescriptor().boardCode
  fun siteName(): String = siteDescriptor().siteName

  val isOP: Boolean
    get() = postNo == threadDescriptor.threadNo

}