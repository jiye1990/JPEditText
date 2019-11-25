package com.example.jpedittext.edittext

import android.content.Context
import android.util.TypedValue
import kotlin.math.roundToInt

fun Float.dp2px(context: Context): Int {
    val r = context.resources
    var px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, this, r.displayMetrics)
    return px.roundToInt()
}
