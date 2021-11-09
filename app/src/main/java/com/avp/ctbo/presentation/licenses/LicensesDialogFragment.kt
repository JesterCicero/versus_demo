package com.avp.ctbo.presentation.licenses

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.avp.ctbo.R
import com.avp.ctbo.databinding.LicensesModalBinding
import com.avp.ctbo.presentation.base.getScreenHeight
import com.avp.ctbo.presentation.base.getScreenWidth
import com.avp.ctbo.presentation.main.MainActivity
import kotlinx.coroutines.DelicateCoroutinesApi
import org.koin.android.ext.android.inject

@DelicateCoroutinesApi
class LicensesDialogFragment : DialogFragment(), ILicensesDialogContract.ILicensesDialogView {

    private val mPresenter by inject<ILicensesDialogContract.ILicensesDialogPresenter>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NO_TITLE, R.style.DialogTheme_transparent)
        mPresenter.attachView(this, lifecycle)
    }

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?, savedInstanceState: Bundle?): View {
        val binding = LicensesModalBinding.inflate(inflater, container, false)
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

    override fun dismissDialog() {
        dismiss()
        (activity as? MainActivity)?.performOnResumeActions(onDialogDismiss = true)
    }
}