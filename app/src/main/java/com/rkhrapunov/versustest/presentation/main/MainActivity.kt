package com.rkhrapunov.versustest.presentation.main

import android.os.Build
import android.os.Bundle
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.Gravity
import android.view.Menu
import android.view.View
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.rkhrapunov.core.domain.IRenderState
import com.rkhrapunov.core.domain.RenderState
import com.rkhrapunov.core.interactors.GetQuizListInteractor
import com.rkhrapunov.versustest.R
import com.rkhrapunov.versustest.databinding.ActivityMainBinding
import com.rkhrapunov.versustest.presentation.base.weak
import com.rkhrapunov.versustest.presentation.empty_pager.EmptyPagerFragment
import com.rkhrapunov.versustest.presentation.error.ErrorDialogFragment
import com.rkhrapunov.versustest.presentation.error.ErrorDialogFragment.Companion.ERROR_MSG_KEY
import com.rkhrapunov.versustest.presentation.quiz_detail.QuizItemDetailFragment
import com.rkhrapunov.versustest.presentation.quiz_pager.QuizPagerFragment
import com.rkhrapunov.versustest.presentation.quizlist.QuizListAdapter
import com.rkhrapunov.versustest.presentation.quizlist.QuizListFragment
import com.rkhrapunov.versustest.presentation.topsnackbar.TopSnackBarHelper
import com.rkhrapunov.versustest.presentation.winner.WinnerFragment
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import org.koin.android.ext.android.inject
import timber.log.Timber

@ExperimentalCoroutinesApi
@FlowPreview
@ExperimentalStdlibApi
class MainActivity : AppCompatActivity(), IMainContract.IMainView {

    private val mPresenter by inject<IMainContract.IMainPresenter>()
    var activityMainBinding: ActivityMainBinding? = null
    private val mGetQuizListInteractor by inject<GetQuizListInteractor>()
    private var mCurrentState: IRenderState? = null
    private var mErrorDialogFragment: ErrorDialogFragment? by weak()
    private val mTopSnackBarHelper by inject<TopSnackBarHelper>()

    companion object {
        const val QUIZ_LIST_EXTRA = "quiz_list_extra"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        activityMainBinding = binding
        setOnQueryTextListener()
        if (savedInstanceState == null) {
            mGetQuizListInteractor.getQuizList()
        }
        mPresenter.attachView(this, lifecycle)
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onResume() {
        super.onResume()
        mTopSnackBarHelper.setActivity(this)
    }

    override fun onPause() {
        super.onPause()
        mTopSnackBarHelper.reset()
    }

    override suspend fun render(renderState: IRenderState) {
        Timber.d("render(): renderState=$renderState")
        val fragment: Fragment = when (renderState) {
            is RenderState.QuizListState -> {
                showTopBar(true)
                QuizPagerFragment()
            }
            is RenderState.StatsListState -> {
                showTopBar(true)
                getQuizListFragment(false)
            }
            is RenderState.QuizItemDetailState -> {
                hideTopBar()
                QuizItemDetailFragment()
            }
            is RenderState.WinnerState -> {
                hideTopBar()
                WinnerFragment()
            }
            is RenderState.ErrorState -> {
                showTopBar(true)
                EmptyPagerFragment()
            }
            is RenderState.WinnerFinalState -> return
            else -> onQuizListRenderState()
        }
        replaceFragmentIfNecessary(fragment)
        mCurrentState = renderState
    }

    override fun renderErrorState(errorMsg: String) {
        Timber.d("errorMsg: $errorMsg")
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        val tag = ErrorDialogFragment::class.simpleName
        val prev = supportFragmentManager.findFragmentByTag(tag)
        prev?.let { fragmentTransaction.remove(it) }
        mErrorDialogFragment = ErrorDialogFragment()
        mErrorDialogFragment?.let {
            val bundle = Bundle()
            bundle.putString(ERROR_MSG_KEY, errorMsg)
            it.arguments = bundle
            it.show(fragmentTransaction, tag)
        }
    }

    private fun setOnQueryTextListener() {
        activityMainBinding?.searchView?.setOnQueryTextListener(
            object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String): Boolean {
                    Timber.d("onQueryTextSubmit()")
                    getQuizListAdapter(mCurrentState is RenderState.QuizListState)?.filter(query)
                    return true
                }

                override fun onQueryTextChange(newText: String): Boolean {
                    Timber.d("onQueryTextChange()")
                    getQuizListAdapter(mCurrentState is RenderState.QuizListState)?.filter(newText)
                    return true
                }
            }
        )
    }

    private fun hideTopBar() {
        activityMainBinding?.let {
            it.searchView.visibility = View.INVISIBLE
            it.menuId.visibility =  View.INVISIBLE
        }
    }

    private fun onQuizListRenderState(): QuizListFragment {
        showTopBar(true)
        return getQuizListFragment(true)
    }

    private fun showTopBar(showTopBar: Boolean) {
        activityMainBinding?.let {
            it.nextButtonFrameLayoutId.visibility = if (showTopBar) View.VISIBLE else View.GONE
            it.topBarSpace.visibility = if (showTopBar) View.VISIBLE else View.GONE
            it.searchView.visibility = if (showTopBar) View.VISIBLE else View.GONE
            it.topBarSpaceEnd.visibility = if (showTopBar) View.VISIBLE else View.GONE
            it.menuId.visibility = if (showTopBar) View.VISIBLE else View.GONE
        }
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
        replaceFragment(fragment)
    }

    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment, fragment.javaClass.simpleName)
            .commit()
    }

    private fun getQuizListAdapter(quizList: Boolean = true): QuizListAdapter<*>? = supportFragmentManager.findFragmentById(
        R.id.fragment_container)?.let {
            if (quizList) {
                (it as? QuizPagerFragment)?.getCurrentPageFragment()?.getAdapter()
            } else {
                (it as? QuizListFragment)?.getAdapter()
            }
        }

    fun onBackButtonClick(@Suppress("UNUSED_PARAMETER") view: View) = onBackPressed()

    @Suppress("UNUSED_PARAMETER")
    fun showPopupMenu(v: View) {
        activityMainBinding?.let {
            PopupMenu(this, it.anchorMenu, Gravity.END, 0, R.style.CustomPopupMenu).apply {
                setMenuItemClickListener(this)
                val fragmentTag = supportFragmentManager.findFragmentById(R.id.fragment_container)?.tag
                if (fragmentTag == QuizPagerFragment::class.simpleName /*&& mQuizList*/) {
                    menuInflater.inflate(R.menu.topbar_menu, menu)
                    setupSubmenu(menu, R.id.refresh)
                } else {
                    menuInflater.inflate(R.menu.top_bar_statistics_menu, menu)
                    setupSubmenu(menu, R.id.sort_by_results)
                    setupSubmenu(menu, R.id.sort_by_name)
                }
                show()
            }
        }
    }

    private fun setMenuItemClickListener(popupMenu: PopupMenu) {
        popupMenu.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.quiz_list_sort_asc -> {
                    getQuizListAdapter()?.sort()
                    true
                }
                R.id.quiz_list_sort_desc -> {
                    getQuizListAdapter()?.sort(false)
                    true
                }
                R.id.ascending_results -> {
                    getQuizListAdapter(false)?.sort()
                    true
                }
                R.id.descending_results -> {
                    getQuizListAdapter(false)?.sort(ascending = false)
                    true
                }
                R.id.ascending_name -> {
                    getQuizListAdapter(false)?.sort(sortByResults = false)
                    true
                }
                R.id.descending_name -> {
                    getQuizListAdapter(false)?.sort(ascending = false, sortByResults = false)
                    true
                }
                else -> false
            }
        }
    }

    private fun setupSubmenu(menu: Menu, menuItemId: Int) {
        val subMenuItem = menu.findItem(menuItemId)
        val subMenu = subMenuItem.subMenu
        val headerTitle = SpannableString(subMenuItem.title)
        headerTitle.setSpan(ForegroundColorSpan(ContextCompat.getColor(this@MainActivity, R.color.quizListItemSelectionColor)), 0, headerTitle.length, 0)
        subMenu.setHeaderTitle(headerTitle)
    }

    private fun cancelQuizAndGetQuizList() {
        mPresenter.cancelQuiz()
        mGetQuizListInteractor.getQuizList()
    }

    override fun onBackPressed() {
        val currentFragment = supportFragmentManager.findFragmentById(R.id.fragment_container)
        Timber.d("onBackPressed() current fragment: ${currentFragment?.tag}")
        when (currentFragment) {
            is QuizListFragment,
            is WinnerFragment -> cancelQuizAndGetQuizList()
            is QuizItemDetailFragment -> {
                cancelQuizAndGetQuizList()
                (currentFragment as? QuizItemDetailFragment)?.onBackPressed()
            }
            else -> super.onBackPressed()
        }
    }
}