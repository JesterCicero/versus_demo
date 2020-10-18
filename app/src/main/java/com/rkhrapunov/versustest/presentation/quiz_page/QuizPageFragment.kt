package com.rkhrapunov.versustest.presentation.quiz_page

import android.content.res.Configuration.ORIENTATION_PORTRAIT
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.bumptech.glide.Glide
import com.rkhrapunov.core.data.ICategory
import com.rkhrapunov.core.data.IQuizShortInfo
import com.rkhrapunov.core.data.ISuperCategory
import com.rkhrapunov.versustest.databinding.QuizPageItemBinding
import com.rkhrapunov.versustest.presentation.base.IItemClickListener
import com.rkhrapunov.versustest.presentation.base.QuizAdapter
import com.rkhrapunov.versustest.presentation.base.QuizDataType
import org.koin.android.ext.android.inject
import timber.log.Timber

@ExperimentalStdlibApi
class QuizPageFragment : Fragment(), IQuizPageContract.IQuizPageView {

    private val mPresenter by inject<IQuizPageContract.IQuizPagePresenter>()
    private var mData: List<*>? = null
    private var mAdapter: QuizAdapter<*>? = null

    fun updateData(data: List<*>) {
        mData = data
    }

    override fun getSelectedDataType(): SelectedData {
        val dataListNotEmpty = mData?.isNotEmpty() ?: false
        return if (dataListNotEmpty) {
            mData?.let {
                when (it[0]) {
                    is ISuperCategory -> SelectedData.SUPER_CATEGORY
                    is ICategory -> SelectedData.CATEGORY
                    is IQuizShortInfo -> SelectedData.QUIZ
                    else -> SelectedData.NONE
                }
            } ?: SelectedData.NONE
        } else SelectedData.NONE
    }

    fun getAdapter() = mAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mPresenter.attachView(this, lifecycle)
    }

    override fun onLowMemory() {
        super.onLowMemory()
        Glide.with(this).onLowMemory()
    }

    @Suppress("UNCHECKED_CAST")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val binding = QuizPageItemBinding.inflate(inflater, container, false)
        binding.quizGridRecyclerViewId.apply {
            setHasFixedSize(true)
            val columnsNumber = if (inflater.context.resources.configuration.orientation == ORIENTATION_PORTRAIT) PORTRAIT_COLUMNS_NUMBER else LANDSCAPE_COLUMNS_NUMBER
            Timber.d("onCreateView(): mColumnsNumber=$columnsNumber")
            layoutManager = GridLayoutManager(this@QuizPageFragment.activity, columnsNumber)
            createAdapter()?.let {
                adapter = it
                mData?.let { dataList ->
                    when (dataList[0]) {
                        is ICategory -> (it as QuizAdapter<ICategory>).updateData(dataList as List<ICategory>)
                        is IQuizShortInfo -> (it as QuizAdapter<IQuizShortInfo>).updateData(dataList as List<IQuizShortInfo>)
                        else -> Timber.w("Unknown data list type")
                    }
                }
                mAdapter = it
            }

        }
        return binding.root
    }

    private fun createAdapter(): QuizAdapter<*>? {
        val dataListNotEmpty = mData?.isNotEmpty() ?: false
        return if (dataListNotEmpty) {
            mData?.let {
                when (it[0]) {
                    is ICategory -> {
                        QuizAdapter<ICategory>(
                            mPresenter as IItemClickListener,
                            this,
                            QuizDataType.QUIZ_LIST
                        )
                    }
                    is IQuizShortInfo -> QuizAdapter<IQuizShortInfo>(
                        mPresenter as IItemClickListener,
                        this,
                        QuizDataType.QUIZ_LIST
                    )
                    else -> null
                }
            }
        } else null
    }

    companion object {
        private const val PORTRAIT_COLUMNS_NUMBER = 2
        private const val LANDSCAPE_COLUMNS_NUMBER = 3
    }
}