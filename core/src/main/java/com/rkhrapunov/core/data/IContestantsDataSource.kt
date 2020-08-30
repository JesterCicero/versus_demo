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
    fun getQuizList()
    fun getQuizItemDetail(itemData: String)
    fun getStats()
    fun sendWinner(winner: String)
    fun cancelQuiz()
    fun savePageIndicatorText(pageIndicatorText: String)
    fun getPageIndicatorText(): String
    fun saveCurrentPagePosition(currentPosition: Int)
    fun getCurrentPagePosition(): Int
}