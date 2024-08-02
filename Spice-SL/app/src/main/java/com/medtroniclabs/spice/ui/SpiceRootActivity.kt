package com.medtroniclabs.spice.ui

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.medtroniclabs.spice.R
import com.medtroniclabs.spice.appextensions.cancelAllWorker
import com.medtroniclabs.spice.common.AppConstants
import com.medtroniclabs.spice.common.DefinedParams
import com.medtroniclabs.spice.common.GeneralErrorDialog
import com.medtroniclabs.spice.common.SecuredPreference
import com.medtroniclabs.spice.ui.landing.LandingActivity

open class SpiceRootActivity : AppCompatActivity() {

    private lateinit var sessionExpiredBroadcastReceiver: SessionExpiredBroadcastReceiver

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sessionExpiredBroadcastReceiver = SessionExpiredBroadcastReceiver()
    }


    override fun onResume() {
        super.onResume()
        LocalBroadcastManager.getInstance(this).registerReceiver(
            sessionExpiredBroadcastReceiver,
            IntentFilter(
                DefinedParams.ACTION_SESSION_EXPIRED
            )
        )
    }

    override fun onPause() {
        super.onPause()
        LocalBroadcastManager.getInstance(this).unregisterReceiver(
            sessionExpiredBroadcastReceiver
        )
    }

    inner class SessionExpiredBroadcastReceiver : BroadcastReceiver() {

        override fun onReceive(context: Context, intent: Intent) {
            val sessionExpired = intent.getBooleanExtra(DefinedParams.SL_SESSION, false)
            val className = this.javaClass.simpleName
            if (sessionExpired && !AppConstants.exemptionList.contains(className)) {
                showErrorDialogue(
                    getString(R.string.alert),
                    getString(R.string.session_expired),
                    isNegativeButtonNeed = false
                ) { status ->
                    if (status) {
                        cancelAllWorker()
                        SecuredPreference.clear(this@SpiceRootActivity)
                        val i = Intent(context, LandingActivity::class.java)
                        i.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        context.startActivity(i)
                    }
                }
            }
        }
    }

    fun showErrorDialogue(
        title: String = getString(R.string.error),
        message: String,
        isNegativeButtonNeed: Boolean? = null,
        positiveButtonName: String? = null,
        okayBtnEnable: Boolean? = null,
        cancelBtnName: String? = null,
        callback: ((isPositiveResult: Boolean) -> Unit)
    ) {
        val generalErrorDialog =
            GeneralErrorDialog.newInstance(
                title,
                callback,
                this,
                isNegativeButtonNeed ?: false,
                okayButton = positiveButtonName ?: getString(R.string.ok),
                cancelButton = cancelBtnName ?: getString(R.string.cancel),
                messageBtnData = Pair(message, okayBtnEnable ?: true)
            )
        val errorFragment = supportFragmentManager.findFragmentByTag(GeneralErrorDialog.TAG)
        if (errorFragment == null)
            generalErrorDialog.show(supportFragmentManager, GeneralErrorDialog.TAG)
    }
}