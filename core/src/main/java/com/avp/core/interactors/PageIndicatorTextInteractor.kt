package com.avp.core.interactors

import com.avp.core.data.ContestantsRepository
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class PageIndicatorTextInteractor : KoinComponent {

    private val mContestantsRepo by inject<ContestantsRepository>()

    fun savePageIndicatorText(pageIndicatorText: String) = mContestantsRepo.savePageIndicatorText(pageIndicatorText)
    fun getPageIndicatorText() = mContestantsRepo.getPageIndicatorText()
    fun saveCurrentPagePosition(currentPosition: Int) = mContestantsRepo.saveCurrentPagePosition(currentPosition)
    fun getCurrentPagePosition() = mContestantsRepo.getCurrentPagePosition()
}