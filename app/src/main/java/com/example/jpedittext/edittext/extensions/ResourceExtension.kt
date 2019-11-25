package com.example.jpedittext.edittext.extensions

import android.content.res.ColorStateList
import android.content.res.Resources
import android.os.Build

fun Resources.getWrapperColor(resId: Int): Int {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        this.getColor(resId, null)
    } else {
        this.getColor(resId)
    }
}

fun Resources.getWrapperColorStateList(resId: Int): ColorStateList {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        this.getColorStateList(resId, null)
    } else {
        this.getColorStateList(resId)
    }
}