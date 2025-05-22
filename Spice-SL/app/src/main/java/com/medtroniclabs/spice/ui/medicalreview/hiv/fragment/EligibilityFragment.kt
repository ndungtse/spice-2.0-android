package com.medtroniclabs.spice.ui.medicalreview.hiv.fragment

import android.app.DatePickerDialog
import android.os.Bundle
import android.text.InputFilter
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.os.bundleOf
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResult
import com.medtroniclabs.spice.R
import com.medtroniclabs.spice.appextensions.gone
import com.medtroniclabs.spice.appextensions.isVisible
import com.medtroniclabs.spice.appextensions.visible
import com.medtroniclabs.spice.common.CommonUtils.getOptionMap
import com.medtroniclabs.spice.common.DateUtils
import com.medtroniclabs.spice.common.DateUtils.DATE_FORMAT_ddMMyyyy
import com.medtroniclabs.spice.common.DateUtils.DATE_ddMMyyyy
import com.medtroniclabs.spice.common.DateUtils.calculateEddFromLmpAndGestationalAge
import com.medtroniclabs.spice.common.DateUtils.convertToRequiredFormat
import com.medtroniclabs.spice.common.DefinedParams
import com.medtroniclabs.spice.common.ViewUtils
import com.medtroniclabs.spice.data.MedicalReviewMetaItems
import com.medtroniclabs.spice.data.model.MultiSelectDropDownModel
import com.medtroniclabs.spice.databinding.FragmentEligibilityBinding
import com.medtroniclabs.spice.formgeneration.model.FormLayout
import com.medtroniclabs.spice.formgeneration.ui.SingleSelectionCustomView
import com.medtroniclabs.spice.formgeneration.utility.CustomSpinnerAdapter
import com.medtroniclabs.spice.formgeneration.utility.MultiSelectSpinnerAdapter
import com.medtroniclabs.spice.formgeneration.utility.MultiSelectionNoneSpinner
import com.medtroniclabs.spice.network.resource.ResourceState
import com.medtroniclabs.spice.ui.BaseFragment
import com.medtroniclabs.spice.ui.assessment.rmnch.RMNCH.PREGNANCY_MAX_AGE
import com.medtroniclabs.spice.ui.assessment.rmnch.RMNCH.PREGNANCY_MIN_AGE
import com.medtroniclabs.spice.ui.medicalreview.hiv.viewmodel.HivViewModel
import com.medtroniclabs.spice.ui.medicalreview.motherneonate.anc.MotherNeonateUtil
import com.medtroniclabs.spice.ui.medicalreview.utils.MedicalReviewDefinedParams
import com.medtroniclabs.spice.ui.medicalreview.utils.MedicalReviewDefinedParams.HaveYouTakenHivTestBefore
import com.medtroniclabs.spice.ui.medicalreview.utils.MedicalReviewTypeEnums
import com.medtroniclabs.spice.ui.mypatients.viewmodel.PatientDetailViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter


class EligibilityFragment : BaseFragment() {
    private lateinit var binding: FragmentEligibilityBinding
    private val hivViewModel: HivViewModel by activityViewModels()
    private var datePickerDialog: DatePickerDialog? = null
    private val patientViewModel: PatientDetailViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentEligibilityBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
        addObservers()
        lmpValidation()
        valueObserver()
    }

    companion object {
        const val TAG = "EligibilityFragment"
    }

    private fun addObservers() {
        hivViewModel.hivMetaListItems.observe(viewLifecycleOwner) { resourceState ->
            when (resourceState.state) {
                ResourceState.LOADING -> {
                    showProgress()
                }

                ResourceState.ERROR -> {
                    hideProgress()
                }

                ResourceState.SUCCESS -> {
                    hideProgress()
                    resourceState.data?.let { list ->
                        initializeHivHistoryOptions(list.filter { it.category == MedicalReviewTypeEnums.hiv_history.name }
                            .distinctBy { it.name }
                            .sortedBy { it.displayOrder })
                        initializePopulationType(list.filter { it.category == MedicalReviewTypeEnums.population_type.name }
                            .distinctBy { it.name }
                            .sortedBy { it.displayOrder })
                        initializeHivTestDuration(list.filter { it.category == MedicalReviewTypeEnums.hiv_test_durations.name }
                            .distinctBy { it.name }
                            .sortedBy { it.displayOrder })
                    }
                }
            }
        }
    }

    private fun initViews() {
        hivViewModel.getHistoryListMetaItems()
        addCustomView(
            getData(),
            HaveYouTakenHivTestBefore,
            hivViewModel.resultHashMap,
            alreadyHIVTestedCallBack,
            binding.haveTestedHIVBeforeRoot
        )
        val isEmtct = arguments?.getBoolean(DefinedParams.isPregnant, false)
        if (isEmtct == true) {
            binding.hivEMTCTViewGroup.visible()
        }
        binding.tvLastMenstrualPeriodDateLabelText.setOnClickListener {
            showDatePickerDialog(
                binding.tvLastMenstrualPeriodDateLabelText,
                disableFuture = true,
                isLmp = true
            )
        }
    }


    private fun initializeHivTestDuration(costList: List<MedicalReviewMetaItems>) {
        val dropDownList = ArrayList<Map<String, Any>>()
        dropDownList.add(
            hashMapOf<String, Any>(
                DefinedParams.NAME to DefinedParams.DefaultIDLabel,
                DefinedParams.id to DefinedParams.DefaultID,
                DefinedParams.Value to DefinedParams.DefaultIDLabel
            )
        )
        for (item in costList) {
            dropDownList.add(
                hashMapOf<String, Any>(
                    DefinedParams.NAME to item.name,
                    DefinedParams.id to item.id.toString(),
                    DefinedParams.Value to (item.value ?: item.name)
                )
            )
        }
        val adapter = CustomSpinnerAdapter(requireContext())
        adapter.setData(dropDownList)
        binding.tvTestingDurationSpinner.adapter = adapter
        binding.tvTestingDurationSpinner.setSelection(0, false)
        binding.tvTestingDurationSpinner.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    adapterView: AdapterView<*>?,
                    view: View?,
                    pos: Int,
                    itemId: Long
                ) {
                    val selectedItem = adapter.getData(position = pos)
                    selectedItem?.let {
                        handleHivTestDuration(it[DefinedParams.NAME] as String)
                    }
                }

                override fun onNothingSelected(p0: AdapterView<*>?) {
                    /**
                     * this method is not used
                     */
                }
            }
    }

    fun handleHivTestDuration(testDuration: String) {
        val isHaveYouTakenHivTestBefore = hivViewModel.resultHashMap[HaveYouTakenHivTestBefore]
        if (testDuration != DefinedParams.DefaultIDLabel) {

            when (isHaveYouTakenHivTestBefore) {
                getString(R.string.yes) -> {
                    hivViewModel.selectedLastTestForHIV = testDuration
                    binding.tvTestingDurationSpinner.visible()
                }

                getString(R.string.no) -> {
                    hivViewModel.selectedLastTestForHIV = null
                    binding.tvTestingDurationSpinner.gone()
                }

                else -> {
                    hivViewModel.selectedLastTestForHIV = null
                    binding.tvTestingDurationSpinner.gone()
                }
            }
        } else {
            hivViewModel.selectedLastTestForHIV = null
        }
    }


    private fun initializeHivHistoryOptions(supplyList: List<MedicalReviewMetaItems>) {
        val dropDownList = ArrayList<MultiSelectDropDownModel>()
        for (item in supplyList) {
            dropDownList.add(
                MultiSelectDropDownModel(
                    id = item.id, name = item.name, value = item.value
                )
            )
        }
        val adapter = MultiSelectionNoneSpinner(
            requireContext(), dropDownList, hivViewModel.selectedHistoryListItem
        )
        binding.tvHistorySpinner.adapter = adapter
        adapter.setOnItemSelectedListener(object :
            MultiSelectionNoneSpinner.OnItemSelectedListener {
            override fun onItemSelected(
                selectedItems: List<MultiSelectDropDownModel>,
                pos: Int,
            ) {
                if (selectedItems.isNotEmpty()) {
                    val containsNone =
                        selectedItems.any { it.name.equals(getString(R.string.none), true) }
                    hivViewModel.selectedHistoryListItem =
                        if (containsNone) {
                            arrayListOf(selectedItems.first {
                                it.name.equals(
                                    getString(R.string.none),
                                    true
                                )
                            })
                        } else {
                            ArrayList(selectedItems)
                        }
                }
            }
        })
    }


    private fun initializePopulationType(supplyList: List<MedicalReviewMetaItems>) {
        val dropDownList = ArrayList<MultiSelectDropDownModel>()
        var defaultSelectedItems = ArrayList<MultiSelectDropDownModel>()
        var pregnantItemIndex = -1  // Initialize to -1 (not found)
        val isFemale = patientViewModel.getGenderIsFemale()
        val dob = patientViewModel.getDob()
        val isDobValid = !dob.isNullOrBlank()

        val isEligibleAge = if (isFemale && isDobValid && !dob.isNullOrBlank()) {
            val ageAndWeek = DateUtils.getV2YearMonthAndWeek(dob)
            val ageYears = ageAndWeek.years
            val ageMonths = ageAndWeek.months
            val ageWeeks = ageAndWeek.weeks
            val ageDays = ageAndWeek.days

            ageYears in PREGNANCY_MIN_AGE..PREGNANCY_MAX_AGE &&
                    !(ageYears == PREGNANCY_MAX_AGE && (ageMonths + ageWeeks + ageDays) != 0)
        } else {
            false
        }


        for ((index, item) in supplyList.withIndex()) {
            val dropDownItem = MultiSelectDropDownModel(
                id = item.id, name = item.name, value = item.value
            )
            val isMale = arguments?.getBoolean(DefinedParams.Gender) != true
            val isExcludedForMale = item.name.equals(getString(R.string.pregnant_), ignoreCase = true) ||
                    item.name.equals(getString(R.string.female_sex_worker_fsw), ignoreCase = true)
            val isExcludedForAge = item.name.equals(getString(R.string.pregnant_), ignoreCase = true)

            if (!(isMale && isExcludedForMale )) {
                if (!(isExcludedForAge && !isEligibleAge)) {
                    dropDownList.add(dropDownItem)
                }
            }

            // Check if the name is "Pregnant" (case-insensitive)
        if (item.name.equals(getString(R.string.pregnant_), ignoreCase = true)) {
            defaultSelectedItems.add(dropDownItem)
            pregnantItemIndex = index  // Capture the index of "Pregnant"
        }
    }

       if(arguments?.getBoolean(DefinedParams.isPregnant, false)== true) {
           // Assign the default selected items to ViewModel (or create an empty list if not found)
           hivViewModel.selectedPopulationType = defaultSelectedItems
       }

        // Create the adapter with the dropDownList and default selection
        val adapter = MultiSelectSpinnerAdapter(
            requireContext(), dropDownList, hivViewModel.selectedPopulationType
        )
        binding.tvPopulationTypeSpinner.adapter = adapter
        adapter.setOnItemSelectedListener(object : MultiSelectSpinnerAdapter.OnItemSelectedListener {
            override fun onItemSelected(
                selectedItems: List<MultiSelectDropDownModel>,
                pos: Int,
            ) {
                if (selectedItems.isNotEmpty()) {
                    hivViewModel.selectedPopulationType = ArrayList(selectedItems)
                    val containsOther = selectedItems.any {
                        it.name.equals(getString(R.string.other), true)
                    }
                    if (containsOther) {
                        binding.viewOtherType.visible()
                    } else {
                        binding.viewOtherType.gone()
                        binding.etOtherPopulated.setText("")
                    }
                    val isPregnant = selectedItems.any {
                        it.name.equals(getString(R.string.pregnant_), true)
                    }
                    if (isPregnant){
                        binding.hivEMTCTViewGroup.visible()
                    }else{
                        binding.hivEMTCTViewGroup.gone()

                    }

                } else {
                    binding.viewOtherType.gone()
                    binding.etOtherPopulated.setText("")
                    binding.hivEMTCTViewGroup.gone()
                }
            }
        })


}

    private fun addCustomView(
        data: ArrayList<Map<String, Any>>,
        tag: String,
        hashMap: HashMap<String, Any>,
        callback: ((selectedID: Any?, elementId: Pair<String, String?>, serverViewModel: FormLayout, name: String?) -> Unit)?,
        container: ViewGroup?
    ) {
        SingleSelectionCustomView(binding.root.context).apply {
            this.tag = tag
            addViewElements(
                data,
                false,
                hashMap,
                Pair(tag, null),
                FormLayout(viewType = "", id = "", title = "", visibility = "", optionsList = null),
                callback
            )
            container?.addView(this)
        }
    }

    private fun getData(): ArrayList<Map<String, Any>> {
        val flowList = ArrayList<Map<String, Any>>()
        flowList.add(getOptionMap(getString(R.string.yes), getString(R.string.yes)))
        flowList.add(getOptionMap(getString(R.string.no), getString(R.string.no)))
        return flowList
    }

    private var alreadyHIVTestedCallBack: (selectedID: Any?, elementId: Pair<String, String?>, serverViewModel: FormLayout, name: String?) -> Unit =
        { selectedID, _, _, _ ->
            hivViewModel.resultHashMap[HaveYouTakenHivTestBefore] = selectedID as String
            hivViewModel.resultHashMap[HaveYouTakenHivTestBefore]?.let {
                if (it == getString(R.string.yes)) {
                    binding.tvHaveTestedHIVBeforeError.gone()
                    binding.viewGroupHivTestDuration.visible()
                } else {
                    binding.tvTestingDurationSpinner.setSelection(0, false)
                    hivViewModel.selectedLastTestForHIV = null
                    binding.tvTestingDurationError.gone()
                    binding.viewGroupHivTestDuration.gone()
                }
                resultMapChanged()
            }
        }

    private fun resultMapChanged() {
        setFragmentResult(
            MedicalReviewDefinedParams.HIV_ELIGIBILITY_ITEM, bundleOf(
                MedicalReviewDefinedParams.HIV_ELIGIBILITY_VALUES to true
            )
        )
    }

    fun validation(): Boolean {
        var isValid = true
       if(binding.etOtherPopulated.isVisible()){
           if(binding.etOtherPopulated.text.isNullOrEmpty()){
               isValid = false
               binding.tvOtherError.visible()
           }else binding.tvOtherError.gone()
       }
        return isValid
    }

    private fun showDatePickerDialog(
        textView: AppCompatTextView,
        disableFuture: Boolean = false,
        isLmp: Boolean = false
    ) {
        var yearMonthDate: Triple<Int?, Int?, Int?>? = null
        if (!textView.text.isNullOrBlank())
            yearMonthDate =
                DateUtils.convertedMMMToddMM(textView.text.toString())
        if (datePickerDialog == null) {
            datePickerDialog = ViewUtils.showDatePicker(
                context = requireContext(),
                minDate = null,
                date = yearMonthDate,
                isMenstrualPeriod = true,
                disableFutureDate = disableFuture,
                cancelCallBack = { datePickerDialog = null }
            ) { _, year, month, dayOfMonth ->
                val stringDate = "$dayOfMonth-$month-$year"
                textView.text =
                    DateUtils.convertDateTimeToDate(
                        stringDate,
                        DATE_FORMAT_ddMMyyyy,
                        DATE_ddMMyyyy
                    )
                lmpValidation()
                datePickerDialog = null
            }
        }
    }

    private fun lmpValidation() {
        patientViewModel.getPregnantDetails()?.lastMenstrualPeriod?.takeIf { it.isNotBlank() }
            ?.let { lmp ->
                binding.apply {
                    tvLastMenstrualPeriodDateLabelText.text = DateUtils.convertDateFormat(
                        lmp,
                        DateUtils.DATE_FORMAT_yyyyMMddHHmmssZZZZZ,
                        DateUtils.DATE_ddMMyyyy
                    )
                }
                calculateGestationalAgeAndEstimationDeliveryDate()
            }

        val isNotEmpty = !binding.tvLastMenstrualPeriodDateLabelText.text.isNullOrEmpty()
        val alphaEnabled = 1.0f
        val alphaDisabled = 0.5f // Faded look
        binding.etGestationalInWeek.isEnabled = isNotEmpty
        binding.etExpectedDateOfDelivery.isEnabled = isNotEmpty
        binding.etGestationalInWeek.alpha = if (isNotEmpty) alphaEnabled else alphaDisabled
        binding.etExpectedDateOfDelivery.alpha = if (isNotEmpty) alphaEnabled else alphaDisabled
        binding.tvGestationalInWeekLabel.alpha = if (isNotEmpty) alphaEnabled else alphaDisabled
        binding.tvExpectedDateOfDeliveryLabel.alpha =
            if (isNotEmpty) alphaEnabled else alphaDisabled

        if (!isNotEmpty) {
            listOf(binding.etGestationalInWeek, binding.etExpectedDateOfDelivery).forEach {
                it.text?.clear()
            }
        }
    }

    private fun expectedDateOfdDelivery(){
        val lmpDateStr = binding.tvLastMenstrualPeriodDateLabelText.text.toString()
        val gestationalWeeksStr = binding.etGestationalInWeek.text?.trim().toString()

            if (!lmpDateStr.isNullOrBlank() && !gestationalWeeksStr.isNullOrBlank()) {
                try {
                    val gestationalWeeks = gestationalWeeksStr.toIntOrNull()

                    if (gestationalWeeks != null && gestationalWeeks in 0..40) {
                        binding.etGestationalInWeek.isClickable =  false
                        binding.etExpectedDateOfDelivery.setText(calculateEddFromLmpAndGestationalAge(lmpDateStr,gestationalWeeks).first)
                        hivViewModel.expectedDateOfDelivery = calculateEddFromLmpAndGestationalAge(lmpDateStr,gestationalWeeks).second
                    } else {
                        binding.etExpectedDateOfDelivery.setText("") // Clear or show invalid input message
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    binding.etExpectedDateOfDelivery.setText("") // Clear or show invalid input message
                }
            } else {
                binding.etExpectedDateOfDelivery.setText("") // Clear or show invalid input message
            }
        }

    private fun valueObserver() {
        binding.etOtherPopulated.doOnTextChanged { text, _, _, _ ->
            if (!text.isNullOrBlank()) {
                hivViewModel.populationOther = text.toString()
            }
        }
        binding.etGestationalInWeek.apply {
            doOnTextChanged { text, _, _, _ ->
                if (!text.isNullOrBlank()) {

//                    expectedDateOfdDelivery()
                    hivViewModel.gestationalWeeks = text.toString()
                }
            }
            filters = arrayOf(InputFilter { source, start, end, dest, dstart, dend ->
                val newValue = dest.substring(0, dstart) + source.subSequence(start, end) + dest.substring(dend)
                if (newValue.isEmpty()) {
                    return@InputFilter null
                }

                try {
                    val value = newValue.toFloat()
                    if (value in 0.0..40.0) null else ""
                } catch (e: NumberFormatException) {
                    ""
                }
            })
        }
        binding.tvLastMenstrualPeriodDateLabelText.doOnTextChanged { text, _, _, _ ->
            if (!text.isNullOrBlank()) {
                calculateGestationalAgeAndEstimationDeliveryDate()
                hivViewModel.lastMenstrualPeriod = convertToRequiredFormat(text.toString())

            }
        }
    }
    private fun calculateGestationalAgeAndEstimationDeliveryDate() {
        val lmpText = binding.tvLastMenstrualPeriodDateLabelText.text.toString().trim()
        if (lmpText.isNotEmpty()) {
            val lmpDate =
                LocalDate.parse(lmpText, DateTimeFormatter.ofPattern(DateUtils.DATE_ddMMyyyy))
            val estimatedDeliveryDate = lmpDate.plusDays(MotherNeonateUtil.EstimatedDeliveryDate)
            val formattedEstimatedDeliveryDate =
                estimatedDeliveryDate.format(DateTimeFormatter.ofPattern(DateUtils.DATE_ddMMyyyy))
            binding.etExpectedDateOfDelivery.setText(formattedEstimatedDeliveryDate.toString())
            val gestationalAgeInWeeks = DateUtils.calculateGestationalAge(lmpDate)
            binding.etGestationalInWeek.setText(gestationalAgeInWeeks.toString())
        }
    }



}