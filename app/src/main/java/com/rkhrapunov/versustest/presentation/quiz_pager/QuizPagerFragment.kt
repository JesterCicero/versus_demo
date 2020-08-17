package com.rkhrapunov.versustest.presentation.quiz_pager

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import androidx.viewpager2.widget.ViewPager2.SCROLL_STATE_IDLE
import com.rkhrapunov.core.domain.RenderState
import com.rkhrapunov.versustest.databinding.FragmentQuizPagerBinding
import com.rkhrapunov.versustest.presentation.main.MainActivity
import com.rkhrapunov.versustest.presentation.quiz_page.QuizPageFragment
import com.rkhrapunov.versustest.presentation.quiz_page.ViewPagerFragmentStateAdapter
import org.koin.android.ext.android.inject

@ExperimentalStdlibApi
class QuizPagerFragment : Fragment(), IQuizPagerContract.IQuizPagerView {

    private val mPresenter by inject<IQuizPagerContract.IQuizPagerPresenter>()
    private var mBinding: FragmentQuizPagerBinding? = null
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

    override fun renderQuizListState(renderState: RenderState.QuizListState) {
        mBinding?.let { binding ->
            fragmentManager?.let { fm ->
                activity?.let {
                    val adapter = ViewPagerFragmentStateAdapter(it, renderState.allContestants, fm, lifecycle)
                    binding.pager.adapter = adapter
                    mPager = binding.pager
                    mPager?.setCurrentItem(mPresenter.getCurrentPagePosition(), false)
                    val pageIndicatorText = mPresenter.getPageIndicatorText()
                    if (binding.pageIndicator.text != pageIndicatorText) {
                        binding.pageIndicator.text = pageIndicatorText
                    }
                    registerOnPageChangeCallback(binding, adapter.itemCount)
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
                    getCurrentPageFragment()?.getAdapter()?.filter((activity as? MainActivity)?.activityMainBinding?.searchView?.query.toString())
                }
            }
        })
    }
}