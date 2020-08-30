package com.rkhrapunov.core.data

import com.rkhrapunov.core.domain.IRenderState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.koin.core.KoinComponent
import org.koin.core.inject

class ContestantsRepository : KoinComponent {

    private val mContestantsDataSource by inject<IContestantsDataSource>()

    @ExperimentalCoroutinesApi
    fun getRenderUiChannel() = mContestantsDataSource.getRenderUiChannel()
    @ExperimentalCoroutinesApi
    fun getErrorMsgChannel() = mContestantsDataSource.getErrorMsgChannel()
    @ExperimentalCoroutinesApi
    fun onChosenContestant(state: IRenderState, chosenFirst: Boolean) = mContestantsDataSource.updateState(state, chosenFirst)
    fun resetContest() = mContestantsDataSource.resetContest()
    fun getQuizList() = mContestantsDataSource.getQuizList()
    fun getQuizItemDetail(itemData: String) = mContestantsDataSource.getQuizItemDetail(itemData)
    fun getStats() = mContestantsDataSource.getStats()
    fun cancelQuiz() = mContestantsDataSource.cancelQuiz()
    fun savePageIndicatorText(pageIndicatorText: String) = mContestantsDataSource.savePageIndicatorText(pageIndicatorText)
    fun getPageIndicatorText() = mContestantsDataSource.getPageIndicatorText()
    fun saveCurrentPagePosition(currentPosition: Int) = mContestantsDataSource.saveCurrentPagePosition(currentPosition)
    fun getCurrentPagePosition() = mContestantsDataSource.getCurrentPagePosition()
}