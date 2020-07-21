package com.rkhrapunov.versustest.presentation.quizlist

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.rkhrapunov.versustest.databinding.FragmentQuizListBinding
import com.rkhrapunov.versustest.presentation.main.MainActivity
import org.koin.android.ext.android.inject

class QuizListFragment : Fragment(), IQuizListContract.IQuizListView {

    private val mPresenter by inject<IQuizListContract.IQuizListPresenter>()
    private var mQuizListState = true
    private var mBinding: FragmentQuizListBinding? = null
    private var mAdapter: QuizListAdapter<*>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mQuizListState = arguments?.getBoolean(MainActivity.QUIZ_LIST_EXTRA, true) ?: true
        mPresenter.attachView(this, lifecycle)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val binding = FragmentQuizListBinding.inflate(inflater, container, false)
        binding.quizListRecyclerViewId.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(this@QuizListFragment.activity)
            addItemDecoration(CustomItemDecorator(context))
        }
        if (!mQuizListState) {
            binding.backButton.visibility = View.VISIBLE
            binding.presenter = mPresenter
        }
        mBinding = binding
        return binding.root
    }

    fun getAdapter() = mAdapter

    override fun setAdapter(quizListAdapter: QuizListAdapter<*>?) {
        mBinding?.let{ it.quizListRecyclerViewId.apply { adapter = quizListAdapter } }
        mAdapter = quizListAdapter
    }

    override fun onBackToQuizzesButtonClicked() {
        activity?.onBackPressed()
    }

    override fun shouldShowQuizList() = mQuizListState
}