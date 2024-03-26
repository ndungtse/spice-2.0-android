package com.medtroniclabs.spice.ui.mypatients

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import com.medtroniclabs.spice.R
import com.medtroniclabs.spice.common.DefinedParams
import com.medtroniclabs.spice.databinding.ActivityReferralTicketBinding
import com.medtroniclabs.spice.formgeneration.extension.safeClickListener
import com.medtroniclabs.spice.ui.BaseActivity
import com.medtroniclabs.spice.ui.home.ToolsActivity
import com.medtroniclabs.spice.ui.mypatients.fragment.PatientInfoFragment
import com.medtroniclabs.spice.ui.mypatients.viewmodel.ReferralTicketViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ReferralTicketActivity : BaseActivity() {
    private lateinit var binding: ActivityReferralTicketBinding
    val viewModel: ReferralTicketViewModel by viewModels()

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
        supportFragmentManager.beginTransaction()
            .add(
                R.id.patientDetailsContainer,
                PatientInfoFragment.newInstance(intent.getStringExtra(DefinedParams.PatientId))
            ).commit()

        binding.btnMedicalReview.safeClickListener {
            val intent = Intent(this, ToolsActivity::class.java)
            if (getString().isNotBlank()) {
                intent.putExtra(DefinedParams.MenuTitle, getString())
            }
            intent.putExtra(
                DefinedParams.PatientId,
                this.intent.getStringExtra(DefinedParams.PatientId)
            )
            startActivity(intent)
        }
    }

}