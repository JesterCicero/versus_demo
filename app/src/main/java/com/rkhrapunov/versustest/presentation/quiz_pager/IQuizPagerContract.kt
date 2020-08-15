package com.rkhrapunov.versustest.presentation.quiz_pager

import com.rkhrapunov.core.domain.RenderState
import com.rkhrapunov.versustest.presentation.base.IBaseStatefulPresenter
import com.rkhrapunov.versustest.presentation.base.IBaseView

interface IQuizPagerContract {
    interface IQuizPagerView : IBaseView {
        fun renderQuizListState(renderState: RenderState.QuizListState)
    }

    interface IQuizPagerPresenter : IBaseStatefulPresenter<IQuizPagerView> {
        fun savePageIndicatorText(pageIndicatorText: String)
        fun getPageIndicatorText(): String
        fun saveCurrentPagePosition(currentPosition: Int)
        fun getCurrentPagePosition(): Int
    }
}