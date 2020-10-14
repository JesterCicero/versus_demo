package com.rkhrapunov.core.data

import com.rkhrapunov.core.domain.IRenderState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.BroadcastChannel

interface IContestantsDataSource {
    @ExperimentalCoroutinesApi
    fun getRenderUiChannel(): BroadcastChannel<IRenderState>
    @ExperimentalCoroutinesApi
    fun getErrorMsgChannel(): BroadcastChannel<String>
    fun updateState(state: IRenderState, chosenFirst: Boolean)
    fun resetContest()
    fun getSuperCategories()
    fun getCategories(itemData: String)
    fun getQuizList(itemData: String)
    fun getQuizItemDetail(itemData: String)
    fun getStats()
    fun sendWinner(winner: String)
    fun cancelQuiz()
    fun savePageIndicatorText(pageIndicatorText: String)
    fun getPageIndicatorText(): String
    fun saveCurrentPagePosition(currentPosition: Int)
    fun saveCurrentSuperCategoryPosition(currentPosition: Int)
    fun getCurrentPagePosition(): Int
    fun getCurrentSuperCategoryPosition(): Int
    fun saveChosenContestant(chosenContestant: ChosenContestant)
    fun getChosenContestant(): ChosenContestant
    fun getCurrentSuperCategory(): String
    fun getCurrentCategory(): String
    fun getCurrentQuiz(): String
    fun getCurrentQuizList()
    fun getStatsOption(): Boolean
    fun saveStatsOption(resultsStats: Boolean)
}