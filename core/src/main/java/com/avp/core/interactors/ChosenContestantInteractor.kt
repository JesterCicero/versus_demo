package com.avp.core.interactors

import com.avp.core.data.ContestantsRepository
import com.avp.core.domain.IRenderState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.koin.core.KoinComponent
import org.koin.core.inject

class ChosenContestantInteractor : KoinComponent {

    private val mContestantsRepo by inject<ContestantsRepository>()

    @ExperimentalCoroutinesApi
    fun onChosenContestant(state: IRenderState, chosenFirst: Boolean) = mContestantsRepo.onChosenContestant(state, chosenFirst)
}