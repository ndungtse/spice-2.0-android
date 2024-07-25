package com.medtroniclabs.spice.ui

import androidx.fragment.app.DialogFragment
import com.medtroniclabs.spice.R
import com.medtroniclabs.spice.network.utils.ConnectivityManager
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
open class BaseDialogFragment:DialogFragment() {

    @Inject
    lateinit var connectivityManager: ConnectivityManager

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
     fun finishFragment() {
        activity?.supportFragmentManager?.beginTransaction()?.remove(this)?.commit()
    }
}