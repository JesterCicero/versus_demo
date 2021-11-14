package com.avp.core.interactors

import com.avp.core.data.ContestantsRepository
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class GetStatsOptionInteractor : KoinComponent {

    private val mContestantsRepo by inject<ContestantsRepository>()

    fun getStatsOption() = mContestantsRepo.getStatsOption()
    fun saveStatsOption(resultsStats: Boolean) = mContestantsRepo.saveStatsOption(resultsStats)
}