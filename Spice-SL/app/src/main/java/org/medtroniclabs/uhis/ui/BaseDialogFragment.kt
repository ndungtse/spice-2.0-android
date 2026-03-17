package org.medtroniclabs.uhis.ui

import androidx.fragment.app.DialogFragment
import dagger.hilt.android.AndroidEntryPoint
import org.medtroniclabs.uhis.R
import org.medtroniclabs.uhis.network.utils.ConnectivityManager
import javax.inject.Inject

@AndroidEntryPoint
open class BaseDialogFragment : DialogFragment() {
    @Inject
    lateinit var connectivityManager: ConnectivityManager

    fun withNetworkAvailability(
        online: () -> Unit,
        offline: () -> Unit = {},
    ) {
        connectivityManager.isNullableNetworkAvailable()?.let { isNetworkAvailable ->
            if (isNetworkAvailable) {
                online()
            } else {
                (requireActivity() as BaseActivity).showErrorDialogue(
                    getString(R.string.error),
                    getString(R.string.no_internet_error),
                    isNegativeButtonNeed = false,
                ) {
                    if (it) {
                        offline()
                    }
                    (requireActivity() as BaseActivity).hideLoading()
                }
            }
        }
    }

    fun finishFragment() {
        activity
            ?.supportFragmentManager
            ?.beginTransaction()
            ?.remove(this)
            ?.commit()
    }
}
