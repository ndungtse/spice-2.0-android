package com.medtroniclabs.spice.ui.mypatients.fragment

import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.GridLayoutManager
import com.medtroniclabs.spice.R
import com.medtroniclabs.spice.common.CommonUtils
import com.medtroniclabs.spice.common.DateUtils
import com.medtroniclabs.spice.common.DateUtils.DATE_FORMAT_yyyyMMddHHmmssZZZZZ
import com.medtroniclabs.spice.common.DateUtils.DATE_ddMMyyyy
import com.medtroniclabs.spice.common.DefinedParams
import com.medtroniclabs.spice.common.DefinedParams.ID
import com.medtroniclabs.spice.databinding.FragmentPatientInfoBinding
import com.medtroniclabs.spice.model.PatientListRespModel
import com.medtroniclabs.spice.network.resource.ResourceState
import com.medtroniclabs.spice.ui.BaseFragment
import com.medtroniclabs.spice.ui.medicalreview.viewmodel.PatientStatusViewModel
import com.medtroniclabs.spice.ui.mypatients.viewmodel.PatientDetailViewModel
import com.medtroniclabs.spice.ui.mypatients.viewmodel.ReferralTicketViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PatientInfoFragment : BaseFragment() {

    private lateinit var binding: FragmentPatientInfoBinding
    val viewModel: PatientDetailViewModel by activityViewModels()
    private val patientStatusViewModel: PatientStatusViewModel by activityViewModels()

    companion object {
        const val TAG = "PatientInfoFragment"

        fun newInstance(): PatientInfoFragment {
            return PatientInfoFragment()
        }

        fun newInstance(patientId: String?, id: String? = null): PatientInfoFragment {
            val fragment = PatientInfoFragment()
            val bundle = Bundle()
            bundle.putString(DefinedParams.PatientId, patientId)
            bundle.putString(ID, id)
            fragment.arguments = bundle
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentPatientInfoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val patientId = arguments?.getString(DefinedParams.PatientId, "")
        patientStatusViewModel.patientId = arguments?.getString(ID, null)
        if (patientId?.isNotBlank() == true) {
            viewModel.getPatients(patientId)
        }
        attachObservers()
    }

    private fun attachObservers() {
        viewModel.patientDetailsLiveData.observe(viewLifecycleOwner) { resource ->
            when (resource.state) {
                ResourceState.LOADING -> {
                    showProgress()
                }

                ResourceState.SUCCESS -> {
                    resource.data?.let {
                        setDataInInfo(it)
                    }
                    hideProgress()
                }

                ResourceState.ERROR -> {
                    hideProgress()
                }
            }
        }
    }

    private fun setDataInInfo(patientListRespModel: PatientListRespModel) {
        val name =
            patientListRespModel.name ?: requireContext().getString(R.string.separator_hyphen)
        val gender =
            patientListRespModel.gender ?: requireContext().getString(R.string.separator_hyphen)
        val age = patientListRespModel.birthDate?.let {
            DateUtils.calculateAge(patientListRespModel.birthDate)
        } ?: (patientListRespModel.age ?: requireContext().getString(R.string.separator_hyphen))
        setTitle(requireContext().getString(R.string.household_summary_member_info, name, age, gender))
        with(binding) {
            // TODO: Need to give date format by backend
            val date = patientListRespModel.dateOfOnset.takeIf { it?.isNotBlank() == true }?.let {
                DateUtils.convertDateFormat(it, DATE_FORMAT_yyyyMMddHHmmssZZZZZ, DATE_ddMMyyyy)
            } ?: requireContext().getString(R.string.hyphen_symbol)
            val dataList = listOf(
                mapOf(
                    DefinedParams.label to requireContext().getString(R.string.patient_id),
                    DefinedParams.value to (patientListRespModel.patientId ?: 0).toString()
                ),
                mapOf(
                    DefinedParams.label to requireContext().getString(R.string.hh_id),
                    DefinedParams.value to (patientListRespModel.houseHoldId ?: 0).toString()
                ),
                mapOf(
                    DefinedParams.label to requireContext().getString(R.string.village),
                    DefinedParams.value to (patientListRespModel.village.takeIf { it?.isNotBlank() == true }
                        ?: requireContext().getString(R.string.hyphen_symbol))),
                mapOf(
                    DefinedParams.label to requireContext().getString(R.string.phone_no),
                    DefinedParams.value to (patientListRespModel.phoneNumber.takeIf { it?.isNotBlank() == true }
                        ?: requireContext().getString(R.string.hyphen_symbol))),
                mapOf(
                    DefinedParams.label to requireContext().getString(R.string.landmark),
                    DefinedParams.value to (patientListRespModel.location.takeIf { it?.isNotBlank() == true }
                        ?: requireContext().getString(R.string.hyphen_symbol))),
                mapOf(
                    DefinedParams.label to requireContext().getString(R.string.chw),
                    DefinedParams.value to (patientListRespModel.chw.takeIf { it?.isNotBlank() == true }
                        ?: requireContext().getString(R.string.hyphen_symbol))),
                mapOf(
                    DefinedParams.label to requireContext().getString(R.string.date_of_onset),
                    DefinedParams.value to date
                ),
            )
            val adapter = PatientInfoAdapter(dataList,R.color.fragment_bg)
            val isLandscape =
                resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

            val spanCount = if (isLandscape) 2 else 1
            if (CommonUtils.checkIsTablet(requireContext())) {
                rvPatientInfo.layoutManager = GridLayoutManager(requireContext(), spanCount)
            } else {
                rvPatientInfo.layoutManager = GridLayoutManager(requireContext(), 1)
            }
            rvPatientInfo.adapter = adapter
        }
    }
}