package com.medtroniclabs.spice.ui.assessment.fragment

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.medtroniclabs.spice.R
import com.medtroniclabs.spice.common.DateUtils
import com.medtroniclabs.spice.common.DefinedParams
import com.medtroniclabs.spice.common.DefinedParams.Other
import com.medtroniclabs.spice.common.StringConverter
import com.medtroniclabs.spice.common.ViewUtils
import com.medtroniclabs.spice.databinding.FragmentAssessmentOtherSymptomSummaryBinding
import com.medtroniclabs.spice.db.entity.HealthFacilityEntity
import com.medtroniclabs.spice.formgeneration.extension.safeClickListener
import com.medtroniclabs.spice.formgeneration.utility.CustomSpinnerAdapter
import com.medtroniclabs.spice.model.AssessmentSummaryModel
import com.medtroniclabs.spice.network.resource.ResourceState
import com.medtroniclabs.spice.ui.MenuConstants.OTHER_SYMPTOMS
import com.medtroniclabs.spice.ui.assessment.AssessmentCommonUtils
import com.medtroniclabs.spice.ui.assessment.AssessmentDefinedParams
import com.medtroniclabs.spice.ui.assessment.AssessmentDefinedParams.Amoxicillin
import com.medtroniclabs.spice.ui.assessment.AssessmentDefinedParams.AssessmentNotes
import com.medtroniclabs.spice.ui.assessment.AssessmentDefinedParams.Dispensed
import com.medtroniclabs.spice.ui.assessment.AssessmentDefinedParams.OtherSymptoms
import com.medtroniclabs.spice.ui.assessment.AssessmentDefinedParams.Summary
import com.medtroniclabs.spice.ui.assessment.AssessmentDefinedParams.hasFever
import com.medtroniclabs.spice.ui.assessment.AssessmentDefinedParams.otherConcerningSymptoms
import com.medtroniclabs.spice.ui.assessment.AssessmentDefinedParams.otherSymptoms
import com.medtroniclabs.spice.ui.assessment.referrallogic.utils.ReferralStatus
import com.medtroniclabs.spice.ui.assessment.viewmodel.AssessmentViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AssessmentOtherSymptomSummaryFragment : Fragment(), View.OnClickListener {

    lateinit var binding: FragmentAssessmentOtherSymptomSummaryBinding
    private val viewModel: AssessmentViewModel by activityViewModels()
    private var datePickerDialog: DatePickerDialog? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAssessmentOtherSymptomSummaryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
        setListeners()
        attachObservers()
    }

    private fun initViews() {
        binding.btnDone.safeClickListener(this)
        binding.etNotes.addTextChangedListener { input ->
            input?.let {
                val resultValue = input.trim().toString()
                if (resultValue.isNotBlank()) {
                    viewModel.otherAssessmentDetails[AssessmentNotes] = resultValue
                }
            }
        }
    }

    private fun setListeners() {
        binding.btnDone.safeClickListener(this)
        binding.etNotes.addTextChangedListener { input ->
            input?.let {
                val resultValue = input.trim().toString()
                if (resultValue.isNotBlank()) {
                    viewModel.otherAssessmentDetails[AssessmentNotes] = resultValue
                }
            }
        }
        binding.etNextFollowUpDate.safeClickListener(this)

    }

    private fun attachObservers() {
        viewModel.assessmentStringLiveData.value?.let {
            updateStatusBar()
            createSummaryView(createListSummaryData(it))
        }

        viewModel.nearestFacilityLiveData.observe(viewLifecycleOwner) { resourceState ->
            when (resourceState.state) {
                ResourceState.SUCCESS -> {
                    resourceState.data?.let { siteList ->
                        loadPhuSitesList(siteList)
                    }
                }

                else -> {}
            }
        }
    }

    private fun updateStatusBar() {
        when(viewModel.referralStatus){
            ReferralStatus.Referred.name -> {
                binding.phuReferredGroup.visibility = View.VISIBLE
                binding.riskResultLayout.backgroundTintList = ContextCompat.getColorStateList(requireContext(), R.color.attention_color)
                binding.riskResultLayout.text = getString(R.string.referred_for_further_assessment)
            }
            ReferralStatus.OnTreatment.name -> {
                binding.coughMalariaGroup.visibility = View.VISIBLE
                binding.etNextFollowUpDate.text = DateUtils.getDateAfterDays(3)
                binding.riskResultLayout.backgroundTintList = ContextCompat.getColorStateList(requireContext(), R.color.red_risk_moderate)
                binding.riskResultLayout.text = getString(R.string.patient_on_treatment)
            }
            else -> {
                binding.riskResultLayout.backgroundTintList = ContextCompat.getColorStateList(requireContext(), R.color.green_attention_color)
                binding.riskResultLayout.text = getString(R.string.no_refferral_treatment_required)
            }
        }
    }

    private fun loadPhuSitesList(healthFacilityList: List<HealthFacilityEntity>) {
        val dropDownList = ArrayList<Map<String, Any>>()
        dropDownList.add(
            hashMapOf<String, Any>(
                DefinedParams.NAME to DefinedParams.DefaultIDLabel,
                DefinedParams.id to DefinedParams.DefaultID
            )
        )
        var defaultPosition = 0
        for ((index, healthFacilityEntity) in healthFacilityList.withIndex()) {
            dropDownList.add(
                hashMapOf<String, Any>(
                    DefinedParams.NAME to healthFacilityEntity.name,
                    DefinedParams.id to healthFacilityEntity.fhirId.toString()
                )
            )
            if (healthFacilityEntity.isDefault) {
                defaultPosition = index
            }
        }
        val adapter = CustomSpinnerAdapter(requireContext())
        adapter.setData(dropDownList)
        binding.etPhuChange.adapter = adapter
        binding.etPhuChange.setSelection(0, false)
        binding.etPhuChange.post {
            binding.etPhuChange.setSelection(defaultPosition + 1, false)
        }
        binding.etPhuChange.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    adapterView: AdapterView<*>?,
                    view: View?,
                    pos: Int,
                    itemId: Long
                ) {
                    val selectedItem = adapter.getData(position = pos)
                    selectedItem?.let {
                        val selectedId = it[DefinedParams.id] as String?
                        val selectedSiteName = it[DefinedParams.NAME] as String?
                        if (selectedId != DefinedParams.DefaultID) {
                            viewModel.otherAssessmentDetails[AssessmentDefinedParams.ReferredPHUSite] = selectedSiteName ?: ""
                            viewModel.otherAssessmentDetails[AssessmentDefinedParams.ReferredPHUSiteID] = selectedId?.toLong() ?: -1L
                        } else {
                            if (viewModel.otherAssessmentDetails.containsKey(AssessmentDefinedParams.ReferredPHUSite))
                                viewModel.otherAssessmentDetails.remove(AssessmentDefinedParams.ReferredPHUSite)
                        }
                    }
                }

                override fun onNothingSelected(p0: AdapterView<*>?) {
                    /**
                     * this method is not used
                     */
                }
            }
    }

    private fun createSummaryView(
        listSummaryData: MutableList<AssessmentSummaryModel>?
    ) {
        listSummaryData?.let { summaryData ->
            binding.emptyErrorMessage.visibility = View.GONE
            binding.parentLayout.visibility = View.VISIBLE
            binding.parentLayout.removeAllViews()
            renderSummaryView(summaryData)
        } ?: kotlin.run {
            showErrorInSummary()
        }
    }

    private fun renderSummaryView(summaryData: MutableList<AssessmentSummaryModel>) {
        bindSummaryView(
            getString(R.string.patient_status),
            viewModel.referralStatus ?: getString(R.string.seperator_hyphen)
        )
        renderDangerSigns(summaryData)
        summaryData.filter { it.title?.lowercase() != AssessmentDefinedParams.General_Danger_Signs.lowercase() }.forEach { item ->
            when (item.id) {
                hasFever -> {
                    if (item.value == DefinedParams.Yes) {
                        bindSummaryView(
                            item.title,
                            requireContext().getString(
                                R.string.nutrition_summary,
                                item.value,
                                getString(R.string.malaria)
                            )
                        )
                    } else {
                        bindSummaryView(item.title, item.value)
                    }
                }

                Amoxicillin.lowercase() -> {
                    if (item.value == Dispensed){
                        bindSummaryView(Dispensed, item.title)
                    }
                }

                else -> {
                        bindSummaryView(item.title, item.value)
                }
            }
        }
    }

    private fun renderDangerSigns(summaryData: MutableList<AssessmentSummaryModel>) {
        summaryData.find { it.id == otherSymptoms }?.let { item ->
            val result = if (item.value == Other ) {
                summaryData.find { it.id == otherConcerningSymptoms }?.let {otherItem ->
                    requireContext().getString(R.string.other_value, item.value, otherItem.value)
                }
            } else item.value
            bindSummaryView(getString(R.string.general_danger_signs), result ?: getString(R.string.seperator_hyphen))
        }
    }

    private fun bindSummaryView(title: String?, value: String?, valueTextColor: Int? = null) {
        value?.let { result ->
            binding.parentLayout.addView(
                AssessmentCommonUtils.addViewSummaryLayout(
                    title,
                    result,
                    valueTextColor,
                    requireContext()
                )
            )
        }
    }

    private fun createListSummaryData(data: String): MutableList<AssessmentSummaryModel>? {
        return viewModel.formLayoutsLiveData.value?.data?.formLayout?.filter { it.isSummary == true }?.map { formLayout ->
            AssessmentSummaryModel(
                title = formLayout.titleSummary ?: formLayout.title,
                id = formLayout.id,
                cultureValue = formLayout.titleCulture,
                value = AssessmentCommonUtils.getValueOfKeyFromMap(
                    StringConverter.stringToMap(data),
                    formLayout.id,
                    OTHER_SYMPTOMS
                ),
                noOfDays = formLayout.noOfDays
            )
        }?.toMutableList()
    }

    private fun showErrorInSummary() {
        binding.emptyErrorMessage.visibility = View.VISIBLE
        binding.parentLayout.visibility = View.GONE
    }

    override fun onClick(view: View) {
        when (view.id) {
            binding.btnDone.id -> {
                binding.etNextFollowUpDate.text?.let {
                    updateFollowUpDate(it.trim().toString())
                }
                //addOtherDetailsToType(Summary.lowercase())
                viewModel.updateOtherAssessmentDetails()
            }

            binding.etNextFollowUpDate.id -> {
                showDatePickerDialog()
            }
        }
    }

    private fun addOtherDetailsToType(key: String) {
        val otherDetailsMap = HashMap<String,Any>()
        otherDetailsMap[key] = viewModel.otherAssessmentDetails
        viewModel.otherAssessmentDetails = otherDetailsMap
    }

    private fun showDatePickerDialog() {
        var yearMonthDate: Triple<Int?, Int?, Int?>? = null
        if (!binding.etNextFollowUpDate.text.isNullOrBlank())
            yearMonthDate =
                DateUtils.convertddMMMToddMM(binding.etNextFollowUpDate.text.toString())
        if (datePickerDialog == null) {
            datePickerDialog = ViewUtils.showDatePicker(
                context = requireContext(),
                minDate = DateUtils.getTomorrowDate(),
                date = yearMonthDate,
                cancelCallBack = { datePickerDialog = null }
            ) { _, year, month, dayOfMonth ->
                val stringDate = "$dayOfMonth-$month-$year"
                binding.etNextFollowUpDate.text =
                    DateUtils.convertDateTimeToDate(
                        stringDate,
                        DateUtils.DATE_FORMAT_ddMMyyyy,
                        DateUtils.DATE_ddMMyyyy
                    )
                updateFollowUpDate(binding.etNextFollowUpDate.text.toString().trim())
                datePickerDialog = null
            }
        }
    }

    private fun updateFollowUpDate(date: String) {
        if (date.isNotEmpty()) {
            viewModel.otherAssessmentDetails[AssessmentDefinedParams.NextFollowupDate] =
                DateUtils.convertDateTimeToDate(
                    date,
                    DateUtils.DATE_ddMMyyyy,
                    DateUtils.DATE_FORMAT_yyyyMMddHHmmssZZZZZ
                )
        }
    }

}