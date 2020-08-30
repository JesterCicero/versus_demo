package com.rkhrapunov.versustest.presentation.base

import android.content.Context
import android.util.DisplayMetrics
import androidx.fragment.app.FragmentActivity
import java.util.Random

val random = Random()

fun <T,U> Map<T,U>.random(): Map.Entry<T,U> = entries.elementAt(random.nextInt(size))

fun dpFromPx(context: Context, px: Float) = px / context.resources.displayMetrics.density

fun pxFromDp(context: Context, dp: Float) = dp * context.resources.displayMetrics.density

fun getScreenWidth(activity: FragmentActivity): Int {
    val displayMetrics = DisplayMetrics()
    activity.windowManager.defaultDisplay.getMetrics(displayMetrics)
    return displayMetrics.widthPixels
}

fun getScreenHeight(activity: FragmentActivity): Int {
    val displayMetrics = DisplayMetrics()
    activity.windowManager.defaultDisplay.getMetrics(displayMetrics)
    return displayMetrics.heightPixels
}