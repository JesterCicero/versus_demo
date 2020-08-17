package com.rkhrapunov.versustest.presentation.quizlist

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.rkhrapunov.core.data.IContestantsStatsInfo
import com.rkhrapunov.core.data.IQuizShortInfo
import com.rkhrapunov.versustest.databinding.QuizListItemBinding
import com.rkhrapunov.versustest.databinding.QuizListPagerItemBinding
import com.rkhrapunov.versustest.framework.helpers.CoroutineLauncherHelper
import com.rkhrapunov.versustest.presentation.base.Constants.EMPTY_STRING
import com.rkhrapunov.versustest.presentation.base.Constants.SPACE_SYMBOL
import com.rkhrapunov.versustest.presentation.base.Constants.UNDERSCORE_SYMBOL
import com.rkhrapunov.versustest.presentation.base.ImageLoader
import com.rkhrapunov.versustest.presentation.base.capitalizeWords
import com.rkhrapunov.versustest.presentation.base.weak
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.core.KoinComponent
import org.koin.core.inject
import timber.log.Timber
import java.util.Locale

@ExperimentalStdlibApi
@Suppress("UNCHECKED_CAST")
class QuizListAdapter<T>(private val mItemClickListener: IItemClickListener,
                         fragment: Fragment,
                         quizList: Boolean = true)
    : RecyclerView.Adapter<QuizListAdapter.QuizListViewHolder>() {

    private var mData = mutableListOf<T>()
    private var mCopyData = mutableListOf<T>()
    private var mFragment: Fragment? by weak()
    private var mQuizList = quizList

    init {
        mFragment = fragment
    }

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
                        if (ascending) contestantsStatsInfo.sortBy { it.percentage.toFloat() } else contestantsStatsInfo.sortByDescending { it.percentage.toFloat() }
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

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = if (mQuizList) QuizListViewHolder(
        QuizListPagerItemBinding.inflate(
            LayoutInflater.from(parent.context), parent, false), mItemClickListener) else QuizListViewHolder(
        QuizListItemBinding.inflate(
            LayoutInflater.from(parent.context), parent, false), mItemClickListener)

    override fun getItemCount() = mData.size

    override fun onBindViewHolder(holder: QuizListViewHolder, position: Int) = holder.bind(position, mData, mFragment)

    @ExperimentalStdlibApi
    class QuizListViewHolder(root: View,
                             private val mItemClickListener: IItemClickListener)
        : RecyclerView.ViewHolder(root), KoinComponent {

        private val mCoroutineLauncherHelper by inject<CoroutineLauncherHelper>()
        private val mImageLoader by inject<ImageLoader>()
        private lateinit var mQuizListPagerItemBinding: QuizListPagerItemBinding
        private lateinit var mQuizListItemBinding: QuizListItemBinding

        constructor(quizListPagerItemBinding: QuizListPagerItemBinding,
                    itemClickListener: IItemClickListener) : this(quizListPagerItemBinding.root, itemClickListener) {
            mQuizListPagerItemBinding = quizListPagerItemBinding
        }

        constructor(quizListItemBinding: QuizListItemBinding, itemClickListener: IItemClickListener)
                : this(quizListItemBinding.root, itemClickListener) {
            mQuizListItemBinding = quizListItemBinding
        }

        fun bind(position: Int, data: List<*>, fragment: Fragment?) {
           when {
                data[position] is IQuizShortInfo -> onBindQuizShortInfo(position, data, fragment)
                data[position] is IContestantsStatsInfo -> onBindContestantsStatsInfo(position, data, fragment)
                else -> Timber.w("Unknown info")
            }
        }

        private fun onBindQuizShortInfo(position: Int, data: List<*>, fragment: Fragment?) {
            mQuizListPagerItemBinding.itemClickListener = mItemClickListener
            mQuizListPagerItemBinding.quizList = true
            val itemData = (data[position] as? IQuizShortInfo)?.let {
                loadImage(fragment, it.url)
                it.title
            } ?: EMPTY_STRING
            mQuizListPagerItemBinding.itemName.isSelected = true
            mQuizListPagerItemBinding.itemDataReal = itemData
            mQuizListPagerItemBinding.itemDataFake = itemData.replace(UNDERSCORE_SYMBOL, SPACE_SYMBOL).capitalizeWords()
            mQuizListPagerItemBinding.executePendingBindings()
        }

        private fun onBindContestantsStatsInfo(position: Int, data: List<*>, fragment: Fragment?) {
            mQuizListItemBinding.quizList = false
            val itemData = (data[position] as? IContestantsStatsInfo)?.let {
                mQuizListItemBinding.itemDataDetail = it.percentage
                loadImage(fragment, it.minUrl, false)
                it.name
            } ?: EMPTY_STRING
            mQuizListItemBinding.itemName.isSelected = true
            mQuizListItemBinding.itemDataReal = itemData
            mQuizListItemBinding.itemDataFake = itemData.replace(UNDERSCORE_SYMBOL, SPACE_SYMBOL).capitalizeWords()
            mQuizListItemBinding.executePendingBindings()
        }

        private fun loadImage(fragment: Fragment?, url: String, quizList: Boolean = true) {
            fragment?.let { nonNullableFragment ->
                mCoroutineLauncherHelper.launch(Dispatchers.Main) {
                    withContext(Dispatchers.IO) {
                        fragment.activity?.let {
                            mImageLoader.loadImage(nonNullableFragment, url, if (quizList) mQuizListPagerItemBinding.smallImg else mQuizListItemBinding.smallImg)
                        }
                    }
                }
            }
        }
    }
}