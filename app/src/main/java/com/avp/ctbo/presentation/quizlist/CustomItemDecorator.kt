package com.avp.ctbo.presentation.quizlist

import android.content.Context
import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.avp.ctbo.R
import com.avp.ctbo.presentation.base.pxFromDp

class CustomItemDecorator(private val mContext: Context) : RecyclerView.ItemDecoration() {

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        outRect.left = pxFromDp(mContext, mContext.resources.getInteger(R.integer.quiz_list_item_padding_start_end).toFloat()/*R.integer.quiz_list_item_padding_start_end.toFloat()*/).toInt()
        outRect.right = pxFromDp(mContext, mContext.resources.getInteger(R.integer.quiz_list_item_padding_start_end).toFloat()).toInt()
        if (parent.getChildAdapterPosition(view) != state.itemCount - 1) {
            outRect.bottom = 20
        }
    }
}