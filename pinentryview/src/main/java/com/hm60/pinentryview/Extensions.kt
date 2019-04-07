package com.hm60.pinentryview

import android.util.TypedValue
import android.view.View
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat


fun View.toPxF(dp:Int): Float{
    return TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,
        dp.toFloat(),
        resources.displayMetrics).toFloat()
}
fun View.spToPxF(sp:Int): Float{
    return TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_SP,
        sp.toFloat(),
        resources.displayMetrics).toFloat()
}

fun View.getColor(@ColorRes colorId:Int):Int {
    return ContextCompat.getColor(context, colorId)
}