package com.avp.ctbo.presentation.quizlist

import com.avp.ctbo.presentation.base.IBaseStatefulPresenter
import com.avp.ctbo.presentation.base.IBaseView
import com.avp.ctbo.presentation.base.QuizAdapter

interface IQuizListContract {

    @ExperimentalStdlibApi
    interface IQuizListView : IBaseView {
        fun shouldShowQuizList(): Boolean
        fun setAdapter(quizAdapter: QuizAdapter<*>?)
        fun onBackToQuizzesButtonClicked()
        fun updateStatsHeaders(resultsStats: Boolean)
    }

    @ExperimentalStdlibApi
    interface IQuizListPresenter : IBaseStatefulPresenter<IQuizListView> {
        fun onBackToQuizzesButtonClickedIntent()
        fun onStatsTabClicked(results: Boolean)
    }
}