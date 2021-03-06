package com.github.k1rakishou.kurobanewnavstacktest.data

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class BoardDescriptor(
  val siteDescriptor: SiteDescriptor,
  val boardCode: String
) : Parcelable