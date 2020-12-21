package com.avp.ctbo.presentation.quiz_pager

import com.avp.core.domain.IRenderState
import com.avp.ctbo.presentation.base.IBaseStatefulPresenter
import com.avp.ctbo.presentation.base.IBaseView

interface IQuizPagerContract {
    interface IQuizPagerView : IBaseView {
        fun renderState(renderState: IRenderState)
        fun onSuperCategoriesBack()
    }

    interface IQuizPagerPresenter : IBaseStatefulPresenter<IQuizPagerView> {
        fun savePageIndicatorText(pageIndicatorText: String)
        fun getPageIndicatorText(): String
        fun saveCurrentPagePosition(currentPosition: Int)
        fun getCurrentPagePosition(): Int
        fun onBackPressed()
        fun getCurrentSuperCategoryBackgroundUrl(): String
        fun getCurrentCategoryBackgroundUrl(): String
        fun getCurrentSuperCategory(): String
    }
}