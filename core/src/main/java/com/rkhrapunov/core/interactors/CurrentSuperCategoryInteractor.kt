package com.rkhrapunov.core.interactors

import com.rkhrapunov.core.data.ContestantsRepository
import org.koin.core.KoinComponent
import org.koin.core.inject

class CurrentSuperCategoryInteractor : KoinComponent {

    private val mContestantsRepo by inject<ContestantsRepository>()

    fun saveCurrentSuperCategoryPosition(position: Int) = mContestantsRepo.saveCurrentSuperCategoryPosition(position)
    fun getCurrentSuperCategoryPosition() = mContestantsRepo.getCurrentSuperCategoryPosition()
}