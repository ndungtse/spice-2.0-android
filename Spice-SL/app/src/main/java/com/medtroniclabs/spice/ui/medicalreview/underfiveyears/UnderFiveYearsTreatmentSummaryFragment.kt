package com.medtroniclabs.spice.ui.medicalreview.underfiveyears

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
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.medtroniclabs.spice.R
import com.medtroniclabs.spice.appextensions.invisible
import com.medtroniclabs.spice.appextensions.visible
import com.medtroniclabs.spice.common.CommonUtils
import com.medtroniclabs.spice.common.CommonUtils.convertAnyToString
import com.medtroniclabs.spice.common.CommonUtils.convertListToString
import com.medtroniclabs.spice.common.CommonUtils.createInvestigation
import com.medtroniclabs.spice.common.DateUtils
import com.medtroniclabs.spice.common.DefinedParams
import com.medtroniclabs.spice.common.DefinedParams.OtherNotes
import com.medtroniclabs.spice.common.SecuredPreference
import com.medtroniclabs.spice.common.ViewUtils
import com.medtroniclabs.spice.data.resource.ExaminationResult
import com.medtroniclabs.spice.databinding.FragmentUnderFiveYearsTreatmentSummarryBinding
import com.medtroniclabs.spice.formgeneration.extension.markMandatory
import com.medtroniclabs.spice.formgeneration.extension.safeClickListener
import com.medtroniclabs.spice.formgeneration.utility.CustomSpinnerAdapter
import com.medtroniclabs.spice.model.medicalreview.CreateUnderTwoMonthsResponse
import com.medtroniclabs.spice.model.medicalreview.ExaminationDetail
import com.medtroniclabs.spice.model.medicalreview.SummaryDetails
import com.medtroniclabs.spice.network.resource.ResourceState
import com.medtroniclabs.spice.ui.BaseFragment
import com.medtroniclabs.spice.ui.assessment.referrallogic.utils.ReferralStatus
import com.medtroniclabs.spice.ui.medicalreview.utils.MedicalReviewDefinedParams
import com.medtroniclabs.spice.ui.medicalreview.utils.MedicalReviewTypeEnums
import com.medtroniclabs.spice.ui.mypatients.adapter.ExaminationSummaryAdapter
import com.medtroniclabs.spice.ui.mypatients.viewmodel.PatientDetailViewModel

class UnderFiveYearsTreatmentSummaryFragment : BaseFragment(), View.OnClickListener {

    private lateinit var binding: FragmentUnderFiveYearsTreatmentSummarryBinding
    private var datePickerDialog: DatePickerDialog? = null
    private val summaryViewModel: UnderFiveYearTreatmentSummaryViewModel by activityViewModels()
    private val patientDetailViewModel: PatientDetailViewModel by activityViewModels()
    private lateinit var examinationSummaryAdapter: ExaminationSummaryAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentUnderFiveYearsTreatmentSummarryBinding.inflate(inflater, container, false)
        return binding.root
    }

    companion object {
        const val TAG = "UnderFiveYearsTreatmentSummaryFragment"
        fun newInstance() = UnderFiveYearsTreatmentSummaryFragment()

        fun newInstance(
            encounterId: String?, patientReference: String?
        ): UnderFiveYearsTreatmentSummaryFragment {
            val fragment = UnderFiveYearsTreatmentSummaryFragment()
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
        summaryViewModel.setMetaPatientStatus(MedicalReviewTypeEnums.patient_status.name)
        summaryViewModel.getUnderFiveYearsSummaryDetails(
            CreateUnderTwoMonthsResponse(
                encounterId = arguments?.getString(DefinedParams.EncounterId) ?: "",
                patientReference = arguments?.getString(DefinedParams.PatientReference) ?: ""
            )
        )
        binding.tvNextVisitTimeLabel.markMandatory()
        binding.tvPatientStatusLabel.markMandatory()
        examinationSummaryAdapter = ExaminationSummaryAdapter()
        binding.rvExaminationList.layoutManager = LinearLayoutManager(requireContext())
        binding.rvExaminationList.adapter = examinationSummaryAdapter
    }

    fun attachObserver() {
        summaryViewModel.metaLiveDataPatientStatus.observe(viewLifecycleOwner) {
            val statusList = ArrayList<Map<String, Any>>()
            for (item in it) {
                statusList.add(
                    hashMapOf<String, Any>(
                        DefinedParams.NAME to item.name,
                        DefinedParams.id to item.id.toString(),
                        DefinedParams.value to (item.value ?: item.name)
                    )
                )
            }
            setListenerToDeliveryStatus(statusList)
        }

        summaryViewModel.summaryDetailsLiveData.observe(viewLifecycleOwner) { resourceState ->
            when (resourceState.state) {
                ResourceState.LOADING -> {
                    showProgress()
                }

                ResourceState.ERROR -> {
                    hideProgress()
                    showErrorDialog(
                        title = getString(R.string.alert),
                        message = getString(R.string.something_went_wrong_try_later),
                    )
                }

                ResourceState.SUCCESS -> {
                    hideProgress()
                    resourceState.data?.let {
                        renderSummaryDetails(it)
                    }
                }
            }
            val swipeRefresh =
                (activity as UnderFiveYearsBaseActivity).findViewById<SwipeRefreshLayout>(R.id.refreshLayout)
            if (swipeRefresh.isRefreshing) {
                swipeRefresh.isRefreshing = false
            }
        }
    }

    private fun renderSummaryDetails(details: SummaryDetails) {
        binding.tvPresentingComplaints.text = details.presentingComplaints.takeIf { it != null }?.toString() ?: getString(R.string.empty__)
        binding.tvClinicalNotes.text = details.clinicalNotes.toString()
        binding.tvClinicalName.text = requireContext().getString(
            R.string.firstname_lastname,
            SecuredPreference.getUserDetails().firstName,
            SecuredPreference.getUserDetails().lastName
        )
        binding.tvDateOfReviewValue.text = DateUtils.convertDateTimeToDate(
            DateUtils.getTodayDateDDMMYYYY(),
            DateUtils.DATE_FORMAT_yyyyMMddHHmmssZZZZZ,
            DateUtils.DATE_ddMMyyyy
        )
        binding.tvDiagnosis.text =
            details.diagnosis?.let {list ->
                if (list.isNotEmpty()){
                    binding.tvDiagnosis.setTextColor(ContextCompat.getColor(requireContext(), R.color.a_red_error))
                }
                convertListToString(
                    ArrayList(list.filter { it.diseaseCategory.lowercase() != OtherNotes.lowercase() }
                        .map { it.diseaseCategory }.distinct())
                )
            } ?: requireContext().getString(R.string.empty__)

        binding.tvPrescription.text= details.prescriptions.let { CommonUtils.createPrescription(it,requireContext()) }?.takeIf { it.isNotEmpty() }
            ?: requireContext().getString(R.string.empty__)

        binding.tvInvestigation.text = details.investigations?.let { createInvestigation(it,requireContext()) }?.takeIf { it.isNotEmpty() }
            ?: requireContext().getString(R.string.hyphen_symbol)

        examinationList(details)
    }

    private fun examinationList(details: SummaryDetails) {
        val diseaseList = details.examination?.entries?.mapIndexed { index, (key, value) ->
            ExaminationResult(index + 1, key, convertExaminationDetailsToString(value))
        } ?: emptyList()
        val updatedExaminationResults: List<ExaminationResult> = diseaseList.map { result ->
            val matchingName = details.examinationDisplayNames?.get(result.symptomsTitle)
            result.copy(
                symptomsTitle = if (matchingName != null) {
                    "${result.index}. $matchingName"
                } else {
                    result.symptomsTitle
                }
            )
        }
        updatedExaminationResults.let { examinationSummaryAdapter.updateData(it) }
        if (diseaseList.isEmpty()) {
            binding.rvExaminationList.invisible()
            binding.tvExaminationEmptyValue.visible()
        } else {
            binding.rvExaminationList.visible()
            binding.tvExaminationEmptyValue.invisible()
        }
    }

    private fun convertExaminationDetailsToString(details: List<ExaminationDetail>): List<String> {
        return details.map { detail ->
            val title = detail.title ?: ""
            val value = convertAnyToString(detail.value,requireContext())
            "$title : $value"
        }
    }

    private fun setListeners() {
        binding.etNextVisitDate.safeClickListener(this)
    }


    private fun showDatePickerDialog() {
        var yearMonthDate: Triple<Int?, Int?, Int?>? = null
        if (!binding.etNextVisitDate.text.isNullOrBlank()) yearMonthDate =
            DateUtils.convertedMMMToddMM(binding.etNextVisitDate.text.toString())
        if (datePickerDialog == null) {
            datePickerDialog = ViewUtils.showDatePicker(context = requireContext(),
                minDate = DateUtils.getTomorrowDate(),
                date = yearMonthDate,
                cancelCallBack = { datePickerDialog = null }) { _, year, month, dayOfMonth ->
                val stringDate = "$dayOfMonth-$month-$year"
                binding.etNextVisitDate.text = DateUtils.convertDateTimeToDate(
                    stringDate, DateUtils.DATE_FORMAT_ddMMyyyy, DateUtils.DATE_ddMMyyyy
                )
                summaryViewModel.nextVisitDate =
                    binding.etNextVisitDate.text.toString().trim().ifBlank { null }
                datePickerDialog = null
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

    private fun setListenerToDeliveryStatus(list: ArrayList<Map<String, Any>>) {

        val adapter = CustomSpinnerAdapter(requireContext())
        adapter.setData(list)
        var defaultPosition = 0
        for ((index, patientStatus) in list.withIndex()) {
            if ((patientStatus[DefinedParams.value] as? String).equals(
                    ReferralStatus.OnTreatment.name,
                    true
                )
            ) {
                defaultPosition = index
            }
        }
        binding.patientStatusSpinner.post {
            binding.patientStatusSpinner.setSelection(defaultPosition, false)
        }
        binding.patientStatusSpinner.adapter = adapter
        binding.patientStatusSpinner.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?, view: View?, position: Int, id: Long
                ) {
                    val selectedItem = adapter.getData(position = position)
                    selectedItem?.let {
                        val selectedName = it[DefinedParams.value] as String?
                        selectedName?.let { name ->
                            summaryViewModel.selectedPatientStatus = name
                        }
                        updateNextFollowUpDate()
                    }
                    summaryListener()
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                    /**
                     * this method is not used
                     */
                }

            }
    }

    private fun updateNextFollowUpDate() {
        if (summaryViewModel.selectedPatientStatus?.equals(ReferralStatus.Recovered.name, true) == true) {
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
}


