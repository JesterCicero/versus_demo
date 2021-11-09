package com.avp.core.data

import com.avp.core.domain.IRenderState
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow

interface IContestantsDataSource {
    fun getRenderUiChannel(): MutableStateFlow<IRenderState>
    fun getErrorMsgChannel(): MutableSharedFlow<String>
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
    fun getPreviousSuperCategoryPosition(): Int
    fun saveChosenContestant(chosenContestant: ChosenContestant)
    fun getChosenContestant(): ChosenContestant
    fun getCurrentSuperCategory(): String
    fun getCurrentCategory(): String
    fun getCurrentQuiz(): String
    fun getCurrentQuizList()
    fun getStatsOption(): Boolean
    fun saveStatsOption(resultsStats: Boolean)
    fun getCurrentRound(): Int
}