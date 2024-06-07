package com.medtroniclabs.spice.ui.assessment.fragment

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ScrollView
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.activityViewModels
import com.medtroniclabs.spice.R
import com.medtroniclabs.spice.appextensions.gone
import com.medtroniclabs.spice.appextensions.isGone
import com.medtroniclabs.spice.appextensions.visible
import com.medtroniclabs.spice.common.CommonUtils
import com.medtroniclabs.spice.common.CommonUtils.convertStringToIntString
import com.medtroniclabs.spice.common.CommonUtils.getOptionMap
import com.medtroniclabs.spice.common.DateUtils
import com.medtroniclabs.spice.common.DateUtils.getDateAfterDays
import com.medtroniclabs.spice.common.DefinedParams
import com.medtroniclabs.spice.common.DefinedParams.DefaultID
import com.medtroniclabs.spice.common.DefinedParams.DefaultIDLabel
import com.medtroniclabs.spice.common.DefinedParams.ICCM
import com.medtroniclabs.spice.common.DefinedParams.True
import com.medtroniclabs.spice.common.DefinedParams.Yes
import com.medtroniclabs.spice.common.StringConverter
import com.medtroniclabs.spice.common.ViewUtils
import com.medtroniclabs.spice.databinding.FragmentAssessmentIccmSummaryBinding
import com.medtroniclabs.spice.db.entity.HealthFacilityEntity
import com.medtroniclabs.spice.formgeneration.extension.markMandatory
import com.medtroniclabs.spice.formgeneration.extension.safeClickListener
import com.medtroniclabs.spice.formgeneration.model.FormLayout
import com.medtroniclabs.spice.formgeneration.ui.SingleSelectionCustomView
import com.medtroniclabs.spice.formgeneration.utility.CustomSpinnerAdapter
import com.medtroniclabs.spice.model.AssessmentSummaryModel
import com.medtroniclabs.spice.network.resource.ResourceState
import com.medtroniclabs.spice.ui.BaseFragment
import com.medtroniclabs.spice.ui.MenuConstants
import com.medtroniclabs.spice.ui.assessment.AssessmentCommonUtils.addViewSummaryLayout
import com.medtroniclabs.spice.ui.assessment.AssessmentCommonUtils.getNutritionStatus
import com.medtroniclabs.spice.ui.assessment.AssessmentCommonUtils.getValueOfKeyFromMap
import com.medtroniclabs.spice.ui.assessment.AssessmentDefinedParams
import com.medtroniclabs.spice.ui.assessment.AssessmentDefinedParams.ACT
import com.medtroniclabs.spice.ui.assessment.AssessmentDefinedParams.Amoxicillin
import com.medtroniclabs.spice.ui.assessment.AssessmentDefinedParams.AssessmentNotes
import com.medtroniclabs.spice.ui.assessment.AssessmentDefinedParams.BreathPerMinute
import com.medtroniclabs.spice.ui.assessment.AssessmentDefinedParams.Dispensed
import com.medtroniclabs.spice.ui.assessment.AssessmentDefinedParams.FB_MAX_BREATHING
import com.medtroniclabs.spice.ui.assessment.AssessmentDefinedParams.FB_MAX_MONTH
import com.medtroniclabs.spice.ui.assessment.AssessmentDefinedParams.FB_MIN_BREATHING
import com.medtroniclabs.spice.ui.assessment.AssessmentDefinedParams.FB_MIN_MONTH
import com.medtroniclabs.spice.ui.assessment.AssessmentDefinedParams.General_Danger_Signs
import com.medtroniclabs.spice.ui.assessment.AssessmentDefinedParams.IsClinicTaken
import com.medtroniclabs.spice.ui.assessment.AssessmentDefinedParams.NextFollowupDate
import com.medtroniclabs.spice.ui.assessment.AssessmentDefinedParams.NoOfDaysDiarrhoea
import com.medtroniclabs.spice.ui.assessment.AssessmentDefinedParams.NoOfDaysOfCough
import com.medtroniclabs.spice.ui.assessment.AssessmentDefinedParams.NoOfDaysOfFever
import com.medtroniclabs.spice.ui.assessment.AssessmentDefinedParams.OrsDispensedStatus
import com.medtroniclabs.spice.ui.assessment.AssessmentDefinedParams.ReferredPHUSite
import com.medtroniclabs.spice.ui.assessment.AssessmentDefinedParams.ReferredPHUSiteID
import com.medtroniclabs.spice.ui.assessment.AssessmentDefinedParams.ZincDispensedStatus
import com.medtroniclabs.spice.ui.assessment.AssessmentDefinedParams.hasCough
import com.medtroniclabs.spice.ui.assessment.AssessmentDefinedParams.hasDiarrhoea
import com.medtroniclabs.spice.ui.assessment.AssessmentDefinedParams.hasFever
import com.medtroniclabs.spice.ui.assessment.AssessmentDefinedParams.isBreastfeed
import com.medtroniclabs.spice.ui.assessment.AssessmentDefinedParams.isConvulsionPastFewDays
import com.medtroniclabs.spice.ui.assessment.AssessmentDefinedParams.isUnusualSleepy
import com.medtroniclabs.spice.ui.assessment.AssessmentDefinedParams.isVomiting
import com.medtroniclabs.spice.ui.assessment.AssessmentDefinedParams.muacCode
import com.medtroniclabs.spice.ui.assessment.referrallogic.model.ReferralDefinedParams
import com.medtroniclabs.spice.ui.assessment.referrallogic.model.ReferralDefinedParams.Diarrhoea
import com.medtroniclabs.spice.ui.assessment.referrallogic.model.ReferralDefinedParams.DiarrhoeaSigns
import com.medtroniclabs.spice.ui.assessment.referrallogic.model.ReferralDefinedParams.NA
import com.medtroniclabs.spice.ui.assessment.referrallogic.model.ReferralDefinedParams.RdtPositive
import com.medtroniclabs.spice.ui.assessment.referrallogic.utils.ReferralStatus
import com.medtroniclabs.spice.ui.assessment.rmnch.RMNCH.otherSigns
import com.medtroniclabs.spice.ui.assessment.viewmodel.AssessmentViewModel
import org.json.JSONObject

class AssessmentICCMSummaryFragment : BaseFragment(), View.OnClickListener {
    private val viewModel: AssessmentViewModel by activityViewModels()
    lateinit var binding: FragmentAssessmentIccmSummaryBinding
    private var datePickerDialog: DatePickerDialog? = null
    private var isValid: Boolean = true

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAssessmentIccmSummaryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
        setListeners()
        attachObservers()
    }

    private fun initViews() {
        binding.labelPhuReferred.markMandatory()
        getClinicTakenData().let {
            val view = SingleSelectionCustomView(binding.root.context)
            view.tag = IsClinicTaken
            view.addViewElements(
                it,
                false,
                viewModel.otherAssessmentDetails,
                Pair(IsClinicTaken,null),
                FormLayout(viewType = "", id = "", title = "", visibility = "", optionsList = null),
                singleSelectionCallback
            )
            binding.clinicTakenGroup.addView(view)
        }
    }

    private var singleSelectionCallback: ((selectedID: Any?, elementId: Pair<String,String?>, serverViewModel: FormLayout, name: String?) -> Unit)? =
        { selectedID, _, _, _ ->
            viewModel.otherAssessmentDetails[IsClinicTaken] = selectedID as String
            viewModel.isInputUpdated = true
        }


    private fun getClinicTakenData(): ArrayList<Map<String, Any>> {
        val flowList = ArrayList<Map<String, Any>>()
        flowList.add(getOptionMap(getString(R.string.yes), getString(R.string.yes)))
        flowList.add(getOptionMap(getString(R.string.no), getString(R.string.no)))
        return flowList
    }

    private fun loadPhuSitesList(healthFacilityList: List<HealthFacilityEntity>) {
            val dropDownList = ArrayList<Map<String, Any>>()
            /*dropDownList.add(
                hashMapOf<String, Any>(
                    DefinedParams.NAME to DefaultIDLabel,
                    DefinedParams.id to DefaultID
            )*/
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
            binding.etPhuChange.post {
                if (dropDownList.size > 0 ){
                    binding.etPhuChange.setSelection(defaultPosition , false)
                }
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
                            if (selectedId != DefaultID) {
                                isValid = true
                                binding.tvSiteErrorMessage.gone()
                                if ((viewModel.otherAssessmentDetails[ReferredPHUSite] != selectedSiteName && viewModel.otherAssessmentDetails[ReferredPHUSite] != null) ||
                                    (viewModel.otherAssessmentDetails[ReferredPHUSiteID] != (healthFacilityList.find { it.fhirId == selectedId }?.fhirId?.toLong()
                                        ?: selectedId?.toLong()!!) && viewModel.otherAssessmentDetails[ReferredPHUSiteID] != null)
                                ) {
                                    viewModel.isInputUpdated = true
                                }
                                viewModel.otherAssessmentDetails[ReferredPHUSite] = selectedSiteName ?: ""
                                viewModel.otherAssessmentDetails[ReferredPHUSiteID] =
                                    healthFacilityList.find { it.fhirId == selectedId }?.fhirId?.toLong()
                                        ?: selectedId?.toLong()!!
                            } else {
                                isValid = false
                                binding.tvSiteErrorMessage.visible()
                                if (viewModel.otherAssessmentDetails.containsKey(ReferredPHUSite))
                                    viewModel.otherAssessmentDetails.remove(ReferredPHUSite)
                                if (viewModel.otherAssessmentDetails.containsKey(ReferredPHUSiteID))
                                    viewModel.otherAssessmentDetails.remove(ReferredPHUSiteID)
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

    private fun setListeners() {
        binding.btnDone.safeClickListener(this)
        binding.etNotes.addTextChangedListener { input ->
            input?.let {
                val resultValue = input.trim().toString()
                if (resultValue.isNotBlank()) {
                    viewModel.otherAssessmentDetails[AssessmentNotes] = resultValue
                    viewModel.isInputUpdated = true
                }
            }
        }
        binding.etNextFollowUpDate.safeClickListener(this)

    }

    private fun attachObservers() {
        viewModel.assessmentStringLiveData.value?.let {result ->
            updateStatusBar()
            createSummaryView(createListSummaryData(result))
        }

        viewModel.nearestFacilityLiveData.observe(viewLifecycleOwner) { resourceState ->
            when (resourceState.state) {
                ResourceState.LOADING -> {
                    showProgress()
                }

                ResourceState.SUCCESS -> {
                    hideProgress()
                    resourceState.data?.let { siteList ->
                        loadPhuSitesList(siteList)
                    }
                }

                ResourceState.ERROR -> {
                    hideProgress()
                }
            }
        }
    }

    private fun updateStatusBar() {
        when(viewModel.referralStatus){
            ReferralStatus.Referred.name -> {
                binding.phuReferredGroup.visibility = View.VISIBLE
                binding.diarrhoeaGroup.visibility = View.VISIBLE
                binding.riskResultLayout.backgroundTintList = ContextCompat.getColorStateList(requireContext(), R.color.attention_color)
                binding.riskResultLayout.text = getString(R.string.referred_for_further_assessment)
            }
            ReferralStatus.OnTreatment.name -> {
                binding.coughMalariaGroup.visibility = View.VISIBLE
                binding.etNextFollowUpDate.text = getDateAfterDays(viewModel.referralReason?.mapNotNull { viewModel.treatmentDays[it] }?.minOrNull() ?: 3)
                binding.riskResultLayout.backgroundTintList = ContextCompat.getColorStateList(requireContext(), R.color.red_risk_moderate)
                binding.riskResultLayout.text = getString(R.string.patient_on_treatment)
            }
            else -> {
                binding.riskResultLayout.backgroundTintList = ContextCompat.getColorStateList(requireContext(), R.color.green_attention_color)
                binding.riskResultLayout.text = getString(R.string.no_refferral_treatment_required)
            }
        }
    }

    private fun showErrorInSummary() {
        binding.emptyErrorMessage.visibility = View.VISIBLE
        binding.parentLayout.visibility = View.GONE
    }

    private fun createSummaryView(
        listSummaryData: MutableList<AssessmentSummaryModel>?
    ) {
        listSummaryData?.let {summaryData ->
            binding.emptyErrorMessage.visibility = View.GONE
            binding.parentLayout.visibility = View.VISIBLE
            binding.parentLayout.removeAllViews()
            composeIccmSummaryView(summaryData)
        } ?: kotlin.run {
            showErrorInSummary()
        }
    }

    companion object {
        const val TAG = "AssessmentICCMSummaryFragment"
        fun newInstance(): AssessmentICCMSummaryFragment {
            return AssessmentICCMSummaryFragment()
        }
    }

    private fun createListSummaryData(data: String): MutableList<AssessmentSummaryModel>? {
        return viewModel.formLayoutsLiveData.value?.data?.formLayout?.filter { it.isSummary == true }?.map { formLayout ->
            AssessmentSummaryModel(
                title = formLayout.titleSummary ?: formLayout.title,
                id = formLayout.id,
                cultureValue = formLayout.titleCulture,
                value = getValueOfKeyFromMap(StringConverter.stringToMap(data), formLayout.id, ICCM),
                noOfDays = formLayout.noOfDays
            )
        }?.toMutableList()
    }

    private fun composeIccmSummaryView(listSummaryData: MutableList<AssessmentSummaryModel>) {
        bindICCMSummaryView(
            getString(R.string.patient_status),
            getStatus(viewModel.referralStatus) ?: getString(R.string.seperator_hyphen)
        )
        val isSignContain = composeGeneralDangerSignsResult(listSummaryData)
        listSummaryData.filter { it.title?.lowercase() != General_Danger_Signs.lowercase() }.forEach { item ->
            when (item.id) {
                muacCode -> {
                    bindICCMSummaryView(
                        item.title,
                        requireContext().getString(
                            R.string.nutrition_summary,
                            item.value,
                            getNutritionStatus(item.value, requireContext())
                        )
                    )
                }

                hasDiarrhoea -> {
                    if (item.value == Yes) {
                        val dehydrationStatus = getDehydrationStatus(isSignContain)
                        bindICCMSummaryView(
                            item.title,
                            dehydrationStatus?.let {result ->
                                requireContext().getString(
                                    R.string.nutrition_summary,
                                    item.value,
                                    result
                                )
                            } ?: kotlin.run {
                                requireContext().getString(
                                    R.string.nutrition_summary_without_signs,
                                    item.value
                                )
                            }
                        )
                    } else {
                        bindICCMSummaryView(item.title, item.value)
                    }
                }

                hasCough -> {
                    if (item.value == Yes) {
                        val status = getPneumoniaStatus()
                        if (status){
                            bindICCMSummaryView(
                                item.title,
                                requireContext().getString(
                                    R.string.nutrition_summary,
                                    item.value,
                                    getString(R.string.pneumonia)
                                )
                            )
                        } else {
                            bindICCMSummaryView(item.title, item.value)
                        }
                    } else {
                        bindICCMSummaryView(item.title, item.value)
                    }
                }

                BreathPerMinute -> {
                    item.value?.let {result ->
                        bindICCMSummaryView(
                            item.title,
                            requireContext().getString(
                                R.string.firstname_lastname,
                                convertStringToIntString(result),
                                getString(R.string.bpm)
                            )
                        )
                    }
                }

                DiarrhoeaSigns -> {
                    item.value?.let {result ->
                        bindICCMSummaryView(
                            item.title,
                            getSelectedSigns(result)
                        )
                    }
                }

                hasFever -> {
                    val rdtResult = viewModel.assessmentStringLiveData.value?.let {
                        val jsonObject = JSONObject(it)
                        val feverObject = jsonObject.optJSONObject(MenuConstants.ICCM_MENU_ID)?.optJSONObject(
                            AssessmentDefinedParams.Fever
                        )
                        feverObject?.optString(ReferralDefinedParams.RdtTest)
                    }
                    if (item.value == Yes && rdtResult == RdtPositive) {
                        bindICCMSummaryView(
                            item.title,
                            requireContext().getString(
                                R.string.nutrition_summary,
                                item.value,
                                getString(R.string.malaria)
                            )
                        )
                    } else {
                        bindICCMSummaryView(item.title, item.value)
                    }
                }

                NoOfDaysOfCough, NoOfDaysDiarrhoea, NoOfDaysOfFever -> {
                    item.noOfDays?.let { maxDays ->
                        item.value?.let { enteredDays ->
                            bindICCMSummaryView(
                                item.title,
                                CommonUtils.getDaysValue(enteredDays, maxDays, requireContext())
                            )
                        }
                    } ?: kotlin.run {
                        bindICCMSummaryView(item.title, item.value)
                    }
                }

                Amoxicillin.lowercase(), ACT.lowercase() -> {
                    if (item.value == Dispensed){
                        bindICCMSummaryView(Dispensed, item.title)
                    }
                }

                else -> {
                    if((item.id != OrsDispensedStatus) && (item.id != ZincDispensedStatus) && (item.id != otherSigns)){
                        bindICCMSummaryView(item.title, item.value)
                    }
                }
            }
        }
        val zincDispensedStatus = listSummaryData.filter { it.id == ZincDispensedStatus  }[0].value
        val orsDispensedStatus = listSummaryData.filter { it.id == OrsDispensedStatus  }[0].value
        if (zincDispensedStatus!=null || orsDispensedStatus!=null){
            bindICCMSummaryView(Dispensed, requireContext().getString(R.string.zinc_ors_status, zincDispensedStatus ?: NA, orsDispensedStatus ?: NA))
        }
    }

    private fun getSelectedSigns(listItems: String): String {
        val otherDiarrhoeaSigns = viewModel.assessmentStringLiveData.value?.let {
            val jsonObject = JSONObject(it)
            val otherObject = jsonObject.optJSONObject(MenuConstants.ICCM_MENU_ID)?.optJSONObject(
                Diarrhoea
            )
            otherObject?.optString(otherSigns)
        }
        val result = if (!otherDiarrhoeaSigns.isNullOrBlank()) {
            requireContext().getString(R.string.other_value, listItems, otherDiarrhoeaSigns)
        } else listItems

        return result
    }

    private fun getPneumoniaStatus(): Boolean {
        var status = false
        viewModel.assessmentStringLiveData.value?.let {
            val jsonObject = JSONObject(it)
            val coughObject = jsonObject.optJSONObject(MenuConstants.ICCM_MENU_ID)?.optJSONObject(
                AssessmentDefinedParams.Cough.lowercase()
            )
            coughObject?.optString(ReferralDefinedParams.BreathPerMinute)?.let { bpmValue ->
                viewModel.memberDetailsLiveData.value?.data?.let { details ->
                    DateUtils.dateToMonths(details.dateOfBirth).let { month ->
                        month?.let {
                            if ((month in FB_MIN_MONTH..11) && bpmValue.toInt() >= FB_MAX_BREATHING) {
                                status = true
                            } else if (month in FB_MAX_MONTH..60 && bpmValue.toInt() >= FB_MIN_BREATHING) {
                                status = true
                            }
                        }
                    }
                }
            }
        }
        return status
    }

    private fun getDehydrationStatus(signContain: Boolean): String? {
            var result: String? = null
            viewModel.assessmentStringLiveData.value?.let {
                val jsonObject = JSONObject(it)
                val feverObject =
                    jsonObject.optJSONObject(MenuConstants.ICCM_MENU_ID)?.optJSONObject(
                        Diarrhoea
                    )
                val diarrhoeaDays =
                    feverObject?.optString(ReferralDefinedParams.NoOfDaysOfDiarrhoea)
                val bloodyDiarrhoea =
                    feverObject?.optString(ReferralDefinedParams.IsBloodyDiarrhoea)
                diarrhoeaDays?.let {
                    bloodyDiarrhoea?.let {
                        result = if (diarrhoeaDays.toInt() >= 14 || bloodyDiarrhoea == True ) {
                            requireContext().getString(R.string.severe_dehydration)
                        } else {
                            requireContext().getString(R.string.moderate_dehydration)
                        }
                    }
                }
            }
            return result
    }


    private fun composeGeneralDangerSignsResult(listSummaryData: MutableList<AssessmentSummaryModel>): Boolean {
        var isSignContain = false
        val targetIds = setOf(
            isUnusualSleepy,
            isConvulsionPastFewDays,
            isVomiting,
            isBreastfeed
        )
        val signsList = listOf(
            AssessmentDefinedParams.SunkenEyes.lowercase(),
            AssessmentDefinedParams.NoTearsWhenCrying.lowercase(),
            AssessmentDefinedParams.LittleOrNoUrine.lowercase(),
            AssessmentDefinedParams.SkinPinch.lowercase(),
            AssessmentDefinedParams.VeryThirsty.lowercase(),
            AssessmentDefinedParams.DryMouthOrTongue.lowercase(),
            AssessmentDefinedParams.SunkenFontanella.lowercase()
        )

        var result = DefinedParams.No
        for (assessment in listSummaryData) {
            if (assessment.id in targetIds && assessment.value == Yes) {
                result = Yes

            }
            if (assessment.title.equals(AssessmentDefinedParams.Signs, true) && signsList.any { sign ->
                    assessment.value?.lowercase()?.contains(sign) == true
                }) {
                isSignContain = true
            }
        }
        bindICCMSummaryView(
            listSummaryData[0].title ?: getString(R.string.general_danger_signs),
            result
        )
        return isSignContain
    }

    private fun bindICCMSummaryView(title: String?, value: String?, valueTextColor: Int? = null) {
        value?.let {result ->
            binding.parentLayout.addView(
                addViewSummaryLayout(
                    title,
                    result,
                    valueTextColor,
                    requireContext()
                )
            )
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            binding.btnDone.id -> {
                binding.etNextFollowUpDate.text?.let {
                    updateFollowUpDate(it.trim().toString())
                }
                //addOtherDetailsToIccmType(Summary.lowercase())
                if (viewModel.otherAssessmentDetails.containsKey(ReferredPHUSite) && viewModel.otherAssessmentDetails.containsKey(ReferredPHUSiteID)) {
                    viewModel.isDismiss = true
                    viewModel.updateOtherAssessmentDetails()
                } else {
                    if(binding.tvSiteErrorMessage.isGone())
                    {
                        binding.tvSiteErrorMessage.visible()
                    }
                    binding.scrollView.fullScroll(ScrollView.FOCUS_DOWN)
                }
            }

            binding.etNextFollowUpDate.id -> {
                showDatePickerDialog()
            }
        }
    }


    private fun showDatePickerDialog() {
        var yearMonthDate: Triple<Int?, Int?, Int?>? = null
        if (!binding.etNextFollowUpDate.text.isNullOrBlank())
            yearMonthDate =
                DateUtils.convertedMMMToddMM(binding.etNextFollowUpDate.text.toString())
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
            viewModel.otherAssessmentDetails[NextFollowupDate] =
                DateUtils.convertDateTimeToDate(
                    date,
                    DateUtils.DATE_ddMMyyyy,
                    DateUtils.DATE_FORMAT_yyyyMMddHHmmssZZZZZ
                )
            viewModel.isInputUpdated = true
        }
    }

    fun getStatus(referralStatus: String?): String? {
        return when (referralStatus) {
            ReferralStatus.Referred.name -> getString(R.string.referred)
            ReferralStatus.OnTreatment.name -> getString(R.string.on_treatment)
            ReferralStatus.Recovered.name -> getString(R.string.recovered)
            else -> {
                null
            }
        }
    }
}