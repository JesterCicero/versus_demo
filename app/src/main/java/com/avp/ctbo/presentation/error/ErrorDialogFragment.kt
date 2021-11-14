package com.avp.ctbo.presentation.error

import android.content.DialogInterface
import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.avp.ctbo.R
import com.avp.ctbo.databinding.ErrorModalBinding
import com.avp.ctbo.presentation.base.Constants.EMPTY_STRING
import com.avp.ctbo.presentation.base.getScreenHeight
import com.avp.ctbo.presentation.base.getScreenWidth
import com.avp.ctbo.presentation.main.MainActivity
import com.avp.ctbo.presentation.quiz_detail.QuizItemDetailFragment
import com.avp.ctbo.presentation.quiz_pager.QuizPagerFragment
import kotlinx.coroutines.DelicateCoroutinesApi
import org.koin.android.ext.android.inject

@DelicateCoroutinesApi
class ErrorDialogFragment : DialogFragment(), IErrorDialogContract.IErrorDialogView {

    private val mPresenter by inject<IErrorDialogContract.IErrorDialogPresenter>()
    private var mCurrentErrorMsg = EMPTY_STRING

    companion object {
        private const val SCREEN_WIDTH_PERCENTAGE = 0.8
        private const val SCREEN_HEIGHT_PERCENTAGE = 0.5
        const val ERROR_MSG_KEY = "error_msg"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NO_TITLE, R.style.DialogTheme_transparent)
        mPresenter.attachView(this, lifecycle)
    }

    override fun onResume() {
        super.onResume()
        activity?.let {
            val params = dialog?.window?.attributes
            params?.width = (getScreenWidth(it) * if (
                resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) SCREEN_WIDTH_PERCENTAGE else SCREEN_HEIGHT_PERCENTAGE).toInt()
            params?.height = (getScreenHeight(it) * if (
                resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) SCREEN_HEIGHT_PERCENTAGE else SCREEN_WIDTH_PERCENTAGE).toInt()
            dialog?.window?.attributes = params
        }
    }

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?, savedInstanceState: Bundle?): View {
        val binding = ErrorModalBinding.inflate(inflater, container, false)
        binding.presenter = mPresenter
        mCurrentErrorMsg = arguments?.getString(ERROR_MSG_KEY, EMPTY_STRING) ?: EMPTY_STRING
        binding.errorText.text = mCurrentErrorMsg
        return binding.root
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        if (mCurrentErrorMsg == getString(R.string.unable_to_get_categories)) {
            (activity as? MainActivity)?.updateSuperCategoryOnError()
        }
    }

    override fun dismissDialog() {
        dismiss()
        (activity as? MainActivity)?.let {
            val currentFragment = it.supportFragmentManager.findFragmentById(R.id.fragment_container)?.tag
            if (currentFragment != QuizPagerFragment::class.simpleName
                && currentFragment != QuizItemDetailFragment::class.simpleName) {
                it.onBackPressed()
            }
        }
    }
}