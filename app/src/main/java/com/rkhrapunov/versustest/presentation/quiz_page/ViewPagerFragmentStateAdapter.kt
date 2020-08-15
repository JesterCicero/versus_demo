package com.rkhrapunov.versustest.presentation.quiz_page

import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.rkhrapunov.core.data.IQuizShortInfo
import timber.log.Timber

@ExperimentalStdlibApi
class ViewPagerFragmentStateAdapter(data: List<IQuizShortInfo>,
                                    fm: FragmentManager,
                                    lifecycle: Lifecycle) : FragmentStateAdapter(fm, lifecycle) {

    private var mData = data

    override fun getItemCount() = getTotalPages()

    override fun createFragment(position: Int): QuizPageFragment {
        Timber.d("createFragment(): position=$position")
        val pagePosition = position + 1
        val calculatedEndIndex = PAGE_ITEMS_NUMBER * pagePosition
        Timber.d("createFragment(): calculatedEndIndex=$calculatedEndIndex")
        val dataSize = mData.size
        Timber.d("createFragment(): dataSize=$dataSize")
        val endIndex = if (calculatedEndIndex <= dataSize) calculatedEndIndex else dataSize
        val remainder = dataSize % PAGE_ITEMS_NUMBER
        val fullPagesNumber = dataSize / PAGE_ITEMS_NUMBER
        val totalPages = fullPagesNumber + if (remainder == 0) 0 else 1
        val startIndex = when {
            endIndex < PAGE_ITEMS_NUMBER -> 0
            remainder > 0 && pagePosition == totalPages -> endIndex - remainder
            else -> endIndex - PAGE_ITEMS_NUMBER
        }
        Timber.d("createFragment(): startIndex=$startIndex, endIndex=$endIndex")
        val quizPageFragment = QuizPageFragment()
        quizPageFragment.updateData(if (mData.isEmpty()) emptyList() else mData.subList(startIndex, endIndex))
        return quizPageFragment
    }

    private fun getTotalPages(): Int {
        var resultsPageNumber = 0
        val dataSize = mData.size
        val fullPagesNumber = dataSize / PAGE_ITEMS_NUMBER
        if (fullPagesNumber == 0 || dataSize % PAGE_ITEMS_NUMBER > 0) {
            ++resultsPageNumber
        }
        resultsPageNumber += fullPagesNumber
        Timber.d("getTotalPages(): $resultsPageNumber")
        return resultsPageNumber
    }

    companion object {
        private const val PAGE_ITEMS_NUMBER = 10
    }
}