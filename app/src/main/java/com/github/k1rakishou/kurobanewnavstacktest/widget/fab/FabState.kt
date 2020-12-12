package com.github.k1rakishou.kurobanewnavstacktest.widget.fab

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class FabState(
  var shown: Boolean = true,
  var locked: Boolean = false
) : Parcelable