package com.rkhrapunov.versustest.presentation.error

import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.rkhrapunov.versustest.R
import com.rkhrapunov.versustest.databinding.ErrorModalBinding
import com.rkhrapunov.versustest.presentation.base.Constants.EMPTY_STRING
import com.rkhrapunov.versustest.presentation.base.getScreenHeight
import com.rkhrapunov.versustest.presentation.base.getScreenWidth
import com.rkhrapunov.versustest.presentation.main.MainActivity
import com.rkhrapunov.versustest.presentation.quiz_pager.QuizPagerFragment
import org.koin.android.ext.android.inject

class ErrorDialogFragment : DialogFragment(), IErrorDialogContract.IErrorDialogView {

    private val mPresenter by inject<IErrorDialogContract.IErrorDialogPresenter>()

    companion object {
        private const val SCREEN_WIDTH_PERCENTAGE = 0.7
        private const val SCREEN_HEIGHT_PERCENTAGE = 0.4
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
                              container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val binding = ErrorModalBinding.inflate(inflater, container, false)
        binding.presenter = mPresenter
        binding.errorText.text = arguments?.getString(ERROR_MSG_KEY, EMPTY_STRING) ?: EMPTY_STRING
        return binding.root
    }

    @ExperimentalStdlibApi
    override fun dismissRecognitionDialog() {
        dismiss()
        (activity as? MainActivity)?.let {
            val currentFragment = it.supportFragmentManager.findFragmentById(R.id.fragment_container)?.tag
            if (currentFragment != QuizPagerFragment::class.simpleName) {
                it.onBackPressed()
            }
        }
    }
}