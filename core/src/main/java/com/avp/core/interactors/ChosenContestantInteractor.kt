package com.avp.core.interactors

import com.avp.core.data.ContestantsRepository
import com.avp.core.domain.IRenderState
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class ChosenContestantInteractor : KoinComponent {

    private val mContestantsRepo by inject<ContestantsRepository>()

    fun onChosenContestant(state: IRenderState, chosenFirst: Boolean) = mContestantsRepo.onChosenContestant(state, chosenFirst)
}