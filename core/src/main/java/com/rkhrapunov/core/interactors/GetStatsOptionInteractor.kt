package com.rkhrapunov.core.interactors

import com.rkhrapunov.core.data.ContestantsRepository
import org.koin.core.KoinComponent
import org.koin.core.inject

class GetStatsOptionInteractor : KoinComponent {

    private val mContestantsRepo by inject<ContestantsRepository>()

    fun getStatsOption() = mContestantsRepo.getStatsOption()
    fun saveStatsOption(resultsStats: Boolean) = mContestantsRepo.saveStatsOption(resultsStats)
}