package com.rkhrapunov.core.interactors

import com.rkhrapunov.core.data.ContestantsRepository
import org.koin.core.KoinComponent
import org.koin.core.inject

class PageIndicatorTextInteractor : KoinComponent {

    private val mContestantsRepo by inject<ContestantsRepository>()

    fun savePageIndicatorText(pageIndicatorText: String) = mContestantsRepo.savePageIndicatorText(pageIndicatorText)
    fun getPageIndicatorText() = mContestantsRepo.getPageIndicatorText()
    fun saveCurrentPagePosition(currentPosition: Int) = mContestantsRepo.saveCurrentPagePosition(currentPosition)
    fun getCurrentPagePosition() = mContestantsRepo.getCurrentPagePosition()
}