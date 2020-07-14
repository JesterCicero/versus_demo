package com.rkhrapunov.versustest.presentation.main

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.rkhrapunov.core.domain.IRenderState
import com.rkhrapunov.core.domain.RenderState
import com.rkhrapunov.versustest.R
import com.rkhrapunov.versustest.presentation.quiz_detail.QuizItemDetailFragment
import com.rkhrapunov.versustest.presentation.quizlist.QuizListFragment
import com.rkhrapunov.versustest.presentation.winner.WinnerFragment
import org.koin.android.ext.android.inject
import timber.log.Timber

class MainActivity : AppCompatActivity(), IMainContract.IMainView {

    private val mPresenter by inject<IMainContract.IMainPresenter>()

    companion object {
        const val QUIZ_LIST_EXTRA = "quiz_list_extra"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        if (savedInstanceState == null) {
            replaceFragmentIfNecessary(QuizListFragment())
        }
        mPresenter.attachView(this, lifecycle)
    }

    override suspend fun render(renderState: IRenderState) {
        Timber.d("render(): renderState=$renderState")
        val fragment: Fragment = when (renderState) {
            is RenderState.QuizListState -> getQuizListFragment(true)
            is RenderState.StatsListState -> getQuizListFragment(false)
            is RenderState.QuizItemDetailState -> QuizItemDetailFragment()
            is RenderState.WinnerState -> WinnerFragment()
            is RenderState.WinnerFinalState -> return
            else -> getQuizListFragment(true)
        }
        replaceFragmentIfNecessary(fragment)
    }

    private fun getQuizListFragment(quizList: Boolean): QuizListFragment {
        val args = Bundle()
        args.putBoolean(QUIZ_LIST_EXTRA, quizList)
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

    override fun onBackPressed() {
        val currentFragment = supportFragmentManager.findFragmentById(R.id.fragment_container)?.tag
        Timber.d("onBackPressed() current fragment: $currentFragment")
        if (currentFragment == QuizListFragment::class.simpleName
            || currentFragment == QuizItemDetailFragment::class.simpleName
            || currentFragment == WinnerFragment::class.simpleName) {
            replaceFragment(QuizListFragment())
        } else {
            super.onBackPressed()
        }
    }
}
