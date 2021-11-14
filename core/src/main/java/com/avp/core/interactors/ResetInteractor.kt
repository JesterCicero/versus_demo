package com.avp.core.interactors

import com.avp.core.data.ContestantsRepository
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class ResetInteractor : KoinComponent {

    private val mContestantsRepo by inject<ContestantsRepository>()

    @Suppress("unused")
    fun resetContest() = mContestantsRepo.resetContest()
}