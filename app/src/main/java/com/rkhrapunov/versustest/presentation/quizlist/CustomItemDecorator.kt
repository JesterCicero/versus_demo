package com.rkhrapunov.versustest.presentation.quizlist

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView

class CustomItemDecorator : RecyclerView.ItemDecoration() {

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        outRect.left = 50
        outRect.right = 50
        if (parent.getChildAdapterPosition(view) != state.itemCount - 1) {
            outRect.bottom = 50
        }
    }
}