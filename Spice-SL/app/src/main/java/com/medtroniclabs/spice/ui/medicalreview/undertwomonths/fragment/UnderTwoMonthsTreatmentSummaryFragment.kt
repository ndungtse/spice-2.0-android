package com.medtroniclabs.spice.ui.medicalreview.undertwomonths.fragment

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResult
import androidx.recyclerview.widget.LinearLayoutManager
import com.medtroniclabs.spice.R
import com.medtroniclabs.spice.appextensions.gone
import com.medtroniclabs.spice.appextensions.invisible
import com.medtroniclabs.spice.appextensions.visible
import com.medtroniclabs.spice.common.CommonUtils
import com.medtroniclabs.spice.common.CommonUtils.convertAnyToString
import com.medtroniclabs.spice.common.CommonUtils.convertListToString
import com.medtroniclabs.spice.common.DateUtils
import com.medtroniclabs.spice.common.DefinedParams
import com.medtroniclabs.spice.common.SecuredPreference
import com.medtroniclabs.spice.common.ViewUtils
import com.medtroniclabs.spice.data.MedicalReviewMetaItems
import com.medtroniclabs.spice.data.resource.ExaminationResult
import com.medtroniclabs.spice.databinding.FragmentUnderTwoMonthsTreatmentSummaryBinding
import com.medtroniclabs.spice.formgeneration.extension.markMandatory
import com.medtroniclabs.spice.formgeneration.extension.safeClickListener
import com.medtroniclabs.spice.formgeneration.utility.CustomSpinnerAdapter
import com.medtroniclabs.spice.model.medicalreview.CreateUnderTwoMonthsResponse
import com.medtroniclabs.spice.model.medicalreview.ExaminationDetail
import com.medtroniclabs.spice.model.medicalreview.SummaryDetails
import com.medtroniclabs.spice.network.resource.ResourceState
import com.medtroniclabs.spice.ui.BaseFragment
import com.medtroniclabs.spice.ui.assessment.referrallogic.utils.ReferralStatus
import com.medtroniclabs.spice.ui.medicalreview.abovefiveyears.PresentingComplaintsViewModel
import com.medtroniclabs.spice.ui.medicalreview.undertwomonths.viewmodel.UnderTwoMonthsTreatmentSummaryViewModel
import com.medtroniclabs.spice.ui.medicalreview.utils.MedicalReviewDefinedParams
import com.medtroniclabs.spice.ui.medicalreview.utils.MedicalReviewTypeEnums
import com.medtroniclabs.spice.ui.mypatients.adapter.ExaminationSummaryAdapter


class UnderTwoMonthsTreatmentSummaryFragment : BaseFragment(), View.OnClickListener {

    private lateinit var binding: FragmentUnderTwoMonthsTreatmentSummaryBinding
    private var datePickerDialog: DatePickerDialog? = null
    private val summaryViewModel: UnderTwoMonthsTreatmentSummaryViewModel by activityViewModels()
    private lateinit var examinationSummaryAdapter: ExaminationSummaryAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentUnderTwoMonthsTreatmentSummaryBinding.inflate(inflater, container, false)
        return binding.root
    }

    companion object {
        const val TAG = "UnderTwoMonthsTreatmentSummaryFragment"
        fun newInstance() = UnderTwoMonthsTreatmentSummaryFragment()

        fun newInstance(
            encounterId: String?, patientReference: String?
        ): UnderTwoMonthsTreatmentSummaryFragment {
            val fragment = UnderTwoMonthsTreatmentSummaryFragment()
            val bundle = Bundle()
            bundle.putString(DefinedParams.EncounterId, encounterId)
            bundle.putString(DefinedParams.PatientReference, patientReference)
            fragment.arguments = bundle
            return fragment
        }

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        setListeners()
        attachObserver()
    }

    private fun initView() {
        binding.tvNextVisitTimeLabel.markMandatory()
        binding.tvPatientStatusLabel.markMandatory()
        summaryViewModel.getUnderTwoMonthsSummaryDetails(
            CreateUnderTwoMonthsResponse(
                encounterId = arguments?.getString(DefinedParams.EncounterId) ?: "",
                patientReference = arguments?.getString(DefinedParams.PatientReference) ?: ""
            )
        )
        examinationSummaryAdapter = ExaminationSummaryAdapter()
        binding.rvExaminationList.layoutManager = LinearLayoutManager(requireContext())
        binding.rvExaminationList.adapter = examinationSummaryAdapter
        summaryViewModel.getSummaryListMetaItems(MedicalReviewTypeEnums.UnderTwoMonths.name)
    }

    fun attachObserver() {
        summaryViewModel.summaryMetaListItems.observe(viewLifecycleOwner) { resourceState ->
            when (resourceState.state) {
                ResourceState.LOADING -> {
                    showProgress()
                }
                ResourceState.SUCCESS -> {
                    resourceState.data?.let { list ->
                        initializePatientStatus(list)
                    }
                }
                ResourceState.ERROR -> {
                    hideProgress()
                }
            }
        }
        summaryViewModel.summaryDetailsLiveData.observe(viewLifecycleOwner) { resourceState ->
            when (resourceState.state) {
                ResourceState.LOADING -> {
                    showProgress()
                }

                ResourceState.ERROR -> {
                    hideProgress()
                    showErrorDialog(getString(R.string.error), getString(R.string.no_internet_error))
                }

                ResourceState.SUCCESS -> {
                    showProgress()
                    resourceState.data?.let {
                        renderSummaryDetails(it)
                    }
                }
            }
        }
    }

    private fun renderSummaryDetails(details: SummaryDetails) {
        binding.apply {
            tvPresentingComplaints.text = details.presentingComplaints.takeIf { it != null }?.toString() ?: getString(R.string.empty__)
            tvClinicalNotes.text = details.clinicalNotes.toString()
            tvDiagnosis.text =
                details.diagnosis?.let {list ->
                    if (list.isNotEmpty()){
                        binding.tvDiagnosis.setTextColor(ContextCompat.getColor(requireContext(), R.color.a_red_error))
                    }
                    convertListToString(
                        ArrayList(list.map { it.diseaseCategory }.distinct())
                    )
                } ?: requireContext().getString(R.string.empty__)

            tvClinicalName.text = requireContext().getString(
                R.string.firstname_lastname,
                SecuredPreference.getUserDetails().firstName,
                SecuredPreference.getUserDetails().lastName
            )
            tvDateOfReviewValue.text = DateUtils.convertDateTimeToDate(
                DateUtils.getTodayDateDDMMYYYY(),
                DateUtils.DATE_FORMAT_yyyyMMddHHmmssZZZZZ,
                DateUtils.DATE_ddMMyyyy
            )
        }
        examinationList(details)
        binding.tvPrescription.text= details?.prescriptions.let { CommonUtils.createPrescription(it,requireContext()) }?.takeIf { it.isNotEmpty() }
            ?: requireContext().getString(R.string.empty__)
    }

    private fun examinationList(details: SummaryDetails) {
        val diseaseList = details.examination?.entries?.mapIndexed { index, (key, value) ->
            ExaminationResult(index + 1, key, convertExaminationDetailsToString(value))
        } ?: emptyList()
        details.examinationDisplayNames= mapOf("verySevereDisease" to "Very Severe Disease (PSBI)",
            "breastfeedingProblem" to "Feeding Problem (For all Breastfeeding)"
            , "diarrhoea" to "Diarrhoea", "jaundice" to "Jaundice", "hivInfection" to "Check for HIV Infection", "nonBreastfeedingProblem" to "Feeding Problem (For all Non-Breastfeeding)")
        var updatedExaminationResults: List<ExaminationResult>? =null
         updatedExaminationResults = diseaseList.map { result ->
            val matchingName = details.examinationDisplayNames?.get(result.symptomsTitle)
            if (matchingName != null) {
                result.copy(symptomsTitle = matchingName)
            } else {
                result
            }
        }
        updatedExaminationResults?.let { examinationSummaryAdapter.updateData(it) }
        if (diseaseList.isEmpty()) {
            binding.rvExaminationList.gone()
            binding.tvExaminationEmptyValue.visible()
        } else {
            binding.rvExaminationList.visible()
            binding.tvExaminationEmptyValue.invisible()
        }
        binding.underTwoMaterialCardView.visible()
        hideProgress()
    }

    private fun convertExaminationDetailsToString(details: List<ExaminationDetail>): List<String> {
        return details.map { detail ->
            val title = detail.title ?: ""
            val value = convertAnyToString(detail.value, requireContext())
            "$title : $value"
        }
    }

    private fun setListeners() {
        binding.etNextVisitDate.safeClickListener(this)
    }

    private fun showDatePickerDialog() {
        val yearMonthDate = binding.etNextVisitDate.text?.takeIf { it.isNotBlank() }
            ?.let { DateUtils.convertedMMMToddMM(it.toString()) }

        if (datePickerDialog == null) {
            datePickerDialog = ViewUtils.showDatePicker(
                context = requireContext(),
                minDate = DateUtils.getTomorrowDate(),
                date = yearMonthDate,
                cancelCallBack = { datePickerDialog = null }
            ) { _, year, month, dayOfMonth ->
                val stringDate = "$dayOfMonth-$month-$year"
                binding.etNextVisitDate.text = DateUtils.convertDateTimeToDate(
                    stringDate, DateUtils.DATE_FORMAT_ddMMyyyy, DateUtils.DATE_ddMMyyyy
                )
                summaryViewModel.nextVisitDate =
                    binding.etNextVisitDate.text.toString().trim().takeIf { it.isNotBlank() }
                datePickerDialog = null
                binding.tvNextVisitTimeError.gone()
                summaryListener()
            }
        }
    }


    override fun onClick(v: View?) {
        when (v?.id) {
            binding.etNextVisitDate.id -> {
                showDatePickerDialog()
            }
        }
    }

    private fun summaryListener() {
        setFragmentResult(
            MedicalReviewDefinedParams.SUMMARY_ITEM, bundleOf(
                MedicalReviewDefinedParams.SUMMARY_ITEM to true
            )
        )
    }

    private fun updateNextFollowUpDate() {
        if (summaryViewModel.selectedPatientStatus == ReferralStatus.Recovered.name) {
            if (summaryViewModel.nextVisitDate != null) {
                summaryViewModel.nextVisitDate = null
                binding.etNextVisitDate.text = ""
            }
            binding.etNextVisitDate.isEnabled = false
        } else {
            if (!binding.etNextVisitDate.isEnabled) {
                binding.etNextVisitDate.isEnabled = true
            }
        }
    }

    private fun initializePatientStatus(patientStatusList: List<MedicalReviewMetaItems>) {
        val dropDownList = ArrayList<Map<String, Any>>()
        for (item in patientStatusList) {
            dropDownList.add(
                hashMapOf<String, Any>(
                    DefinedParams.NAME to item.name,
                    DefinedParams.id to item.id.toString(),
                    DefinedParams.value to (item.value ?: item.name)
                )
            )
        }
        val adapter = CustomSpinnerAdapter(requireContext())
        adapter.setData(dropDownList)
        var defaultPosition = 0
        for ((index, patientStatus) in dropDownList.withIndex()) {
            if ((patientStatus[DefinedParams.value] as? String).equals(
                    ReferralStatus.OnTreatment.name,
                    true
                )
            ) {
                defaultPosition = index
            }
        }
        binding.tvPatientStatus.post {
            binding.tvPatientStatus.setSelection(defaultPosition, false)
        }
        binding.tvPatientStatus.adapter = adapter
        binding.tvPatientStatus.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    adapterView: AdapterView<*>?,
                    view: View?,
                    pos: Int,
                    itemId: Long
                ) {
                    val selectedItem = adapter.getData(position = pos)
                    selectedItem?.let {
                        val selectedName = it[DefinedParams.value] as String?
                        selectedName?.let { name ->
                            summaryViewModel.selectedPatientStatus = name
                        }
                        updateNextFollowUpDate()
                    }
                    summaryListener()
                }

                override fun onNothingSelected(p0: AdapterView<*>?) {
                    /**
                     * this method is not used
                     */
                }
            }
    }
}