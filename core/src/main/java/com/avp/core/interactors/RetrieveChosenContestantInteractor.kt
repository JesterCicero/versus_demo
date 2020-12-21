package com.avp.core.interactors

import com.avp.core.data.ChosenContestant
import com.avp.core.data.ContestantsRepository
import org.koin.core.KoinComponent
import org.koin.core.inject

class RetrieveChosenContestantInteractor : KoinComponent {

    private val mContestantsRepo by inject<ContestantsRepository>()

    fun saveChosenContestant(chosenContestant: ChosenContestant) = mContestantsRepo.saveChosenContestant(chosenContestant)
    fun getChosenContestant() = mContestantsRepo.getChosenContestant()
}