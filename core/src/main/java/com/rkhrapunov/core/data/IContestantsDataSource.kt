package com.rkhrapunov.core.data

import com.rkhrapunov.core.domain.IRenderState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.BroadcastChannel

interface IContestantsDataSource {
    fun getAllContestants(): Map<String, Int>
    @ExperimentalCoroutinesApi
    fun getRenderUiChannel(): BroadcastChannel<IRenderState>
    fun updateState(state: IRenderState, chosenFirst: Boolean)
    fun resetContest()
}