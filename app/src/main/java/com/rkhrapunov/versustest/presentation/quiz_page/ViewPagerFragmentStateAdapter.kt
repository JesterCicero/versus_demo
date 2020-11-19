package com.rkhrapunov.versustest.presentation.quiz_page

import android.content.Context
import android.content.res.Configuration.ORIENTATION_PORTRAIT
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.rkhrapunov.versustest.R
import timber.log.Timber

@ExperimentalStdlibApi
class ViewPagerFragmentStateAdapter<T>(private val mContext: Context,
                                    data: List<T>,
                                    fm: FragmentManager,
                                    lifecycle: Lifecycle) : FragmentStateAdapter(fm, lifecycle) {

    private var mData = data

    private val mPageItemsNumber = getPageItemsNumber()

    override fun getItemCount() = getTotalPages()

    override fun createFragment(position: Int): QuizPageFragment {
        val pagePosition = position + 1
        val calculatedEndIndex = mPageItemsNumber * pagePosition
        val dataSize = mData.size
        Timber.d("createFragment(): position=$position, pageItemsNumber=$mPageItemsNumber, calculatedEndIndex=$calculatedEndIndex, dataSize=$dataSize")
        val endIndex = if (calculatedEndIndex <= dataSize) calculatedEndIndex else dataSize
        val remainder = dataSize % mPageItemsNumber
        val fullPagesNumber = dataSize / mPageItemsNumber
        val totalPages = fullPagesNumber + if (remainder == 0) 0 else 1
        val startIndex = when {
            endIndex < mPageItemsNumber -> 0
            remainder > 0 && pagePosition == totalPages -> endIndex - remainder
            else -> endIndex - mPageItemsNumber
        }
        Timber.d("createFragment(): startIndex=$startIndex, endIndex=$endIndex")
        val quizPageFragment = QuizPageFragment()
        quizPageFragment.updateData(if (mData.isEmpty()) emptyList() else mData.subList(startIndex, endIndex))
        return quizPageFragment
    }

    private fun getPageItemsNumber(): Int {
        val config = mContext.resources.configuration
        val smallestWidth = config.smallestScreenWidthDp
        Timber.d("getPageItemsNumber(): smallestWidth=$smallestWidth")
        return if (mContext.resources.configuration.orientation == ORIENTATION_PORTRAIT) {
            mContext.resources.getInteger(R.integer.quiz_page_items_number)
        } else {
            mContext.resources.getInteger(R.integer.quiz_page_land_items_number)
        }
    }

    private fun getTotalPages(): Int {
        var resultsPageNumber = 0
        val dataSize = mData.size
        val fullPagesNumber = dataSize / mPageItemsNumber
        if (fullPagesNumber == 0 || dataSize % mPageItemsNumber > 0) {
            ++resultsPageNumber
        }
        resultsPageNumber += fullPagesNumber
        Timber.d("getTotalPages(): $resultsPageNumber")
        return resultsPageNumber
    }
}