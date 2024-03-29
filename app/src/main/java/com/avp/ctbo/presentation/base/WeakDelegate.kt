package com.avp.ctbo.presentation.base

import java.lang.ref.WeakReference
import kotlin.reflect.KProperty

fun <T> weak() = WeakRefDelegate<T>()
fun <T> weak(value: T) = WeakRefDelegate(value)

class WeakRefDelegate<T> {
    private var weakReference: WeakReference<T>? = null

    constructor()
    constructor(value: T) {
        weakReference = WeakReference(value)
    }

    operator fun getValue(thisRef: Any, property: KProperty<*>): T? = weakReference?.get()
    operator fun setValue(thisRef: Any, property: KProperty<*>, value: T) {
        weakReference = WeakReference(value)
    }
}