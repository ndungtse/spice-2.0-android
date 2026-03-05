package com.medtroniclabs.spice.ui.assessment.fragment

import android.app.DatePickerDialog
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import com.medtroniclabs.spice.R
import com.medtroniclabs.spice.app.analytics.utils.AnalyticsDefinedParams
import com.medtroniclabs.spice.app.analytics.utils.AnalyticsDefinedParams.DONEBUTTONTRIGGERED
import com.medtroniclabs.spice.appextensions.gone
import com.medtroniclabs.spice.appextensions.setVisible
import com.medtroniclabs.spice.appextensions.startBackgroundOfflineSync
import com.medtroniclabs.spice.appextensions.visible
import com.medtroniclabs.spice.common.DateUtils
import com.medtroniclabs.spice.common.DateUtils.calculateAgeInMonths
import com.medtroniclabs.spice.common.DateUtils.calculateGestationalAge
import com.medtroniclabs.spice.common.DateUtils.convertStringToDate
import com.medtroniclabs.spice.common.DateUtils.getDateStringFromDate
import com.medtroniclabs.spice.common.DateUtils.getLastMenstrualDate
import com.medtroniclabs.spice.common.DefinedParams
import com.medtroniclabs.spice.common.DefinedParams.AssessmentId
import com.medtroniclabs.spice.common.SecuredPreference
import com.medtroniclabs.spice.common.StringConverter
import com.medtroniclabs.spice.common.ViewUtils
import com.medtroniclabs.spice.databinding.CardLayoutBinding
import com.medtroniclabs.spice.databinding.FragmentRmnchSummaryBinding
import com.medtroniclabs.spice.databinding.InstructionLayoutBinding
import com.medtroniclabs.spice.databinding.TextLabelLayoutBinding
import com.medtroniclabs.spice.formgeneration.FormSupport.translateTitle
import com.medtroniclabs.spice.formgeneration.config.ViewType
import com.medtroniclabs.spice.formgeneration.extension.safeClickListener
import com.medtroniclabs.spice.formgeneration.model.FormLayout
import com.medtroniclabs.spice.formgeneration.utility.CustomSpinnerAdapterCustomLayout
import com.medtroniclabs.spice.formgeneration.utility.InformationLayoutFragment
import com.medtroniclabs.spice.network.resource.ResourceState
import com.medtroniclabs.spice.ui.BaseFragment
import com.medtroniclabs.spice.ui.MenuConstants
import com.medtroniclabs.spice.ui.assessment.AssessmentCommonUtils.addViewSummaryLayout
import com.medtroniclabs.spice.ui.assessment.AssessmentDefinedParams
import com.medtroniclabs.spice.ui.assessment.AssessmentDefinedParams.DEATH_OF_MOTHER_KEY
import com.medtroniclabs.spice.ui.assessment.referrallogic.utils.ReferralStatus
import com.medtroniclabs.spice.ui.assessment.rmnch.RMNCH
import com.medtroniclabs.spice.ui.assessment.rmnch.RMNCH.ANC
import com.medtroniclabs.spice.ui.assessment.rmnch.RMNCH.ChildHoodVisit
import com.medtroniclabs.spice.ui.assessment.rmnch.RMNCH.DeathOfMother
import com.medtroniclabs.spice.ui.assessment.rmnch.RMNCH.childHoodVisitMaxMonth
import com.medtroniclabs.spice.ui.assessment.rmnch.RMNCH.deathOfBaby
import com.medtroniclabs.spice.ui.assessment.rmnch.RMNCH.getValueFromMap
import com.medtroniclabs.spice.ui.assessment.rmnch.RMNCHAssessmentEvaluator
import com.medtroniclabs.spice.ui.assessment.viewmodel.AssessmentViewModel
import com.medtroniclabs.spice.ui.cbs.activity.CbsActivity
import com.medtroniclabs.spice.ui.household.HouseholdSearchActivity

class AssessmentRMNCHSummaryFragment : BaseFragment(), View.OnClickListener {
    lateinit var binding: FragmentRmnchSummaryBinding

    private val viewModel: AssessmentViewModel by activityViewModels()

    private var datePickerDialog: DatePickerDialog? = null

    // Variables to collect summary data for saving to PregnancyDetail

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentRmnchSummaryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)
        setListener()
        initSummaryViewByWorkFlowName()
        viewModel.setUserJourney(getUserJourneyName())
        binding.etNextFollowUpDate.background = ContextCompat.getDrawable(requireContext(), R.drawable.edittext_background)
        val background = binding.etNextFollowUpDate.background as? GradientDrawable
        background?.setStroke(resources.getDimensionPixelSize(R.dimen._1sdp), ContextCompat.getColor(requireContext(), R.color.edittext_stroke))
    }

    private fun getUserJourneyName(): String {
        when (viewModel.workflowName) {
            ChildHoodVisit -> {
                return "${viewModel.workflowName}${AnalyticsDefinedParams.RMNCHCHILDASSESSMENTSUMMARY}"
            }
            else -> {
                return "${viewModel.workflowName}${AnalyticsDefinedParams.RMNCHSummaryAssessment}"
            }
        }
    }

    private fun setListener() {
        binding.btnDone.safeClickListener(this)
        binding.callSupervisor.safeClickListener(this)
        binding.etNextFollowUpDate.safeClickListener(this)
        binding.etNextFollowUpDate.addTextChangedListener {
            binding.btnDone.isEnabled = !it.isNullOrEmpty()
        }
    }

    private fun updateStatusBar() {
        // For ANC workflow, referral facility visibility is handled in initSummaryViewByWorkFlowName
        // based on highRiskPregnantWoman and gapsInAnc values
        if (viewModel.workflowName == RMNCH.ANC) {
            // Visibility is already set in initSummaryViewByWorkFlowName, just update status
            if (binding.labelPhuReferred.visibility == View.VISIBLE) {
                viewModel.referralStatus = ReferralStatus.Referred.name
            }
//            return
        }

        when (viewModel.referralStatus) {
            ReferralStatus.Referred.name -> {
                viewModel.nearestFacilityLiveData.value?.data?.let { siteList ->
//                    loadPhuSitesList(siteList)
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
            // Hide patient status for ANC workflow
            if (viewModel.workflowName != RMNCH.ANC) {
                bindRmnchSummaryView(
                    getString(R.string.patient_status),
                    getStatus(viewModel.referralStatus) ?: getString(R.string.seperator_hyphen),
                )
            }
            conditionBasedRendering(map)
            // Skip default summary views for ANC - only show specific isSummary fields
            if (viewModel.workflowName != RMNCH.ANC) {
                addDefaultSummaryView(map)
            } else {
                // For ANC, show Next Follow-up Date with label "Follow up Visit"
                binding.etNextFollowUpDate.visible()
                binding.tvNextFollowupDateTitle.visible()
                binding.tvNextFollowupDateTitle.text = AssessmentDefinedParams.LABEL_FOLLOW_UP_VISIT

                // Set follow-up date to 4 weeks (28 days) from current date
                val fourWeeksFromNow = DateUtils.getDateAfterDays(28)
                binding.etNextFollowUpDate.text = fourWeeksFromNow
                updateFollowUpDate(fourWeeksFromNow)

                // Load Referral Facility spinner options from JSON
                loadReferralFacilityOptions()

                // For ANC workflow, render Result section with only highRiskPregnantWoman and gapsInAnc
                // (Counselling fields will be rendered separately after this block)
                // First, read values to check if referral facility should be shown
                val ancMap = map[viewModel.workflowName] as? Map<*, *>
                val summaryGroup = ancMap?.get(AssessmentDefinedParams.GROUP_SUMMARY) as? Map<*, *>
                val highRiskList = (summaryGroup?.get(AssessmentDefinedParams.HIGH_RISK_PREGNANT_WOMAN) as? List<*>)?.filterIsInstance<String>() ?: emptyList()
                val gapsList = (summaryGroup?.get(AssessmentDefinedParams.GAPS_IN_ANC) as? List<*>)?.filterIsInstance<String>() ?: emptyList()

                // Show referral facility only if highRiskPregnantWoman or gapsInAnc has values
                val hasHighRiskOrGaps = highRiskList.isNotEmpty() || gapsList.isNotEmpty()
                if (hasHighRiskOrGaps) {
                    viewModel.referralStatus = ReferralStatus.Referred.name
                    binding.labelPhuReferred.text = AssessmentDefinedParams.LABEL_REFERRAL_FACILITY
                    binding.labelPhuReferred.visible()
                    binding.etPhuChange.visible()
                } else {
                    binding.labelPhuReferred.gone()
                    binding.etPhuChange.gone()
                }

                viewModel.formLayoutsLiveData.value
                    ?.data
                    ?.formLayout
                    ?.filter { it.isSummary == true }
                    ?.filter {
                        it.id == AssessmentDefinedParams.HIGH_RISK_PREGNANT_WOMAN ||
                            it.id == AssessmentDefinedParams.GAPS_IN_ANC
                    }?.sortedBy { it.orderId ?: Int.MAX_VALUE }
                    ?.forEach { data ->
                        with(data) {
                            updateStatusBar()
                            // For High Risk pregnant woman and Gaps in ANC, read from result map
                            if (id == AssessmentDefinedParams.HIGH_RISK_PREGNANT_WOMAN) {
                                // Display heading only
                                val displayTitle = title
                                with(TextLabelLayoutBinding.inflate(LayoutInflater.from(requireContext()))) {
                                    with(tvTitle) {
                                        text = displayTitle
                                        setTypeface(null, Typeface.BOLD)
                                    }
                                    binding.parentLayout.addView(root)
                                }

                                // Display all high risk conditions as list items
                                if (highRiskList.isNotEmpty()) {
                                    highRiskList.forEach { condition ->
                                        with(TextLabelLayoutBinding.inflate(LayoutInflater.from(requireContext()))) {
                                            with(tvTitle) {
                                                text = "    • $condition" // Use bigger bullet (•)
                                                setTextColor(Color.RED)
                                            }
                                            binding.parentLayout.addView(root)
                                        }
                                    }
                                } else {
                                    // Show hyphen if no high risk conditions
                                    with(TextLabelLayoutBinding.inflate(LayoutInflater.from(requireContext()))) {
                                        with(tvTitle) {
                                            text = getString(R.string.hyphen_symbol)
                                        }
                                        binding.parentLayout.addView(root)
                                    }
                                }
                            } else if (id == AssessmentDefinedParams.GAPS_IN_ANC) {
                                // Display heading only
                                val displayTitle = title
                                with(TextLabelLayoutBinding.inflate(LayoutInflater.from(requireContext()))) {
                                    with(tvTitle) {
                                        text = displayTitle
                                        setTypeface(null, Typeface.BOLD)
                                    }
                                    binding.parentLayout.addView(root)
                                }

                                // Add gaps as list items
                                if (gapsList.isNotEmpty()) {
                                    gapsList.forEach { gap ->
                                        with(TextLabelLayoutBinding.inflate(LayoutInflater.from(requireContext()))) {
                                            with(tvTitle) {
                                                text = "    • $gap" // Use bigger bullet (•)
                                            }
                                            binding.parentLayout.addView(root)
                                        }
                                    }
                                } else {
                                    // Show hyphen if no gaps
                                    with(TextLabelLayoutBinding.inflate(LayoutInflater.from(requireContext()))) {
                                        with(tvTitle) {
                                            text = getString(R.string.hyphen_symbol)
                                        }
                                        binding.parentLayout.addView(root)
                                    }
                                }
                            } else {
                                val displayValue = getValueFromMap(
                                    map,
                                    id,
                                    viewType,
                                    viewModel.workflowName,
                                    isBooleanAnswer,
                                    Triple(
                                        getString(R.string.yes),
                                        getString(R.string.no),
                                        getString(R.string.hyphen_symbol),
                                    ),
                                    requireContext(),
                                )
                                binding.parentLayout.addView(
                                    addViewSummaryLayout(
                                        titleSummary ?: (titleCulture ?: title),
                                        displayValue,
                                        null,
                                        requireContext(),
                                    ),
                                )
                            }
                        }
                    }

            }

            // For ANC workflow, render Counselling fields in a CardView (similar to result card)
            if (viewModel.workflowName == RMNCH.ANC) {
                // Get the parent LinearLayout that contains resultCardView and referral facility fields
                val scrollView = binding.scrollView
                val parentLinearLayout = scrollView?.getChildAt(0) as? android.widget.LinearLayout

                // Find the index right after resultCardView to insert counselling card
                val resultCardIndex = parentLinearLayout?.indexOfChild(binding.resultCardView) ?: -1
                val insertIndex = if (resultCardIndex >= 0) resultCardIndex + 1 else -1

                // Get counselling card layout info for title
                val counsellingCardLayout = viewModel.formLayoutsLiveData.value
                    ?.data
                    ?.formLayout
                    ?.firstOrNull { it.id == AssessmentDefinedParams.GROUP_COUNSELLING && it.viewType == ViewType.VIEW_TYPE_FORM_CARD_FAMILY }

                // Get counselling items
                val counsellingItems = viewModel.formLayoutsLiveData.value
                    ?.data
                    ?.formLayout
                    ?.filter { it.isSummary == true }
                    ?.filter {
                        it.id == AssessmentDefinedParams.NUTRITION_COUNSELLING ||
                            it.id == AssessmentDefinedParams.CARE_DURING_ANTENATAL_PERIOD ||
                            it.id == AssessmentDefinedParams.BIRTH_PREPAREDNESS ||
                            it.id == AssessmentDefinedParams.NEW_BORN_CARE_EDUCATION ||
                            (it.viewType == ViewType.VIEW_TYPE_INSTRUCTION && it.isSummary == true && it.family == AssessmentDefinedParams.GROUP_COUNSELLING)
                    }?.sortedBy { it.orderId ?: Int.MAX_VALUE }

                // Only create counselling card if there are items to display
                if (counsellingItems != null && counsellingItems.isNotEmpty()) {
                    // Create Counselling CardView using CardLayoutBinding
                    val counsellingCardBinding = CardLayoutBinding.inflate(LayoutInflater.from(requireContext()))

                    // Set card title
                    counsellingCardLayout?.let { cardLayout ->
                        val isTranslationEnabled = com.medtroniclabs.spice.common.SecuredPreference
                            .getIsTranslationEnabled()
                        val cardTitle = if (isTranslationEnabled && !cardLayout.titleCulture.isNullOrBlank()) {
                            cardLayout.titleCulture ?: cardLayout.title
                        } else {
                            cardLayout.title
                        }
                        counsellingCardBinding.cardTitle.text = cardTitle
                    } ?: run {
                        // Fallback title if cardLayout not found
                        counsellingCardBinding.cardTitle.text = "Counselling"
                    }

                    // Add counselling items to the card's content layout
                    counsellingItems.forEach { data ->
                        with(data) {
                            val instructionBinding = InstructionLayoutBinding.inflate(LayoutInflater.from(requireContext()))
                            val isTranslationEnabled = SecuredPreference.getIsTranslationEnabled()
                            instructionBinding.tvTitle.text = translateTitle(titleCulture, title, isTranslationEnabled)
                            instructionBinding.tvTitle.visibility = View.VISIBLE
                            instructionBinding.tvTitle.setTextColor(Color.BLACK)

                            instructionBinding.clInstructionRoot.safeClickListener {
                                // Use instructionsCulture if translation is enabled and available, otherwise use instructions
                                val instructionsList = if (isTranslationEnabled && !instructionsCulture.isNullOrEmpty()) {
                                    instructionsCulture
                                } else {
                                    instructions
                                }
                                if (instructionsList != null && instructionsList.isNotEmpty()) {
                                    InformationLayoutFragment
                                        .newInstance(id, translateTitle(titleCulture, title, isTranslationEnabled), customInformationList = instructionsList)
                                        .show(childFragmentManager, InformationLayoutFragment.TAG)
                                    viewModel.setUserJourney("$id ${AnalyticsDefinedParams.INFORMATIONDIALOUGE}")
                                }
                            }
                            // Add to counselling card's content layout
                            counsellingCardBinding.llFamilyRoot.addView(instructionBinding.root)
                        }
                    }

                    // Insert counselling card after result card
                    if (insertIndex >= 0 && parentLinearLayout != null) {
                        parentLinearLayout.addView(counsellingCardBinding.root, insertIndex)
                    } else {
                        // Fallback: add to end
                        parentLinearLayout?.addView(counsellingCardBinding.root)
                    }
                } else {
                    false
                }
            } else {
                // For other workflows (ChildHoodVisit, PNC), use existing logic
                viewModel.formLayoutsLiveData.value
                    ?.data
                    ?.formLayout
                    ?.filter { it.isSummary == true }
                    ?.filter {
                        map.entries.any { map -> map.key != ChildHoodVisit } ||
                            (map[ChildHoodVisit] as? Map<String, Any>)?.containsKey(
                                it.id,
                            ) == true
                    }?.filterNot {
                        // Only apply age-based filtering for ChildHoodVisit workflow
                        if (viewModel.workflowName == ChildHoodVisit) {
                            it.id in showQuestionBasedAge(map, it)
                        } else {
                            false
                        }
                    }?.forEach { data ->
                        with(data) {
                            updateStatusBar()
                            binding.parentLayout.addView(
                                addViewSummaryLayout(
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
                                            getString(R.string.hyphen_symbol),
                                        ),
                                        requireContext(),
                                    ),
                                    null,
                                    requireContext(),
                                ),
                            )
                        }
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
        // For ANC workflow, Next Follow-up Date is always shown (handled in initSummaryViewByWorkFlowName)
        if (viewModel.workflowName == RMNCH.ANC) {
            return
        }

        if (map.containsKey(viewModel.workflowName)) {
            val workflowMap = map[viewModel.workflowName]
            if (workflowMap is Map<*, *>) {
                if (workflowMap.containsKey(RMNCH.Miscarriage) || workflowMap.containsKey(DeathOfMother) || workflowMap.containsKey(deathOfBaby)) {
                    val miscarriageValue = workflowMap[RMNCH.Miscarriage]
                    val deathOfMother = workflowMap[DeathOfMother]
                    val deathOfBaby = workflowMap[deathOfBaby]
                    showCallBtnForDeathMother((deathOfMother is Boolean && deathOfMother) || deathOfBaby is Boolean && deathOfBaby)
                    if ((miscarriageValue is Boolean && miscarriageValue) ||
                        (deathOfMother is Boolean && deathOfMother) ||
                        (deathOfBaby is Boolean && deathOfBaby)
                    ) {
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

    private fun showCallBtnForDeathMother(isShow: Boolean) {
        binding.callSupervisor.setVisible(isShow)
    }

    private fun loadReferralFacilityOptions() {
        val referralFacilityField = viewModel.formLayoutsLiveData.value
            ?.data
            ?.formLayout
            ?.find { it.id == AssessmentDefinedParams.REFERRAL_FACILITY }

        val optionsList = referralFacilityField?.optionsList as? ArrayList<Map<String, Any>> ?: return

        // Convert optionsList to the format expected by the spinner
        val siteList = ArrayList<Map<String, Any>>()
        optionsList.forEach { option ->
            val optionMap = HashMap<String, Any>()
            optionMap[DefinedParams.id] = option["id"]?.toString() ?: ""
            optionMap[DefinedParams.name] = option["name"]?.toString() ?: ""
            siteList.add(optionMap)
        }

        binding.etPhuChange.background = ContextCompat.getDrawable(requireContext(), R.drawable.edittext_background)
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
                    itemId: Long,
                ) {
                    val selectedItem = adapter.getData(position = pos)
                    selectedItem?.let {
                        val selectedId = it[DefinedParams.id] as String?
                        // Store the selected referral facility ID
                        viewModel.otherAssessmentDetails[AssessmentDefinedParams.ReferredPHUSiteID] =
                            selectedId ?: ""
                    }
                }

                override fun onNothingSelected(p0: AdapterView<*>?) {
                    /**
                     * this method is not used
                     */
                }
            }
    }

    private fun loadPhuSitesList(siteList: ArrayList<Map<String, Any>>) {
        binding.etPhuChange.background = ContextCompat.getDrawable(requireContext(), R.drawable.edittext_background)
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
                    itemId: Long,
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
            ChildHoodVisit -> getString(R.string.child_hood_visit)
            RMNCH.PNC -> getString(R.string.pnc_visit)
            else -> getString(R.string.hyphen_symbol)
        }

        if (viewModel.workflowName == RMNCH.ANC) {
            binding.parentLayout.addView(
                addViewSummaryLayout(
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
                            getString(R.string.hyphen_symbol),
                        ),
                        requireContext(),
                    ),
                    null,
                    requireContext(),
                ),
            )

            if (map.containsKey(viewModel.workflowName)) {
                val ancMap = map[viewModel.workflowName] as Map<*, *>
                if (ancMap.containsKey(RMNCH.lastMenstrualPeriod)) {
                    val lmp = ancMap[RMNCH.lastMenstrualPeriod] as String
                    val estimatedDeliveryDate = DateUtils.calculateEstimatedDeliveryDate(DateUtils.getLastMenstrualDate(lmp))
                    val formattedEstimatedDeliveryDate =
                        DateUtils.getDateFormat().format(estimatedDeliveryDate.time)
                    binding.parentLayout.addView(
                        addViewSummaryLayout(
                            getString(R.string.estimated_delivery_date),
                            formattedEstimatedDeliveryDate,
                            null,
                            requireContext(),
                        ),
                    )
                    convertStringToDate(
                        lmp,
                        DateUtils.DATE_FORMAT_yyyyMMddHHmmssZZZZZ,
                    )?.let { lmpDate ->
                        RMNCH
                            .calculateNextANCVisitDate(
                                lmpDate,
                            )?.let { visitDate ->
                                binding.etNextFollowUpDate.text = getDateStringFromDate(
                                    visitDate,
                                    DateUtils.DATE_ddMMyyyy,
                                )
                                updateFollowUpDate(
                                    getDateStringFromDate(
                                        visitDate,
                                        DateUtils.DATE_ddMMyyyy,
                                    ),
                                )
                            }
                    }
                }
            }
        } else if (viewModel.workflowName == ChildHoodVisit) {
            viewModel.memberDetailsLiveData.value?.data?.dateOfBirth?.let {
                calculateAgeInMonths(it)?.let { pair ->
                    if (pair.first <= childHoodVisitMaxMonth) {
                        RMNCH
                            .calculateNextChildHoodVisitDate(
                                age = pair.first,
                                birthDate = pair.second,
                            )?.let { visitDate ->
                                binding.etNextFollowUpDate.text = getDateStringFromDate(
                                    visitDate,
                                    DateUtils.DATE_ddMMyyyy,
                                )
                                updateFollowUpDate(
                                    getDateStringFromDate(
                                        visitDate,
                                        DateUtils.DATE_ddMMyyyy,
                                    ),
                                )
                            }
                    }
                }
            }
        }

        binding.parentLayout.addView(
            addViewSummaryLayout(
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
                        getString(R.string.hyphen_symbol),
                    ),
                    requireContext(),
                ),
                null,
                requireContext(),
            ),
        )
    }

    companion object {
        const val TAG: String = "AssessmentRMNCHSummaryFragment"
    }

    private fun handleDoneButtonClick() {
        viewModel.fetchCurrentLocation(requireContext())
        if (binding.etNextFollowUpDate.visibility == View.VISIBLE && binding.etNextFollowUpDate.text.isNotEmpty()) {
            updateFollowUpDate(
                binding.etNextFollowUpDate.text
                    .trim()
                    .toString(),
            )
        }
        if (viewModel.otherAssessmentDetails.isEmpty()) {
            val intent = Intent(requireActivity(), HouseholdSearchActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            requireActivity().finish()
            requireActivity().startBackgroundOfflineSync()
        } else {
            viewModel.updateOtherAssessmentDetails()
        }
        viewModel.setUserJourney(DONEBUTTONTRIGGERED)
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.btnDone -> {
                withLocationCheck(::handleDoneButtonClick)
            }
            R.id.callSupervisor -> {
                viewModel.workflowName?.let { startCbsActivity(it) }
            }
            binding.etNextFollowUpDate.id -> {
                showDatePickerDialog()
            }
        }
    }

    private fun startCbsActivity(workFlowName: String) {
        val intent = Intent(requireContext(), CbsActivity::class.java)
        intent.putExtra(DefinedParams.MemberID, viewModel.selectedHouseholdMemberId)
        intent.putExtra(DefinedParams.DOB, viewModel.selectedMemberDob)
        intent.putExtra(MenuConstants.WORKFLOW_NAME, workFlowName)
        viewModel.assessmentSaveLiveData.value?.data?.second?.id?.let {
            intent.putExtra(AssessmentId, it)
        }
        if (workFlowName.equals(ChildHoodVisit, true)) {
            intent.putExtra(RMNCH.deathOfNewborn, true)
        } else {
            intent.putExtra(DeathOfMother, true)
        }
        intent.putExtra(DefinedParams.MenuId, DefinedParams.CBS.lowercase())
        startActivity(intent)
    }

    private fun showDatePickerDialog() {
        var yearMonthDate: Triple<Int?, Int?, Int?>? = null
        if (!binding.etNextFollowUpDate.text.isNullOrBlank()) {
            yearMonthDate =
                DateUtils.convertedMMMToddMM(binding.etNextFollowUpDate.text.toString())
        }
        if (datePickerDialog == null) {
            datePickerDialog = ViewUtils.showDatePicker(
                context = requireContext(),
                minDate = DateUtils.getTomorrowDate(),
                date = yearMonthDate,
                cancelCallBack = { datePickerDialog = null },
            ) { _, year, month, dayOfMonth ->
                val stringDate = "$dayOfMonth-$month-$year"
                binding.etNextFollowUpDate.text =
                    DateUtils.convertDateTimeToDate(
                        stringDate,
                        DateUtils.DATE_FORMAT_ddMMyyyy,
                        DateUtils.DATE_ddMMyyyy,
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
                    inUTC = true,
                )
        }
    }

    fun getCurrentAnsweredStatus(): Boolean = viewModel.otherAssessmentDetails.isNotEmpty()

    private fun showQuestionBasedAge(
        map: HashMap<String, Any>,
        formLayout: FormLayout,
    ): List<String> {
        var age = viewModel.ageInMonth.value
        var questionList = ArrayList<String>()
        val resultMap = map[ANC] as? Map<String, Any> // Cast to Map<String, Any>
        val deathOfMother = resultMap?.get(DEATH_OF_MOTHER_KEY) as? Boolean
        if (deathOfMother == true) {
            if (formLayout.id != DEATH_OF_MOTHER_KEY) {
                questionList.add(getAllIdsExcludingDeathOfMother(formLayout))
            }
        } else {
            if (age?.contains(getString(R.string.week), true) == true ||
                age?.contains(getString(R.string.day), true) == true
            ) {
                questionList.add(AssessmentDefinedParams.TakingMinimumMealsPerDay)
                questionList.add(AssessmentDefinedParams.FedFrom4FoodGroups)
                questionList.add(AssessmentDefinedParams.Measles1Given)
                questionList.add(AssessmentDefinedParams.YellowFeverVacineGiven)
                questionList.add(AssessmentDefinedParams.Measles2Given)
            } else {
                when (
                    age
                        ?.replace(getString(R.string.months), "")
                        ?.replace(getString(R.string.month), "")
                        ?.trim()
                        ?.toInt()
                ) {
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
        }

        return questionList
    }

    private fun getAllIdsExcludingDeathOfMother(formLayouts: FormLayout): String {
        return formLayouts.id // Exclude "deathOfMother"
    }

    private fun bindRmnchSummaryView(
        title: String?,
        value: String?,
        valueTextColor: Int? = null,
    ) {
        value?.let { result ->
            binding.parentLayout.addView(
                addViewSummaryLayout(
                    title,
                    result,
                    valueTextColor,
                    requireContext(),
                ),
            )
        }
    }

    fun getStatus(referralStatus: String?): String? =
        when (referralStatus) {
            ReferralStatus.Referred.name -> getString(R.string.referred)
            ReferralStatus.OnTreatment.name -> getString(R.string.on_treatment)
            ReferralStatus.Recovered.name -> getString(R.string.recovered)
            else -> {
                null
            }
        }


}
