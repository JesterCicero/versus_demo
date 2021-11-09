package com.avp.ctbo.presentation.base

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.avp.core.data.ISuperCategory
import com.avp.core.data.ICategory
import com.avp.core.data.IQuizShortInfo
import com.avp.core.data.IContestantsInfo
import com.avp.core.data.IContestantsStatsInfo
import com.avp.core.interactors.CurrentSuperCategoryInteractor
import com.avp.ctbo.R
import com.avp.ctbo.databinding.QuizListPagerItemBinding
import com.avp.ctbo.databinding.SuperCategoryItemBinding
import com.avp.ctbo.databinding.QuizListItemBinding
import com.avp.ctbo.framework.helpers.CoroutineLauncherHelper
import com.avp.ctbo.presentation.base.Constants.EMPTY_STRING
import com.avp.ctbo.presentation.base.Constants.INVALID_VALUE
import com.avp.ctbo.presentation.base.Constants.SPACE_SYMBOL
import com.avp.ctbo.presentation.base.Constants.UNDERSCORE_SYMBOL
import kotlinx.coroutines.DelicateCoroutinesApi
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import timber.log.Timber
import java.util.Locale

@DelicateCoroutinesApi
@Suppress("UNCHECKED_CAST")
class QuizAdapter<T>(private val mItemClickListener: IItemClickListener,
                     quizDataType: QuizDataType = QuizDataType.QUIZ_LIST)
    : RecyclerView.Adapter<QuizAdapter.QuizListViewHolder>() {

    private var mData = mutableListOf<T>()
    private var mCopyData = mutableListOf<T>()
    private var mActivity: FragmentActivity? by weak()
    private var mFragment: Fragment? by weak()
    private var mQuizDataType = quizDataType

    constructor(itemClickListener: IItemClickListener,
                activity: FragmentActivity,
                quizDataType: QuizDataType) : this(itemClickListener, quizDataType) {
        mActivity = activity
    }

    constructor(itemClickListener: IItemClickListener,
                fragment: Fragment,
                quizDataType: QuizDataType) : this(itemClickListener, quizDataType) {
        mFragment = fragment
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateData(data: List<T>) {
        Timber.d("data size: ${data.size}")
        mData = data.toMutableList()
        notifyDataSetChanged()
    }

    @SuppressLint("NotifyDataSetChanged")
    fun sort(ascending: Boolean = true, sortByResults: Boolean = true) {
        if (mData.isNotEmpty()) {
            when (mData[0]) {
                is ICategory -> onSortCategories(ascending)
                is IQuizShortInfo -> onSortQuizShortInfo(ascending)
                else -> onSortContestantsStatsInfo(ascending, sortByResults)
            }
            notifyDataSetChanged()
        }
    }

    @SuppressLint("NotifyDataSetChanged")
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
                when (mCopyData[0]) {
                    is ICategory -> onFilterCategories(text)
                    is IQuizShortInfo -> onFilterQuizzes(text)
                    else -> onFilterContestantsStatsInfo(text)
                }
            }
        }
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = when (mQuizDataType) {
        QuizDataType.QUIZ_LIST -> {
            QuizListViewHolder(
                QuizListPagerItemBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                ), mItemClickListener
            )
        }
        QuizDataType.QUIZ_SUPER_CATEGORIES -> QuizListViewHolder(
            SuperCategoryItemBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            ), mItemClickListener
        )
        QuizDataType.QUIZ_STATS,
        QuizDataType.QUIZ_TOP_4 -> QuizListViewHolder(
            QuizListItemBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            ), mItemClickListener
        )
    }

    override fun getItemCount() = mData.size

    override fun onBindViewHolder(holder: QuizListViewHolder, position: Int) {
        if (mQuizDataType == QuizDataType.QUIZ_SUPER_CATEGORIES) holder.bindSuperCategory(
            position, mData, mActivity) else holder.bind(position, mData, mFragment)
    }

    private fun onSortCategories(ascending: Boolean) {
        val list = mData as? MutableList<ICategory>
        list?.let { categories ->
            if (ascending) categories.sortBy { it.name } else categories.sortByDescending { it.name }
        }
    }

    private fun onSortQuizShortInfo(ascending: Boolean) {
        val list = mData as? MutableList<IQuizShortInfo>
        list?.let { quizInfoList ->
            if (ascending) quizInfoList.sortBy { it.title } else quizInfoList.sortByDescending { it.title }
        }
    }

    private fun onSortContestantsStatsInfo(ascending: Boolean, sortByResults: Boolean) {
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

    private fun onFilterCategories(text: String) {
        Timber.d("onFilterCategories(): ICategories")
        (mCopyData as? MutableList<ICategory>)?.forEach {
            if (it.name.lowercase(Locale.getDefault()).contains(text.lowercase(Locale.getDefault()))) {
                (mData as? MutableList<ICategory>)?.add(it)
            }
        }
    }

    private fun onFilterQuizzes(text: String) {
        Timber.d("onFilterSuperCategories(): IQuizShortInfo")
        (mCopyData as? MutableList<IQuizShortInfo>)?.forEach {
            if (it.title.lowercase(Locale.getDefault()).contains(text.lowercase(Locale.getDefault()))) {
                (mData as? MutableList<IQuizShortInfo>)?.add(it)
            }
        }
    }

    private fun onFilterContestantsStatsInfo(text: String) {
        Timber.d("onFilterSuperCategories(): IContestantsStatsInfo")
        (mCopyData as? MutableList<IContestantsStatsInfo>)?.forEach {
            if (it.name.lowercase(Locale.getDefault()).contains(text.lowercase(Locale.getDefault()))) {
                (mData as? MutableList<IContestantsStatsInfo>)?.add(it)
            }
        }
    }

    @Suppress("JoinDeclarationAndAssignment")
    @DelicateCoroutinesApi
    class QuizListViewHolder(root: View,
                             private val mItemClickListener: IItemClickListener
    )
        : RecyclerView.ViewHolder(root), KoinComponent {

        private val mCoroutineLauncherHelper by inject<CoroutineLauncherHelper>()
        private val mImageLoader by inject<ImageLoader>()
        private val mCurrentSuperCategoryInteractor by inject<CurrentSuperCategoryInteractor>()
        private lateinit var mQuizListPagerItemBinding: QuizListPagerItemBinding
        private lateinit var mQuizListItemBinding: QuizListItemBinding
        private lateinit var mSuperCategoryItemBinding: SuperCategoryItemBinding

        constructor(quizListPagerItemBinding: QuizListPagerItemBinding,
                    itemClickListener: IItemClickListener
        ) : this(quizListPagerItemBinding.root, itemClickListener) {
            mQuizListPagerItemBinding = quizListPagerItemBinding
        }

        constructor(quizListItemBinding: QuizListItemBinding, itemClickListener: IItemClickListener)
                : this(quizListItemBinding.root, itemClickListener) {
            mQuizListItemBinding = quizListItemBinding
        }

        constructor(superCategoryItemBinding: SuperCategoryItemBinding, itemClickListener: IItemClickListener)
                : this(superCategoryItemBinding.root, itemClickListener) {
            mSuperCategoryItemBinding = superCategoryItemBinding
        }

        fun bind(position: Int, data: List<*>, fragment: Fragment?) {
            when {
                data[position] is ICategory -> onBindCategory(position, data, fragment)
                data[position] is IQuizShortInfo -> onBindQuizShortInfo(position, data, fragment)
                data[position] is IContestantsStatsInfo || data[position] is IContestantsInfo -> onBindContestantsStatsInfo(position, data, fragment)
                else -> Timber.w("Unknown info")
            }
        }

        @Suppress("UNUSED_PARAMETER", "SimpleRedundantLet")
        fun bindSuperCategory(position: Int, data: List<*>, activity: FragmentActivity?) {
            mSuperCategoryItemBinding.itemClickListener = mItemClickListener
            mSuperCategoryItemBinding.position = position
            val currentSuperCategoryPositionFromSource = mCurrentSuperCategoryInteractor.getCurrentSuperCategoryPosition()
            Timber.d("position from data source: $currentSuperCategoryPositionFromSource")
            if (position == 0 && currentSuperCategoryPositionFromSource == INVALID_VALUE) {
                val parent = mSuperCategoryItemBinding.itemName
                parent.setBackgroundResource(R.drawable.selected_super_category_background)
                parent.isEnabled = false
                mCurrentSuperCategoryInteractor.saveCurrentSuperCategoryPosition(position)
            } else if (position == currentSuperCategoryPositionFromSource) {
                val parent = mSuperCategoryItemBinding.itemName
                parent.setBackgroundResource(R.drawable.selected_super_category_background)
                parent.isEnabled = false
            }
            val itemData = (data[position] as? ISuperCategory)?.let {
                // Leave this code for possible future super category icon use
//                activity?.let { fragmentActivity ->
//                    mCoroutineLauncherHelper.launchImgLoading {
//                        mImageLoader.loadImage(fragmentActivity, it.url, mSuperCategoryItemBinding.smallImg)
//                    }
//                }
                it.name
            } ?: EMPTY_STRING
            setSuperCategoriesBindingItemData(itemData)
        }

        private fun onBindCategory(position: Int, data: List<*>, fragment: Fragment?) {
            mQuizListPagerItemBinding.itemClickListener = mItemClickListener
            mQuizListPagerItemBinding.position = position
            val itemData = (data[position] as? ICategory)?.let {
                loadImage(fragment, it.url)
                it.name
            } ?: EMPTY_STRING
            setBindingItemData(itemData)
        }

        private fun onBindQuizShortInfo(position: Int, data: List<*>, fragment: Fragment?) {
            mQuizListPagerItemBinding.itemClickListener = mItemClickListener
            mQuizListPagerItemBinding.position = position
            val itemData = (data[position] as? IQuizShortInfo)?.let {
                loadImage(fragment, it.url)
                it.title
            } ?: EMPTY_STRING
            setBindingItemData(itemData)
        }

        private fun onBindContestantsStatsInfo(position: Int, data: List<*>, fragment: Fragment?) {
            mQuizListItemBinding.quizList = data[position] is IContestantsInfo
            mQuizListItemBinding.position = position
            val itemData = (data[position] as? IContestantsStatsInfo)?.let {
                mQuizListItemBinding.itemDataDetail = it.percentage
                mQuizListItemBinding.progressBar.progress = it.percentage.toFloat().toInt()
                loadImage(fragment, it.minUrl, true)
                it.name
            } ?: (data[position] as? IContestantsInfo)?.let {
                loadImage(fragment, it.minUrl, true)
                it.name
            } ?: EMPTY_STRING
            setStatsBindingItemData(itemData)
        }

        private fun setSuperCategoriesBindingItemData(itemData: String) {
            // Leave this line for future use
            // mSuperCategoryItemBinding.itemName.isSelected = true
            mSuperCategoryItemBinding.itemDataReal = itemData
            mSuperCategoryItemBinding.itemDataFake = itemData.replace(UNDERSCORE_SYMBOL, SPACE_SYMBOL).capitalizeWords()
            mSuperCategoryItemBinding.executePendingBindings()
        }

        private fun setBindingItemData(itemData: String) {
            mQuizListPagerItemBinding.itemName.isSelected = true
            mQuizListPagerItemBinding.itemDataReal = itemData
            mQuizListPagerItemBinding.itemDataFake = itemData.replace(UNDERSCORE_SYMBOL, SPACE_SYMBOL).capitalizeWords()
            mQuizListPagerItemBinding.executePendingBindings()
        }

        private fun setStatsBindingItemData(itemData: String) {
            mQuizListItemBinding.itemName.isSelected = true
            mQuizListItemBinding.itemDataReal = itemData
            mQuizListItemBinding.itemDataFake = itemData.replace(UNDERSCORE_SYMBOL, SPACE_SYMBOL).capitalizeWords()
            mQuizListItemBinding.executePendingBindings()
        }

        private fun loadImage(fragment: Fragment?, url: String, quizStatsList: Boolean = false) {
            fragment?.let {
                fragment.activity?.let {
                    mCoroutineLauncherHelper.launchImgLoading {
                        mImageLoader.loadImage(it, url, if (quizStatsList) mQuizListItemBinding.smallImg else mQuizListPagerItemBinding.smallImg)
                    }
                }
            }
        }
    }
}