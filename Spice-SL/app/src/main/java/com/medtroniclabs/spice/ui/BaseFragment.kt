package com.medtroniclabs.spice.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import androidx.fragment.app.replace

open class BaseFragment : Fragment() {

    fun showProgress() {
        if (activity is BaseActivity) {
            (activity as BaseActivity?)?.showLoading()
        }
    }

    fun hideProgress() {
        if (activity is BaseActivity) {
            (activity as BaseActivity?)?.hideLoading()
        }
    }

    inline fun <reified fragment : Fragment> replaceFragmentInId(
        id: Int,
        bundle: Bundle? = null,
        tag: String? = null
    ) {
        childFragmentManager.commit {
            setReorderingAllowed(true)
            replace<fragment>(
                id,
                args = bundle,
                tag = tag
            )
        }
    }

}