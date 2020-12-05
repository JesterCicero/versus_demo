package com.rkhrapunov.versustest.presentation.quiz_detail

import android.content.res.Configuration.ORIENTATION_PORTRAIT
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewPropertyAnimator
import androidx.fragment.app.Fragment
import com.rkhrapunov.core.data.ChosenContestant
import com.rkhrapunov.core.domain.RenderState
import com.rkhrapunov.versustest.R
import com.rkhrapunov.versustest.databinding.ActivityMainBinding
import com.rkhrapunov.versustest.databinding.FragmentQuizItemDetailBinding
import com.rkhrapunov.versustest.framework.helpers.CoroutineLauncherHelper
import com.rkhrapunov.versustest.presentation.base.Constants.DISABLED_ALPHA
import com.rkhrapunov.versustest.presentation.base.Constants.EMPTY_STRING
import com.rkhrapunov.versustest.presentation.base.Constants.ENABLED_ALPHA
import com.rkhrapunov.versustest.presentation.base.Constants.SPACE_SYMBOL
import com.rkhrapunov.versustest.presentation.base.Constants.UNDERSCORE_SYMBOL
import com.rkhrapunov.versustest.presentation.base.ImageLoader
import com.rkhrapunov.versustest.presentation.base.capitalizeWords
import com.rkhrapunov.versustest.presentation.main.MainActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import org.koin.android.ext.android.inject
import timber.log.Timber

@ExperimentalStdlibApi
class QuizItemDetailFragment : Fragment(), IQuizItemDetailContract.IQuizItemDetailView {

    private val mPresenter by inject<IQuizItemDetailContract.IQuizItemDetailPresenter>()
    private val mCoroutineLauncherHelper by inject<CoroutineLauncherHelper>()
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
        binding.presenter = mPresenter
        val chosenContestant = mPresenter.retrieveChosenContestant()
        Timber.d("onCreateView(): chosenContestant=$chosenContestant")
        binding.firstImg.setBackgroundResource(if (chosenContestant == ChosenContestant.CHOSEN_FIRST) R.drawable.selected_contestant_shape else R.drawable.unselected_contestant_shape)
        binding.secondImg.setBackgroundResource(if (chosenContestant == ChosenContestant.CHOSEN_SECOND) R.drawable.selected_contestant_shape else R.drawable.unselected_contestant_shape)
        binding.nextButtonFrameLayoutId.isEnabled = chosenContestant == ChosenContestant.CHOSEN_FIRST || chosenContestant == ChosenContestant.CHOSEN_SECOND
        binding.nextButtonFrameLayoutId.alpha = if (binding.nextButtonFrameLayoutId.isEnabled) ENABLED_ALPHA else DISABLED_ALPHA
        mBinding = binding
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

    @FlowPreview
    @ExperimentalCoroutinesApi
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
                    it.nextButtonFrameLayoutId.visibility = View.INVISIBLE
                    it.nextButtonFrameLayoutId.isEnabled = false
                    it.nextButtonFrameLayoutId.alpha = DISABLED_ALPHA
                    it.questionButtonImg1FrameLayoutId.visibility = View.GONE
                    it.questionButtonImg2FrameLayoutId.visibility = View.GONE
                    it.tooltipNavUp1.visibility = View.GONE
                    it.tooltipText1.visibility = View.GONE
                    it.tooltipNavUp2.visibility = View.GONE
                    it.tooltipText2.visibility = View.GONE
                    mAnimationsStarted = true
                    animateImg(it, itemView, getTranslationCoordinate(activityBinding, it, chosenFirst, itemView), chosenFirst)
                    animateDescription(activityBinding, it, itemView.width, itemView.height, chosenFirst)
                }
            }
        }
    }

    override fun onItemChosen(chosenFirst: Boolean) {
        mBinding?.let {
            if (chosenFirst) {
                Timber.d("onItemChosen(): chosen first")
                it.firstImg.setBackgroundResource(R.drawable.selected_contestant_shape)
                it.secondImg.setBackgroundResource(R.drawable.unselected_contestant_shape)
                mPresenter.saveChosenContestant(ChosenContestant.CHOSEN_FIRST)
            } else {
                Timber.d("onItemChosen(): chosen second")
                it.firstImg.setBackgroundResource(R.drawable.unselected_contestant_shape)
                it.secondImg.setBackgroundResource(R.drawable.selected_contestant_shape)
                mPresenter.saveChosenContestant(ChosenContestant.CHOSEN_SECOND)
            }
            if (!it.nextButtonFrameLayoutId.isEnabled) {
                it.nextButtonFrameLayoutId.isEnabled = true
                it.nextButtonFrameLayoutId.alpha = ENABLED_ALPHA
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
            val translationPortraitValue = if (chosenFirst) {
                activityBinding.parent.height / 2 - fragmentQuizItemDetailBinding.firstImg.y - itemHalfHeight
            } else {
                fragmentQuizItemDetailBinding.secondImg.y - activityBinding.parent.height / 2 + itemHalfHeight
            }
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
                itemView.setBackgroundResource(R.drawable.unselected_contestant_shape)
                mImgReverseAnimation = animateView(itemView, -translationValue,
                    descriptionAnimation = false,
                    reverseAnimation = true
                ) {
                    val currentRound = mPresenter.getCurrentRound()
                    Timber.d("current round: $currentRound")
                    if (currentRound == 1) {
                        mPresenter.onItemClickFinished(chosenFirst)
                    } else {
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
                            binding.nextButtonFrameLayoutId.visibility = View.VISIBLE
                            itemView.isEnabled = true
                            itemView.visibility = View.VISIBLE
                            mPresenter.onItemClickFinished(chosenFirst)
                        }
                        mSecondImgRunnable?.let { mHandler.postDelayed(it, NEXT_ROUND_TIMEOUT) }
                    }
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

    private fun animateDescription(activityBinding: ActivityMainBinding, binding: FragmentQuizItemDetailBinding, itemViewWidth: Int, itemViewHeight: Int, chosenFirst: Boolean) {
        val descriptionViewToAnimate = binding.secondImgDescription
        val translationByCoordinateDescription = if (resources.configuration.orientation == ORIENTATION_PORTRAIT) {
            (activityBinding.parent.height / 2 - itemViewHeight / 2 - binding.secondImg.y) * TRANSLATION_DESCRIPTION_FACTOR
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
                binding.firstImgDescription.text = EMPTY_STRING
                binding.secondImgDescription.text = EMPTY_STRING
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
            viewPropertyAnimator.translationXBy(translationValue)
            // leave this code for possible future use
            //.translationYBy(if (descriptionAnimation) LANDSCAPE_DESCRIPTION_Y_VALUE else LANDSCAPE_IMG_Y_VALUE)
        }
        animator.scaleXBy(scaleFactor)
                .scaleYBy(scaleFactor)
                .setDuration(ANIM_DURATION_MS)
                .withEndAction { endAction() }
        viewPropertyAnimator.start()
        return viewPropertyAnimator
    }

    override fun renderQuizItemDetailState(renderState: RenderState.QuizItemDetailState, shouldLoadBackground: Boolean) {
        Timber.d("round: ${renderState.round}")
        mBinding?.let { binding ->
            binding.round = "${renderState.quizTitle.replace(UNDERSCORE_SYMBOL, SPACE_SYMBOL).capitalizeWords()}: ${renderState.round}"
            binding.firstDescription = renderState.firstContestant.name.replace(UNDERSCORE_SYMBOL, SPACE_SYMBOL).capitalizeWords()
            binding.secondDescription = renderState.secondContestant.name.replace(UNDERSCORE_SYMBOL, SPACE_SYMBOL).capitalizeWords()
            binding.firstContestantTooltipVisible = renderState.firstContestant.shortDescription.isNotEmpty()
            binding.questionButtonImg1FrameLayoutId.setOnClickListener {
                val tooltipNavUp1Visible = binding.tooltipNavUp1.visibility == View.VISIBLE
                binding.tooltipNavUp1.visibility = if (tooltipNavUp1Visible) View.GONE else View.VISIBLE
                val tooltipText1Visible = binding.tooltipText1.visibility == View.VISIBLE
                binding.tooltipText1.visibility = if (tooltipText1Visible) View.GONE else View.VISIBLE
                if (binding.tooltipText1.visibility == View.VISIBLE) {
                    binding.tooltipText1.text = renderState.firstContestant.shortDescription
                }
            }
            binding.secondContestantTooltipVisible = renderState.secondContestant.shortDescription.isNotEmpty()
            binding.questionButtonImg2FrameLayoutId.setOnClickListener {
                val tooltipNavUp2Visible = binding.tooltipNavUp2.visibility == View.VISIBLE
                binding.tooltipNavUp2.visibility =if (tooltipNavUp2Visible) View.GONE else View.VISIBLE
                val tooltipText2Visible = binding.tooltipText2.visibility == View.VISIBLE
                binding.tooltipText2.visibility = if (tooltipText2Visible) View.GONE else View.VISIBLE
                if (binding.tooltipText2.visibility == View.VISIBLE) {
                    binding.tooltipText2.text = renderState.secondContestant.shortDescription
                }
            }
            activity?.let {
                mCoroutineLauncherHelper.launch(Dispatchers.Main) {
                    mCoroutineLauncherHelper.launchImgLoading {
                        Timber.d("launch img loading coroutine, shouldLoadBackground: $shouldLoadBackground")
                        if (shouldLoadBackground) {
                            val backgroundUrl = mPresenter.getCurrentQuizBackgroundUrl()
                            Timber.d("background url: $backgroundUrl")
                            mImageLoader.loadImage(it, backgroundUrl, binding.fragmentContainer)
                        }
                        mImageLoader.loadImage(it, renderState.firstContestant.url, binding.firstImageId)
                        mImageLoader.loadImage(it, renderState.secondContestant.url, binding.secondImageId)
                    }
                }
            }
        }
    }

    fun onBackPressed() = mPresenter.saveChosenContestant(ChosenContestant.UNKNOWN)

    companion object {
        private const val ANIM_DURATION_MS = 300L
        private const val PORTRAIT_SCALE_FACTOR = 0.1F
        private const val LANDSCAPE_SCALE_FACTOR = 0.1F
        private const val CHOSEN_CONTESTANT_TIMEOUT = 750L
        private const val NEXT_ROUND_TIMEOUT = 30L
        private const val TRANSLATION_DESCRIPTION_FACTOR = 0.9F
        // leave these constants for possible future use
//        private const val LANDSCAPE_DESCRIPTION_Y_VALUE = -30F
//        private const val LANDSCAPE_IMG_Y_VALUE = -50F
    }
}