package com.rkhrapunov.versustest.presentation.quiz_detail

import android.content.res.Configuration.ORIENTATION_PORTRAIT
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewPropertyAnimator
import androidx.fragment.app.Fragment
import com.rkhrapunov.core.domain.RenderState
import com.rkhrapunov.versustest.R
import com.rkhrapunov.versustest.databinding.ActivityMainBinding
import com.rkhrapunov.versustest.databinding.FragmentQuizItemDetailBinding
import com.rkhrapunov.versustest.presentation.base.Constants.SPACE_SYMBOL
import com.rkhrapunov.versustest.presentation.base.Constants.UNDERSCORE_SYMBOL
import com.rkhrapunov.versustest.presentation.base.ImageLoader
import com.rkhrapunov.versustest.presentation.base.capitalizeWords
import com.rkhrapunov.versustest.presentation.main.MainActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.android.ext.android.inject
import timber.log.Timber

@ExperimentalStdlibApi
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
    private var mAnimationsStarted = false
    private var mImgAnimationEndAction: () -> Unit = {}
    private var mDescriptionAnimationEndAction: () -> Unit = {}

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

    override fun onResume() {
        super.onResume()
        if (mAnimationsStarted) {
            mImgAnimationEndAction.invoke()
            mDescriptionAnimationEndAction.invoke()
        }
    }

    override fun onItemClicked(chosenFirst: Boolean) {
        mBinding?.let {
            (activity as? MainActivity)?.let { activity ->
                activity.activityMainBinding?.let { activityBinding ->
                    val itemView = if (chosenFirst) it.firstImg else it.secondImg
                    itemView.isEnabled = false
                    it.vsTextView.visibility = View.INVISIBLE
                    it.roundDescription.visibility = View.INVISIBLE
                    it.firstImgDescription.visibility = View.INVISIBLE
                    it.secondImgDescription.visibility = View.INVISIBLE
                    mAnimationsStarted = true
                    animateImg(it, itemView, getTranslationCoordinate(activityBinding, it, chosenFirst, itemView), chosenFirst)
                    animateDescription(it, itemView.width, itemView.height, chosenFirst)
                }
            }
        }
    }

    private fun getTranslationCoordinate(activityBinding: ActivityMainBinding,
                                         fragmentQuizItemDetailBinding: FragmentQuizItemDetailBinding,
                                         chosenFirst: Boolean,
                                         itemView: View): Float {
        val itemHalfHeight = itemView.height / 2
        val itemHalfWidth = itemView.width / 2
        if (chosenFirst) {
            fragmentQuizItemDetailBinding.secondImg.visibility = View.INVISIBLE
        } else {
            fragmentQuizItemDetailBinding.firstImg.visibility = View.INVISIBLE
        }
        val translationValue = if (resources.configuration.orientation == ORIENTATION_PORTRAIT) {
            val translationPortraitValue = (activityBinding.parent.height / 2 - itemView.height + if (chosenFirst) -itemHalfHeight else itemHalfHeight).toFloat()
            Timber.d("getTranslationCoordinate(): chosenFirstTranslationPortraitValue: $translationPortraitValue")
            translationPortraitValue
        } else {
            val translationLandscapeValue = (itemHalfWidth + fragmentQuizItemDetailBinding.vsTextView.width / 2 + (fragmentQuizItemDetailBinding.vsTextView.x - (fragmentQuizItemDetailBinding.firstImg.x + fragmentQuizItemDetailBinding.firstImg.width)))
            Timber.d("getTranslationCoordinate(): chosenFirstTranslationLandscapeValue: $translationLandscapeValue")
            translationLandscapeValue
        }
        return if (chosenFirst) translationValue else -translationValue
    }

    private fun animateImg(binding: FragmentQuizItemDetailBinding, itemView: View, translationValue: Float, chosenFirst: Boolean) {
        mImgAnimationEndAction = {
            mFirstImgRunnable = Runnable {
                mAnimationsStarted = false
                Timber.d("First img postDelayed()")
                itemView.visibility = View.INVISIBLE
                binding.firstImageId.setImageResource(R.drawable.empty_drawable)
                binding.secondImageId.setImageResource(R.drawable.empty_drawable)
                mImgReverseAnimation = animateView(itemView, -translationValue,
                    descriptionAnimation = false,
                    reverseAnimation = true
                ) {
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
        mImgAnimation = animateView(itemView, translationValue,
            descriptionAnimation = false,
            reverseAnimation = false,
            endAction = mImgAnimationEndAction
        )
    }

    private fun animateDescription(binding: FragmentQuizItemDetailBinding, itemViewWidth: Int, itemViewHeight: Int, chosenFirst: Boolean) {
        val descriptionViewToAnimate = binding.secondImgDescription
        val translationByCoordinateDescription = if (resources.configuration.orientation == ORIENTATION_PORTRAIT) {
            (-itemViewHeight / 2 - (binding.firstImg.y - binding.fragmentContainer.y)) * TRANSLATION_DESCRIPTION_FACTOR
        } else {
            val translationValue = itemViewWidth / 2 + binding.vsTextView.width / 2 + (binding.vsTextView.x - (binding.firstImg.x + binding.firstImg.width))
            Timber.d("animateDescription(): translationValue=$translationValue")
            -translationValue
        }
        mDescriptionAnimationEndAction = {
            descriptionViewToAnimate.visibility = View.VISIBLE
            descriptionViewToAnimate.text = if (chosenFirst) binding.firstImgDescription.text else binding.secondImgDescription.text
            mFirstDescriptionRunnable = Runnable {
                mAnimationsStarted = false
                Timber.d("First description postDelayed()")
                descriptionViewToAnimate.visibility = View.INVISIBLE
                mDescriptionReverseAnimation = animateView(descriptionViewToAnimate, -translationByCoordinateDescription,
                    descriptionAnimation = true,
                    reverseAnimation = true
                ) {
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
        mDescriptionAnimation = animateView(descriptionViewToAnimate, translationByCoordinateDescription,
            descriptionAnimation = true,
            reverseAnimation = false,
            endAction = mDescriptionAnimationEndAction
        )
    }

    private fun animateView(viewToAnimate: View, translationValue: Float, descriptionAnimation: Boolean,
                            reverseAnimation: Boolean, endAction: () -> Unit): ViewPropertyAnimator {
        val scaleFactorDependingOnOrientation = if (resources.configuration.orientation == ORIENTATION_PORTRAIT) PORTRAIT_SCALE_FACTOR else LANDSCAPE_SCALE_FACTOR
        val scaleFactor = if (reverseAnimation) -scaleFactorDependingOnOrientation else scaleFactorDependingOnOrientation
        val viewPropertyAnimator = viewToAnimate.animate()
        val animator = if (resources.configuration.orientation == ORIENTATION_PORTRAIT) {
            viewPropertyAnimator.translationYBy(translationValue)
        } else {
            viewPropertyAnimator.translationXBy(translationValue).translationYBy(if (descriptionAnimation) LANDSCAPE_DESCRIPTION_Y_VALUE else LANDSCAPE_IMG_Y_VALUE)
        }
        animator.scaleXBy(scaleFactor)
                .scaleYBy(scaleFactor)
                .setDuration(ANIM_DURATION_MS)
                .withEndAction { endAction() }
        viewPropertyAnimator.start()
        return viewPropertyAnimator
    }

    override suspend fun renderQuitItemDetailState(renderState: RenderState.QuizItemDetailState) {
        Timber.d("round: ${renderState.round}")
        mBinding?.let { binding ->
            binding.round = "${renderState.quizTitle.replace(UNDERSCORE_SYMBOL, SPACE_SYMBOL).capitalizeWords()}: ${renderState.round}"
            binding.firstDescription = renderState.firstContestant.name.replace(UNDERSCORE_SYMBOL, SPACE_SYMBOL).capitalizeWords()
            binding.secondDescription = renderState.secondContestant.name.replace(UNDERSCORE_SYMBOL, SPACE_SYMBOL).capitalizeWords()
            withContext(Dispatchers.IO) {
                activity?.let {
                    mImageLoader.loadImage(it, renderState.firstContestant.url, binding.firstImageId)
                    mImageLoader.loadImage(it, renderState.secondContestant.url, binding.secondImageId)
                }
            }
        }
    }

    companion object {
        private const val ANIM_DURATION_MS = 300L
        private const val PORTRAIT_SCALE_FACTOR = 0.2F
        private const val LANDSCAPE_SCALE_FACTOR = 0.1F
        private const val CHOSEN_CONTESTANT_TIMEOUT = 750L
        private const val NEXT_ROUND_TIMEOUT = 30L
        private const val TRANSLATION_DESCRIPTION_FACTOR = 0.9F
        private const val LANDSCAPE_DESCRIPTION_Y_VALUE = -50F
        private const val LANDSCAPE_IMG_Y_VALUE = -80F
    }
}