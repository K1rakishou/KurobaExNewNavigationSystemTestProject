package com.github.k1rakishou.kurobanewnavstacktest.data

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class PostDescriptor(
    val threadDescriptor: ThreadDescriptor,
    val postNo: Long
) : Parcelable {

  val isOP: Boolean
    get() = postNo == threadDescriptor.threadNo

}