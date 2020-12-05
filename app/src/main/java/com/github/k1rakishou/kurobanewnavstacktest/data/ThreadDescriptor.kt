package com.github.k1rakishou.kurobanewnavstacktest.data

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class ThreadDescriptor(
  val boardDescriptor: BoardDescriptor,
  val threadNo: Long
) : Parcelable