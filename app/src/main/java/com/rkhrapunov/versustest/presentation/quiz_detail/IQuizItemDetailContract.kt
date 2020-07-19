package com.rkhrapunov.versustest.presentation.quiz_detail

import com.rkhrapunov.core.domain.RenderState
import com.rkhrapunov.versustest.presentation.base.IBaseStatefulPresenter
import com.rkhrapunov.versustest.presentation.base.IBaseView

interface IQuizItemDetailContract {

    interface IQuizItemDetailView : IBaseView {
        suspend fun renderQuitItemDetailState(renderState: RenderState.QuizItemDetailState)
        fun onItemClicked(chosenFirst: Boolean)
    }

    interface IQuizItemDetailPresenter : IBaseStatefulPresenter<IQuizItemDetailView> {
        fun onFirstImgClickedIntent()
        fun onSecondImgClickedIntent()
        fun onItemClickFinished(chosenFirst: Boolean)
    }
}