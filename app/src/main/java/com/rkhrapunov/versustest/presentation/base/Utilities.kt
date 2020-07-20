package com.rkhrapunov.versustest.presentation.base

import android.content.Context
import java.util.*

val random = Random()

fun <T,U> Map<T,U>.random(): Map.Entry<T,U> = entries.elementAt(random.nextInt(size))

fun dpFromPx(context: Context, px: Float) = px / context.resources.displayMetrics.density

fun pxFromDp(context: Context, dp: Float) = dp * context.resources.displayMetrics.density
