package com.rkhrapunov.versustest.presentation.base

import java.util.Random

val random = Random()

fun <T,U> Map<T,U>.random(): Map.Entry<T,U> = entries.elementAt(random.nextInt(size))