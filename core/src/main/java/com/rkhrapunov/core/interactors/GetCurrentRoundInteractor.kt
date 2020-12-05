package com.rkhrapunov.core.interactors

import com.rkhrapunov.core.data.ContestantsRepository
import org.koin.core.KoinComponent
import org.koin.core.inject

class GetCurrentRoundInteractor : KoinComponent {
    private val mContestantsRepo by inject<ContestantsRepository>()

    fun getCurrentRound() = mContestantsRepo.getCurrentRound()
}