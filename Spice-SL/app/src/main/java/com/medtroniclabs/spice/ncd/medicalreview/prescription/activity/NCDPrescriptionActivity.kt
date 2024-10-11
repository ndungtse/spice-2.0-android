package com.medtroniclabs.spice.ncd.medicalreview.prescription.activity

import android.os.Bundle
import android.view.View
import com.medtroniclabs.spice.R
import com.medtroniclabs.spice.appextensions.gone
import com.medtroniclabs.spice.databinding.ActivityNcdPrescriptionBinding
import com.medtroniclabs.spice.formgeneration.extension.safeClickListener
import com.medtroniclabs.spice.ncd.medicalreview.prescription.dialog.NCDDeleteConfirmationDialog
import com.medtroniclabs.spice.ncd.medicalreview.prescription.dialog.NCDInstructionExpansionDialog
import com.medtroniclabs.spice.ncd.medicalreview.prescription.dialog.NCDMedicationHistoryDialog
import com.medtroniclabs.spice.ui.BaseActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class NCDPrescriptionActivity : BaseActivity(), View.OnClickListener {

    private lateinit var binding: ActivityNcdPrescriptionBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNcdPrescriptionBinding.inflate(layoutInflater)
        setMainContentView(
            binding.root,
            true,
            getString(R.string.prescription),
            homeAndBackVisibility = Pair(true, false),
            callback = {
            },
            callbackHome = {
            }
        )
        clickListener()
    }

    private fun clickListener() {
        binding.btnAddMedicine.safeClickListener(this)
        binding.btnPrescribe.safeClickListener(this)
        binding.tvDiscontinuedMedication.safeClickListener(this)
        binding.btnBack.safeClickListener(this)
        binding.btnRenewAll.safeClickListener(this)
    }

    override fun onClick(view: View?) {

        when (view?.id) {
            R.id.btnAddMedicine -> {
                val dialog = NCDMedicationHistoryDialog()
                dialog.show(supportFragmentManager, "NCDMedicationHistoryDialog")
            }

            R.id.btnPrescribe -> {
                val dialog = NCDDeleteConfirmationDialog.newInstance(
                    title = getString(R.string.confirmation),
                    message = getString(R.string.delete_alert),
                    okButton = getString(R.string.ok),
                    cancelButton = getString(R.string.cancel),
                    isNegativeButton = true,
                    context = this,
                    callback = { status, reason, otherReason -> }
                )
                dialog.show(supportFragmentManager, "NCDDeleteConfirmationDialog")
            }

            R.id.tvDiscontinuedMedication -> {
            }

            binding.btnBack.id -> {
                finish()
            }

            R.id.btnRenewAll -> {
            }

        }
    }
}