package com.rkhrapunov.versustest.presentation.about

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.rkhrapunov.versustest.R
import com.rkhrapunov.versustest.databinding.AboutModalBinding
import com.rkhrapunov.versustest.presentation.base.getScreenHeight
import com.rkhrapunov.versustest.presentation.base.getScreenWidth
import org.koin.android.ext.android.inject

class AboutDialogFragment : DialogFragment(), IAboutDialogContract.IAboutDialogView {

    private val mPresenter by inject<IAboutDialogContract.IAboutDialogPresenter>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NO_TITLE, R.style.DialogTheme_transparent)
        mPresenter.attachView(this, lifecycle)
    }

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val binding = AboutModalBinding.inflate(inflater, container, false)
        binding.presenter = mPresenter
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        activity?.let {
            val params = dialog?.window?.attributes
            params?.width = getScreenWidth(it)
            params?.height = getScreenHeight(it)
            dialog?.window?.attributes = params
        }
    }

    override fun dismissDialog() = dismiss()
}