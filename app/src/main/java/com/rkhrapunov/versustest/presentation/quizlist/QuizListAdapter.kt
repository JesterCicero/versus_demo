package com.rkhrapunov.versustest.presentation.quizlist

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.rkhrapunov.core.data.IContestantsStatsInfo
import com.rkhrapunov.core.data.IQuizShortInfo
import com.rkhrapunov.versustest.databinding.QuizListItemBinding
import com.rkhrapunov.versustest.framework.helpers.CoroutineLauncherHelper
import com.rkhrapunov.versustest.presentation.base.Constants.EMPTY_STRING
import com.rkhrapunov.versustest.presentation.base.Constants.SPACE_SYMBOL
import com.rkhrapunov.versustest.presentation.base.Constants.UNDERSCORE_SYMBOL
import com.rkhrapunov.versustest.presentation.base.ImageLoader
import com.rkhrapunov.versustest.presentation.base.capitalizeWords
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.core.KoinComponent
import org.koin.core.inject
import timber.log.Timber
import java.util.Locale

@ExperimentalStdlibApi
@Suppress("UNCHECKED_CAST")
class QuizListAdapter<T>(private val mItemClickListener: IItemClickListener,
                         private val mQuizListView: IQuizListContract.IQuizListView)
    : RecyclerView.Adapter<QuizListAdapter.QuizListViewHolder>() {

    private var mData = mutableListOf<T>()
    private var mCopyData = mutableListOf<T>()

    fun updateData(data: List<T>) {
        Timber.d("data size: {${data.size}}")
        mData = data.toMutableList()
        notifyDataSetChanged()
    }

    fun sort(ascending: Boolean = true, sortByResults: Boolean = true) {
        if (mData.isNotEmpty()) {
            if (mData[0] is IQuizShortInfo) {
                val list = mData as? MutableList<IQuizShortInfo>
                list?.let { quizInfoList ->
                    if (ascending) quizInfoList.sortBy { it.title } else quizInfoList.sortByDescending { it.title }
                }
            } else {
                val list = mData as? MutableList<IContestantsStatsInfo>
                if (sortByResults) {
                    list?.let { contestantsStatsInfo ->
                        if (ascending) contestantsStatsInfo.sortBy { it.percentage } else contestantsStatsInfo.sortByDescending { it.percentage }
                    }
                } else {
                    list?.let { contestantsStatsInfo ->
                        if (ascending) contestantsStatsInfo.sortBy { it.name } else contestantsStatsInfo.sortByDescending { it.name }
                    }
                }
            }
            notifyDataSetChanged()
        }
    }

    fun filter(text: String) {
        Timber.d("filter(): text: $text")
        if (mCopyData.isEmpty()) {
            mCopyData.addAll(mData)
        }
        mData.clear()
        if (text.isEmpty()) {
            Timber.d("filter(): text is empty")
            mData.addAll(mCopyData)
        } else {
            if (mCopyData.isNotEmpty()) {
                if (mCopyData[0] is IQuizShortInfo) {
                    Timber.d("filter(): IQuizShortInfo")
                    (mCopyData as? MutableList<IQuizShortInfo>)?.forEach {
                        if (it.title.toLowerCase(Locale.getDefault()).contains(text.toLowerCase(Locale.getDefault()))) {
                            (mData as? MutableList<IQuizShortInfo>)?.add(it)
                        }
                    }
                } else {
                    Timber.d("filter(): IContestantsStatsInfo")
                    (mCopyData as? MutableList<IContestantsStatsInfo>)?.forEach {
                        if (it.name.toLowerCase(Locale.getDefault()).contains(text.toLowerCase(Locale.getDefault()))) {
                            (mData as? MutableList<IContestantsStatsInfo>)?.add(it)
                        }
                    }
                }
            }
        }
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = QuizListViewHolder(
        QuizListItemBinding.inflate(LayoutInflater.from(parent.context), parent, false), mItemClickListener)

    override fun getItemCount() = mData.size

    override fun onBindViewHolder(holder: QuizListViewHolder, position: Int) = holder.bind(position, mData, mQuizListView as? Fragment)

    @ExperimentalStdlibApi
    class QuizListViewHolder(private val mRecognitionDialogItemBinding: QuizListItemBinding,
                             private val mItemClickListener: IItemClickListener)
        : RecyclerView.ViewHolder(mRecognitionDialogItemBinding.root), KoinComponent {

        private val mCoroutineLauncherHelper by inject<CoroutineLauncherHelper>()
        private val mImageLoader by inject<ImageLoader>()

        fun bind(position: Int, data: List<*>, fragment: Fragment?) {
            val itemData = when {
                data[position] is IQuizShortInfo -> onBindQuizShortInfo(position, data, fragment)
                data[position] is IContestantsStatsInfo -> onBindContestantsStatsInfo(position, data, fragment)
                else -> EMPTY_STRING
            }
            Timber.d("itemData is: $itemData")
            mRecognitionDialogItemBinding.itemName.isSelected = true
            mRecognitionDialogItemBinding.itemDataReal = itemData
            mRecognitionDialogItemBinding.itemDataFake = itemData.replace(UNDERSCORE_SYMBOL, SPACE_SYMBOL).capitalizeWords()
            mRecognitionDialogItemBinding.executePendingBindings()
        }

        private fun onBindQuizShortInfo(position: Int, data: List<*>, fragment: Fragment?): String {
            mRecognitionDialogItemBinding.itemClickListener = mItemClickListener
            mRecognitionDialogItemBinding.quizList = true
            return (data[position] as? IQuizShortInfo)?.let {
                loadImage(fragment, it.url)
                it.title
            } ?: EMPTY_STRING
        }

        private fun onBindContestantsStatsInfo(position: Int, data: List<*>, fragment: Fragment?): String {
            mRecognitionDialogItemBinding.quizList = false
            return (data[position] as? IContestantsStatsInfo)?.let {
                mRecognitionDialogItemBinding.itemDataDetail = it.percentage
                loadImage(fragment, it.minUrl)
                it.name
            } ?: EMPTY_STRING
        }

        private fun loadImage(fragment: Fragment?, url: String) {
            fragment?.let {
                mCoroutineLauncherHelper.launch(Dispatchers.Main) {
                    withContext(Dispatchers.IO) {

                        mImageLoader.loadImage(it, url, mRecognitionDialogItemBinding.smallImg)
                    }
                }
            }
        }
    }
}