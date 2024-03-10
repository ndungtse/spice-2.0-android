package com.medtroniclabs.spice.ui.mypatients.fragment

import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import com.medtroniclabs.spice.R
import com.medtroniclabs.spice.common.DefinedParams
import com.medtroniclabs.spice.databinding.FragmentPatientSearchBinding
import com.medtroniclabs.spice.model.PatientListRespModel
import com.medtroniclabs.spice.ui.BaseFragment
import com.medtroniclabs.spice.ui.home.ToolsActivity
import com.medtroniclabs.spice.ui.medicalreview.MedicalReviewBaseActivity
import com.medtroniclabs.spice.ui.mypatients.PatientSelectionListener
import com.medtroniclabs.spice.ui.mypatients.PatientsListAdapter
import com.medtroniclabs.spice.ui.mypatients.viewmodel.PatientListViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PatientSearchFragment : BaseFragment(), PatientSelectionListener {
    lateinit var binding: FragmentPatientSearchBinding
    private val patientListViewModel: PatientListViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentPatientSearchBinding.inflate(layoutInflater)
        return binding.root
    }

    companion object {
        const val TAG = "PatientSearchFragment"
        fun newInstance(): PatientSearchFragment {
            return PatientSearchFragment()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
        setAdapterViews()
    }

    private fun initViews() {
        binding.llFilter.btnFilter.text = getString(R.string.filters)
        val tabletSize =
            resources.getBoolean(R.bool.isLargeTablet) || resources.getBoolean(R.bool.isTablet)
        if (tabletSize) {
            requireActivity().requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            patientListViewModel.spanCount = DefinedParams.span_count_3
        } else {
            requireActivity().requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
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
                GridLayoutManager(requireContext(), patientListViewModel.spanCount)
            adapter = PatientsListAdapter(this@PatientSearchFragment, patientList)
            binding.tvPatientCount.text =
                getString(R.string.patients_found, patientList.size)
        }
    }

    override fun onSelectedPatient(item: PatientListRespModel) {
        startActivity(Intent(requireActivity(), ToolsActivity::class.java))
    }
}