package com.medtroniclabs.spice.ui.medicalreview.abovefiveyears

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ScrollView
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResult
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.medtroniclabs.spice.R
import com.medtroniclabs.spice.appextensions.visible
import com.medtroniclabs.spice.common.CommonUtils.convertListToString
import com.medtroniclabs.spice.common.CommonUtils.formatListToStringWithOther
import com.medtroniclabs.spice.common.DateUtils
import com.medtroniclabs.spice.common.DefinedParams
import com.medtroniclabs.spice.common.SecuredPreference
import com.medtroniclabs.spice.common.ViewUtils.showDatePicker
import com.medtroniclabs.spice.data.AboveFiveYearsSummaryDetails
import com.medtroniclabs.spice.data.MedicalReviewMetaItems
import com.medtroniclabs.spice.data.model.MultiSelectDropDownModel
import com.medtroniclabs.spice.databinding.FragmentMedicalReviewTreatmentPlanSummaryBinding
import com.medtroniclabs.spice.formgeneration.extension.markMandatory
import com.medtroniclabs.spice.formgeneration.extension.safeClickListener
import com.medtroniclabs.spice.formgeneration.utility.CustomSpinnerAdapter
import com.medtroniclabs.spice.formgeneration.utility.MultiSelectSpinnerAdapter
import com.medtroniclabs.spice.network.resource.ResourceState
import com.medtroniclabs.spice.ui.BaseFragment
import com.medtroniclabs.spice.ui.assessment.referrallogic.utils.ReferralStatus
import com.medtroniclabs.spice.ui.medicalreview.diagnosis.viewmodel.DiagnosisViewModel
import com.medtroniclabs.spice.ui.medicalreview.utils.MedicalReviewDefinedParams
import com.medtroniclabs.spice.ui.medicalreview.utils.MedicalReviewTypeEnums

class AboveFiveYearsTreatmentSummaryFragment : BaseFragment(), View.OnClickListener {
    companion object {
        const val TAG = "AboveFiveYearsTreatmentSummaryFragment"
        fun newInstance(): AboveFiveYearsTreatmentSummaryFragment {
            return AboveFiveYearsTreatmentSummaryFragment()
        }
    }

    private lateinit var binding: FragmentMedicalReviewTreatmentPlanSummaryBinding
    private val viewModel: AboveFiveYearsViewModel by activityViewModels()
    private val chipItemViewModel: ClinicalNotesViewModel by activityViewModels()
    private val presentingComplaintsViewModel: PresentingComplaintsViewModel by activityViewModels()
    private val diagnosisViewModel : DiagnosisViewModel by activityViewModels()
    private var datePickerDialog: DatePickerDialog? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding =
            FragmentMedicalReviewTreatmentPlanSummaryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        initializeListener()
        attachObserver()
    }

    private fun initializeListener() {
        binding.tvNextMedicalReviewLabelText.safeClickListener(this)
    }

    private fun attachObserver() {
        viewModel.summaryMetaListItems.observe(viewLifecycleOwner) { resourceState ->
            when (resourceState.state) {
                ResourceState.LOADING -> {
                    showProgress()
                }

                ResourceState.SUCCESS -> {
                    hideProgress()
                    resourceState.data?.let { list ->
                        initializePatientStatus(list.filter { item -> item.category == MedicalReviewTypeEnums.patient_status.name }
                            .sortedBy { it.displayOrder })
                        initializeCostItem(list.filter { item -> item.category == MedicalReviewTypeEnums.cost.name }
                            .sortedBy { it.displayOrder })
                        initializeMedicalSupplies(list.filter { item -> item.category == MedicalReviewTypeEnums.medical_supplies.name }
                            .sortedBy { it.displayOrder })
                    }
                }

                ResourceState.ERROR -> {
                    hideProgress()
                }
            }
        }

        viewModel.summaryDetailsLiveData.observe(viewLifecycleOwner) { resourceState ->
            when (resourceState.state) {
                ResourceState.LOADING -> {
                    showProgress()
                }

                ResourceState.ERROR -> {
                    hideProgress()
                }

                ResourceState.SUCCESS -> {
                    hideProgress()
                    resourceState.data?.let {
                        renderSummaryDetails(it)
                    }
                    val swipeRefresh =
                        (activity as AboveFiveYearsBaseActivity).findViewById<SwipeRefreshLayout>(R.id.refreshLayout)
                    if (swipeRefresh.isRefreshing) {
                        swipeRefresh.isRefreshing = false
                    }
                }
            }
        }
    }

    private fun renderSummaryDetails(details: AboveFiveYearsSummaryDetails) {
        binding.tvPresentingComplaintsText.text = presentingComplaintsViewModel.selectedPresentingComplaints.map { it.name }.let {
            formatListToStringWithOther(
                it, details.presentingComplaintsNotes
            )
        }
        binding.tvDiagnosisText.text =
            diagnosisViewModel.diagnosisSaveUpdateResponse.value?.data?.let {list ->
                convertListToString(
                    ArrayList(list.map { it.diseaseCategory })
                )
            } ?: requireContext().getString(R.string.hyphen_symbol)
        binding.tvClinicalNotesText.text = chipItemViewModel.enteredClinicalNotes
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
    }

    private fun initView() {
        binding.tvNextMedicalReviewLabel.markMandatory()
        viewModel.getSummaryListMetaItems(MedicalReviewTypeEnums.AboveFiveYears.name)
    }

    private fun initializePatientStatus(patientStatusList: List<MedicalReviewMetaItems>) {
        val dropDownList = ArrayList<Map<String, Any>>()
        dropDownList.add(
            hashMapOf<String, Any>(
                DefinedParams.NAME to DefinedParams.DefaultIDLabel,
                DefinedParams.id to DefinedParams.DefaultID,
                DefinedParams.value to DefinedParams.DefaultIDLabel
            )
        )
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
        binding.tvPatientStatusSpinner.adapter = adapter
        var defaultPosition = 0
        for ((index, patientStatus) in dropDownList.withIndex()) {
            if (patientStatus[DefinedParams.NAME] as? String == ReferralStatus.OnTreatment.name) {
                defaultPosition = index
            }
        }
        binding.tvPatientStatusSpinner.setSelection(defaultPosition, false)
        binding.tvPatientStatusSpinner.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    adapterView: AdapterView<*>?,
                    view: View?,
                    pos: Int,
                    itemId: Long
                ) {
                    val selectedItem = adapter.getData(position = pos)
                    selectedItem?.let {
                        val selectedName = it[DefinedParams.NAME] as String?
                        if (selectedName != DefinedParams.DefaultIDLabel) {
                            viewModel.selectedPatientStatus = it[DefinedParams.value] as String
                        } else {
                            viewModel.selectedPatientStatus = null
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

    private fun updateNextFollowUpDate() {
        if (viewModel.selectedPatientStatus == ReferralStatus.Recovered.name) {
            if (viewModel.nextFollowupDate != null) {
                viewModel.nextFollowupDate = null
                binding.tvNextMedicalReviewLabelText.text = ""
            }
            binding.tvNextMedicalReviewLabelText.isEnabled = false
        } else {
            if(!binding.tvNextMedicalReviewLabelText.isEnabled){
                binding.tvNextMedicalReviewLabelText.isEnabled = true
            }
        }
    }

    private fun initializeCostItem(costList: List<MedicalReviewMetaItems>) {
        val dropDownList = ArrayList<Map<String, Any>>()
        dropDownList.add(
            hashMapOf<String, Any>(
                DefinedParams.NAME to DefinedParams.DefaultIDLabel,
                DefinedParams.id to DefinedParams.DefaultID,
                DefinedParams.value to DefinedParams.DefaultIDLabel
            )
        )
        for (item in costList) {
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
        binding.tvCostSpinner.adapter = adapter
        binding.tvCostSpinner.setSelection(0, false)
        binding.tvCostSpinner.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    adapterView: AdapterView<*>?,
                    view: View?,
                    pos: Int,
                    itemId: Long
                ) {
                    val selectedItem = adapter.getData(position = pos)
                    selectedItem?.let {
                        val selectedName = it[DefinedParams.NAME] as String?
                        if (selectedName != DefinedParams.DefaultIDLabel) {
                                viewModel.selectedCostItem = it[DefinedParams.value] as String
                        } else {
                            viewModel.selectedCostItem = null
                        }
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

    private fun initializeMedicalSupplies(supplyList: List<MedicalReviewMetaItems>) {
        val dropDownList = ArrayList<MultiSelectDropDownModel>()
        for (item in supplyList) {
            dropDownList.add(
                MultiSelectDropDownModel(
                    id = item.id,
                    name = item.name,
                    value = item.value
                )
            )
        }
        val adapter = MultiSelectSpinnerAdapter(requireContext(), dropDownList, viewModel.selectedMedicalSupplyListItem)
        binding.tvMedicalSupplySpinner.adapter = adapter
        adapter.setOnItemSelectedListener(object :
            MultiSelectSpinnerAdapter.OnItemSelectedListener {
            override fun onItemSelected(
                selectedItems: List<MultiSelectDropDownModel>,
                pos: Int,
            ) {
                if (selectedItems.isNotEmpty()){
                    viewModel.selectedMedicalSupplyListItem = ArrayList(selectedItems)
                }
                summaryListener()
            }
        }
        )
    }

    override fun onClick(view: View) {
        when (view.id) {
            binding.tvNextMedicalReviewLabelText.id -> {
                showDatePickerDialog()
            }
        }
    }

    private fun showDatePickerDialog() {
        var yearMonthDate: Triple<Int?, Int?, Int?>? = null
        if (!binding.tvNextMedicalReviewLabelText.text.isNullOrBlank())
            yearMonthDate =
                DateUtils.convertedMMMToddMM(binding.tvNextMedicalReviewLabelText.text.toString())

        if (datePickerDialog == null) {
            datePickerDialog = showDatePicker(
                context = requireContext(),
                minDate = DateUtils.getTomorrowDate(),
                date = yearMonthDate,
                cancelCallBack = { datePickerDialog = null }
            ) { _, year, month, dayOfMonth ->
                val stringDate = "$dayOfMonth-$month-$year"
                binding.tvNextMedicalReviewLabelText.text =
                    DateUtils.convertDateTimeToDate(
                        stringDate,
                        DateUtils.DATE_FORMAT_ddMMyyyy,
                        DateUtils.DATE_ddMMyyyy
                    )
                viewModel.nextFollowupDate = binding.tvNextMedicalReviewLabelText.text.toString()
                datePickerDialog = null
                summaryListener()
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

}