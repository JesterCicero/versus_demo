package com.rkhrapunov.versustest.presentation.quiz_detail

import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewPropertyAnimator
import androidx.fragment.app.Fragment
import com.rkhrapunov.core.domain.RenderState
import com.rkhrapunov.versustest.R
import com.rkhrapunov.versustest.databinding.FragmentQuizItemDetailBinding
import com.rkhrapunov.versustest.presentation.base.ImageLoader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.android.ext.android.inject
import timber.log.Timber

class QuizItemDetailFragment : Fragment(), IQuizItemDetailContract.IQuizItemDetailView {

    private val mPresenter by inject<IQuizItemDetailContract.IQuizItemDetailPresenter>()
    private var mBinding: FragmentQuizItemDetailBinding? = null
    private val mImageLoader by inject<ImageLoader>()
    private val mHandler = Handler()
    private var mFirstImgRunnable: Runnable? = null
    private var mSecondImgRunnable: Runnable? = null
    private var mFirstDescriptionRunnable: Runnable? = null
    private var mSecondDescriptionRunnable: Runnable? = null
    private var mImgAnimation: ViewPropertyAnimator? = null
    private var mImgReverseAnimation: ViewPropertyAnimator? = null
    private var mDescriptionAnimation: ViewPropertyAnimator? = null
    private var mDescriptionReverseAnimation: ViewPropertyAnimator? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mPresenter.attachView(this, lifecycle)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val binding = FragmentQuizItemDetailBinding.inflate(inflater, container, false)
        mBinding = binding
        mBinding?.presenter = mPresenter
        return binding.root
    }

    override fun onPause() {
        super.onPause()
        mBinding?.let {
            it.firstImg.clearAnimation()
            it.firstImgDescription.clearAnimation()
            it.secondImg.clearAnimation()
            it.secondImgDescription.clearAnimation()
        }
        mFirstImgRunnable?.let { mHandler.removeCallbacks(it) }
        mSecondImgRunnable?.let { mHandler.removeCallbacks(it) }
        mFirstDescriptionRunnable?.let { mHandler.removeCallbacks(it) }
        mSecondDescriptionRunnable?.let { mHandler.removeCallbacks(it) }
        mImgAnimation?.cancel()
        mImgReverseAnimation?.cancel()
        mDescriptionAnimation?.cancel()
        mDescriptionReverseAnimation?.cancel()
    }
    
    override fun onItemClicked(chosenFirst: Boolean) {
        mBinding?.let {
            val itemView = if (chosenFirst) it.firstImg else it.secondImg
            val translationByYCoordinate = if (chosenFirst) {
                it.secondImg.visibility = View.INVISIBLE
                itemView.height / 2 - it.firstImg.y + it.firstImgDescription.y
            } else {
                it.firstImg.visibility = View.INVISIBLE
                -itemView.height / 2 - it.firstImg.y + it.fragmentContainer.y
            }
            itemView.isEnabled = false
            it.vsTextView.visibility = View.INVISIBLE
            it.roundDescription.visibility = View.INVISIBLE
            it.firstImgDescription.visibility = View.INVISIBLE
            it.secondImgDescription.visibility = View.INVISIBLE
            animateImg(it, itemView, translationByYCoordinate, chosenFirst)
            animateDescription(it, itemView.height, chosenFirst)
        }
    }

    private fun animateImg(binding: FragmentQuizItemDetailBinding, itemView: View, translationByYCoordinate: Float, chosenFirst: Boolean) {
        mImgAnimation = animateView(itemView, translationByYCoordinate, false) {
            mFirstImgRunnable = Runnable {
                Timber.d("First img postDelayed()")
                itemView.visibility = View.INVISIBLE
                binding.firstImageId.setImageResource(R.drawable.empty_drawable)
                binding.secondImageId.setImageResource(R.drawable.empty_drawable)
                mImgReverseAnimation = animateView(itemView, -translationByYCoordinate, true) {
                    mPresenter.onItemClickFinished(chosenFirst)
                    mSecondImgRunnable = Runnable {
                        Timber.d("Second img postDelayed()")
                        if (chosenFirst) {
                            binding.secondImg.visibility = View.VISIBLE
                            binding.secondImgDescription.visibility = View.VISIBLE
                        } else {
                            binding.firstImg.visibility = View.VISIBLE
                            binding.firstImgDescription.visibility = View.VISIBLE
                        }
                        binding.vsTextView.visibility = View.VISIBLE
                        binding.roundDescription.visibility = View.VISIBLE
                        itemView.isEnabled = true
                        itemView.visibility = View.VISIBLE
                    }
                    mSecondImgRunnable?.let { mHandler.postDelayed(it, NEXT_ROUND_TIMEOUT) }
                }
            }
            mFirstImgRunnable?.let { mHandler.postDelayed(it, CHOSEN_CONTESTANT_TIMEOUT) }
        }
    }

    private fun animateDescription(binding: FragmentQuizItemDetailBinding, itemViewHeight: Int, chosenFirst: Boolean) {
        val descriptionViewToAnimate = binding.secondImgDescription
        val translationByYCoordinateDescription = (-itemViewHeight / 2 - (binding.firstImg.y - binding.fragmentContainer.y)) * TRANSLATION_DESCRIPTION_FACTOR
        mDescriptionAnimation = animateView(descriptionViewToAnimate, translationByYCoordinateDescription, false) {
            descriptionViewToAnimate.visibility = View.VISIBLE
            descriptionViewToAnimate.text = if (chosenFirst) binding.firstImgDescription.text else binding.secondImgDescription.text
            mFirstDescriptionRunnable = Runnable {
                Timber.d("First description postDelayed()")
                descriptionViewToAnimate.visibility = View.INVISIBLE
                mDescriptionReverseAnimation = animateView(descriptionViewToAnimate, -translationByYCoordinateDescription, true) {
                    mSecondDescriptionRunnable = Runnable {
                        Timber.d("Second description postDelayed()")
                        binding.firstImgDescription.visibility = View.VISIBLE
                        binding.secondImgDescription.visibility = View.VISIBLE
                    }
                    mSecondDescriptionRunnable?.let { mHandler.postDelayed(it, NEXT_ROUND_TIMEOUT) }
                }
            }
            mFirstDescriptionRunnable?.let { mHandler.postDelayed(it, CHOSEN_CONTESTANT_TIMEOUT) }
        }
    }

    private fun animateView(viewToAnimate: View, translationValue: Float,
                            reverseAnimation: Boolean, endAction: () -> Unit): ViewPropertyAnimator {
        val scaleFactor = if (reverseAnimation) -SCALE_FACTOR else SCALE_FACTOR
        val viewPropertyAnimator = viewToAnimate.animate()
            .translationYBy(translationValue)
            .scaleXBy(scaleFactor)
            .scaleYBy(scaleFactor)
            .setDuration(ANIM_DURATION_MS)
            .withEndAction { endAction() }
        viewPropertyAnimator.start()
        return viewPropertyAnimator
    }

    override suspend fun renderQuitItemDetailState(renderState: RenderState.QuizItemDetailState) {
        Timber.d("round: ${renderState.round}")
        mBinding?.let {
            it.quizItemDetailState = renderState
            withContext(Dispatchers.IO) {
                mImageLoader.loadImage(this@QuizItemDetailFragment, renderState.firstContestant.url, it.firstImageId)
                mImageLoader.loadImage(this@QuizItemDetailFragment, renderState.secondContestant.url, it.secondImageId)
            }
        }
    }

    companion object {
        private const val ANIM_DURATION_MS = 750L
        private const val SCALE_FACTOR = 0.2F
        private const val CHOSEN_CONTESTANT_TIMEOUT = 2000L
        private const val NEXT_ROUND_TIMEOUT = 20L
        private const val TRANSLATION_DESCRIPTION_FACTOR = 0.8F
    }
}