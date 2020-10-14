package com.rkhrapunov.versustest.presentation.quiz_pager

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.viewpager2.widget.ViewPager2
import androidx.viewpager2.widget.ViewPager2.SCROLL_STATE_IDLE
import com.rkhrapunov.core.domain.IRenderState
import com.rkhrapunov.core.domain.RenderState
import com.rkhrapunov.versustest.databinding.FragmentQuizPagerBinding
import com.rkhrapunov.versustest.framework.helpers.CoroutineLauncherHelper
import com.rkhrapunov.versustest.presentation.base.ImageLoader
import com.rkhrapunov.versustest.presentation.main.MainActivity
import com.rkhrapunov.versustest.presentation.quiz_page.QuizPageFragment
import com.rkhrapunov.versustest.presentation.quiz_page.ViewPagerFragmentStateAdapter
import kotlinx.android.synthetic.main.fragment_quiz_pager.pager
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import org.koin.android.ext.android.inject
import timber.log.Timber

@ExperimentalStdlibApi
class QuizPagerFragment : Fragment(), IQuizPagerContract.IQuizPagerView {

    private val mPresenter by inject<IQuizPagerContract.IQuizPagerPresenter>()
    private val mCoroutineLauncherHelper by inject<CoroutineLauncherHelper>()
    private var mBinding: FragmentQuizPagerBinding? = null
    private val mImageLoader by inject<ImageLoader>()
    private var mPositionChanged = false
    private var mPager: ViewPager2? = null

    fun getCurrentPageFragment() = fragmentManager?.findFragmentByTag("f${mPager?.currentItem}") as? QuizPageFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mPresenter.attachView(this, lifecycle)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val binding = FragmentQuizPagerBinding.inflate(inflater, container,  false)
        mBinding = binding
        return binding.root
    }

    @ExperimentalCoroutinesApi
    @FlowPreview
    override fun renderState(renderState: IRenderState) {
        Timber.d("renderState(): renderState: $renderState")
        mBinding?.let { binding ->
            fragmentManager?.let { fm ->
                activity?.let {
                    getAdapter(renderState, it, fm)?.let { adapter ->
                        binding.pager.adapter = adapter
                        mPager = binding.pager
                        mPager?.setCurrentItem(mPresenter.getCurrentPagePosition(), false)
                        val pageIndicatorText = mPresenter.getPageIndicatorText()
                        if (binding.pageIndicator.text != pageIndicatorText) {
                            binding.pageIndicator.text = pageIndicatorText
                        }
                        registerOnPageChangeCallback(binding, adapter.itemCount)
                    }
                    loadBackgroundImg(renderState, it.pager)
                }
            }
        }
    }

    @FlowPreview
    @ExperimentalCoroutinesApi
    override fun onSuperCategoriesBack() {
        (activity as? MainActivity)?.onSuperCategoriesBack()
    }

    fun onBackPressed() = mPresenter.onBackPressed()

    private fun getAdapter(renderState: IRenderState, context: Context, fm: FragmentManager): ViewPagerFragmentStateAdapter<*>? {
        return when (renderState) {
            is RenderState.SuperCategoriesState -> ViewPagerFragmentStateAdapter(context, renderState.superCategories, fm, lifecycle)
            is RenderState.CategoriesState -> ViewPagerFragmentStateAdapter(context, renderState.categories, fm, lifecycle)
            is RenderState.QuizListState -> ViewPagerFragmentStateAdapter(context, renderState.allContestants, fm, lifecycle)
            else -> null
        }
    }

    private fun loadBackgroundImg(renderState: IRenderState, viewGroup: ViewGroup) {
        activity?.let {
            val url = when (renderState) {
                is RenderState.CategoriesState -> mPresenter.getCurrentSuperCategoryBackgroundUrl()
                is RenderState.QuizListState -> mPresenter.getCurrentCategoryBackgroundUrl()
                else -> {
                    Timber.w("Invalid state: $renderState")
                    return
                }
            }
            mCoroutineLauncherHelper.launchImgLoading {
                mImageLoader.loadImage(it, url, viewGroup)
            }
        }
    }

    @FlowPreview
    @ExperimentalCoroutinesApi
    private fun registerOnPageChangeCallback(binding: FragmentQuizPagerBinding, itemCount: Int) {
        mPager?.registerOnPageChangeCallback(object: ViewPager2.OnPageChangeCallback() {
            @SuppressLint("SetTextI18n")
            override fun onPageSelected(position: Int) {
                mPositionChanged = mPresenter.getCurrentPagePosition() != position
                mPresenter.saveCurrentPagePosition(position)
                val pageIndicatorText = "${position + 1}/$itemCount"
                binding.pageIndicator.text = pageIndicatorText
                mPresenter.savePageIndicatorText(pageIndicatorText)
            }

            override fun onPageScrollStateChanged(state: Int) {
                super.onPageScrollStateChanged(state)
                if (state == SCROLL_STATE_IDLE && mPositionChanged) {
                    mPositionChanged = false
                    getCurrentPageFragment()?.getAdapter()?.filter((activity as? MainActivity)?.activityMainBinding?.searchView?.query.toString())
                }
            }
        })
    }
}