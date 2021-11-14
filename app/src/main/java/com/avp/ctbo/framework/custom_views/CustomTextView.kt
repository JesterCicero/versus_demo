package com.avp.ctbo.framework.custom_views

import android.content.Context
import android.graphics.Typeface
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView

@Suppress("unused")
class CustomTextView : AppCompatTextView {

    constructor(context: Context) : super(context) {
        setFont()
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        setFont()
    }

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle) {
        setFont()
    }

    private fun setFont() = setTypeface(Typeface.createFromAsset(context.assets, "fonts/OpenSans-Regular.ttf"), Typeface.NORMAL)

    fun setFont(font: Typeface?) = setTypeface(font, Typeface.NORMAL)
}