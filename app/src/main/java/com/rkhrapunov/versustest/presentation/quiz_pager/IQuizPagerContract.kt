package com.rkhrapunov.versustest.presentation.quiz_pager

import com.rkhrapunov.core.domain.IRenderState
import com.rkhrapunov.versustest.presentation.base.IBaseStatefulPresenter
import com.rkhrapunov.versustest.presentation.base.IBaseView

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
    }
}