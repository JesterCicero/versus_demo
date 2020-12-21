package com.avp.core.interactors

import com.avp.core.data.ContestantsRepository
import org.koin.core.KoinComponent
import org.koin.core.inject

class CurrentSuperCategoryInteractor : KoinComponent {

    private val mContestantsRepo by inject<ContestantsRepository>()

    fun saveCurrentSuperCategoryPosition(position: Int) = mContestantsRepo.saveCurrentSuperCategoryPosition(position)
    fun getCurrentSuperCategoryPosition() = mContestantsRepo.getCurrentSuperCategoryPosition()
    fun getPreviousSuperCategoryPosition() = mContestantsRepo.getPreviousSuperCategoryPosition()
}