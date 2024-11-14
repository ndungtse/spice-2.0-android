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

    fun showSuccessDialogue(title: String, message: String) {
        (requireActivity() as BaseActivity).showSuccessDialogue(
            title = title,
            message = message,
        ) {}
    }


    fun showErrorDialog(title: String, message: String) {
        (requireActivity() as BaseActivity).showErrorDialogue(
            title,
            message,
            isNegativeButtonNeed = false,
        ) {}
    }

    fun withNetworkAvailability(
        online: () -> Unit,
        offline: () -> Unit = {},
        requireErrorDialog: Boolean = true
    ) {
        connectivityManager.isNullableNetworkAvailable()?.let { isNetworkAvailable ->
            if (isNetworkAvailable) {
                online()
            } else {
                if (requireErrorDialog) {
                    showErrorDialog(getString(R.string.error), getString(R.string.no_internet_error))
                }
                offline()
            }
        }
    }

    inline fun <reified F : Fragment> replaceFragmentIfExists(
        id: Int,
        bundle: Bundle? = null,
        tag: String? = null
    ) {
        val existingFragment = parentFragmentManager.findFragmentByTag(tag)

        parentFragmentManager.commit {
            setReorderingAllowed(true)
            if (existingFragment != null) {
                // Fragment exists, replace it
                replace(id, existingFragment, tag)
            } else {
                // Fragment does not exist, create a new instance and replace it
                replace<F>(id, args = bundle, tag = tag)
            }
        }
    }
}