package com.rkhrapunov.core.interactors

import com.rkhrapunov.core.data.ContestantsRepository
import com.rkhrapunov.core.domain.IRenderState
import org.koin.core.KoinComponent
import org.koin.core.inject

class ChosenContestantInteractor : KoinComponent {

    private val mContestantsRepo by inject<ContestantsRepository>()

    fun onChosenContestant(state: IRenderState, chosenFirst: Boolean) = mContestantsRepo.onChosenContestant(state, chosenFirst)
}