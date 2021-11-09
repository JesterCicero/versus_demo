package com.avp.ctbo.presentation.base

import java.util.Locale

fun String.capitalizeWords(): String = split(" ").joinToString(" ") {
    it.replaceFirstChar { symbol ->
        if (symbol.isLowerCase()) symbol.titlecase(Locale.getDefault()) else "$symbol"
    }
}