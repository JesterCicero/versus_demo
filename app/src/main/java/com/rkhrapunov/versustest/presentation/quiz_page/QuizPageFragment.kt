package com.rkhrapunov.versustest.presentation.quiz_page

import android.content.res.Configuration.ORIENTATION_PORTRAIT
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.rkhrapunov.core.data.IQuizShortInfo
import com.rkhrapunov.versustest.databinding.QuizPageItemBinding
import com.rkhrapunov.versustest.presentation.quizlist.IItemClickListener
import com.rkhrapunov.versustest.presentation.quizlist.QuizListAdapter
import org.koin.android.ext.android.inject
import timber.log.Timber

@ExperimentalStdlibApi
class QuizPageFragment : Fragment(), IQuizPageContract.IQuizPageView {

    private val mPresenter by inject<IQuizPageContract.IQuizPagePresenter>()
    private var mData = emptyList<IQuizShortInfo>()
    private var mAdapter: QuizListAdapter<*>? = null

    fun updateData(data: List<IQuizShortInfo>) {
        mData = data
    }

    fun getAdapter() = mAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mPresenter.attachView(this, lifecycle)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val binding = QuizPageItemBinding.inflate(inflater, container, false)
        binding.quizGridRecyclerViewId.apply {
            setHasFixedSize(true)
            val columnsNumber = if (inflater.context.resources.configuration.orientation == ORIENTATION_PORTRAIT) PORTRAIT_COLUMNS_NUMBER else LANDSCAPE_COLUMNS_NUMBER
            Timber.d("onCreateView(): mColumnsNumber=$columnsNumber")
            layoutManager = GridLayoutManager(this@QuizPageFragment.activity, columnsNumber)
            val quizListAdapter = QuizListAdapter<IQuizShortInfo>(mPresenter as IItemClickListener, this@QuizPageFragment)
            adapter = quizListAdapter
            quizListAdapter.updateData(mData)
            mAdapter = quizListAdapter
        }
        return binding.root
    }

    companion object {
        private const val PORTRAIT_COLUMNS_NUMBER = 2
        private const val LANDSCAPE_COLUMNS_NUMBER = 3
    }
}