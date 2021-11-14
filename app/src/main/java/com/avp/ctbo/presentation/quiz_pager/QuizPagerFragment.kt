package com.avp.ctbo.presentation.quiz_pager

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
import com.bumptech.glide.Glide
import com.avp.core.domain.IRenderState
import com.avp.core.domain.RenderState
import com.avp.ctbo.R
import com.avp.ctbo.databinding.FragmentQuizPagerBinding
import com.avp.ctbo.framework.helpers.CoroutineLauncherHelper
import com.avp.ctbo.presentation.base.Constants.EMPTY_STRING
import com.avp.ctbo.presentation.base.ImageLoader
import com.avp.ctbo.presentation.main.MainActivity
import com.avp.ctbo.presentation.quiz_page.QuizPageFragment
import com.avp.ctbo.presentation.quiz_page.ViewPagerFragmentStateAdapter
import kotlinx.coroutines.DelicateCoroutinesApi
import org.koin.android.ext.android.inject
import timber.log.Timber

@DelicateCoroutinesApi
class QuizPagerFragment : Fragment(), IQuizPagerContract.IQuizPagerView {

    private val mPresenter by inject<IQuizPagerContract.IQuizPagerPresenter>()
    private val mCoroutineLauncherHelper by inject<CoroutineLauncherHelper>()
    private var mBinding: FragmentQuizPagerBinding? = null
    private val mImageLoader by inject<ImageLoader>()
    private var mPositionChanged = false
    private var mPager: ViewPager2? = null

    fun getCurrentPageFragment() = activity?.supportFragmentManager?.findFragmentByTag("f${mPager?.currentItem}") as? QuizPageFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mPresenter.attachView(this, lifecycle)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val binding = FragmentQuizPagerBinding.inflate(inflater, container,  false)
        mBinding = binding
        return binding.root
    }

    override fun renderState(renderState: IRenderState) {
        Timber.d("renderState(): renderState: $renderState")
        mBinding?.let { binding ->
            activity?.let {
                getAdapter(renderState, it, it.supportFragmentManager)?.let { adapter ->
                    binding.pager.adapter = adapter
                    mPager = binding.pager
                    mPager?.setCurrentItem(mPresenter.getCurrentPagePosition(), false)
                    val pageIndicatorText = mPresenter.getPageIndicatorText()
                    if (binding.pageIndicator.text != pageIndicatorText) {
                        binding.pageIndicator.text = pageIndicatorText
                    }
                    registerOnPageChangeCallback(binding, adapter.itemCount)
                }
                loadBackgroundImg(renderState, binding.pager)
            }
        }
    }

    override fun onSuperCategoriesBack() {
        (activity as? MainActivity)?.onSuperCategoriesBack()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        Glide.with(this).onLowMemory()
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
                is RenderState.QuizListState -> {
                    if (mPresenter.getCurrentSuperCategory() == getString(R.string.all)) {
                        viewGroup.setBackgroundResource(R.color.pagerBackgroundColor)
                        EMPTY_STRING
                    } else {
                        mPresenter.getCurrentCategoryBackgroundUrl()
                    }
                }
                else -> {
                    Timber.w("Invalid state: $renderState")
                    return
                }
            }
            if (url.isNotEmpty()) {
                mCoroutineLauncherHelper.launchImgLoading {
                    mImageLoader.loadImage(it, url, viewGroup)
                }
            }
        }
    }

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
                    // leave this for possible future search issue
                    // getCurrentPageFragment()?.getAdapter()?.filter((activity as? MainActivity)?.activityMainBinding?.searchView?.query.toString())
                }
            }
        })
    }
}