package com.rkhrapunov.core.interactors

import com.rkhrapunov.core.data.ContestantsRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.koin.core.KoinComponent
import org.koin.core.inject

class GetRenderUiChannelInteractor : KoinComponent {

    private val mContestantsRepo by inject<ContestantsRepository>()

    @ExperimentalCoroutinesApi
    fun getRenderUiChannel() = mContestantsRepo.getRenderUiChannel()
}