package com.medtroniclabs.spice.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import androidx.fragment.app.replace
import com.medtroniclabs.spice.R
import com.medtroniclabs.spice.network.utils.ConnectivityManager
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
open class BaseFragment : Fragment(){

    @Inject
    lateinit var connectivityManager: ConnectivityManager


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

    fun setTitle(title: String) {
        (activity as? BaseActivity)?.setTitle(title)
    }

    fun getTitle() :String? {
       return (activity as? BaseActivity)?.getString()
    }


    fun showErrorDialog(title: String, message: String) {
        (requireActivity() as BaseActivity).showErrorDialogue(
            title,
            message,
            isNegativeButtonNeed = false,
        ) {}
    }
    fun withNetworkCheck(
        connectivityManager: ConnectivityManager,
        onNetworkAvailable: () -> Unit,
        onNetworkNotAvailable: (() -> Unit?)? = null

    ) {
        if (connectivityManager.isNetworkAvailable()) {
            onNetworkAvailable()
        } else {
            (requireActivity() as BaseActivity).showErrorDialogue(
                getString(R.string.error),
                getString(R.string.no_internet_error),
                isNegativeButtonNeed = false
            ) {
                if (it && onNetworkNotAvailable != null) {
                    onNetworkNotAvailable()
                }
                (requireActivity() as BaseActivity).hideLoading()
            }
        }
    }
}