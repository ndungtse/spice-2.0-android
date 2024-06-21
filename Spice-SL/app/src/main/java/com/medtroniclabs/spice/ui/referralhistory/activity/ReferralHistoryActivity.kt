package com.medtroniclabs.spice.ui.referralhistory.activity

import android.Manifest
import android.content.Intent
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.medtroniclabs.spice.R
import com.medtroniclabs.spice.appextensions.isFineAndCoarseLocationPermissionGranted
import com.medtroniclabs.spice.appextensions.isGpsEnabled
import com.medtroniclabs.spice.common.DateUtils
import com.medtroniclabs.spice.common.DefinedParams
import com.medtroniclabs.spice.common.DefinedParams.ID
import com.medtroniclabs.spice.databinding.ActivityReferralTicketBinding
import com.medtroniclabs.spice.formgeneration.extension.safeClickListener
import com.medtroniclabs.spice.model.PatientListRespModel
import com.medtroniclabs.spice.ui.BaseActivity
import com.medtroniclabs.spice.ui.home.MedicalReviewToolsActivity
import com.medtroniclabs.spice.ui.medicalreview.motherneonate.anc.AncVisitCallBack
import com.medtroniclabs.spice.ui.mypatients.fragment.PatientInfoFragment
import com.medtroniclabs.spice.ui.mypatients.viewmodel.PatientDetailViewModel
import com.medtroniclabs.spice.ui.referralhistory.fragment.MedicalReviewHistoryFragment
import com.medtroniclabs.spice.ui.referralhistory.fragment.PrescriptionHistoryFragment
import com.medtroniclabs.spice.ui.referralhistory.fragment.ReferralTicketFragment
import com.medtroniclabs.spice.ui.referralhistory.viewmodel.ReferralHistoryViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ReferralHistoryActivity : BaseActivity(), AncVisitCallBack {
    private lateinit var binding: ActivityReferralTicketBinding
    val viewModel: ReferralHistoryViewModel by viewModels()
    private val patientDetailViewModel: PatientDetailViewModel by viewModels()

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
        val patientFragment =
            PatientInfoFragment.newInstance(intent.getStringExtra(DefinedParams.PatientId))
        patientFragment.setDataCallback(this)
        supportFragmentManager.beginTransaction()
            .add(
                R.id.patientDetailsContainer,
                patientFragment
            ).commit()
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
        val intent = Intent(this, MedicalReviewToolsActivity::class.java)
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
        intent.putExtra(
            DefinedParams.DOB,
            this.intent.getStringExtra(DefinedParams.DOB)
        )
        startActivity(intent)
    }

    override fun onDataLoaded(details: PatientListRespModel) {
        if (connectivityManager.isNetworkAvailable()) {
            val dob = intent.getStringExtra(DefinedParams.DOB)
            dob?.let { DateUtils.calculateAge(dob) } ?: 0
            showLoading()
            val referralTicketFragment = ReferralTicketFragment.newInstance(details.id)
            addFragmentIfAbsent(
                R.id.cardReferralTicket,
                ReferralTicketFragment.TAG,
                referralTicketFragment
            )

            val medicalReviewFragment = MedicalReviewHistoryFragment.newInstance(details.id)
            addFragmentIfAbsent(
                R.id.cardMedicalReviewContainer,
                MedicalReviewHistoryFragment.TAG,
                medicalReviewFragment
            )

            val prescriptionFragment = PrescriptionHistoryFragment.newInstance(details.id)
            addFragmentIfAbsent(
                R.id.cardPrescriptionContainer,
                PrescriptionHistoryFragment.TAG,
                prescriptionFragment
            )
            binding.btnMedicalReview.safeClickListener {
                if (ableToGetLocation())
                    launchToolsActivity()
            }
            hideLoading()
        }
    }

    private fun addFragmentIfAbsent(
        containerId: Int,
        fragmentTag: String,
        fragmentInstance: Fragment
    ) {
        val existingFragment = supportFragmentManager.findFragmentByTag(fragmentTag)
        if (existingFragment == null) {
            supportFragmentManager.beginTransaction()
                .add(containerId, fragmentInstance, fragmentTag)
                .commit()
        }
    }
}