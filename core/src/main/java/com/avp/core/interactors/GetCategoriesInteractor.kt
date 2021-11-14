package com.avp.core.interactors

import com.avp.core.data.Constants.EMPTY_STRING
import com.avp.core.data.ContestantsRepository
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class GetCategoriesInteractor : KoinComponent {

    private val mContestantsRepo by inject<ContestantsRepository>()

    fun getCategories(itemData: String = EMPTY_STRING) = mContestantsRepo.getCategories(itemData)
}