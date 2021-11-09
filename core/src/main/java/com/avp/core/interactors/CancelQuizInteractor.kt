package com.avp.core.interactors

import com.avp.core.data.ContestantsRepository
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class CancelQuizInteractor : KoinComponent {

    private val mContestantsRepo by inject<ContestantsRepository>()

    fun cancelQuiz() = mContestantsRepo.cancelQuiz()
}