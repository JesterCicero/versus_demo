package com.rkhrapunov.versustest.presentation.main

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import com.rkhrapunov.core.domain.IRenderState
import com.rkhrapunov.core.domain.RenderState
import com.rkhrapunov.versustest.R
import com.rkhrapunov.versustest.databinding.ActivityMainBinding
import com.rkhrapunov.versustest.presentation.quiz_detail.QuizItemDetailFragment
import com.rkhrapunov.versustest.presentation.quizlist.QuizListAdapter
import com.rkhrapunov.versustest.presentation.quizlist.QuizListFragment
import com.rkhrapunov.versustest.presentation.winner.WinnerFragment
import org.koin.android.ext.android.inject
import timber.log.Timber

class MainActivity : AppCompatActivity(), IMainContract.IMainView {

    private val mPresenter by inject<IMainContract.IMainPresenter>()
    private var mQuizList = true
    private var mBinding: ActivityMainBinding? = null
    //private var mQuizListAdapter: QuizListAdapter<*>? = null

    companion object {
        const val QUIZ_LIST_EXTRA = "quiz_list_extra"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.searchView.setOnQueryTextListener(
            object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String): Boolean {
                    Timber.d("########## onQueryTextSubmit()")
                    getQuizListAdapter()?.filter(query)
                    return true
                }

                override fun onQueryTextChange(newText: String): Boolean {
                    Timber.d("########## onQueryTextChange()")
                    getQuizListAdapter()?.filter(newText)
                    return true
                }
            }
        )
        mBinding = binding
        if (savedInstanceState == null) {
            replaceFragmentIfNecessary(QuizListFragment())
        }
        mPresenter.attachView(this, lifecycle)
    }

    override suspend fun render(renderState: IRenderState) {
        Timber.d("render(): renderState=$renderState")
        val fragment: Fragment = when (renderState) {
            is RenderState.QuizListState -> onQuizListRenderState()
            is RenderState.StatsListState -> {
                showTopBar(true)
                getQuizListFragment(false)
            }
            is RenderState.QuizItemDetailState -> {
                showTopBar(false)
                QuizItemDetailFragment()
            }
            is RenderState.WinnerState -> {
                showTopBar(false)
                WinnerFragment()
            }
            is RenderState.WinnerFinalState -> return
            else -> onQuizListRenderState()
        }
        replaceFragmentIfNecessary(fragment)
    }

    private fun onQuizListRenderState(): QuizListFragment {
        showTopBar(true)
        return getQuizListFragment(true)
    }

    private fun showTopBar(showTopBar: Boolean) {
        mBinding?.let {
            it.backButtonFrameLayoutId.visibility = if (showTopBar) View.VISIBLE else View.GONE
            it.topBarSpace.visibility = if (showTopBar) View.VISIBLE else View.GONE
            it.searchView.visibility = if (showTopBar) View.VISIBLE else View.GONE
            it.topBarSpaceEnd.visibility = if (showTopBar) View.VISIBLE else View.GONE
            it.menuId.visibility = if (showTopBar) View.VISIBLE else View.GONE
        }
    }

    private fun getQuizListFragment(quizList: Boolean): QuizListFragment {
        val args = Bundle()
        args.putBoolean(QUIZ_LIST_EXTRA, quizList)
        mQuizList = quizList
        val fragment = QuizListFragment()
        fragment.arguments = args
        return fragment
    }

    private fun replaceFragmentIfNecessary(fragment: Fragment) {
        val currentFragmentClassName = supportFragmentManager.findFragmentById(R.id.fragment_container)?.tag
        val fragmentClassName = fragment.javaClass.simpleName
        Timber.d("current fragment: $currentFragmentClassName, new fragment: $fragmentClassName")
        currentFragmentClassName?.let { if (it != fragmentClassName) replaceFragment(fragment) } ?: replaceFragment(fragment)
    }

    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment, fragment.javaClass.simpleName)
            .commit()
    }

    private fun getQuizListAdapter(): QuizListAdapter<*>? = supportFragmentManager.findFragmentById(
        R.id.fragment_container)?.let {
            (it as? QuizListFragment)?.getAdapter()
        }

    fun onBackButtonClick(@Suppress("UNUSED_PARAMETER") view: View) = onBackPressed()

    override fun onBackPressed() {
        val currentFragment = supportFragmentManager.findFragmentById(R.id.fragment_container)?.tag
        Timber.d("onBackPressed() current fragment: $currentFragment")
        if (currentFragment == QuizListFragment::class.simpleName && !mQuizList
            || currentFragment == QuizItemDetailFragment::class.simpleName
            || currentFragment == WinnerFragment::class.simpleName) {
            mPresenter.cancelQuiz()
            replaceFragment(QuizListFragment())
        } else {
            super.onBackPressed()
        }
    }
}
