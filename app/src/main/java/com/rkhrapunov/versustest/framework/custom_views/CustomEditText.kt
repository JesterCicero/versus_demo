package com.rkhrapunov.versustest.framework.custom_views

import android.content.Context
import android.graphics.Typeface
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatEditText

class CustomEditText : AppCompatEditText {
    constructor(context: Context?) : super(context) {
        setFont()
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        setFont()
    }

    constructor(context: Context?, attrs: AttributeSet?, defStyle: Int) : super(context, attrs, defStyle) {
        setFont()
    }

    private fun setFont() {
        val font = Typeface.createFromAsset(context.assets, "fonts/OpenSans-Regular.ttf")
        setTypeface(font, Typeface.NORMAL)
    }
}