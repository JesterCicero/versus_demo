package com.avp.ctbo.presentation.base

import android.content.Context
import android.os.Build
import android.util.DisplayMetrics
import android.view.WindowInsets
import androidx.fragment.app.FragmentActivity
import java.util.Random

val random = Random()

@Suppress("unused")
fun <T,U> Map<T,U>.random(): Map.Entry<T,U> = entries.elementAt(random.nextInt(size))

@Suppress("unused")
fun dpFromPx(context: Context, px: Float) = px / context.resources.displayMetrics.density

fun pxFromDp(context: Context, dp: Float) = dp * context.resources.displayMetrics.density

@Suppress("DEPRECATION")
fun getScreenWidth(activity: FragmentActivity): Int {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        val metrics = activity.windowManager.currentWindowMetrics
        val insets = activity.windowManager.currentWindowMetrics.windowInsets.getInsetsIgnoringVisibility(
            WindowInsets.Type.navigationBars() or WindowInsets.Type.displayCutout())
        metrics.bounds.width() - insets.right - insets.left
    } else {
        val displayMetrics = DisplayMetrics()
        activity.windowManager.defaultDisplay.getMetrics(displayMetrics)
        displayMetrics.widthPixels
    }
}

@Suppress("DEPRECATION")
fun getScreenHeight(activity: FragmentActivity): Int {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        val metrics = activity.windowManager.currentWindowMetrics
        val insets = activity.windowManager.currentWindowMetrics.windowInsets.getInsetsIgnoringVisibility(
            WindowInsets.Type.navigationBars() or WindowInsets.Type.displayCutout())
        metrics.bounds.height() - insets.top - insets.bottom
    } else {
        val displayMetrics = DisplayMetrics()
        activity.windowManager.defaultDisplay.getMetrics(displayMetrics)
        displayMetrics.heightPixels
    }
}