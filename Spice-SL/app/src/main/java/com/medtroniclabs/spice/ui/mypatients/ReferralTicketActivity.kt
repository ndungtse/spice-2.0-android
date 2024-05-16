package com.medtroniclabs.spice.ui.mypatients

import android.Manifest
import android.content.Intent
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.app.ActivityCompat
import com.medtroniclabs.spice.R
import com.medtroniclabs.spice.appextensions.isFineAndCoarseLocationPermissionGranted
import com.medtroniclabs.spice.appextensions.isGpsEnabled
import com.medtroniclabs.spice.common.DefinedParams
import com.medtroniclabs.spice.common.DefinedParams.ID
import com.medtroniclabs.spice.databinding.ActivityReferralTicketBinding
import com.medtroniclabs.spice.formgeneration.extension.safeClickListener
import com.medtroniclabs.spice.ui.BaseActivity
import com.medtroniclabs.spice.ui.home.ToolsActivity
import com.medtroniclabs.spice.ui.mypatients.fragment.PatientInfoFragment
import com.medtroniclabs.spice.ui.mypatients.fragment.ReferralTicketFragment
import com.medtroniclabs.spice.ui.mypatients.viewmodel.PatientDetailViewModel
import com.medtroniclabs.spice.ui.mypatients.viewmodel.ReferralTicketViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ReferralTicketActivity : BaseActivity() {
    private lateinit var binding: ActivityReferralTicketBinding
    val viewModel: ReferralTicketViewModel by viewModels()
    val patientDetailViewModel: PatientDetailViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityReferralTicketBinding.inflate(layoutInflater)
        setMainContentView(
            binding.root,
            true,
            getString(R.string.patient_medical_review)
        )
        initView()
    }


    private fun initView() {
        showLoading()
        supportFragmentManager.beginTransaction()
            .add(
                R.id.patientDetailsContainer,
                PatientInfoFragment.newInstance(intent.getStringExtra(DefinedParams.PatientId))
            ).commit()
        hideLoading()
//        supportFragmentManager.beginTransaction()
//            .add(
//                R.id.cardReferralTicket,
//                ReferralTicketFragment.newInstance(intent.getStringExtra(DefinedParams.PatientId))
//            ).commit()

        binding.btnMedicalReview.safeClickListener {
            if (ableToGetLocation())
                launchToolsActivity()
        }
//        val fragment = supportFragmentManager.findFragmentById(R.id.patientDetailsContainer) as? PatientInfoFragment
//        fragment?.hideProgress()
    }

    private fun ableToGetLocation(): Boolean {
        //Check Location service is enabled
        if (!isGpsEnabled()) {
            showTurnOnGPSDialog()
            return false
        }

        //Check Location permission for limit exceed
        if (ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        ) {
            showAllowLocationServiceDialog()
            return false
        }

        //Check Location permission
        if (!isFineAndCoarseLocationPermissionGranted()) {
            requestPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
            return false
        }

        return true
    }

    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            val finePermission = permissions[Manifest.permission.ACCESS_FINE_LOCATION]
            val coarsePermission = permissions[Manifest.permission.ACCESS_COARSE_LOCATION]

            if (finePermission == true && coarsePermission == true) {
                launchToolsActivity()
            }
        }

    private fun launchToolsActivity() {
        val intent = Intent(this, ToolsActivity::class.java)
        if (getString().isNotBlank()) {
            intent.putExtra(DefinedParams.MenuTitle, getString())
        }
        intent.putExtra(
            DefinedParams.PatientId,
            this.intent.getStringExtra(DefinedParams.PatientId)
        )
        intent.putExtra(
            ID,
            patientDetailViewModel.patientDetailsId
        )
        intent.putExtra(
            DefinedParams.Gender,
            this.intent.getStringExtra(DefinedParams.Gender)
        )
        startActivity(intent)
    }

}