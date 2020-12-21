package com.avp.core.interactors

import com.avp.core.data.ContestantsRepository
import com.sun.org.apache.xml.internal.utils.LocaleUtility.EMPTY_STRING
import org.koin.core.KoinComponent
import org.koin.core.inject

class GetCategoriesInteractor : KoinComponent {

    private val mContestantsRepo by inject<ContestantsRepository>()

    fun getCategories(itemData: String = EMPTY_STRING) = mContestantsRepo.getCategories(itemData)
}