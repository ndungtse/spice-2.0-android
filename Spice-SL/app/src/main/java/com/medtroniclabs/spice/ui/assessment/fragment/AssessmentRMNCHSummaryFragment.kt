package com.medtroniclabs.spice.ui.assessment.fragment

import android.app.DatePickerDialog
import android.content.Intent
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.activityViewModels
import com.medtroniclabs.spice.R
import com.medtroniclabs.spice.app.analytics.utils.AnalyticsDefinedParams
import com.medtroniclabs.spice.appextensions.gone
import com.medtroniclabs.spice.appextensions.startBackgroundOfflineSync
import com.medtroniclabs.spice.appextensions.visible
import com.medtroniclabs.spice.common.DateUtils
import com.medtroniclabs.spice.common.DateUtils.calculateAgeInMonths
import com.medtroniclabs.spice.common.DateUtils.convertStringToDate
import com.medtroniclabs.spice.common.DateUtils.getDateStringFromDate
import com.medtroniclabs.spice.common.DefinedParams
import com.medtroniclabs.spice.common.StringConverter
import com.medtroniclabs.spice.common.ViewUtils
import com.medtroniclabs.spice.databinding.FragmentRmnchSummaryBinding
import com.medtroniclabs.spice.formgeneration.config.ViewType
import com.medtroniclabs.spice.formgeneration.extension.safeClickListener
import com.medtroniclabs.spice.formgeneration.utility.CustomSpinnerAdapterCustomLayout
import com.medtroniclabs.spice.network.resource.ResourceState
import com.medtroniclabs.spice.ui.BaseFragment
import com.medtroniclabs.spice.ui.assessment.AssessmentCommonUtils
import com.medtroniclabs.spice.ui.assessment.AssessmentDefinedParams
import com.medtroniclabs.spice.ui.assessment.AssessmentDefinedParams.ExclusivelyBreastfeeding
import com.medtroniclabs.spice.ui.assessment.AssessmentDefinedParams.rootSuffix
import com.medtroniclabs.spice.ui.assessment.referrallogic.utils.ReferralStatus
import com.medtroniclabs.spice.ui.assessment.rmnch.RMNCH
import com.medtroniclabs.spice.ui.assessment.rmnch.RMNCH.DeathOfMother
import com.medtroniclabs.spice.ui.assessment.rmnch.RMNCH.childHoodVisitMaxMonth
import com.medtroniclabs.spice.ui.assessment.rmnch.RMNCH.deathOfBaby
import com.medtroniclabs.spice.ui.assessment.rmnch.RMNCH.getValueFromMap
import com.medtroniclabs.spice.ui.assessment.viewmodel.AssessmentViewModel
import com.medtroniclabs.spice.ui.household.HouseholdSearchActivity

class AssessmentRMNCHSummaryFragment : BaseFragment(), View.OnClickListener {

    lateinit var binding: FragmentRmnchSummaryBinding

    private val viewModel: AssessmentViewModel by activityViewModels()

    private var datePickerDialog: DatePickerDialog? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentRmnchSummaryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setListener()
        initSummaryViewByWorkFlowName()
        viewModel.setUserJourney(AnalyticsDefinedParams.RMNCHSummaryAssessment)
        binding.etNextFollowUpDate.background= ContextCompat.getDrawable(requireContext(),R.drawable.edittext_background)
        val background = binding.etNextFollowUpDate.background as? GradientDrawable
        background?.setStroke(resources.getDimensionPixelSize(R.dimen._1sdp), ContextCompat.getColor(requireContext(), R.color.edittext_stroke))
    }

    private fun setListener() {
        binding.btnDone.safeClickListener(this)
        binding.etNextFollowUpDate.safeClickListener(this)
        binding.etNextFollowUpDate.addTextChangedListener {
            binding.btnDone.isEnabled = !it.isNullOrEmpty()
        }
    }

    private fun updateStatusBar() {
        when (viewModel.referralStatus) {
            ReferralStatus.Referred.name -> {
                viewModel.nearestFacilityLiveData.value?.data?.let { siteList ->
                    loadPhuSitesList(siteList)
                }
                binding.riskResultLayout.backgroundTintList =
                    ContextCompat.getColorStateList(requireContext(), R.color.attention_color)
                binding.riskResultLayout.text = getString(R.string.referred_for_further_assessment)
                binding.etPhuChange.visible()
                binding.labelPhuReferred.visible()
            }

            ReferralStatus.OnTreatment.name -> {
                binding.riskResultLayout.backgroundTintList =
                    ContextCompat.getColorStateList(requireContext(), R.color.red_risk_moderate)
                binding.riskResultLayout.text = getString(R.string.patient_on_treatment)
                binding.etPhuChange.gone()
                binding.labelPhuReferred.gone()
            }

            else -> {
                binding.riskResultLayout.backgroundTintList =
                    ContextCompat.getColorStateList(requireContext(), R.color.green_attention_color)
                binding.riskResultLayout.text = getString(R.string.no_refferral_treatment_required)
                binding.etPhuChange.gone()
                binding.labelPhuReferred.gone()
            }
        }
    }

    private fun initSummaryViewByWorkFlowName() {
        viewModel.assessmentStringLiveData.value?.let { mapString ->
            val map = StringConverter.stringToMap(mapString)
            binding.parentLayout.removeAllViews()
            conditionBasedRendering(map)
            addDefaultSummaryView(map)
            viewModel.formLayoutsLiveData.value?.data?.formLayout?.filter { it.isSummary == true }
                ?.filterNot { it.id in showQuestionBasedAge() } // Remove the item with id "childhoodVisitSigns"
                ?.forEach { data ->
                    with(data) {
                        updateStatusBar()
                        binding.parentLayout.addView(
                            AssessmentCommonUtils.addViewSummaryLayout(
                                titleSummary ?: (titleCulture ?: title),
                                getValueFromMap(
                                    map,
                                    id,
                                    viewType,
                                    viewModel.workflowName,
                                    isBooleanAnswer,
                                    Triple(
                                        getString(R.string.yes),
                                        getString(R.string.no),
                                        getString(R.string.hyphen_symbol)
                                    ),
                                    requireContext()
                                ),
                                null,
                                requireContext()
                            )
                        )
                    }
                }
        }

        viewModel.nearestFacilityLiveData.observe(viewLifecycleOwner) { resourceState ->
            when (resourceState.state) {
                ResourceState.LOADING -> {
                    showProgress()
                }

                ResourceState.SUCCESS -> {
                    hideProgress()
                }

                ResourceState.ERROR -> {
                    hideProgress()
                }
            }
        }
    }

    private fun conditionBasedRendering(map: HashMap<String, Any>) {
        if (map.containsKey(viewModel.workflowName)) {
            val workflowMap = map[viewModel.workflowName]
            if (workflowMap is Map<*, *>) {
                if (workflowMap.containsKey(RMNCH.Miscarriage) || workflowMap.containsKey(DeathOfMother) || workflowMap.containsKey(deathOfBaby)) {
                    val miscarriageValue = workflowMap[RMNCH.Miscarriage]
                    val deathOfMother = workflowMap[DeathOfMother]
                    val deathOfBaby = workflowMap[deathOfBaby]
                    if ((miscarriageValue is Boolean && miscarriageValue) || (deathOfMother is Boolean && deathOfMother) || (deathOfBaby is Boolean && deathOfBaby)) {
                        binding.etNextFollowUpDate.gone()
                        binding.tvNextFollowupDateTitle.gone()
                        binding.btnDone.isEnabled = true
                    } else {
                        binding.etNextFollowUpDate.visible()
                        binding.tvNextFollowupDateTitle.visible()
                    }
                }
            }
        }
    }


    private fun loadPhuSitesList(siteList: ArrayList<Map<String, Any>>) {
        binding.etPhuChange.background= ContextCompat.getDrawable(requireContext(),R.drawable.edittext_background)
        val background = binding.etPhuChange.background as? GradientDrawable
        background?.setStroke(resources.getDimensionPixelSize(R.dimen._1sdp), ContextCompat.getColor(requireContext(), R.color.edittext_stroke))
        val adapter = CustomSpinnerAdapterCustomLayout(requireContext())
        adapter.setData(siteList)
        binding.etPhuChange.adapter = adapter
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
                        viewModel.otherAssessmentDetails[AssessmentDefinedParams.ReferredPHUSiteID] =
                            selectedId?.toLong() ?: -1L
                    }
                }

                override fun onNothingSelected(p0: AdapterView<*>?) {
                    /**
                     * this method is not used
                     */
                }
            }
    }

    private fun addDefaultSummaryView(map: HashMap<String, Any>) {

        val title: String = when (viewModel.workflowName) {
            RMNCH.ANC -> getString(R.string.anc_visit)
            RMNCH.ChildHoodVisit -> getString(R.string.child_hood_visit)
            RMNCH.PNC -> getString(R.string.pnc_visit)
            else -> getString(R.string.hyphen_symbol)
        }

        if (viewModel.workflowName == RMNCH.ANC) {
            binding.parentLayout.addView(
                AssessmentCommonUtils.addViewSummaryLayout(
                    getString(R.string.gestational_age),
                    getValueFromMap(
                        map,
                        RMNCH.gestationalAge,
                        ViewType.VIEW_TYPE_FORM_EDITTEXT,
                        viewModel.workflowName,
                        false,
                        Triple(
                            getString(R.string.yes),
                            getString(R.string.no),
                            getString(R.string.hyphen_symbol)
                        ),
                        requireContext()
                    ),
                    null,
                    requireContext()
                )
            )

            if (map.containsKey(viewModel.workflowName)) {
                val ancMap = map[viewModel.workflowName] as Map<*, *>
                if (ancMap.containsKey(RMNCH.lastMenstrualPeriod)) {
                    val lmp = ancMap[RMNCH.lastMenstrualPeriod] as String
                    val estimatedDeliveryDate = DateUtils.calculateEstimatedDeliveryDate(DateUtils.getLastMenstrualDate(lmp))
                    val formattedEstimatedDeliveryDate =
                        DateUtils.getDateFormat().format(estimatedDeliveryDate.time)
                    binding.parentLayout.addView(
                        AssessmentCommonUtils.addViewSummaryLayout(
                            getString(R.string.estimated_delivery_date),
                            formattedEstimatedDeliveryDate,
                            null,
                            requireContext()
                        )
                    )
                    convertStringToDate(
                        lmp,
                        DateUtils.DATE_FORMAT_yyyyMMddHHmmssZZZZZ
                    )?.let { lmpDate ->
                        RMNCH.calculateNextANCVisitDate(
                            lmpDate
                        )?.let { visitDate ->
                            binding.etNextFollowUpDate.text = getDateStringFromDate(
                                visitDate, DateUtils.DATE_ddMMyyyy
                            )
                            updateFollowUpDate(getDateStringFromDate(
                                visitDate, DateUtils.DATE_ddMMyyyy
                            ))
                        }

                    }
                }
            }
        } else if (viewModel.workflowName == RMNCH.ChildHoodVisit) {
            viewModel.memberDetailsLiveData.value?.data?.dateOfBirth?.let {
                calculateAgeInMonths(it)?.let { pair ->
                    if (pair.first <= childHoodVisitMaxMonth) {
                        RMNCH.calculateNextChildHoodVisitDate(
                            age = pair.first,
                            birthDate = pair.second
                        )?.let { visitDate ->
                            binding.etNextFollowUpDate.text = getDateStringFromDate(
                                visitDate, DateUtils.DATE_ddMMyyyy
                            )
                            updateFollowUpDate(getDateStringFromDate(
                                visitDate, DateUtils.DATE_ddMMyyyy
                            ))
                        }
                    }
                }
            }
        }

        binding.parentLayout.addView(
            AssessmentCommonUtils.addViewSummaryLayout(
                title,
                getValueFromMap(
                    map,
                    RMNCH.visitNo,
                    ViewType.VIEW_TYPE_FORM_EDITTEXT,
                    viewModel.workflowName,
                    false,
                    Triple(
                        getString(R.string.yes),
                        getString(R.string.no),
                        getString(R.string.hyphen_symbol)
                    ),
                    requireContext()
                ),
                null,
                requireContext()
            )
        )

    }


    companion object {
        const val TAG: String = "AssessmentRMNCHSummaryFragment"
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.btnDone -> {
                if (binding.etNextFollowUpDate.visibility == View.VISIBLE && binding.etNextFollowUpDate.text.isNotEmpty()) {
                    updateFollowUpDate(binding.etNextFollowUpDate.text.trim().toString())
                }
                if (viewModel.otherAssessmentDetails.isEmpty()) {
                    val intent =  Intent(requireActivity(), HouseholdSearchActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(intent)
                    requireActivity().finish()
                    requireActivity().startBackgroundOfflineSync()
                } else {
                    viewModel.updateOtherAssessmentDetails()
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
                updateFollowUpDate(binding.etNextFollowUpDate.text.toString())
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
                    DateUtils.DATE_FORMAT_yyyyMMddHHmmssZZZZZ,
                    inUTC = true
                )
        }
    }

    fun getCurrentAnsweredStatus(): Boolean {
        return viewModel.otherAssessmentDetails.isNotEmpty()
    }

    private fun showQuestionBasedAge():List<String> {
        var age=viewModel.ageInMonth.value
        var questionList=ArrayList<String>()
        if (age?.contains(getString(R.string.week), true) == true
            || age?.contains(getString(R.string.day), true) == true
        ){
            questionList.add(AssessmentDefinedParams.TakingMinimumMealsPerDay)
            questionList.add(AssessmentDefinedParams.FedFrom4FoodGroups)
            questionList.add(AssessmentDefinedParams.Measles1Given)
            questionList.add(AssessmentDefinedParams.YellowFeverVacineGiven)
            questionList.add(AssessmentDefinedParams.Measles2Given)

        }else {
                when (age?.replace(getString(R.string.months), "")?.replace(getString(R.string.month), "")?.trim()?.toInt()) {
                    in 0..5 -> {
                        questionList.add(AssessmentDefinedParams.TakingMinimumMealsPerDay)
                        questionList.add(AssessmentDefinedParams.FedFrom4FoodGroups)
                        questionList.add(AssessmentDefinedParams.Measles1Given)
                        questionList.add(AssessmentDefinedParams.YellowFeverVacineGiven)
                        questionList.add(AssessmentDefinedParams.Measles2Given)
                    }
                    in 6..12 -> {
                        questionList.add(AssessmentDefinedParams.ExclusivelyBreastfeeding)
                        questionList.add(AssessmentDefinedParams.Measles2Given)

                    }
                    in 13..15 -> {
                        questionList.add(AssessmentDefinedParams.ExclusivelyBreastfeeding)
                    }

                    else -> {
                    }
                }
            }

        return questionList
    }
}