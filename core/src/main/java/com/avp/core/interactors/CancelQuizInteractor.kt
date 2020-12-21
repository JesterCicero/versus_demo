package com.avp.core.interactors

import com.avp.core.data.ContestantsRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.koin.core.KoinComponent
import org.koin.core.inject

class CancelQuizInteractor : KoinComponent {

    private val mContestantsRepo by inject<ContestantsRepository>()

    @ExperimentalCoroutinesApi
    fun cancelQuiz() = mContestantsRepo.cancelQuiz()
}