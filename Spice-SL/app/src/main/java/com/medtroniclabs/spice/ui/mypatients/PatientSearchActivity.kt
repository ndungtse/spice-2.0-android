package com.medtroniclabs.spice.ui.mypatients

import android.animation.ObjectAnimator
import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import com.medtroniclabs.spice.R
import com.medtroniclabs.spice.common.DefinedParams
import com.medtroniclabs.spice.databinding.ActivityPatientSearchBinding
import com.medtroniclabs.spice.model.PatientListRespModel
import com.medtroniclabs.spice.ui.BaseActivity
import com.medtroniclabs.spice.ui.medicalreview.MedicalReviewBaseActivity
import com.medtroniclabs.spice.ui.mypatients.viewmodel.PatientListViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PatientSearchActivity : BaseActivity(), PatientSelectionListener {

    private lateinit var binding: ActivityPatientSearchBinding
    private val patientListViewModel: PatientListViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPatientSearchBinding.inflate(layoutInflater)
        setMainContentView(
            binding.root,
            title = getString(R.string.search_patient),
            isToolbarVisible = true,
            homeAndBackVisibility = Pair(true, true)
        )
        initViews()
        setAdapterViews()
    }

    private fun initViews() {
        binding.llFilter.btnFilter.text = getString(R.string.filters)
        val tabletSize =
            resources.getBoolean(R.bool.isLargeTablet) || resources.getBoolean(R.bool.isTablet)
        if (tabletSize) {
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            patientListViewModel.spanCount = DefinedParams.span_count_3
        } else {
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            patientListViewModel.spanCount = DefinedParams.span_count_1
        }
    }

    private fun setAdapterViews() {
        val patientList = ArrayList<PatientListRespModel>()
        patientList.add(
            PatientListRespModel(
                firstName = "Tanya",
                lastName = "Joseph",
                age = 23,
                gender = "Male",
                nationalID = 112234,
                patientId = 2343242
            )
        )
        patientList.add(
            PatientListRespModel(
                firstName = "Sara",
                lastName = "Mist",
                age = 25,
                gender = "Female",
                nationalID = 113244,
                patientId = 232542
            )
        )
        patientList.add(
            PatientListRespModel(
                firstName = "Sam",
                lastName = "Valarie",
                age = 21,
                gender = "Male",
                nationalID = 1556634,
                patientId = 2984342
            )
        )
        patientList.add(
            PatientListRespModel(
                firstName = "Becky",
                lastName = "Lynch",
                age = 25,
                gender = "Female",
                nationalID = 156644,
                patientId = 942839
            )
        )
        binding.rvPatientsList.apply {
            layoutManager =
                GridLayoutManager(this@PatientSearchActivity, patientListViewModel.spanCount)
            adapter = PatientsListAdapter(this@PatientSearchActivity, patientList)
            binding.tvPatientCount.text =  getString(R.string.patients_found, patientList.size)
        }
    }


    override fun onSelectedPatient(item: PatientListRespModel) {
        startActivity(Intent(this, MedicalReviewBaseActivity::class.java))
    }

    private fun clearExactSearch() {
            binding.llExactSearch.etPatientSearch.text?.clear()
            binding.llExactSearch.btnSearch.isEnabled = false
    }

}