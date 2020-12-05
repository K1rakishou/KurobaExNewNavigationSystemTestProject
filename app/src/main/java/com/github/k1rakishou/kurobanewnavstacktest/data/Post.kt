package com.github.k1rakishou.kurobanewnavstacktest.data

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Post(
  val postDescriptor: PostDescriptor,
  var color: Int,
  val text: String,
  var selected: Boolean
) : Parcelable