package com.medtroniclabs.spice.ui.mypatients.fragment

import android.content.res.Configuration
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.text.color
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.GridLayoutManager
import com.medtroniclabs.spice.R
import com.medtroniclabs.spice.common.CommonUtils
import com.medtroniclabs.spice.common.CommonUtils.combineText
import com.medtroniclabs.spice.common.CommonUtils.getContactNumber
import com.medtroniclabs.spice.common.DateUtils
import com.medtroniclabs.spice.common.DateUtils.DATE_FORMAT_ddMMMyyyy
import com.medtroniclabs.spice.common.DateUtils.DATE_FORMAT_yyyyMMddHHmmssZZZZZ
import com.medtroniclabs.spice.common.DateUtils.DATE_TIME_yyyyMMddTHHmmssSSSXXX
import com.medtroniclabs.spice.common.DateUtils.DATE_ddMMyyyy
import com.medtroniclabs.spice.common.DefinedParams
import com.medtroniclabs.spice.common.DefinedParams.IsReferredScreen
import com.medtroniclabs.spice.common.DefinedParams.OtherNotes
import com.medtroniclabs.spice.common.StringConverter
import com.medtroniclabs.spice.databinding.FragmentPatientInfoBinding
import com.medtroniclabs.spice.formgeneration.extension.capitalizeFirstChar
import com.medtroniclabs.spice.model.PatientListRespModel
import com.medtroniclabs.spice.network.resource.ResourceState
import com.medtroniclabs.spice.ui.BaseActivity
import com.medtroniclabs.spice.ui.BaseFragment
import com.medtroniclabs.spice.ui.mypatients.viewmodel.PatientDetailViewModel
import com.medtroniclabs.spice.ui.assessment.rmnch.RMNCH.ANC
import com.medtroniclabs.spice.ui.assessment.rmnch.RMNCH.PNC
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

        fun newInstance(
            patientId: String?,
            isAnc: Boolean = false,
            isPnc:Boolean =false,
            isReferredScreen: Boolean = false
        ): PatientInfoFragment {
            val fragment = PatientInfoFragment()
            val bundle = Bundle()
            bundle.putString(DefinedParams.PatientId, patientId)
            bundle.putBoolean(ANC, isAnc)
            bundle.putBoolean(PNC,isPnc)
            bundle.putBoolean(IsReferredScreen, isReferredScreen)
            fragment.arguments = bundle
            return fragment
        }

        fun newInstanceForNCD(
            patientId: String?,
            origin: String
        ): PatientInfoFragment {
            val fragment = PatientInfoFragment()
            val bundle = Bundle()
            bundle.putString(DefinedParams.PatientId, patientId)
            bundle.putString(DefinedParams.ORIGIN, origin)
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
            if (CommonUtils.isNonNcdWorkflow()) {
                viewModel.getPatients(patientId, if (isAnc() == true) ANC.uppercase() else null)
            } else {
                val origin = arguments?.getString(DefinedParams.ORIGIN, "")
                viewModel.getPatients(
                    patientId,
                    origin = origin
                )
            }
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
                        if (CommonUtils.isNonNcdWorkflow()) {
                            setDataInInfo(it)
                        } else {
                            setNCDData(it)
                        }
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
    private fun isPnc(): Boolean? {
        return arguments?.getBoolean(PNC, false)
    }

    private fun isReferredScreen(): Boolean? {
        return arguments?.getBoolean(IsReferredScreen, false)
    }
    private fun setDataInInfo(patientListRespModel: PatientListRespModel) {
        showProgress()
        viewModel.patientDetailsId = patientListRespModel.id
        viewModel.childPatientDetails=patientListRespModel.pregnancyDetails?.neonatePatientId
        viewModel.dateOfDelivery=patientListRespModel.pregnancyDetails?.dateOfDelivery
        val isAnc = isAnc()
        val isPnc= isPnc()
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
                }
            val dateOfDelivery =
                patientListRespModel.pregnancyDetails?.dateOfDelivery.takeIf { it?.isNotBlank() == true }?.let {
                    DateUtils.convertDateFormat(it, DATE_FORMAT_yyyyMMddHHmmssZZZZZ, DATE_ddMMyyyy)?:requireContext().getString(R.string.hyphen_symbol)
                }

            val dataList = mutableListOf(
                mapOf(
                    DefinedParams.label to requireContext().getString(R.string.patient_id),
                    DefinedParams.value to (patientListRespModel.patientId
                        ?: requireContext().getString(R.string.hyphen_symbol)).toString().trim()
                ),
                mapOf(
                    DefinedParams.label to requireContext().getString(R.string.contact_number),
                    DefinedParams.value to (getContactNumber(patientListRespModel.phoneNumber.takeIf { it?.isNotBlank() == true }
                        ?.trim())
                        ?: requireContext().getString(R.string.hyphen_symbol))
                ),
                mapOf(
                    DefinedParams.label to requireContext().getString(R.string.hh_id),
                    DefinedParams.value to (patientListRespModel.houseHoldNumber
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
            if (isAnc == true && lastMenstrualDate != null) {
                dataList.add(
                    mapOf(
                        DefinedParams.label to requireContext().getString(R.string.last_menstrual_period),
                        DefinedParams.value to lastMenstrualDate
                    )
                )
            }
            if (isPnc==true&&dateOfDelivery != null){
                dataList.add(
                    mapOf(
                        DefinedParams.label to requireContext().getString(R.string.date_of_delivery),
                        DefinedParams.value to dateOfDelivery
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
            if (isReferredScreen() == true) {
                dataList.add(
                    mapOf(
                        DefinedParams.label to requireContext().getString(R.string.diagnosis),
                        DefinedParams.value to combineText(
                            patientListRespModel.diagnosis?.filter { it.diseaseCategory?.lowercase() != OtherNotes.lowercase() }
                                ?.map { it.diseaseCategory }?.distinct(),
                            "",
                            getString(R.string.hyphen_symbol)
                        )
                    )
                )
            }
            commonAdapter(dataList as MutableList<Map<String, Any>>)
        }
    }

    private fun commonAdapter(dataList: MutableList<Map<String, Any>>) {
        val adapter =
            PatientInfoAdapter(dataList, R.color.fragment_bg, (requireActivity() as BaseActivity))
        val isLandscape =
            resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

        val spanCount = if (isLandscape) 2 else 1
        if (CommonUtils.checkIsTablet(requireContext())) {
            binding.rvPatientInfo.layoutManager = GridLayoutManager(requireContext(), spanCount)
        } else {
            binding.rvPatientInfo.layoutManager = GridLayoutManager(requireContext(), 1)
        }
        binding.rvPatientInfo.adapter = adapter
        hideProgress()
    }

    private fun setNCDData(data: PatientListRespModel) {
        showProgress()
        data.name?.let { name ->
            setTitle(
                setTitleBasedOnRole(data, name)
            )
        }

        val cvdRiskLevel = data.cvdRiskScore?.let {
            Pair(
                StringConverter.appendTexts(
                    "${it}%",
                    data.cvdRiskLevel, separator = "-"
                ), CommonUtils.cvdRiskColorCode(it, requireContext())
            )
        }

        val dataList = mutableListOf(
            mapOf(
                DefinedParams.label to requireContext().getString(R.string.registration_date),
                DefinedParams.value to (data.enrollmentAt?.let {
                    DateUtils.convertDateFormat(
                        it,
                        DATE_TIME_yyyyMMddTHHmmssSSSXXX,
                        DATE_FORMAT_ddMMMyyyy
                    ).takeIf { it.isNotBlank() } ?: requireContext().getString(R.string.hyphen_symbol)
                } ?: requireContext().getString(R.string.pending_registration)).toString().trim()
            ),
            mapOf(
                DefinedParams.label to requireContext().getString(R.string.cvd_risk),
                DefinedParams.value to (cvdRiskLevel?.first
                    ?: requireContext().getString(R.string.hyphen_symbol)).toString().trim(),
                DefinedParams.color to cvdRiskLevel?.second
            ),
            mapOf(
                DefinedParams.label to requireContext().getString(R.string.patient_id),
                DefinedParams.value to (data.patientId
                    ?: requireContext().getString(R.string.hyphen_symbol)).toString().trim()
            ),
            mapOf(
                DefinedParams.label to requireContext().getString(R.string.bmi),
                DefinedParams.value to (CommonUtils.getBMIFormattedText(requireContext(), data.bmi).first
                    ?: requireContext().getString(R.string.hyphen_symbol)).toString().trim(),
                DefinedParams.color to CommonUtils.getBMIFormattedText(requireContext(), data.bmi).second
            ),
            mapOf(
                DefinedParams.label to requireContext().getString(R.string.national_id),
                DefinedParams.value to (data.identityValue
                    ?: requireContext().getString(R.string.hyphen_symbol)).toString().trim()
            ),
            mapOf(
                DefinedParams.label to requireContext().getString(R.string.high_risk),
                DefinedParams.value to (data.isPregnancyRisk ?: false),
                DefinedParams.Gender to (data.gender)
            ),
            mapOf(
                DefinedParams.label to requireContext().getString(R.string.contact_number),
                DefinedParams.value to (getContactNumber(data.phoneNumber.takeIf { it?.isNotBlank() == true }
                    ?.trim())
                    ?: requireContext().getString(R.string.hyphen_symbol))
            )
        )
        commonAdapter(dataList as MutableList<Map<String, Any>>)
    }

    private fun setTitleBasedOnRole(response: PatientListRespModel, text: String): String {
        return StringConverter.appendTexts(
            firstText = text,
            response.age.toString(),
            response.gender?.capitalizeFirstChar(),
            separator = getString(R.string.hyphen_symbol)
        ).capitalizeFirstChar()
    }
}