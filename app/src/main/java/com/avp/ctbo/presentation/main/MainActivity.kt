package com.avp.ctbo.presentation.main

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
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.avp.core.data.ISuperCategory
import com.avp.core.domain.IRenderState
import com.avp.core.domain.RenderState
import com.avp.ctbo.R
import com.avp.ctbo.databinding.ActivityMainBinding
import com.avp.ctbo.presentation.about.AboutDialogFragment
import com.avp.ctbo.presentation.base.Constants.INVALID_VALUE
import com.avp.ctbo.presentation.base.IItemClickListener
import com.avp.ctbo.presentation.empty_pager.EmptyPagerFragment
import com.avp.ctbo.presentation.error.ErrorDialogFragment
import com.avp.ctbo.presentation.error.ErrorDialogFragment.Companion.ERROR_MSG_KEY
import com.avp.ctbo.presentation.quiz_detail.QuizItemDetailFragment
import com.avp.ctbo.presentation.quiz_pager.QuizPagerFragment
import com.avp.ctbo.presentation.base.QuizAdapter
import com.avp.ctbo.presentation.base.QuizDataType
import com.avp.ctbo.presentation.licenses.LicensesDialogFragment
import com.avp.ctbo.presentation.quizlist.QuizListFragment
import com.avp.ctbo.presentation.topsnackbar.TopSnackBarHelper
import com.avp.ctbo.presentation.winner.WinnerFragment
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
    private var mCurrentState: IRenderState? = null
    private var mAdapter: QuizAdapter<*>? = null
    private val mTopSnackBarHelper by inject<TopSnackBarHelper>()
    private var mFirstSuperCategorySelected = false
    @Volatile
    private var mShouldGetSuperCategoriesOnResume = true

    companion object {
        const val QUIZ_LIST_EXTRA = "quiz_list_extra"
    }

    @Suppress("UNCHECKED_CAST")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.recyclerView.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(this@MainActivity, LinearLayoutManager.HORIZONTAL, false)
            adapter = QuizAdapter<ISuperCategory>(
                mPresenter as IItemClickListener,
                this@MainActivity,
                QuizDataType.QUIZ_SUPER_CATEGORIES
            )
            mAdapter = adapter as? QuizAdapter<*>
        }
        activityMainBinding = binding
        setOnQueryTextListener()
        mShouldGetSuperCategoriesOnResume = if (savedInstanceState == null) {
            Timber.d("onCreate(): first time launch")
            true
        } else {
            mPresenter.getSuperCategoriesFromCache()?.let {
                Timber.d("onCreate(): trying to get super categories from cache")
                (mAdapter as? QuizAdapter<ISuperCategory>)?.updateData(it)
            } ?: run {
                Timber.d("onCreate(): failed to get super categories from cache, trying to get not from cache")
                mPresenter.getSuperCategories()
            }
            Timber.d("onCreate(): setting mShouldGetSuperCategoriesOnResume to false")
            false
        }
        mPresenter.attachView(this, lifecycle)
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onResume() {
        super.onResume()
        mTopSnackBarHelper.setActivity(this)
        Timber.d("onResume(): mShouldGetSuperCategoriesOnResume: $mShouldGetSuperCategoriesOnResume, current state: $mCurrentState")
        if (mCurrentState == null && mShouldGetSuperCategoriesOnResume) {
            Timber.d("onResume(): getSuperCategories")
            mPresenter.getSuperCategories()
        }
        Timber.d("onResume(): setting mShouldGetSuperCategoriesOnResume to true")
        mShouldGetSuperCategoriesOnResume = true
    }

    override fun onPause() {
        super.onPause()
        mTopSnackBarHelper.reset()
    }

    override fun onDestroy() {
        super.onDestroy()
        mFirstSuperCategorySelected = false
    }

    @Suppress("UNCHECKED_CAST")
    override suspend fun render(renderState: IRenderState) {
        Timber.d("render(): renderState=$renderState")
        val fragment: Fragment = when (renderState) {
            is RenderState.SuperCategoriesState -> {
                showSuperCategories(true)
                val superCategories = renderState.superCategories
                (mAdapter as? QuizAdapter<ISuperCategory>)?.updateData(superCategories)
                if (superCategories.isNotEmpty()) {
                    mPresenter.onInitialSuperCategory()
                }
                return
            }
            is RenderState.CategoriesState,
            is RenderState.QuizListState -> onCategoriesState()
            is RenderState.StatsListState -> onStatsListState()
            is RenderState.QuizItemDetailState -> {
                if (mCurrentState is RenderState.QuizItemDetailState) {
                    return
                }
                onQuizItemDetailState(renderState.quizDescription)
            }
            is RenderState.WinnerState -> onWinnerState()
            is RenderState.ErrorState -> onErrorState()
            is RenderState.WinnerFinalState -> {
                showTopBarAndSuperCategories(showTopBar = false, showSuperCategories = false)
                return
            }
            else -> onQuizListRenderState()
        }
        replaceFragmentIfNecessary(fragment)
        mCurrentState = renderState
    }

    private fun showTopBarAndSuperCategories(showTopBar: Boolean, showSuperCategories: Boolean) {
        showTopBar(showTopBar)
        showSuperCategories(showSuperCategories)
    }

    private fun onCategoriesState(): QuizPagerFragment {
        showTopBarAndSuperCategories(showTopBar = true, showSuperCategories = true)
        return QuizPagerFragment()
    }

    private fun onStatsListState(): QuizListFragment {
        showTopBarAndSuperCategories(showTopBar = true, showSuperCategories = false)
        return getQuizListFragment(false)
    }

    private fun onQuizItemDetailState(quizDescription: String): QuizItemDetailFragment {
        showTopBarAndSuperCategories(showTopBar = false, showSuperCategories = false)
        Timber.d("onQuizItemDetailState(): current state: $mCurrentState")
        mCurrentState?.let {
            if (it !is RenderState.QuizItemDetailState) {
                renderErrorState(quizDescription)
            }
        }
        return QuizItemDetailFragment()
    }

    private fun onWinnerState(): WinnerFragment {
        showTopBarAndSuperCategories(showTopBar = false, showSuperCategories = false)
        return WinnerFragment()
    }

    private fun onErrorState(): EmptyPagerFragment {
        showTopBarAndSuperCategories(showTopBar = true, showSuperCategories = false)
        return EmptyPagerFragment()
    }

    override fun onSuperCategoriesBack() = super.onBackPressed()

    override fun updateSuperCategoryOnError() = mPresenter.updateSuperCategoryOnError()

    override fun onSuperCategoryChanged(previousPosition: Int, currentPosition: Int) {
        activityMainBinding?.let { binding ->
            binding.recyclerView.layoutManager?.findViewByPosition(currentPosition)?.let {
                mPresenter.updateSuperCategoryPosition(currentPosition)
                it.setBackgroundResource(R.drawable.selected_super_category_background)
                it.isEnabled = false
                if (previousPosition != INVALID_VALUE) {
                    binding.recyclerView.layoutManager?.findViewByPosition(previousPosition)
                        ?.let { view ->
                            view.setBackgroundResource(R.drawable.custom_page_item_selector)
                            view.isEnabled = true
                        }
                }
            }
        }
    }

    override fun renderErrorState(errorMsg: String) {
        Timber.d("errorMsg: $errorMsg")
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        val tag = ErrorDialogFragment::class.simpleName
        val prev = supportFragmentManager.findFragmentByTag(tag)
        prev?.let { fragmentTransaction.remove(it) }
        val errorDialogFragment = ErrorDialogFragment()
        val bundle = Bundle()
        bundle.putString(ERROR_MSG_KEY, errorMsg)
        errorDialogFragment.arguments = bundle
        errorDialogFragment.show(fragmentTransaction, tag)
    }

    private fun setOnQueryTextListener() {
        activityMainBinding?.searchView?.setOnQueryTextListener(
            object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String): Boolean {
                    Timber.d("onQueryTextSubmit()")
                    getQuizListAdapter(mCurrentState is RenderState.SuperCategoriesState
                            || mCurrentState is RenderState.CategoriesState
                            || mCurrentState is RenderState.QuizListState)?.filter(query)
                    return true
                }

                override fun onQueryTextChange(newText: String): Boolean {
                    Timber.d("onQueryTextChange()")
                    getQuizListAdapter(mCurrentState is RenderState.SuperCategoriesState
                            || mCurrentState is RenderState.CategoriesState
                            ||mCurrentState is RenderState.QuizListState)?.filter(newText)
                    return true
                }
            }
        )
    }

    private fun showSuperCategories(showSuperCategories: Boolean) {
        activityMainBinding?.let {
            it.recyclerView.visibility = if (showSuperCategories) View.VISIBLE else View.GONE
        }
    }

    private fun onQuizListRenderState(): QuizListFragment {
        showTopBar(true)
        return getQuizListFragment(true)
    }

    @Suppress("SameParameterValue")
    private fun showTopBar(showTopBar: Boolean) {
        activityMainBinding?.let {
            it.nextButtonFrameLayoutId.visibility = if (showTopBar) View.VISIBLE else View.GONE
            it.topBarSpace.visibility = if (showTopBar) View.VISIBLE else View.GONE
            it.searchView.visibility = if (showTopBar) View.VISIBLE else View.GONE
            it.topBarSpaceEnd.visibility = if (showTopBar) View.VISIBLE else View.GONE
            it.menuId.visibility = if (showTopBar) View.VISIBLE else View.GONE
            it.topBarSpaceTop.visibility = if (showTopBar) View.VISIBLE else View.GONE
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

    private fun getQuizListAdapter(quizList: Boolean = true): QuizAdapter<*>? = supportFragmentManager.findFragmentById(
        R.id.fragment_container)?.let {
            if (quizList) {
                (it as? QuizPagerFragment)?.getCurrentPageFragment()?.getAdapter()
            } else {
                (it as? QuizListFragment)?.getAdapter()
            }
        }

    fun onBackButtonClick(@Suppress("UNUSED_PARAMETER") view: View) = onBackPressed()

    override fun onLowMemory() {
        super.onLowMemory()
        Glide.with(this).onLowMemory()
    }

    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)
        Glide.with(this).onTrimMemory(level)
    }

    @Suppress("UNUSED_PARAMETER")
    fun showPopupMenu(v: View) {
        activityMainBinding?.let {
            PopupMenu(this, it.anchorMenu, Gravity.END, 0, R.style.CustomPopupMenu).apply {
                setMenuItemClickListener(this)
                val fragmentTag = supportFragmentManager.findFragmentById(R.id.fragment_container)?.tag
                if (fragmentTag == QuizPagerFragment::class.simpleName) {
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
                R.id.licenses -> {
                    showLicensesDialog()
                    true
                }
                R.id.about -> {
                    showAboutDialog()
                    true
                }
                else -> false
            }
        }
    }

    private fun showLicensesDialog() {
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        val tag = LicensesDialogFragment::class.simpleName
        val prev = supportFragmentManager.findFragmentByTag(tag)
        prev?.let { fragmentTransaction.remove(it) }
        val licensesDialogFragment = LicensesDialogFragment()
        licensesDialogFragment.show(fragmentTransaction, tag)
    }

    private fun showAboutDialog() {
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        val tag = AboutDialogFragment::class.simpleName
        val prev = supportFragmentManager.findFragmentByTag(tag)
        prev?.let { fragmentTransaction.remove(it) }
        val aboutDialogFragment = AboutDialogFragment()
        aboutDialogFragment.show(fragmentTransaction, tag)
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
    }

    override fun onBackPressed() {
        val currentFragment = supportFragmentManager.findFragmentById(R.id.fragment_container)
        Timber.d("onBackPressed() current fragment: ${currentFragment?.tag}")
        when (currentFragment) {
            is QuizListFragment -> {
                cancelQuizAndGetQuizList()
                mPresenter.resetResultsStatsOption()
            }
            is WinnerFragment -> cancelQuizAndGetQuizList()
            is QuizItemDetailFragment -> {
                cancelQuizAndGetQuizList()
                (currentFragment as? QuizItemDetailFragment)?.onBackPressed()
            }
            is QuizPagerFragment -> (currentFragment as? QuizPagerFragment)?.let {
                if (mPresenter.getCurrentSuperCategoryPosition() == 0) {
                    super.onBackPressed()
                } else {
                    it.onBackPressed()
                }
            }
            else -> super.onBackPressed()
        }
    }
}