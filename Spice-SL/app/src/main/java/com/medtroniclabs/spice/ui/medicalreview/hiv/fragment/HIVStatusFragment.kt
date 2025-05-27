package com.medtroniclabs.spice.ui.medicalreview.hiv.fragment

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.os.bundleOf
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResult
import com.medtroniclabs.spice.appextensions.gone
import com.medtroniclabs.spice.appextensions.setVisible
import com.medtroniclabs.spice.common.CommonUtils
import com.medtroniclabs.spice.common.DateUtils
import com.medtroniclabs.spice.common.DefinedParams
import com.medtroniclabs.spice.common.SecuredPreference
import com.medtroniclabs.spice.common.ViewUtils
import com.medtroniclabs.spice.data.MedicalReviewMetaItems
import com.medtroniclabs.spice.data.model.HivStatus
import com.medtroniclabs.spice.databinding.FragmentHivStatusBinding
import com.medtroniclabs.spice.formgeneration.extension.safeClickListener
import com.medtroniclabs.spice.formgeneration.model.FormLayout
import com.medtroniclabs.spice.formgeneration.ui.SingleSelectionCustomView
import com.medtroniclabs.spice.formgeneration.utility.CustomSpinnerAdapter
import com.medtroniclabs.spice.network.resource.ResourceState
import com.medtroniclabs.spice.ui.BaseFragment
import com.medtroniclabs.spice.ui.assessment.rmnch.RMNCH.PREGNANCY_MAX_AGE
import com.medtroniclabs.spice.ui.assessment.rmnch.RMNCH.PREGNANCY_MIN_AGE
import com.medtroniclabs.spice.ui.medicalreview.hiv.viewmodel.HIVStatusViewModel
import com.medtroniclabs.spice.ui.medicalreview.motherneonate.anc.MotherNeonateUtil
import com.medtroniclabs.spice.ui.medicalreview.utils.MedicalReviewDefinedParams
import com.medtroniclabs.spice.ui.medicalreview.utils.MedicalReviewDefinedParams.notEstablished
import com.medtroniclabs.spice.ui.medicalreview.utils.MedicalReviewTypeEnums
import com.medtroniclabs.spice.ui.mypatients.viewmodel.PatientDetailViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@AndroidEntryPoint
class HIVStatusFragment : BaseFragment() {

    val adapter: CustomSpinnerAdapter by lazy { CustomSpinnerAdapter(requireContext()) }
    private val viewModel: HIVStatusViewModel by activityViewModels()
    private val patientViewModel: PatientDetailViewModel by activityViewModels()
    private lateinit var binding: FragmentHivStatusBinding
    private var datePickerDialog: DatePickerDialog? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHivStatusBinding.inflate(inflater, container, false)
        return binding.root
    }

    companion object {
        const val TAG = "HIVStatusFragment"
        fun newInstance() =
            HIVStatusFragment()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        attachObserver()
    }

    fun attachObserver() {
        viewModel.getHivStatusMetaList.observe(viewLifecycleOwner) { resource ->
            when (resource.state) {
                ResourceState.LOADING -> {
                    showProgress()
                }

                ResourceState.SUCCESS -> {
                    resource.data?.let { listItems ->
                        fun filterAndMap(category: String): ArrayList<Map<String, Any>> {
                            return listItems
                                .filter { it.category == category }
                                .map { item ->
                                    CommonUtils.getOptionMap(
                                        value = item.value.orEmpty(),
                                        name = item.name
                                    )
                                }
                                .toCollection(ArrayList())
                        }

                        if(viewModel.isEMTCT) {
                            initTBStatus(listItems)
//                            binding.lmbGroup.gone()
//                            binding.gestationalAgeGroup.gone()
//                            binding.expectedDateGroup.gone()
                        }else{
                            binding.tbGroup.gone()
                        }
                        initPregnancyStatus(filterAndMap(MedicalReviewTypeEnums.hivPreganancyBreastFeedingStatus.name))
                        initAHD(filterAndMap(MedicalReviewTypeEnums.ahdStatus.name))
                        initDSD(filterAndMap(MedicalReviewTypeEnums.dsdStatus.name))
                        val dropDownList = ArrayList<Map<String, Any>>()
                        dropDownList.add(
                            mapOf(
                                DefinedParams.NAME to DefinedParams.DefaultIDLabel,
                                DefinedParams.Value to DefinedParams.DefaultID
                            )
                        )
                        dropDownList.addAll(listItems
                            .filter { it.category == MedicalReviewTypeEnums.nonEstablishedModels.name }
                            .map { item ->
                                mapOf(
                                    DefinedParams.NAME to item.name,
                                    DefinedParams.Value to (item.value ?: item.name)
                                )
                            })
                        if (dropDownList.isNotEmpty()) {
                            setSpinner(ArrayList(dropDownList))
                        }
                    }
                    hideProgress()
                }

                ResourceState.ERROR -> {
                    hideProgress()
                }
            }
        }
    }

    private fun initAHD(data: ArrayList<Map<String, Any>>?) {
        if (data.isNullOrEmpty()) return  // Exit if null or empty

        val view = SingleSelectionCustomView(requireContext()).apply {
            tag = MedicalReviewTypeEnums.ahdStatus.name
            addViewElements(
                data,
                SecuredPreference.getIsTranslationEnabled(),
                viewModel.resultAHD,
                Pair(MedicalReviewTypeEnums.ahdStatus.name, null),
                FormLayout(viewType = "", id = "", title = "", visibility = "", optionsList = null),
                singleSelectionAHDCallback
            )
        }

        binding.llAHD.addView(view)
    }

    private fun initDSD(data: ArrayList<Map<String, Any>>?) {
        if (data.isNullOrEmpty()) return  // Exit if null or empty

        val view = SingleSelectionCustomView(requireContext()).apply {
            tag = MedicalReviewTypeEnums.dsdStatus.name
            addViewElements(
                data,
                SecuredPreference.getIsTranslationEnabled(),
                viewModel.resultDSD,
                Pair(MedicalReviewTypeEnums.dsdStatus.name, null),
                FormLayout(viewType = "", id = "", title = "", visibility = "", optionsList = null),
                singleSelectionDSDCallback
            )
        }

        binding.llDSD.addView(view)
    }

    private fun initView() {
        viewModel.isEMTCT = arguments?.getBoolean(DefinedParams.EMTCTMR,false) == true
        viewModel.getHivStatusMeta(MedicalReviewTypeEnums.HIV.name)
        binding.etSelectModel.setVisible(false)
        binding.tvSelectModelLabel.setVisible(false)
        binding.tvSelectModelError.setVisible(false)
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

        // Show pregnancy group only if gender is female AND eligible age
        binding.pregnancyStatusGroup.setVisible(isEligibleAge)
        binding.tvLastMenstrualPeriodDate.isEnabled = true
        binding.tvLastMenstrualPeriodDate.safeClickListener {
            showDatePickerDialog(binding.tvLastMenstrualPeriodDate)
        }
        autoPopulatePregnantDetails()

    }

    private fun autoPopulatePregnantDetails() {
        if (patientViewModel.isPregnant()) {
            viewModel.resultPregnantStatus[MedicalReviewTypeEnums.hivPreganancyBreastFeedingStatus.name] =
                DefinedParams.yes
            binding.lmbGroup.setVisible(true)
            binding.gestationalAgeGroup.setVisible(true)
            binding.expectedDateGroup.setVisible(true)
        }
        patientViewModel.getPregnantDetails()?.lastMenstrualPeriod?.takeIf { it.isNotBlank() }
            ?.let { lmp ->
                binding.apply {
                    tvLastMenstrualPeriodDate.text = DateUtils.convertDateFormat(
                        lmp,
                        DateUtils.DATE_FORMAT_yyyyMMddHHmmssZZZZZ,
                        DateUtils.DATE_ddMMyyyy
                    )
//                    tvLastMenstrualPeriodDate.isEnabled = false
                }
                calculateGestationalAgeAndEstimationDeliveryDate()
            }
    }

    private fun initPregnancyStatus(data: ArrayList<Map<String, Any>>?) {
        if (data.isNullOrEmpty()) return  // Exit if null or empty

        val view = SingleSelectionCustomView(requireContext()).apply {
            tag = MedicalReviewTypeEnums.hivPreganancyBreastFeedingStatus.name
            addViewElements(
                data,
                SecuredPreference.getIsTranslationEnabled(),
                viewModel.resultPregnantStatus,
                Pair(MedicalReviewTypeEnums.hivPreganancyBreastFeedingStatus.name, null),
                FormLayout(viewType = "", id = "", title = "", visibility = "", optionsList = null),
                singleSelectionCallback
            )
        }

        binding.llPregnancyAndBreastFeedingStatus.addView(view)
    }

    private var singleSelectionCallback: ((selectedID: Any?, elementId: Pair<String, String?>, serverViewModel: FormLayout, name: String?) -> Unit)? =
        { selectedID, _, _, _ ->
            viewModel.resultPregnantStatus[MedicalReviewTypeEnums.hivPreganancyBreastFeedingStatus.name] =
                selectedID as? String ?: ""
            val isValid = (selectedID as? String).equals(DefinedParams.yes, ignoreCase = true)
            showPregnantRelatedViews(isValid)
            setFragmentResult(
                MedicalReviewDefinedParams.HIV_STATUS, bundleOf(
                    MedicalReviewDefinedParams.CHIP_ITEMS to true
                )
            )
        }

    private fun showPregnantRelatedViews(isValid: Boolean) {
        binding.lmbGroup.setVisible(isValid)
        binding.tvLastMenstrualPeriodDate.text = ""
        binding.gestationalAgeGroup.setVisible(isValid)
        binding.etGestationalAge.text = ""
        binding.expectedDateGroup.setVisible(isValid)
        binding.etExpectedDate.text = ""
        calculateGestationalAgeAndEstimationDeliveryDate()
    }

    private fun showDatePickerDialog(textView: AppCompatTextView) {
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
                disableFutureDate = true,
                cancelCallBack = { datePickerDialog = null }
            ) { _, year, month, dayOfMonth ->
                val stringDate = "$dayOfMonth-$month-$year"
                textView.text =
                    DateUtils.convertDateTimeToDate(
                        stringDate,
                        DateUtils.DATE_FORMAT_ddMMyyyy,
                        DateUtils.DATE_ddMMyyyy
                    )
                calculateGestationalAgeAndEstimationDeliveryDate()
                setFragmentResult(
                    MedicalReviewDefinedParams.HIV_STATUS, bundleOf(
                        MedicalReviewDefinedParams.CHIP_ITEMS to true)
                )
                datePickerDialog = null
            }
        }
    }

    private fun calculateGestationalAgeAndEstimationDeliveryDate() {
        val lmpText = binding.tvLastMenstrualPeriodDate.text.toString().trim()
        if (lmpText.isNotEmpty()) {
            val lmpDate =
                LocalDate.parse(lmpText, DateTimeFormatter.ofPattern(DateUtils.DATE_ddMMyyyy))
            val estimatedDeliveryDate = lmpDate.plusDays(MotherNeonateUtil.EstimatedDeliveryDate)
            val formattedEstimatedDeliveryDate =
                estimatedDeliveryDate.format(DateTimeFormatter.ofPattern(DateUtils.DATE_ddMMyyyy))
            binding.etExpectedDate.text = formattedEstimatedDeliveryDate
            val gestationalAgeInWeeks = DateUtils.calculateGestationalAge(lmpDate)
            binding.etGestationalAge.text =
                DateUtils.formatGestationalAge(gestationalAgeInWeeks, requireContext())
        }
    }

    private var singleSelectionAHDCallback: ((selectedID: Any?, elementId: Pair<String, String?>, serverViewModel: FormLayout, name: String?) -> Unit)? =
        { selectedID, _, _, _ ->
            viewModel.resultAHD[MedicalReviewTypeEnums.ahdStatus.name] =
                selectedID as? String ?: ""
            setFragmentResult(
                MedicalReviewDefinedParams.HIV_STATUS, bundleOf(
                    MedicalReviewDefinedParams.CHIP_ITEMS to true)
            )
        }

    private var singleSelectionDSDCallback: ((selectedID: Any?, elementId: Pair<String, String?>, serverViewModel: FormLayout, name: String?) -> Unit)? =
        { selectedID, _, _, _ ->
            viewModel.resultDSD[MedicalReviewTypeEnums.dsdStatus.name] =
                selectedID as? String ?: ""
            setFragmentResult(
                MedicalReviewDefinedParams.HIV_STATUS, bundleOf(
                    MedicalReviewDefinedParams.CHIP_ITEMS to true)
            )
            val isValid = (selectedID as? String).equals(notEstablished, ignoreCase = true)
            showModel(isValid)
        }

    private fun showModel(isValid: Boolean) {
        binding.tvSelectModelLabel.setVisible(isValid)
        binding.etSelectModel.setVisible(isValid)
        binding.tvSelectModelError.setVisible(false)
        if (!isValid) {
            viewModel.selectModel = null
            binding.etSelectModel.post {
                binding.etSelectModel.setSelection(0, false)
            }
        }
    }

    private fun setSpinner(statusList: ArrayList<Map<String, Any>>) {
        adapter.setData(statusList)
        binding.etSelectModel.adapter = adapter
        val defaultPosition = 0
        binding.etSelectModel.post {
            binding.etSelectModel.setSelection(defaultPosition, false)
        }
        binding.etSelectModel.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    adapterView: AdapterView<*>?,
                    view: View?,
                    pos: Int,
                    itemId: Long
                ) {
                    val selectedItem = adapter.getData(pos)
                    val selectedModel = selectedItem?.get(DefinedParams.Value) as? String
                    viewModel.selectModel =
                        if (!selectedModel.isNullOrEmpty() && !selectedModel.equals(
                                DefinedParams.DefaultID,
                                ignoreCase = true
                            )
                        ) {
                            selectedModel
                        } else {
                            null
                        }
                    setFragmentResult(
                        MedicalReviewDefinedParams.HIV_STATUS,
                        bundleOf(MedicalReviewDefinedParams.CHIP_ITEMS to true)
                    )
                }

                override fun onNothingSelected(p0: AdapterView<*>?) {
                    /**
                     * this method is not used
                     */
                }
            }
    }

    fun validateInput(): Boolean {
        val isValidPregnantStatus = viewModel.resultPregnantStatus.isNotEmpty()
        val isValidAHD = viewModel.resultAHD.isNotEmpty()
        val isValidDSD = viewModel.resultDSD.isNotEmpty()
        val isLMB = binding.tvLastMenstrualPeriodDate.text.toString().trim().isNotBlank()
        val isSelectModel = viewModel.selectModel != DefinedParams.DefaultID && viewModel.selectModel != null
        val isTBStatus = viewModel.tbStatus != DefinedParams.DefaultID && viewModel.tbStatus != null
        return isValidPregnantStatus || isValidAHD || isValidDSD || isLMB || isSelectModel || isTBStatus
    }

    fun getRequest(): HivStatus {
        val lmpText = binding.tvLastMenstrualPeriodDate.text.toString().trim()
        val expectedDateText = binding.etExpectedDate.text.toString().trim()

        val lmpDate = lmpText.takeIf { it.isNotBlank() }?.let {
            LocalDate.parse(it, DateTimeFormatter.ofPattern(DateUtils.DATE_ddMMyyyy))
        }

        val lastMenstrualPeriodFormatted = DateUtils.convertDateTimeToDate(
            lmpText.takeIf { it.isNotBlank() },
            DateUtils.DATE_ddMMyyyy,
            DateUtils.DATE_FORMAT_yyyyMMddHHmmssZZZZZ
        ).takeIf { it.isNotBlank() }

        val expectedDeliveryDateFormatted = DateUtils.convertDateTimeToDate(
            expectedDateText.takeIf { it.isNotBlank() },
            DateUtils.DATE_ddMMyyyy,
            DateUtils.DATE_FORMAT_yyyyMMddHHmmssZZZZZ
        ).takeIf { it.isNotBlank() }

        return HivStatus(
            pregnancyBreastfeedStatus = viewModel.resultPregnantStatus[MedicalReviewTypeEnums.hivPreganancyBreastFeedingStatus.name] as? String,
            ahdStatus = viewModel.resultAHD[MedicalReviewTypeEnums.ahdStatus.name] as? String,
            dsdStatus = viewModel.resultDSD[MedicalReviewTypeEnums.dsdStatus.name] as? String,
            model = viewModel.selectModel,
            lastMenstrualPeriod = lastMenstrualPeriodFormatted,
            gestationalInWeeks = lmpDate?.let { DateUtils.calculateGestationalAge(it) },
            expectedDateOfDelivery = expectedDeliveryDateFormatted ,
            tbStatus = viewModel.tbStatus
        )
    }
    private fun initTBStatus(data: List<MedicalReviewMetaItems>) {
        val dropDownList = ArrayList<Map<String, Any>>()
        dropDownList.add(
            mapOf(
                DefinedParams.NAME to DefinedParams.DefaultIDLabel,
                DefinedParams.Value to DefinedParams.DefaultID
            )
        )
        dropDownList.addAll(data
            .filter { it.category == MedicalReviewTypeEnums.nonEstablishedModels.name }
            .map { item ->
                mapOf(
                    DefinedParams.NAME to item.name,
                    DefinedParams.Value to (item.value ?: item.name)
                )
            })
        if (dropDownList.isNotEmpty()) {
            adapter.setData(dropDownList)
            binding.etTBStatus.adapter = adapter
            val defaultPosition = 0
            binding.etTBStatus.post {
                binding.etTBStatus.setSelection(defaultPosition, false)
            }
            binding.etTBStatus.onItemSelectedListener =
                object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(
                        adapterView: AdapterView<*>?,
                        view: View?,
                        pos: Int,
                        itemId: Long
                    ) {
                        val selectedItem = adapter.getData(position = pos)
                        selectedItem?.let {
                            var selectedModel = it[DefinedParams.Value] as String?
                            if (selectedModel == DefinedParams.DefaultID) {
                                selectedModel = null
                            }
                            selectedModel?.let {
                                viewModel.tbStatus = selectedModel
                            } ?: kotlin.run {
                                viewModel.tbStatus = null
                            }
                            setFragmentResult(
                                MedicalReviewDefinedParams.HIV_STATUS, bundleOf(
                                    MedicalReviewDefinedParams.CHIP_ITEMS to true)
                            )
                        }
                    }

                    override fun onNothingSelected(p0: AdapterView<*>?) {
                        /**
                         * this method is not used
                         */
                    }
                }        }
    }
}