package com.rkhrapunov.versustest.presentation.empty_pager

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.rkhrapunov.core.domain.RenderState
import com.rkhrapunov.versustest.R
import com.rkhrapunov.versustest.databinding.EmptyPagerBinding
import org.koin.android.ext.android.inject

@ExperimentalStdlibApi
class EmptyPagerFragment : Fragment(), IEmptyPagerContract.IEmptyPagerView {

    private val mPresenter by inject<IEmptyPagerContract.IEmptyPagerPresenter>()
    private var mBinding: EmptyPagerBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mPresenter.attachView(this, lifecycle)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val binding = EmptyPagerBinding.inflate(inflater, container, false)
        mBinding = binding
        return binding.root
    }

    override fun showToast() {
        Toast.makeText(activity,getString(R.string.unable_to_get_quiz_list), Toast.LENGTH_SHORT).show()
    }

    override suspend fun renderErrorState(renderState: RenderState.ErrorState) {
        mBinding?.let {
            it.presenter = mPresenter
            it.errorText.text = renderState.errorMsg
        }
    }
}