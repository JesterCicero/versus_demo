package com.avp.core.interactors

import com.avp.core.data.ContestantsRepository
import org.koin.core.KoinComponent
import org.koin.core.inject

class GetCurrentRoundInteractor : KoinComponent {
    private val mContestantsRepo by inject<ContestantsRepository>()

    fun getCurrentRound() = mContestantsRepo.getCurrentRound()
}