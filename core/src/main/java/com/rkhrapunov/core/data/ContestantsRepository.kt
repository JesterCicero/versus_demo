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
    fun getSuperCategories() = mContestantsDataSource.getSuperCategories()
    fun getCategories(itemData: String) = mContestantsDataSource.getCategories(itemData)
    fun getQuizList(itemData: String) = mContestantsDataSource.getQuizList(itemData)
    fun getQuizItemDetail(itemData: String) = mContestantsDataSource.getQuizItemDetail(itemData)
    fun getStats() = mContestantsDataSource.getStats()
    fun cancelQuiz() = mContestantsDataSource.cancelQuiz()
    fun savePageIndicatorText(pageIndicatorText: String) = mContestantsDataSource.savePageIndicatorText(pageIndicatorText)
    fun getPageIndicatorText() = mContestantsDataSource.getPageIndicatorText()
    fun saveCurrentPagePosition(currentPosition: Int) = mContestantsDataSource.saveCurrentPagePosition(currentPosition)
    fun getCurrentPagePosition() = mContestantsDataSource.getCurrentPagePosition()
    fun saveCurrentSuperCategoryPosition(position: Int) = mContestantsDataSource.saveCurrentSuperCategoryPosition(position)
    fun getCurrentSuperCategoryPosition() = mContestantsDataSource.getCurrentSuperCategoryPosition()
    fun getPreviousSuperCategoryPosition() = mContestantsDataSource.getPreviousSuperCategoryPosition()
    fun saveChosenContestant(chosenContestant: ChosenContestant) = mContestantsDataSource.saveChosenContestant(chosenContestant)
    fun getChosenContestant() = mContestantsDataSource.getChosenContestant()
    fun getCurrentSuperCategory() = mContestantsDataSource.getCurrentSuperCategory()
    fun getCurrentCategory() = mContestantsDataSource.getCurrentCategory()
    fun getCurrentQuiz() = mContestantsDataSource.getCurrentQuiz()
    fun getCurrentQuizList() = mContestantsDataSource.getCurrentQuizList()
    fun getStatsOption() = mContestantsDataSource.getStatsOption()
    fun saveStatsOption(resultsStats: Boolean) = mContestantsDataSource.saveStatsOption(resultsStats)
    fun getCurrentRound() = mContestantsDataSource.getCurrentRound()
}