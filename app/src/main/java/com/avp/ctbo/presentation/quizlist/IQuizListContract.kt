package com.avp.ctbo.presentation.quizlist

import com.avp.ctbo.presentation.base.IBaseStatefulPresenter
import com.avp.ctbo.presentation.base.IBaseView
import com.avp.ctbo.presentation.base.QuizAdapter
import kotlinx.coroutines.DelicateCoroutinesApi

interface IQuizListContract {

    @DelicateCoroutinesApi
    interface IQuizListView : IBaseView {
        fun shouldShowQuizList(): Boolean
        fun setAdapter(quizAdapter: QuizAdapter<*>?)
        fun onBackToQuizzesButtonClicked()
        fun updateStatsHeaders(resultsStats: Boolean)
    }

    @DelicateCoroutinesApi
    interface IQuizListPresenter : IBaseStatefulPresenter<IQuizListView> {
        fun onBackToQuizzesButtonClickedIntent()
        fun onStatsTabClicked(results: Boolean)
    }
}