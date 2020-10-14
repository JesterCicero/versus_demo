package com.rkhrapunov.versustest.presentation.quizlist

import com.rkhrapunov.versustest.presentation.base.IBaseStatefulPresenter
import com.rkhrapunov.versustest.presentation.base.IBaseView
import com.rkhrapunov.versustest.presentation.base.QuizAdapter

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