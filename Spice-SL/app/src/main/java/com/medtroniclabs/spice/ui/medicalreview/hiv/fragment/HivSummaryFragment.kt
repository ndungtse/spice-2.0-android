package com.medtroniclabs.spice.ui.medicalreview.hiv.fragment

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
import com.medtroniclabs.spice.appextensions.gone
import com.medtroniclabs.spice.appextensions.invisible
import com.medtroniclabs.spice.appextensions.visible
import com.medtroniclabs.spice.common.CommonUtils
import com.medtroniclabs.spice.common.DateUtils
import com.medtroniclabs.spice.common.DefinedParams
import com.medtroniclabs.spice.common.DefinedParams.Other
import com.medtroniclabs.spice.common.SecuredPreference
import com.medtroniclabs.spice.common.ViewUtils
import com.medtroniclabs.spice.data.model.HivCreateScreeningSummaryResponse
import com.medtroniclabs.spice.data.model.HivScreeningResponse
import com.medtroniclabs.spice.data.resource.ExaminationResult
import com.medtroniclabs.spice.databinding.FragmentHivSummaryBinding
import com.medtroniclabs.spice.formgeneration.extension.capitalizeFirstChar
import com.medtroniclabs.spice.formgeneration.extension.markMandatory
import com.medtroniclabs.spice.formgeneration.extension.safeClickListener
import com.medtroniclabs.spice.formgeneration.utility.CustomSpinnerAdapter
import com.medtroniclabs.spice.network.resource.ResourceState
import com.medtroniclabs.spice.ui.BaseFragment
import com.medtroniclabs.spice.ui.assessment.referrallogic.utils.ReferralStatus
import com.medtroniclabs.spice.ui.medicalreview.hiv.activity.HivMedicalReviewBaseActivity
import com.medtroniclabs.spice.ui.medicalreview.hiv.viewmodel.HivViewModel
import com.medtroniclabs.spice.ui.medicalreview.utils.MedicalReviewDefinedParams
import com.medtroniclabs.spice.ui.medicalreview.utils.MedicalReviewTypeEnums
import com.medtroniclabs.spice.ui.mypatients.adapter.ExaminationSummaryAdapter

class HivSummaryFragment : BaseFragment(), View.OnClickListener {
    private lateinit var binding: FragmentHivSummaryBinding
    private val hivViewModel: HivViewModel by activityViewModels()
    private lateinit var examinationSummaryAdapter: ExaminationSummaryAdapter
    private var datePickerDialog: DatePickerDialog? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHivSummaryBinding.inflate(inflater, container, false)
        return binding.root
    }

    companion object {
        const val TAG = "HivSummaryFragment"
        fun newInstance(): HivSummaryFragment {
            val args = Bundle()
            val fragment = HivSummaryFragment()
            fragment.arguments = args
            return fragment
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        setListeners()
        attachObserver()
    }

    fun initView() {
        binding.tvNextMedicalReviewLabel.markMandatory()
        binding.tvPatientStatus.markMandatory()
        if (arguments?.getBoolean(
                DefinedParams.EMTCT,
                false
            ) == false
        ) {
            binding.groupHivHbsag?.gone()
        }
        hivViewModel.getHivScreeningDetails(
            HivScreeningResponse(
                encounterId = arguments?.getString(DefinedParams.EncounterId) ?: "",
                patientReference = arguments?.getString(DefinedParams.PatientReference) ?: ""
            )
        )

        examinationSummaryAdapter = ExaminationSummaryAdapter()
        binding.rvExaminationList.layoutManager = LinearLayoutManager(requireContext())
        binding.rvExaminationList.adapter = examinationSummaryAdapter
    }

    private fun attachObserver() {
        hivViewModel.hivScreeningDetailsLiveData.observe(viewLifecycleOwner) { resourceState ->
            when (resourceState.state) {
                ResourceState.LOADING -> {
                    showProgress()
                }

                ResourceState.SUCCESS -> {
                    hideProgress()
                    resourceState.data?.let {
                        renderSummaryDetails(it)
                    }
                }

                ResourceState.ERROR -> {
                    hideProgress()
                    showErrorDialogue(
                        title = getString(R.string.alert),
                        message = getString(R.string.something_went_wrong_try_later),
                        positiveButtonName = getString(R.string.ok),
                    ) {}
                }
            }
        }

        val swipeRefresh =
            (activity as HivMedicalReviewBaseActivity).findViewById<SwipeRefreshLayout>(R.id.refreshLayout)
        if (swipeRefresh.isRefreshing) {
            swipeRefresh.isRefreshing = false
        }
    }

    private fun renderSummaryDetails(response: HivCreateScreeningSummaryResponse) {
        with(binding) {
//             Diagnosis Text
            val diagnosisList = response.diagnosis ?: emptyList()
            if (diagnosisList.isNotEmpty()) {
                tvDiagnosesText.setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.a_red_error
                    )
                )
            }
            tvDiagnosesText.text = diagnosisList
                .filter {
                    it.diseaseCategory.lowercase() != DefinedParams.OtherNotes.lowercase()
                }
                .map { it.diseaseCategory }
                .distinct()
                .takeIf { it.isNotEmpty() }
                ?.let { CommonUtils.convertListToString(ArrayList(it)) }
                ?: getString(R.string.hyphen_symbol)
        }
        binding.tvClinicalName.text = requireContext().getString(
            R.string.firstname_lastname,
            SecuredPreference.getUserDetails()?.firstName,
            SecuredPreference.getUserDetails()?.lastName
        )
        binding.tvDateOfReviewValue.text = DateUtils.convertDateTimeToDate(
            DateUtils.getTodayDateDDMMYYYY(),
            DateUtils.DATE_FORMAT_yyyyMMddHHmmssZZZZZ,
            DateUtils.DATE_ddMMyyyy
        )
        binding.tvA1TestText.text = response.a1TestResult?.takeIf { it.isNotEmpty() } ?: "-"
        binding.tvA2TestText.text = response.a2TestResult?.takeIf { it.isNotEmpty() } ?: "-"
        binding.tvA3TestText.text = response.a3TestResult?.takeIf { it.isNotEmpty() } ?: "-"
        binding.tvEntryPointText.text = generateEntryPointText(response) ?: "-"
        binding.tvHivSyphilisText.text =
            response.hivSyphilisDuoTest?.takeIf { it.isNotEmpty() } ?: "-"
        binding.tvHbsgText.text =
            response.hbsAGTest?.takeIf { it.isNotEmpty() } ?: "-"
        binding.tvClinicalNotesText.text =  response.clinicalNotes?.takeIf { it.isNotEmpty() } ?: "-"

        // Prescription
        binding.tvPrescrptionText.text = response.prescriptions
            ?.let { CommonUtils.createPrescription(it, requireContext()) }
            ?.takeIf { it.isNotEmpty() }
            ?: getString(R.string.hyphen_symbol)

        // Investigation
        binding.tvInvestigationText.text = response.investigations
            ?.let { CommonUtils.createInvestigation(it, requireContext()) }
            ?.takeIf { it.isNotEmpty() }
            ?: getString(R.string.hyphen_symbol)
        val dropDownList = ArrayList<Map<String, Any>>()
        val (test1, test2, test3) = if (response.hbsAGTest.isNullOrEmpty())
            Triple(response.a1TestResult, response.a2TestResult, response.a3TestResult)
        else
            Triple(response.hbsAGTest, response.a2TestResult, response.a3TestResult)

        val nextVisitType = getTestResultStatus(test1, test2, test3)

        binding.tvNextMedicalReviewLabelText.text = when (nextVisitType) {
            1 -> DateUtils.getFormattedDateAfterMonths(1)
            2 -> DateUtils.getFormattedDateAfterDays(14)
            else -> DateUtils.getFormattedDateAfterMonths(1)
        }
        hivViewModel.nextVisitDate = binding.tvNextMedicalReviewLabelText.text.toString().trim()
        if (!response.summaryStatus.isNullOrEmpty()) {
            for (item in response.summaryStatus) {
                dropDownList.add(
                    hashMapOf<String, Any>(
                        DefinedParams.NAME to item.name,
                        DefinedParams.Value to item.value
                    )
                )
            }
            setListenerToDeliveryStatus(dropDownList)
        }
        renderSymptoms(response)
    }
    private fun getTestResultStatus(a1: String?, a2: String?, a3: String?): Int {
        val testResults = listOf(a1, a2, a3)

        return when {
            testResults.all { it.equals(getString(R.string.reactive), ignoreCase = true) } -> 1
            testResults.any { it.equals(getString(R.string.inconclusive), ignoreCase = true) } -> 2
            else -> 3
        }
    }

    private fun generateEntryPointText(response: HivCreateScreeningSummaryResponse): String? {
       return response.entryPoint?.let {
            if (it.isNotEmpty()) {
                if (it.equals(Other, true)) {
                   return "${it.capitalizeFirstChar()} - ${hivViewModel.otherEntryPoint}"
                }
                return it
            } else{
                return getString(R.string.seperator_hyphen)
            }
        }
    }

    private fun renderSymptoms(response: HivCreateScreeningSummaryResponse) {
        hivViewModel.getHivPatientStatusByCategory(MedicalReviewTypeEnums.patient_status.name)

        val eligibility = mapOf(
            "${getString(R.string.symptoms)} - " to response.eligibilities?.Symptoms,
            "${getString(R.string.hivPopulationType)} - " to response.eligibilities?.hivPopulationType
        )

        val list = eligibility.filterNot { it.value.isNullOrEmpty() }
            .map { (key, value) ->
                ExaminationResult(symptomsTitle = key, description = value)
            }

        if (list.isNotEmpty()) {
            examinationSummaryAdapter.updateData(list)
            binding.rvExaminationList.visible()
            binding.tvExaminationEmptyValue.invisible()
        } else {
            binding.rvExaminationList.invisible()
            binding.tvExaminationEmptyValue.visible()
        }
    }

    private fun setListeners() {
        binding.tvNextMedicalReviewLabelText.safeClickListener(this)
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            binding.tvNextMedicalReviewLabelText.id -> {
                showDatePickerDialog()
            }
        }
    }

    private fun showDatePickerDialog() {
        val yearMonthDate = binding.tvNextMedicalReviewLabelText.text?.takeIf { it.isNotBlank() }
            ?.let { DateUtils.convertedMMMToddMM(it.toString()) }
        if (datePickerDialog == null) {
            datePickerDialog = ViewUtils.showDatePicker(
                context = requireContext(),
                minDate = DateUtils.getTomorrowDate(),
                date = yearMonthDate,
                cancelCallBack = { datePickerDialog = null }
            ) { _, year, month, dayOfMonth ->
                val stringDate = "$dayOfMonth-$month-$year"
                binding.tvNextMedicalReviewLabelText.text = DateUtils.convertDateTimeToDate(
                    stringDate, DateUtils.DATE_FORMAT_ddMMyyyy, DateUtils.DATE_ddMMyyyy
                )
                hivViewModel.nextVisitDate =
                    binding.tvNextMedicalReviewLabelText.text.toString().trim()
                        .takeIf { it.isNotBlank() }
                datePickerDialog = null
                binding.tvNextMedicalReviewError.invisible()
                summaryListener()
            }
        }
    }


    private fun setListenerToDeliveryStatus(list: ArrayList<Map<String, Any>>) {
        val adapter = CustomSpinnerAdapter(requireContext())
        adapter.setData(list)
        var defaultPosition = 0
        for ((index, patientStatus) in list.withIndex()) {
            if ((patientStatus[DefinedParams.Value] as? String).equals(
                    ReferralStatus.OnTreatment.name,
                    true
                )
            ) {
                defaultPosition = index
            }
        }
        binding.tvPatientStatusSpinner.post {
            binding.tvPatientStatusSpinner.setSelection(defaultPosition, false)
        }

        binding.tvPatientStatusSpinner.adapter = adapter
        binding.tvPatientStatusSpinner.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?, view: View?, position: Int, id: Long
                ) {
                    val selectedItem = adapter.getData(position = position)
                    selectedItem?.let {
                        val selectedName = it[DefinedParams.Value] as String?
                        selectedName?.let { name ->
                            hivViewModel.selectedPatientStatus = name
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
        if (hivViewModel.selectedPatientStatus?.equals(
                ReferralStatus.Recovered.name,
                true
            ) == true || hivViewModel.selectedPatientStatus?.equals(
                ReferralStatus.Died.name,
                true
            ) == true
        ) {
            if (hivViewModel.nextVisitDate != null) {
                hivViewModel.nextVisitDate = null
                binding.tvNextMedicalReviewLabelText.text = ""
            }
            binding.tvNextMedicalReviewLabelText.isEnabled = false
        } else {
            if (!binding.tvNextMedicalReviewLabelText.isEnabled) {
                binding.tvNextMedicalReviewLabelText.isEnabled = true
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

    fun validation(): Boolean {
        var isValid = true
        if ( hivViewModel.nextVisitDate != null ) {
            binding.tvNextMedicalReviewError.invisible()
        } else{
            binding.tvNextMedicalReviewError.visible()
            isValid = false
        }

        if (hivViewModel.selectedPatientStatus.isNullOrEmpty()) {
            binding.tvPatientError.visible()
            isValid = false
        } else binding.tvPatientError.gone()

        return isValid
    }


}