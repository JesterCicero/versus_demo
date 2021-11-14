package com.avp.ctbo.presentation.quizlist

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.avp.ctbo.R
import com.avp.ctbo.databinding.FragmentQuizListBinding
import com.avp.ctbo.presentation.base.QuizAdapter
import com.avp.ctbo.presentation.main.MainActivity
import kotlinx.coroutines.DelicateCoroutinesApi
import org.koin.android.ext.android.inject

@DelicateCoroutinesApi
class QuizListFragment : Fragment(), IQuizListContract.IQuizListView {

    private val mPresenter by inject<IQuizListContract.IQuizListPresenter>()
    private var mQuizListState = true
    private var mBinding: FragmentQuizListBinding? = null
    private var mAdapter: QuizAdapter<*>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mQuizListState = arguments?.getBoolean(MainActivity.QUIZ_LIST_EXTRA, true) ?: true
        mPresenter.attachView(this, lifecycle)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val binding = FragmentQuizListBinding.inflate(inflater, container, false)
        binding.quizListRecyclerViewId.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(this@QuizListFragment.activity)
            addItemDecoration(CustomItemDecorator(context))
        }
        if (!mQuizListState) {
            binding.nextButtonFrameLayoutId.visibility = View.VISIBLE
            binding.presenter = mPresenter
        }
        mBinding = binding
        return binding.root
    }

    override fun updateStatsHeaders(resultsStats: Boolean) {
        mBinding?.let {
            if (resultsStats) {
                it.stats.setBackgroundResource(R.drawable.selected_stats_tab_background)
                it.stats.isEnabled = false
                it.top4.setBackgroundResource(R.drawable.stats_tab_button_selector)
                it.top4.isEnabled = true
            } else {
                it.stats.setBackgroundResource(R.drawable.stats_tab_button_selector)
                it.stats.isEnabled = true
                it.top4.setBackgroundResource(R.drawable.selected_stats_tab_background)
                it.top4.isEnabled = false
            }
            (activity as? MainActivity)?.let { activity ->
                activity.activityMainBinding?.let { binding ->
                    binding.menuId.visibility = if (resultsStats) View.VISIBLE else View.INVISIBLE
                    binding.searchView.visibility = if (resultsStats) View.VISIBLE else View.INVISIBLE
                }
            }
        }
    }

    fun getAdapter() = mAdapter

    override fun setAdapter(quizAdapter: QuizAdapter<*>?) {
        mBinding?.let{ it.quizListRecyclerViewId.apply { adapter = quizAdapter } }
        mAdapter = quizAdapter
    }

    override fun onBackToQuizzesButtonClicked() {
        activity?.onBackPressed()
    }

    override fun shouldShowQuizList() = mQuizListState
}