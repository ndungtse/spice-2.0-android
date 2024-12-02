package com.medtroniclabs.spice.ui.patientEdit

import android.os.Bundle
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import androidx.fragment.app.replace
import com.medtroniclabs.spice.R
import com.medtroniclabs.spice.common.DefinedParams
import com.medtroniclabs.spice.databinding.ActivityNcdPatientEditBinding
import com.medtroniclabs.spice.ncd.medicalreview.NCDMRUtil
import com.medtroniclabs.spice.ui.BaseActivity
import com.medtroniclabs.spice.ui.mypatients.viewmodel.PatientDetailViewModel
import com.medtroniclabs.spice.ui.patientEdit.fragment.NCDPatientEditFragment
import com.medtroniclabs.spice.ui.patientEdit.viewModel.NCDPatientEditViewModel
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class NCDPatientEditActivity : BaseActivity() {

    private lateinit var binding: ActivityNcdPatientEditBinding
    private val patientDetailViewModel: PatientDetailViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNcdPatientEditBinding.inflate(layoutInflater)
        setMainContentView(
            binding.root,
            true,
            getString(R.string.patient_details),
            homeAndBackVisibility = Pair(true, null)
        )
        showVerticalMoreIcon(false)
        getPatientDetails()
        loadFragment()
    }

    private fun getPatientDetails() {
        intent?.let {
            patientDetailViewModel.origin = it.getStringExtra(DefinedParams.ORIGIN)
            intent.putExtra(NCDMRUtil.PATIENT_REFERENCE, patientDetailViewModel.getPatientId())
            it.getStringExtra(NCDMRUtil.MEMBER_REFERENCE)?.let { id ->
                patientDetailViewModel.getPatients(
                    id,
                    origin = patientDetailViewModel.origin?.lowercase()
                )
            }
        }
    }

    private fun loadFragment() {
        replaceFragmentInId<NCDPatientEditFragment>(
            binding.fragmentContainerAssessment.id,
            tag = NCDPatientEditFragment.TAG
        )
    }

    private inline fun <reified fragment : Fragment> replaceFragmentInId(
        id: Int? = null,
        bundle: Bundle? = null,
        tag: String? = null
    ) {
        supportFragmentManager.commit {
            setReorderingAllowed(true)
            replace<fragment>(
                id ?: binding.fragmentContainerAssessment.id,
                args = bundle,
                tag = tag
            )
        }
        hideHomeButton(true)
    }
}