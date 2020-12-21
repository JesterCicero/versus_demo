package com.avp.ctbo.presentation.base

import java.util.Locale

@ExperimentalStdlibApi
fun String.capitalizeWords(): String = split(" ").joinToString(" ") { it.capitalize(Locale.getDefault()) }