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
import com.medtroniclabs.spice.databinding.FragmentPatientInfoBinding
import com.medtroniclabs.spice.formgeneration.extension.capitalizeFirstChar
import com.medtroniclabs.spice.model.PatientListRespModel
import com.medtroniclabs.spice.network.resource.ResourceState
import com.medtroniclabs.spice.ui.BaseFragment
import com.medtroniclabs.spice.ui.mypatients.viewmodel.PatientDetailViewModel
import com.medtroniclabs.spice.ui.assessment.rmnch.RMNCH.ANC
import com.medtroniclabs.spice.ui.medicalreview.motherneonate.anc.AncVisitCallBack
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PatientInfoFragment : BaseFragment() {

    private lateinit var binding: FragmentPatientInfoBinding
    val viewModel: PatientDetailViewModel by activityViewModels()
    private var dataCallback: AncVisitCallBack? = null
    fun setDataCallback(callback: AncVisitCallBack) {
        dataCallback = callback
    }


    companion object {
        const val TAG = "PatientInfoFragment"

        fun newInstance(): PatientInfoFragment {
            return PatientInfoFragment()
        }

        fun newInstance(patientId: String?,isAnc:Boolean = false): PatientInfoFragment {
            val fragment = PatientInfoFragment()
            val bundle = Bundle()
            bundle.putString(DefinedParams.PatientId, patientId)
            bundle.putBoolean(ANC, isAnc)
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
        if (patientId?.isNotBlank() == true) {
            viewModel.getPatients(patientId, if (isAnc() == true) ANC.uppercase() else null)
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
                        dataCallback?.onDataLoaded(it)
                    }
                }

                ResourceState.ERROR -> {
//                    hideProgress()
                }
            }
        }
    }

    private fun isAnc(): Boolean? {
        return arguments?.getBoolean(ANC, false)
    }
    private fun setDataInInfo(patientListRespModel: PatientListRespModel) {
        showProgress()
        viewModel.patientDetailsId = patientListRespModel.id
        val isAnc = isAnc()
        val name =
            patientListRespModel.name ?: requireContext().getString(R.string.separator_hyphen)
        val gender =
            patientListRespModel.gender ?: requireContext().getString(R.string.separator_hyphen)
        val age = patientListRespModel.birthDate?.let {
            DateUtils.getAgeDescription(patientListRespModel.birthDate, requireContext())
        } ?: (patientListRespModel.age ?: requireContext().getString(R.string.separator_hyphen))
        setTitle(requireContext().getString(R.string.household_summary_member_info, name.trim(), age, gender.lowercase().capitalizeFirstChar().trim()))
        with(binding) {
            val lastMenstrualDate =
                patientListRespModel.pregnancyDetails?.lastMenstrualPeriod.takeIf { it?.isNotBlank() == true }?.let {
                    DateUtils.convertDateFormat(it, DATE_FORMAT_yyyyMMddHHmmssZZZZZ, DATE_ddMMyyyy)
                } ?: requireContext().getString(R.string.hyphen_symbol)

            val dataList = mutableListOf(
                mapOf(
                    DefinedParams.label to requireContext().getString(R.string.patient_id),
                    DefinedParams.value to (patientListRespModel.patientId
                        ?: requireContext().getString(R.string.hyphen_symbol)).toString().trim()
                ),
                mapOf(
                    DefinedParams.label to requireContext().getString(R.string.contact_number),
                    DefinedParams.value to (patientListRespModel.phoneNumber.takeIf { it?.isNotBlank() == true }?.trim()
                        ?: requireContext().getString(R.string.hyphen_symbol))),
                mapOf(
                    DefinedParams.label to requireContext().getString(R.string.hh_id),
                    DefinedParams.value to (patientListRespModel.houseHoldId.takeIf { it?.isNotBlank() == true }?.trim()
                        ?: requireContext().getString(R.string.hyphen_symbol)).toString()
                ),
                mapOf(
                    DefinedParams.label to requireContext().getString(R.string.landmark),
                    DefinedParams.value to (patientListRespModel.landmark.takeIf { it?.isNotBlank() == true }?.trim()
                        ?: requireContext().getString(R.string.hyphen_symbol))),
                mapOf(
                    DefinedParams.label to requireContext().getString(R.string.village),
                    DefinedParams.value to (patientListRespModel.village.takeIf { it?.isNotBlank() == true }?.trim()
                        ?: requireContext().getString(R.string.hyphen_symbol)))
            )
            if (isAnc == true) {
                dataList.add(
                    mapOf(
                        DefinedParams.label to requireContext().getString(R.string.last_menstrual_period),
                        DefinedParams.value to lastMenstrualDate
                    )
                )
            }
            if (isAnc == true && !(viewModel.isSummary)) {
                dataList.add(
                    mapOf(
                        DefinedParams.label to requireContext().getString(R.string.anc_visit),
                        DefinedParams.value to (patientListRespModel.pregnancyDetails?.ancVisitMedicalReview?.takeIf { true }
                            ?.plus(1)
                            ?.toString()
                            ?: "1")
                    )
                )
            }
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
            hideProgress()
        }
    }
}