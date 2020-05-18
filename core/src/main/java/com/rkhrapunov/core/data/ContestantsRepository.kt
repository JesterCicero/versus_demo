package com.rkhrapunov.core.data

import com.rkhrapunov.core.domain.IRenderState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.koin.core.KoinComponent
import org.koin.core.inject

class ContestantsRepository : KoinComponent {

    private val mContestantsDataSource by inject<IContestantsDataSource>()

    @ExperimentalCoroutinesApi
    fun getRenderUiChannel() = mContestantsDataSource.getRenderUiChannel()
    fun onChosenContestant(state: IRenderState, chosenFirst: Boolean) = mContestantsDataSource.updateState(state, chosenFirst)
    fun resetContest() = mContestantsDataSource.resetContest()
}