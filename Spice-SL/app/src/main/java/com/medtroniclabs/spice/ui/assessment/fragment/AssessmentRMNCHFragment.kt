package com.medtroniclabs.spice.ui.assessment.fragment

import android.content.res.Configuration
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.RadioButton
import android.widget.TextView
import androidx.core.text.buildSpannedString
import androidx.core.text.color
import androidx.core.view.children
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.medtroniclabs.spice.R
import com.medtroniclabs.spice.app.analytics.model.UserDetail
import com.medtroniclabs.spice.app.analytics.utils.AnalyticsDefinedParams
import com.medtroniclabs.spice.app.analytics.utils.AnalyticsUtils
import com.medtroniclabs.spice.appextensions.getLongTime
import com.medtroniclabs.spice.appextensions.gone
import com.medtroniclabs.spice.appextensions.visible
import com.medtroniclabs.spice.common.CommonUtils
import com.medtroniclabs.spice.common.CommonUtils.extractNumber
import com.medtroniclabs.spice.common.DateUtils
import com.medtroniclabs.spice.common.DateUtils.calculateAgeInMonths
import com.medtroniclabs.spice.common.DefinedParams
import com.medtroniclabs.spice.common.EntityMapper
import com.medtroniclabs.spice.common.SecuredPreference
import com.medtroniclabs.spice.common.parseJsonStringToList
import com.medtroniclabs.spice.data.model.RecommendedDosageListModel
import com.medtroniclabs.spice.databinding.FragmentAssessmentRmnchBinding
import com.medtroniclabs.spice.db.entity.PregnancyDetail
import com.medtroniclabs.spice.formgeneration.FormGenerator
import com.medtroniclabs.spice.formgeneration.config.ViewType
import com.medtroniclabs.spice.formgeneration.extension.safeClickListener
import com.medtroniclabs.spice.formgeneration.listener.FormEventListener
import com.medtroniclabs.spice.formgeneration.model.FormLayout
import com.medtroniclabs.spice.formgeneration.ui.FormResultComposer
import com.medtroniclabs.spice.formgeneration.utility.CheckBoxDialog
import com.medtroniclabs.spice.formgeneration.utility.InformationLayoutFragment
import com.medtroniclabs.spice.mappingkey.PregnantWomen
import com.medtroniclabs.spice.mappingkey.Screening
import com.medtroniclabs.spice.network.resource.ResourceState
import com.medtroniclabs.spice.ui.BaseFragment
import com.medtroniclabs.spice.ui.assessment.AssessmentCommonUtils.getMuacColorCode
import com.medtroniclabs.spice.ui.assessment.AssessmentCommonUtils.getNutritionStatus
import com.medtroniclabs.spice.ui.assessment.AssessmentDefinedParams
import com.medtroniclabs.spice.ui.assessment.AssessmentDefinedParams.MUAC
import com.medtroniclabs.spice.ui.assessment.AssessmentDefinedParams.muacStatus
import com.medtroniclabs.spice.ui.assessment.AssessmentDefinedParams.rootSuffix
import com.medtroniclabs.spice.ui.assessment.AssessmentDefinedParams.summaryKey
import com.medtroniclabs.spice.ui.assessment.referrallogic.AnemiaLevel
import com.medtroniclabs.spice.ui.assessment.referrallogic.PNCAssessmentEvaluator
import com.medtroniclabs.spice.ui.assessment.referrallogic.PNCAssessmentEvaluator.isSelectionPresent
import com.medtroniclabs.spice.ui.assessment.referrallogic.PNCReferralType
import com.medtroniclabs.spice.ui.assessment.referrallogic.PostpartumDangerSigns
import com.medtroniclabs.spice.ui.assessment.referrallogic.ReferralResultGenerator
import com.medtroniclabs.spice.ui.assessment.rmnch.RMNCH
import com.medtroniclabs.spice.ui.assessment.rmnch.RMNCHAssessmentEvaluator
import com.medtroniclabs.spice.ui.assessment.viewmodel.AssessmentViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlin.math.abs

class AssessmentRMNCHFragment :
    BaseFragment(),
    View.OnClickListener,
    FormEventListener {
    private lateinit var binding: FragmentAssessmentRmnchBinding
    private val viewModel: AssessmentViewModel by activityViewModels()
    private lateinit var formGenerator: FormGenerator

    // Store original titles (without status) for each field
    private val originalTitles = HashMap<String, String>()

    /**
     * By default non-mandatory
     */
    private var mandatoryHemoglobin = false

    /**
     * Dialog checkbox id
     */
    private var id: String = ""

    /**
     * Dialog checkbox formLayout
     */
    private var formLayout: FormLayout? = null

    /**
     * Dialog checkbox resultMap
     */
    private var resultMap: Any? = null

    /**
     * Dialog checkbox dialog key
     */
    private var dialogKey: String = ""

    /**
     * Boolean flag storing whether user has already selected treatments for existing illness or not.
     * Based on this we are showing high risk
     */
    private var ancSelectedTreatment = false

    companion object {
        const val TAG = "AssessmentRMNCHFragment"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentAssessmentRmnchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        viewModel.getNearestHealthFacility()
        attachObservers()
        setListener()
        UserDetail.startDateTime = AnalyticsUtils.getCurrentDateTimeInLocalTime()
    }

    private fun setListener() {
        binding.btnSubmit.safeClickListener(this)
    }

    private fun attachObservers() {
        viewModel.formLayoutsLiveData.observe(viewLifecycleOwner) { resourceState ->
            when (resourceState.state) {
                ResourceState.SUCCESS -> {
                    hideProgress()
                    resourceState.data?.let { data ->
                        // For ANC and PNC workflow, filter out fields marked as isSummary
                        val filteredFormLayout = if (viewModel.workflowName == RMNCH.ANC || viewModel.workflowName == RMNCH.PNC) {
                            data.formLayout.filter { it.isSummary != true }
                        } else {
                            data.formLayout
                        }
                        formGenerator.populateViews(filteredFormLayout)
                    }
                }

                ResourceState.ERROR -> {
                    hideProgress()
                }

                ResourceState.LOADING -> {
                    showProgress()
                }
            }
        }

        viewModel.memberDetailsLiveData.observe(viewLifecycleOwner) { resource ->
            when (resource.state) {
                ResourceState.LOADING -> {}
                ResourceState.SUCCESS -> {
                    showHideOptionsForChildHealth()
                }

                ResourceState.ERROR -> {}
            }
        }

        viewModel.pregnancyDetailLiveData.observe(viewLifecycleOwner) { detail ->
            when (viewModel.workflowName) {
                RMNCH.PNC -> {
                    managePncFormBasedOnPregnancyDetail(detail)
                }

                RMNCH.ANC -> {
                    updateConditionalFieldVisibility()
                    updateAllFieldStatuses()
                }

                RMNCH.ChildHoodVisit -> {
                    manageChildFormBasedOnPregnancyDetail(detail)
                }
            }
        }

        viewModel.isAssessmentCancelLiveData.observe(viewLifecycleOwner) {
            if (it) {
                formGenerator.getViewByTag(MUAC)?.apply {
                    val background = background as? GradientDrawable
                    background?.setStroke(
                        resources.getDimensionPixelSize(R.dimen._1sdp),
                        context.getColor(R.color.edittext_stroke),
                    )
                }
            }
        }

        // Child symptoms filter
        viewModel.symptomTypeListResponse.observe(this) { options ->
            val fragment = childFragmentManager.findFragmentByTag(CheckBoxDialog.TAG)
            // Fragment is already displayed, just return
            if (fragment != null) {
                return@observe
            }
            if (id == AssessmentDefinedParams.ID_CHILD_ILLNESS_TYPE) {
                viewModel.memberDetailsLiveData.value?.data?.dateOfBirth?.let { dateOfBirth ->
                    calculateAgeInMonths(dateOfBirth)?.let { pair ->
                        val mutableOptions = options.toMutableList()
                        val listOfOptionsNotEligible = if (pair.first <= 18) {
                            listOf("cannotStandWalk", "cannotBalance", "cannotSpeak")
                        } else {
                            listOf("convulsion", "rapidBreathing", "fever", "lethargy", "unableToSuckMilk", "redUmbilicus", "skinRash", "eyeProblem")
                        }
                        mutableOptions.removeIf {
                            listOfOptionsNotEligible.contains(it.value)
                        }
                        CheckBoxDialog
                            .newInstance(dialogKey, resultMap, title = formLayout?.hint, inputData = mutableOptions) { map ->
                                formLayout?.let { formLayout ->
                                    formGenerator.validateCheckboxDialogue(id, formLayout, map)
                                }
                            }.show(childFragmentManager, CheckBoxDialog.TAG)
                    }
                }
            }
        }
    }

    private fun initView() {
        replaceFragmentInId<BioDataFragment>(
            binding.bioDataFragmentContainer.id,
            tag = BioDataFragment.TAG,
        )

        // Add Pregnancy Details Fragment for ANC & PNC workflow
        if (viewModel.workflowName == RMNCH.ANC || viewModel.workflowName == RMNCH.PNC) {
            replaceFragmentInId<PregnancyDetailsRMNCHFragment>(
                binding.pregnancyDetailsFragmentContainer.id,
                tag = PregnancyDetailsRMNCHFragment.TAG,
            )
        }
        childFragmentManager.executePendingTransactions() // Ensures transaction is complete

        formGenerator = FormGenerator(
            requireContext(),
            binding.llForm,
            this,
            binding.scrollView,
            translate = SecuredPreference.getIsTranslationEnabled(),
            callback = { map, id ->
                when (viewModel.workflowName) {
                    RMNCH.PNC -> {
                        handlePNCDetails(id, map)
                    }

                    RMNCH.ANC -> {
                        handleAncDetails(id, map)
                    }
                }
            },
        )
        fetchWorkFlowData()
    }

    /**
     * Gets triggered on changing of any value in the ANC form
     */
    private fun handleAncDetails(
        id: String,
        map: HashMap<String, Any>,
    ) {
        // Handle status updates for all fields that need status evaluation
        when (id) {
            AssessmentDefinedParams.BLOOD_SUGAR -> {
                val resultMap = formGenerator.getResultMap()
                if (resultMap[id] == AssessmentDefinedParams.VALUE_FASTING) {
                    formGenerator.getViewByTag(AssessmentDefinedParams.BLOOD_SUGAR_FASTING + rootSuffix)?.apply {
                        visibility = View.VISIBLE
                    }
                    formGenerator.getViewByTag(AssessmentDefinedParams.BLOOD_SUGAR_RANDOM + rootSuffix)?.apply {
                        visibility = View.GONE
                    }
                    // Get EditText directly using tag without rootSuffix
                    val editText = formGenerator.getViewByTag(AssessmentDefinedParams.BLOOD_SUGAR_RANDOM) as? EditText
                    editText?.setText("")
                    // Also remove from result map
                    formGenerator.getResultMap().remove(AssessmentDefinedParams.BLOOD_SUGAR_RANDOM)
                }
                if (resultMap[id] == AssessmentDefinedParams.VALUE_RANDOM) {
                    formGenerator.getViewByTag(AssessmentDefinedParams.BLOOD_SUGAR_RANDOM + rootSuffix)?.apply {
                        visibility = View.VISIBLE
                    }
                    formGenerator.getViewByTag(AssessmentDefinedParams.BLOOD_SUGAR_FASTING + rootSuffix)?.apply {
                        visibility = View.GONE
                    }
                    // Get EditText directly using tag without rootSuffix
                    val editText = formGenerator.getViewByTag(AssessmentDefinedParams.BLOOD_SUGAR_FASTING) as? EditText
                    editText?.setText("")
                    // Also remove from result map
                    formGenerator.getResultMap().remove(AssessmentDefinedParams.BLOOD_SUGAR_FASTING)
                }
            }

            AssessmentDefinedParams.SYSTOLIC, AssessmentDefinedParams.DIASTOLIC -> {
                // Update BP-related fields when systolic or diastolic changes
                handleAncFieldStatusUpdate(AssessmentDefinedParams.SYSTOLIC)
                handleAncFieldStatusUpdate(AssessmentDefinedParams.DIASTOLIC)
                updateAllFieldStatuses() // BP affects multiple fields (edema, urinaryAlbumin)
            }

            AssessmentDefinedParams.PREGNANT_WOMAN_EXISTING_ILLNESS -> {
                // When existing illness changes, update status for dependent fields
                updateAllFieldStatuses() // Illness affects multiple fields
                // Specifically update BP fields, blood sugar fields, and on treatment status
                handleAncFieldStatusUpdate(AssessmentDefinedParams.SYSTOLIC)
                handleAncFieldStatusUpdate(AssessmentDefinedParams.DIASTOLIC)
                handleAncFieldStatusUpdate(AssessmentDefinedParams.BLOOD_SUGAR_FASTING)
                handleAncFieldStatusUpdate(AssessmentDefinedParams.BLOOD_SUGAR_RANDOM)
                handleAncFieldStatusUpdate(AssessmentDefinedParams.PREGNANT_WOMAN_ON_TREATMENT)
            }

            AssessmentDefinedParams.PREGNANT_WOMAN_ON_TREATMENT -> {
                updateAllFieldStatuses() // Treatment affects multiple fields
            }

            AssessmentDefinedParams.EDEMA, AssessmentDefinedParams.URINARY_ALBUMIN -> {
                updateAllFieldStatuses() // These affect each other
            }

            AssessmentDefinedParams.FOLIC_ACID_TOTAL_CONSUMED -> {
                handleAncFieldStatusUpdate(AssessmentDefinedParams.FOLIC_ACID_TABLETS)
            }

            AssessmentDefinedParams.IFA_TOTAL_CONSUMED -> {
                handleAncFieldStatusUpdate(AssessmentDefinedParams.IFA_TABLETS)
            }

            AssessmentDefinedParams.CALCIUM_TOTAL_CONSUMED -> {
                handleAncFieldStatusUpdate(AssessmentDefinedParams.CALCIUM_TABLETS)
            }

            AssessmentDefinedParams.BLOOD_SUGAR_FASTING, AssessmentDefinedParams.BLOOD_SUGAR_RANDOM -> {
                // Update blood sugar status when value changes
                handleAncFieldStatusUpdate(id)
            }

            AssessmentDefinedParams.ANC_FROM_MEDICAL_DOCTOR, AssessmentDefinedParams.ULTRASOUND -> {
                // Update status when these fields change (they depend on gestational age)
                handleAncFieldStatusUpdate(id)
            }

            AssessmentDefinedParams.TEMPERATURE,
            AssessmentDefinedParams.PULSE,
            AssessmentDefinedParams.FUNDAL_HEIGHT,
            AssessmentDefinedParams.HEMOGLOBIN,
            AssessmentDefinedParams.URINARY_SUGAR,
            AssessmentDefinedParams.URINARY_BILIRUBIN,
            AssessmentDefinedParams.FACILITY_IDENTIFIED_FOR_DELIVERY,
            -> {
                // Update status for individual fields
                handleAncFieldStatusUpdate(id)
            }

            Screening.Weight -> {
                viewModel.renderBMIValue(requireContext(), formGenerator, map)
                // Update status for individual fields
                handleAncFieldStatusUpdate(id)
            }

            Screening.Height -> {
                viewModel.renderBMIValue(requireContext(), formGenerator, map)
            }
        }
    }

    private fun showHideOptionsForChildHealth() {
        viewModel.memberDetailsLiveData.value?.data?.dateOfBirth?.let {
            calculateAgeInMonths(it)?.let { pair ->
                if (pair.first > 18) {
                    formGenerator.getViewByTag(AssessmentDefinedParams.WHAT_FED_LAST_24_HRS + rootSuffix)?.visibility = View.GONE
                    formGenerator.getViewByTag(AssessmentDefinedParams.BREAST_FEEDING + rootSuffix)?.visibility = View.VISIBLE
                    formGenerator.getViewByTag(AssessmentDefinedParams.ADDITIONAL_FOOD_GIVEN_LAST_24_HRS + rootSuffix)?.visibility = View.VISIBLE
                    formGenerator.getViewByTag(AssessmentDefinedParams.DEWORMING_MEDICINE + rootSuffix)?.visibility = View.VISIBLE
                    formGenerator.getViewByTag(AssessmentDefinedParams.VACCINE_RECEIVED + rootSuffix)?.visibility = View.VISIBLE
                } else if (pair.first > 11) {
                    formGenerator.getViewByTag(AssessmentDefinedParams.BREAST_FEEDING + rootSuffix)?.visibility = View.VISIBLE
                    formGenerator.getViewByTag(AssessmentDefinedParams.ADDITIONAL_FOOD_GIVEN_LAST_24_HRS + rootSuffix)?.visibility = View.VISIBLE
                    formGenerator.getViewByTag(AssessmentDefinedParams.DEWORMING_MEDICINE + rootSuffix)?.visibility = View.VISIBLE
                } else if (pair.first > 6) {
                    formGenerator.getViewByTag(AssessmentDefinedParams.ADDITIONAL_FOOD_GIVEN_MONTHS + rootSuffix)?.visibility = View.VISIBLE
                } else if (pair.first <= 3) {
                    formGenerator.getViewByTag(AssessmentDefinedParams.HOURS_BREAST_FEED_AFTER_BIRTH + rootSuffix)?.visibility = View.VISIBLE
                }
            }
        }
    }

    /**
     * Gets triggered on changing of any value in the PNC form
     */
    private fun handlePNCDetails(
        id: String,
        resultMap: HashMap<String, Any>,
    ) {
        when (id) {
            RMNCH.ID_POSTPARTUM_DANGER_SIGNS -> {
                val dangerSigns = resultMap[id] as? List<*>
                if (!dangerSigns.isNullOrEmpty()) {
                    val haveHeavyBleeding = dangerSigns.any {
                        it is HashMap<*, *> && it[DefinedParams.Value] == PostpartumDangerSigns.HEAVY_BLEEDING.value
                    }
                    // Hemoglobin (Hb) : Mandatory Woman reporting heavy bleeding during PNC period
                    formGenerator.markMandatory(RMNCH.ID_HEMOGLOBIN, haveHeavyBleeding || mandatoryHemoglobin)
                }
                handlePncFieldStatusUpdate(id)
            }

            PregnantWomen.ID_GRAVIDA -> {
                val gravida = CommonUtils.getDoubleOrNull(resultMap[id]) ?: return
                // Parity should less than or equal gravida (as gravida includes parity + current birth)
                formGenerator.getFormLayout(PregnantWomen.ID_PARITY)?.maxValue = gravida
            }

            PregnantWomen.ID_PARITY -> {
                val parity = CommonUtils.getDoubleOrNull(resultMap[id]) ?: return
                // Living children can be less than or equal to parity
                formGenerator.getFormLayout(PregnantWomen.ID_LIVING_CHILDREN)?.maxValue = parity
            }

            RMNCH.ID_IFA_TABLETS_CONSUMED -> {
                handlePncFieldStatusUpdate(RMNCH.ID_IFA_TABLETS)
            }

            RMNCH.ID_CALCIUM_TABLETS_CONSUMED -> {
                handlePncFieldStatusUpdate(RMNCH.ID_CALCIUM_TABLETS)
            }

            RMNCH.ID_VITAMIN_A_CONSUMED,
            RMNCH.ID_FAMILY_PLANNING_METHODS,
            RMNCH.ID_TEMPERATURE,
            RMNCH.ID_URINARY_BILIRUBIN,
            RMNCH.ID_PULSE,
            RMNCH.ID_HEMOGLOBIN,
            RMNCH.ID_ECLAMPSIA,
            -> {
                handlePncFieldStatusUpdate(id)
            }

            RMNCH.ID_EDEMA,
            RMNCH.ID_URINARY_ALBUMIN,
            -> {
                handlePncBpHighRiskHighlight()
            }

            RMNCH.ID_KNOWN_HTN -> {
                handlePncFieldStatusUpdate(id)
                handlePncBpHighRiskHighlight()
            }

            RMNCH.ID_GDM_PATIENT,
            RMNCH.ID_DM_PATIENT,
            -> {
                handlePncFieldStatusUpdate(id)
                handlePncBloodSugarHighRiskHighlight()
            }

            RMNCH.ID_FASTING_BLOOD_SUGAR,
            RMNCH.ID_RANDOM_BLOOD_SUGAR,
            -> {
                handlePncBloodSugarHighRiskHighlight()
            }

            RMNCH.ID_SYSTOLIC,
            RMNCH.ID_DIASTOLIC,
            -> {
                handlePncBpHighRiskHighlight()
            }
        }
    }

    private fun fetchWorkFlowData() {
        viewModel.workflowName?.let { name ->
            viewModel.getFormData(name)
        }
    }

    override fun onClick(v: View) {
        when (v.id) {
            binding.btnSubmit.id -> {
                withLocationCheck({
                    viewModel.fetchCurrentLocation(requireContext())
                    formGenerator.formSubmitAction(v)
                })
            }
        }
    }

    override fun loadLocalCache(
        id: String,
        localDataCache: Any,
        selectedParent: Long?,
    ) {
    }

    override fun onPopulate(targetId: String) {
    }

    override fun onCheckBoxDialogueClicked(
        id: String,
        formLayout: FormLayout,
        resultMap: Any?,
    ) {
        this.id = id
        this.formLayout = formLayout
        this.resultMap = resultMap
        // Use localDataCache if available, otherwise use id (for database loading)
        dialogKey = formLayout.localDataCache ?: id

        // If type is child illness the prefetch the data as
        // we want to manipulate it based on some conditions
        if (id == AssessmentDefinedParams.ID_CHILD_ILLNESS_TYPE) {
            viewModel.getSymptomListByType(dialogKey)
            return
        }

        // For treatment field, convert illness selections to SignsAndSymptomsEntity and pass as inputData
        // This will filter the dialog to only show the selected illness items as available options
        if (id == AssessmentDefinedParams.PREGNANT_WOMAN_ON_TREATMENT) {
            // Get illness selections - these will be available as options in treatment dialog
            val illnessSelections = formGenerator.getResult(AssessmentDefinedParams.PREGNANT_WOMAN_EXISTING_ILLNESS)
                as? ArrayList<HashMap<String, Any>> ?: arrayListOf()

            // Filter out "none" option from treatment dialog options
            val filteredSelections = illnessSelections.filter { illnessItem ->
                val id = illnessItem[DefinedParams.ID]?.toString()?.lowercase() ?: ""
                val name = illnessItem[DefinedParams.NAME]?.toString()?.lowercase() ?: ""
                id != AssessmentDefinedParams.ILLNESS_NONE_ID &&
                    name != DefinedParams.None.lowercase()
            }

            val inputData = EntityMapper.mapToSignsAndSymptomsEntity(filteredSelections, dialogKey)

            CheckBoxDialog
                .newInstance(
                    dialogKey,
                    resultMap,
                    title = formLayout.hint,
                    inputData = inputData,
                ) { map ->
                    ancSelectedTreatment = true
                    formGenerator.validateCheckboxDialogue(id, formLayout, map)
                }.show(childFragmentManager, CheckBoxDialog.TAG)
        } else {
            if (formLayout.localDataCache != null) {
                CheckBoxDialog
                    .newInstance(dialogKey, resultMap, title = formLayout.hint) { map ->
                        formGenerator.validateCheckboxDialogue(id, formLayout, map)
                    }.show(childFragmentManager, CheckBoxDialog.TAG)
            } else {
                val inputData = EntityMapper.mapToSignsAndSymptomsEntity(formLayout.optionsList)
                CheckBoxDialog
                    .newInstance(dialogKey, resultMap, title = formLayout.hint, inputData = inputData) { map ->
                        formGenerator.validateCheckboxDialogue(id, formLayout, map)
                    }.show(childFragmentManager, CheckBoxDialog.TAG)
            }
        }
    }

    override fun onInstructionClicked(
        id: String,
        title: String,
        informationList: ArrayList<String>?,
        description: String?,
        dosageListModel: ArrayList<RecommendedDosageListModel>?,
    ) {
        viewModel.instructionId = id
        showInstructionDialog(id)
    }

    override fun onFormSubmit(
        resultMap: HashMap<String, Any>?,
        serverData: List<FormLayout>?,
    ) {
        resultMap?.let { details ->
            viewModel.setUserJourney(AnalyticsDefinedParams.SUBMITBUTTONTRIGGERED)
            val result = serverData?.let {
                val menuType = when (viewModel.workflowName) {
                    RMNCH.PNC -> RMNCH.PNC
                    RMNCH.ANC -> RMNCH.ANC
                    else -> {
                        null
                    }
                }
                FormResultComposer().groupValues(
                    serverData = it,
                    details,
                    menuType = menuType,
                )
            }
            result?.second?.let { second ->
                handleNextPregnancyFlow(serverData, second)
            }
        }
        viewModel.setAnalyticsData(
            UserDetail.startDateTime,
            eventType = viewModel.workflowName.plus(AnalyticsDefinedParams.RMNCHAssessment),
            eventName = AnalyticsDefinedParams.AssessmentCreation,
        )
    }

    private fun handleNextPregnancyFlow(
        serverData: List<FormLayout>,
        second: HashMap<String, Any>,
    ) {
        viewModel.workflowName?.let { name ->
            when (name) {
                RMNCH.PNC -> {
                    savePNC(second)
                }

                else -> {
                    if (second.containsKey(name) && second[name] is Map<*, *>) {
                        val clinicalMap = second[name] as HashMap<String, Any>
                        clinicalMap[RMNCH.visitNo] = getANCVisitNumber()
                    }
                    calculateGestationalAge(second, name)

                    // Evaluate and add highRiskPregnantWoman and gapsInAnc to result map for ANC workflow
                    if (name == RMNCH.ANC) {
                        evaluateAndAddAncSummaryData(second)
                    } else {
                        // Save visit detail into the pregnancy table
                        viewModel.memberDetailsLiveData.value?.data?.let { memberDetail ->
                            viewModel.handlePregnancy(
                                second,
                                workflowName = name,
                                memberDetail,
                            )
                        }
                    }

                    val resultGenerator = ReferralResultGenerator()
                    val referralResult = resultGenerator.calculateRMNCHReferralResult(second)
                    viewModel.saveAssessment(
                        serverData,
                        second,
                        referralResult,
                        RMNCH.getMenuName(viewModel.workflowName),
                    )
                }
            }
        }
    }

    private fun savePNC(assessmentMap: HashMap<String, Any>) {
        viewModel.memberDetailsLiveData.value?.data?.let { memberDetail ->
            evaluateAndAddPncSummaryData(assessmentMap)
            viewModel.handlePregnancy(
                assessmentMap,
                workflowName = RMNCH.PNC,
                memberDetail,
            )
            viewModel.savePNCDetails(
                formGenerator.getServerData(),
                assessmentMap = assessmentMap,
                memberDetail = memberDetail,
                followUpId = viewModel.followUpId,
            )
        }
    }

    /**
     * Calculates risks and gaps and stores in the map
     */
    private fun evaluateAndAddPncSummaryData(assessmentMap: HashMap<String, Any>) {
        val pncMap = assessmentMap[RMNCH.PNC] as? HashMap<String, Any> ?: return
        val gson = Gson()
        // Update days since delivery
        getDaysSinceDelivery()?.let { days ->
            pncMap[RMNCH.ID_DAYS_SINCE_DELIVERY] = days
        }
        val gapsInPnc = PNCAssessmentEvaluator.getPncGaps(pncMap)
        if (gapsInPnc.isNotEmpty()) {
            val gapsJson = gson.toJson(gapsInPnc)
            pncMap[RMNCH.ID_PNC_GAPS] = gapsJson
        }
        val urgentReferrals = PNCAssessmentEvaluator.getUrgentReferral(pncMap)
        val nonUrgentReferrals = PNCAssessmentEvaluator.getNonUrgentReferral(pncMap)
        val finalReferrals = urgentReferrals + nonUrgentReferrals
        if (finalReferrals.isNotEmpty()) {
            val referralType = if (urgentReferrals.isNotEmpty()) {
                PNCReferralType.URGENT
            } else {
                PNCReferralType.NON_URGENT
            }
            val motherRisks = JsonObject()
            val referralsJson = gson.toJsonTree(finalReferrals)
            motherRisks.addProperty(RMNCH.KEY_REFERRAL_TYPE, referralType.name)
            motherRisks.add(RMNCH.KEY_REFERRAL_VALUE, referralsJson)
            pncMap[RMNCH.ID_MOTHER_RISKS] = motherRisks.toString()
        }
        val pncIllness = JsonObject()
        (pncMap[RMNCH.ID_MATERNAL_HEALTH_ASSESSMENT] as? Map<*, *>)?.let { maternalAssessment ->
            val anemiaLevel = PNCAssessmentEvaluator.getAnemiaLevel(pncMap)
            pncIllness.addProperty(RMNCH.ID_DM_PATIENT, maternalAssessment[RMNCH.ID_DM_PATIENT] as? String)
            pncIllness.addProperty(RMNCH.ID_GDM_PATIENT, maternalAssessment[RMNCH.ID_GDM_PATIENT] as? String)
            pncIllness.addProperty(RMNCH.ID_KNOWN_HTN, maternalAssessment[RMNCH.ID_KNOWN_HTN] as? String)
            pncIllness.addProperty(RMNCH.ID_ECLAMPSIA, maternalAssessment[RMNCH.ID_ECLAMPSIA] as? String)
            pncIllness.addProperty(RMNCH.ID_ANEMIA, anemiaLevel.name)
            pncIllness.addProperty(RMNCH.ID_BLOOD_SUGAR, PNCAssessmentEvaluator.isHighBloodSugar(pncMap))
        }
        pncMap[RMNCH.ID_PNC_ILLNESS] = pncIllness.toString()
    }

    private fun calculateGestationalAge(
        details: HashMap<String, Any>,
        name: String,
    ) {
        if (details.containsKey(name) && details[name] is Map<*, *>) {
            val second = details[name] as HashMap<String, Any>
            if (second.containsKey(RMNCH.lastMenstrualPeriod)) {
                val lastMenstrualDate = second[RMNCH.lastMenstrualPeriod]
                if (lastMenstrualDate is String) {
                    val calendar = getLastMenstrualDate(lastMenstrualDate)
                    second[RMNCH.gestationalAge] = extractNumber(
                        DateUtils.formatGestationalAge(
                            DateUtils
                                .calculateGestationalAge(
                                    calendar,
                                ).first,
                            requireContext(),
                        ),
                    )
                }
            }
        }
    }

    private fun getLastMenstrualDate(clinicalDate: String): Calendar {
        // Define the format of the input date string
        val lastMenstrualDateString = DateUtils.convertDateFormat(
            clinicalDate,
            DateUtils.DATE_FORMAT_yyyyMMddHHmmssZZZZZ,
            DateUtils.DATE_ddMMyyyy,
        )
        return Calendar.getInstance().apply {
            time = getDateFormat().parse(lastMenstrualDateString)
        }
    }

    private fun getDateFormat(): SimpleDateFormat =
        SimpleDateFormat(
            DateUtils.DATE_ddMMyyyy,
            Locale.getDefault(),
        )

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        val instructionDialog =
            childFragmentManager.findFragmentByTag(AssessmentDefinedParams.InformationLayoutFragment) as? DialogFragment
        if (instructionDialog != null && instructionDialog.showsDialog) {
            instructionDialog.dismiss()
            showDialogBasedOnId()
        }
    }

    private fun showDialogBasedOnId() {
        viewModel.instructionId?.let {
            showInstructionDialog(it)
        }
    }

    private fun showInstructionDialog(id: String) {
        val titleById = getTitleById(id)
        when (id) {
            MUAC -> {
                InformationLayoutFragment
                    .newInstance(id, titleById)
                    .show(childFragmentManager, InformationLayoutFragment.TAG)
                viewModel.setUserJourney("$id ${AnalyticsDefinedParams.INFORMATIONDIALOUGE}")
            }
        }
    }

    private fun getTitleById(id: String): String =
        when (id) {
            MUAC -> getString(R.string.measuring_muac)
            else -> {
                id
            }
        }

    override fun onRenderingComplete() {
        viewModel.formRenderedLiveData.postValue(true)
        // Show biodata after form rendering completes
        binding.bioDataFragmentContainer.visible()
        // Show pregnancy biodata after form rendering completes
        if (viewModel.workflowName == RMNCH.ANC || viewModel.workflowName == RMNCH.PNC) {
            binding.pregnancyDetailsFragmentContainer.visible()
        }
        // Store original titles for fields that have status conditions
        storeOriginalTitles()
        if (viewModel.pregnancyDetailLiveData.isInitialized) {
            when (viewModel.workflowName) {
                RMNCH.PNC -> {
                    managePncFormBasedOnPregnancyDetail(viewModel.pregnancyDetailLiveData.value)
                }

                RMNCH.ANC -> {
                    updateConditionalFieldVisibility()
                    updateAllFieldStatuses()
                }

                RMNCH.ChildHoodVisit -> {
                    manageChildFormBasedOnPregnancyDetail(viewModel.pregnancyDetailLiveData.value)
                }
            }
        }
    }

    /**
     * Manipulates form based on pregnancy details
     */
    private fun managePncFormBasedOnPregnancyDetail(details: PregnancyDetail?) {
        val pregnancyHistoryCardView = formGenerator.getViewByTag(RMNCH.ID_PREGNANCY_HISTORY + formGenerator.rootSuffix)
        val pregnancyHistoryView = formGenerator.getViewByTag(RMNCH.ID_PREGNANCY_HISTORY) as? ViewGroup
        val pncVisitCount = getPncVisitNumber()
        val ancVisitCountForPnc = getAncVisitNumberForPnc()

        // For first PNC visit only, if gravida is not set, then ask for pregnancy history
        // The user is visiting the member for the first time, no PW Profile, no ANC.
        if (pncVisitCount <= 1L && (details?.gravida ?: 0) == 0) {
            pregnancyHistoryCardView?.visible()
            pregnancyHistoryView?.children?.forEach { pregnancyField ->
                pregnancyField.visible()
            }
        } else {
            pregnancyHistoryCardView?.gone()
            pregnancyHistoryView?.children?.forEach { pregnancyField ->
                formGenerator.resetChildViews(pregnancyField)
                pregnancyField.gone()
            }
        }
        // By default non-mandatory
        var bloodSugarMandatory = false

        if (pncVisitCount <= 1) {
            // For first PNC visit autopopulate the data from ANC details and disable the fields,
            // if ANC details captured

            // Mandatory during 1st PNC visit in case of
            // Woman with excessive bleeding during delivery
            if (details?.complicationsDuringDelivery?.contains("Excessive bleeding", true) == true) {
                mandatoryHemoglobin = true
            }

            if (ancVisitCountForPnc > 0) {
                // HTN
                val htnView = formGenerator.getViewByTag(RMNCH.ID_KNOWN_HTN + formGenerator.rootSuffix)
                htnView?.let {
                    formGenerator.disableView(htnView)
                    (htnView.findViewWithTag(DefinedParams.yes) as? RadioButton)?.isEnabled = false
                    (htnView.findViewWithTag(DefinedParams.no) as? RadioButton)?.isEnabled = false
                    if (details?.pregnantWomanExistingIllness?.contains("HTN", true) == true) {
                        (htnView.findViewWithTag(DefinedParams.yes) as? RadioButton)?.isChecked = true
                    } else {
                        (htnView.findViewWithTag(DefinedParams.no) as? RadioButton)?.isChecked = true
                    }
                }

                // Eclampsia
                val eclampsiaView = formGenerator.getViewByTag(RMNCH.ID_ECLAMPSIA + formGenerator.rootSuffix)
                eclampsiaView?.let {
                    formGenerator.disableView(eclampsiaView)
                    (eclampsiaView.findViewWithTag(DefinedParams.yes) as? RadioButton)?.isEnabled = false
                    (eclampsiaView.findViewWithTag(DefinedParams.no) as? RadioButton)?.isEnabled = false
                    if (details?.highRiskPregnantWoman?.contains("Eclampsia", true) == true) {
                        (eclampsiaView.findViewWithTag(DefinedParams.yes) as? RadioButton)?.isChecked = true
                    } else {
                        (eclampsiaView.findViewWithTag(DefinedParams.no) as? RadioButton)?.isChecked = true
                    }
                }

                // DM
                val dmView = formGenerator.getViewByTag(RMNCH.ID_DM_PATIENT + formGenerator.rootSuffix)
                dmView?.let {
                    formGenerator.disableView(dmView)
                    (dmView.findViewWithTag(DefinedParams.yes) as? RadioButton)?.isEnabled = false
                    (dmView.findViewWithTag(DefinedParams.no) as? RadioButton)?.isEnabled = false
                    if (details?.pregnantWomanExistingIllness?.contains("DM", true) == true) {
                        // Mandatory during 1st PNC visit in case of
                        // Woman had GDM or was a known DM patient
                        bloodSugarMandatory = true
                        (dmView.findViewWithTag(DefinedParams.yes) as? RadioButton)?.isChecked = true
                    } else {
                        (dmView.findViewWithTag(DefinedParams.no) as? RadioButton)?.isChecked = true
                    }
                }

                // GDM
                val gdmView = formGenerator.getViewByTag(RMNCH.ID_GDM_PATIENT + formGenerator.rootSuffix)
                gdmView?.let {
                    (gdmView.findViewWithTag(DefinedParams.yes) as? RadioButton)?.isEnabled = false
                    (gdmView.findViewWithTag(DefinedParams.no) as? RadioButton)?.isEnabled = false
                    formGenerator.disableView(gdmView)
                    if (details?.highRiskPregnantWoman?.contains("GDM", true) == true) {
                        // Mandatory during 1st PNC visit in case of
                        // Woman had GDM or was a known DM patient
                        bloodSugarMandatory = true
                        (gdmView.findViewWithTag(DefinedParams.yes) as? RadioButton)?.isChecked = true
                    } else {
                        (gdmView.findViewWithTag(DefinedParams.no) as? RadioButton)?.isChecked = true
                    }
                }

                if (details?.highRiskPregnantWoman?.contains("Anemia", true) == true) {
                    // Mandatory during 1st PNC visit in case of
                    // Woman who had Anemia during pregnancy
                    mandatoryHemoglobin = true
                }
            } else {
                // Mandatory during 1st PNC visit in case of
                // Woman's first interaction with SK during PNC (i,e no ANC received from SK) only during 1st PNC visit
                bloodSugarMandatory = true

                // Mandatory  during 1st PNC Visit only for
                // Woman's first interaction with SK during PNC (i,e no ANC received from SK) only during 1st PNC visit
                mandatoryHemoglobin = true
            }
        } else {
            // For subsequent visit hide the fields and autopopulate the data from PNC details

            // Hide HTN
            (formGenerator.getViewByTag(RMNCH.ID_KNOWN_HTN + formGenerator.rootSuffix))?.gone()

            // Hide Eclampsia
            (formGenerator.getViewByTag(RMNCH.ID_ECLAMPSIA + formGenerator.rootSuffix))?.gone()

            // Hide DM
            (formGenerator.getViewByTag(RMNCH.ID_DM_PATIENT + formGenerator.rootSuffix))?.gone()

            // Hide GD
            (formGenerator.getViewByTag(RMNCH.ID_GDM_PATIENT + formGenerator.rootSuffix))?.gone()

            details?.pncIllness?.let { pncIllness ->
                val pncIllnessObject = JsonParser.parseString(pncIllness).asJsonObject
                val dmPatient = pncIllnessObject.get(RMNCH.ID_DM_PATIENT).asString
                val gdmPatient = pncIllnessObject.get(RMNCH.ID_GDM_PATIENT).asString
                val isHighBloodSugar = pncIllnessObject.get(RMNCH.ID_BLOOD_SUGAR).asBoolean
                // Mandatory during subsequent PNC visit if -
                // Woman who had GDM or was a known DM patient OR
                // Blood Sugar values were higher than normal
                if (dmPatient.equals(DefinedParams.yes, true) ||
                    gdmPatient.equals(DefinedParams.yes, true) ||
                    isHighBloodSugar
                ) {
                    bloodSugarMandatory = true
                }
                formGenerator.getResultMap()[RMNCH.ID_DM_PATIENT] = pncIllnessObject.get(RMNCH.ID_DM_PATIENT).asString
                formGenerator.getResultMap()[RMNCH.ID_GDM_PATIENT] = pncIllnessObject.get(RMNCH.ID_GDM_PATIENT).asString
                formGenerator.getResultMap()[RMNCH.ID_KNOWN_HTN] = pncIllnessObject.get(RMNCH.ID_KNOWN_HTN).asString
                formGenerator.getResultMap()[RMNCH.ID_ECLAMPSIA] = pncIllnessObject.get(RMNCH.ID_ECLAMPSIA).asString
                val anemiaLevel = try {
                    AnemiaLevel.valueOf(pncIllnessObject.get(RMNCH.ID_ANEMIA).asString)
                } catch (_: Exception) {
                    AnemiaLevel.None
                }

                // Mandatory during subsequent PNC  visit in case of Woman with moderate and/or severe anemia
                mandatoryHemoglobin = anemiaLevel == AnemiaLevel.Moderate || anemiaLevel == AnemiaLevel.Severe
            }
        }

        formGenerator.markMandatory(RMNCH.ID_HEMOGLOBIN, mandatoryHemoglobin)

        formGenerator.markMandatory(RMNCH.ID_BLOOD_SUGAR, bloodSugarMandatory)
        formGenerator.markMandatory(RMNCH.ID_FASTING_BLOOD_SUGAR, bloodSugarMandatory)
        formGenerator.markMandatory(RMNCH.ID_RANDOM_BLOOD_SUGAR, bloodSugarMandatory)
    }

    /**
     * Manipulates form based on pregnancy details
     */
    private fun manageChildFormBasedOnPregnancyDetail(details: PregnancyDetail?) {
        val visitNumber = getChildVisitNumber()
        if (visitNumber > 1) {
            // CongenitalDefect
            val congenitalView = formGenerator.getViewByTag(AssessmentDefinedParams.ID_CONGENITAL_DEFECT + formGenerator.rootSuffix)
            congenitalView?.let {
                (congenitalView.findViewWithTag("${DefinedParams.yes}_${AssessmentDefinedParams.ID_CONGENITAL_DEFECT}") as? View)?.isEnabled = false
                (congenitalView.findViewWithTag("${DefinedParams.no}_${AssessmentDefinedParams.ID_CONGENITAL_DEFECT}") as? View)?.isEnabled = false
                formGenerator.disableView(congenitalView)
                if (isSelectionPresent(details?.childCongenitalDefect, DefinedParams.yes)) {
                    (congenitalView.findViewWithTag("${DefinedParams.yes}_${AssessmentDefinedParams.ID_CONGENITAL_DEFECT}") as? View)?.isSelected = true
                } else {
                    (congenitalView.findViewWithTag("${DefinedParams.no}_${AssessmentDefinedParams.ID_CONGENITAL_DEFECT}") as? View)?.isSelected = true
                }
            }
            formGenerator.getResultMap()[AssessmentDefinedParams.ID_CONGENITAL_DEFECT] = details?.childCongenitalDefect ?: ""
        }
    }

    override fun onUpdateInstruction(
        id: String,
        selectedId: Any?,
    ) {
        when (id) {
            MUAC -> {
                val rootSuffixTag = muacStatus + rootSuffix
                val summaryKeyTag = muacStatus + summaryKey
                val muacStatusTag = muacStatus

                if (selectedId is String && selectedId != DefinedParams.DefaultID) {
                    formGenerator.getViewByTag(rootSuffixTag)?.visibility = View.VISIBLE

                    (formGenerator.getViewByTag(summaryKeyTag) as? TextView)?.text =
                        requireContext().getString(
                            R.string.firstname_lastname,
                            MUAC.uppercase(),
                            selectedId,
                        )

                    (formGenerator.getViewByTag(muacStatusTag) as? TextView)?.text =
                        getNutritionStatus(selectedId, requireContext())
                    formGenerator.getViewByTag(MUAC)?.apply {
                        val background = background as? GradientDrawable
                        background?.setStroke(
                            resources.getDimensionPixelSize(R.dimen._4sdp),
                            getMuacColorCode(selectedId, requireContext()),
                        )
                    }
                } else {
                    formGenerator.getViewByTag(rootSuffixTag)?.visibility = View.GONE
                    formGenerator.getViewByTag(MUAC)?.apply {
                        val background = background as? GradientDrawable
                        background?.setStroke(
                            resources.getDimensionPixelSize(R.dimen._1sdp),
                            getMuacColorCode(selectedId as String, requireContext()),
                        )
                    }
                }
            }
        }
    }

    override fun onInformationHandling(
        id: String,
        noOfDays: Int,
        enteredDays: Int?,
        resultMap: HashMap<String, Any>?,
    ) {
    }

    override fun onAgeCheckForPregnancy() {
    }

    override fun handleMandatoryCondition(formLayout: FormLayout?) {
    }

    override fun onAgeUpdateListener(
        age: Int,
        serverData: List<FormLayout>?,
        resultHashMap: HashMap<String, Any>,
    ) {
        /*
       Never used
         */
    }

    fun getCurrentAnsweredStatus(): Boolean = formGenerator.getResultMap().isNotEmpty()

    /**
     * Calculates gestational age in weeks from LMP date
     * Returns null if LMP is not available
     * @param fullMap Optional map to use instead of formGenerator result map
     */
    private fun calculateGestationalAgeInWeeks(fullMap: HashMap<String, Any>? = null): Double? =
        try {
            val resultMap = fullMap ?: formGenerator.getResultMap()
            val ancData = resultMap[RMNCH.ANC] as? HashMap<String, Any>
            val lmpString = ancData?.get(RMNCH.lastMenstrualPeriod) as? String

            if (lmpString.isNullOrBlank()) {
                // Try to get from pregnancy detail
                viewModel.pregnancyDetailLiveData.value?.lastMenstrualPeriod?.let { lmp ->
                    val lmpCalendar = DateUtils.getLastMenstrualDate(lmp)
                    val gestationalAge = DateUtils.calculateGestationalAge(lmpCalendar)
                    gestationalAge.first.toDouble() + (gestationalAge.second / AssessmentDefinedParams.DAYS_PER_WEEK)
                }
            } else {
                val lmpCalendar = DateUtils.getLastMenstrualDate(lmpString)
                val gestationalAge = DateUtils.calculateGestationalAge(lmpCalendar)
                gestationalAge.first.toDouble() + (gestationalAge.second / 7.0)
            }
        } catch (e: Exception) {
            null
        }

    /**
     * Updates field visibility based on ANC visit number and gestational age conditions
     */
    private fun updateConditionalFieldVisibility() {
        val visitNumber = getANCVisitNumber()
        val gestationalAgeWeeks = calculateGestationalAgeInWeeks()

        // Pre-fill existing illness from last visit
        if (visitNumber > 1) {
            viewModel.pregnancyDetailLiveData.value?.pregnantWomanExistingIllness?.let { existingIllness ->
                val existingIllnessList = parseJsonStringToList<String>(existingIllness)
                val existingIllnessValue = ArrayList(existingIllnessList.map { hashMapOf(DefinedParams.Value to it) })
                formGenerator.getResultMap()[AssessmentDefinedParams.PREGNANT_WOMAN_EXISTING_ILLNESS] = existingIllnessValue
            }
        }

        // Show previous pregnancy complications only for 1st visit
        updateFieldVisibility(AssessmentDefinedParams.PREVIOUS_PREGNANCY_COMPLICATIONS, visitNumber == AssessmentDefinedParams.ANC_VISIT_NUMBER_1)

        // 1. Height - show only if ANC visit is 1
        updateFieldVisibility(AssessmentDefinedParams.HEIGHT, visitNumber == AssessmentDefinedParams.ANC_VISIT_NUMBER_1)

        // 2. BMI - show only if ANC visit 1 AND gestational age < 12 weeks
        val showBMI =
            visitNumber == AssessmentDefinedParams.ANC_VISIT_NUMBER_1 &&
                (gestationalAgeWeeks == null || gestationalAgeWeeks < AssessmentDefinedParams.GESTATIONAL_AGE_WEEK_12)
        updateFieldVisibility(AssessmentDefinedParams.BMI, showBMI)

        // 3. Edema - show only if gestational age >= 12 weeks
        val showEdema = gestationalAgeWeeks != null && gestationalAgeWeeks >= AssessmentDefinedParams.GESTATIONAL_AGE_WEEK_12
        updateFieldVisibility(AssessmentDefinedParams.EDEMA, showEdema)

        // 4. Fundal Height - show only if gestational age >= 24 weeks
        val showFundalHeight = gestationalAgeWeeks != null && gestationalAgeWeeks >= AssessmentDefinedParams.GESTATIONAL_AGE_WEEK_24
        updateFieldVisibility(AssessmentDefinedParams.FUNDAL_HEIGHT, showFundalHeight)

        // 5. Folic Acid Total Consumed - show only if gestational age <= 12 weeks
        val showFolicAcid = gestationalAgeWeeks == null || gestationalAgeWeeks <= AssessmentDefinedParams.GESTATIONAL_AGE_WEEK_12
        updateFieldVisibility(AssessmentDefinedParams.FOLIC_ACID_TABLETS, showFolicAcid)
        updateFieldVisibility(AssessmentDefinedParams.FOLIC_ACID_TOTAL_CONSUMED, showFolicAcid)
        updateFieldVisibility(AssessmentDefinedParams.FOLIC_ACID_PROVIDED, showFolicAcid)

        // 6. IFA/Iron Total Consumed - show only if gestational age <= 12 weeks
        val showIFA = gestationalAgeWeeks == null || gestationalAgeWeeks > AssessmentDefinedParams.GESTATIONAL_AGE_WEEK_12
        updateFieldVisibility(AssessmentDefinedParams.IFA_TABLETS, showIFA)
        updateFieldVisibility(AssessmentDefinedParams.IFA_TOTAL_CONSUMED, showIFA)
        updateFieldVisibility(AssessmentDefinedParams.IFA_PROVIDED, showIFA)

        // 7. Calcium Total Consumed - show only if gestational age <= 12 weeks
        val showCalcium = gestationalAgeWeeks == null || gestationalAgeWeeks > AssessmentDefinedParams.GESTATIONAL_AGE_WEEK_12
        updateFieldVisibility(AssessmentDefinedParams.CALCIUM_TABLETS, showCalcium)
        updateFieldVisibility(AssessmentDefinedParams.CALCIUM_TOTAL_CONSUMED, showCalcium)
        updateFieldVisibility(AssessmentDefinedParams.CALCIUM_PROVIDED, showCalcium)

        // 8. Danger Signs - show based on gestational age ranges
        // Show dangerSignsExperienced12 if gestational age <= 12 weeks
        val showDangerSigns12 = gestationalAgeWeeks == null || gestationalAgeWeeks <= AssessmentDefinedParams.GESTATIONAL_AGE_WEEK_12
        updateFieldVisibility(AssessmentDefinedParams.DANGER_SIGNS_EXPERIENCED_12, showDangerSigns12)

        // Show dangerSignsExperienced13To27 if gestational age >= 13 AND <= 27 weeks
        val showDangerSigns1327 =
            gestationalAgeWeeks != null &&
                gestationalAgeWeeks >= AssessmentDefinedParams.GESTATIONAL_AGE_WEEK_13 &&
                gestationalAgeWeeks <= AssessmentDefinedParams.GESTATIONAL_AGE_WEEK_27
        updateFieldVisibility(AssessmentDefinedParams.DANGER_SIGNS_EXPERIENCED_13_27, showDangerSigns1327)

        // Show dangerSignsExperienced28To40 if gestational age >= 28 AND <= 40 weeks
        val showDangerSigns2840 =
            gestationalAgeWeeks != null &&
                gestationalAgeWeeks >= AssessmentDefinedParams.GESTATIONAL_AGE_WEEK_28 &&
                gestationalAgeWeeks <= AssessmentDefinedParams.GESTATIONAL_AGE_WEEK_40
        updateFieldVisibility(AssessmentDefinedParams.DANGER_SIGNS_EXPERIENCED_28_40, showDangerSigns2840)

        updateFieldVisibility(
            AssessmentDefinedParams.GROUP_DANGER_SIGNS_RISK_IDENTIFICATION,
            showDangerSigns12 || showDangerSigns1327 || showDangerSigns2840,
        )

        // 9. ANC Services and Birth Preparedness - show only if gestational age >= 28 weeks
        val showANCServices = gestationalAgeWeeks != null && gestationalAgeWeeks >= AssessmentDefinedParams.GESTATIONAL_AGE_WEEK_28
        updateFieldVisibility(AssessmentDefinedParams.ULTRASOUND, showANCServices)
        updateFieldVisibility(AssessmentDefinedParams.ANC_FROM_MEDICAL_DOCTOR, showANCServices)
    }

    /**
     * Helper method to show or hide a field by its ID
     */
    private fun updateFieldVisibility(
        fieldId: String,
        shouldShow: Boolean,
    ) {
        val rootSuffix = formGenerator.rootSuffix
        val view = formGenerator.getViewByTag(fieldId + rootSuffix)
        if (shouldShow) {
            view?.visible()
        } else {
            view?.gone()
        }
    }

    /**
     * Stores original titles (without status) for fields that have status conditions
     */
    private fun storeOriginalTitles() {
        val fieldsToStore = listOf(
            AssessmentDefinedParams.PREGNANT_WOMAN_ON_TREATMENT,
            AssessmentDefinedParams.SYSTOLIC,
            AssessmentDefinedParams.DIASTOLIC,
            AssessmentDefinedParams.EDEMA,
            AssessmentDefinedParams.TEMPERATURE,
            AssessmentDefinedParams.PULSE,
            AssessmentDefinedParams.FUNDAL_HEIGHT,
            AssessmentDefinedParams.HEMOGLOBIN,
            AssessmentDefinedParams.URINARY_ALBUMIN,
            AssessmentDefinedParams.URINARY_SUGAR,
            AssessmentDefinedParams.URINARY_BILIRUBIN,
            AssessmentDefinedParams.FOLIC_ACID_TABLETS,
            AssessmentDefinedParams.IFA_TABLETS,
            AssessmentDefinedParams.CALCIUM_TABLETS,
            AssessmentDefinedParams.ANC_FROM_MEDICAL_DOCTOR,
            AssessmentDefinedParams.ULTRASOUND,
            AssessmentDefinedParams.FACILITY_IDENTIFIED_FOR_DELIVERY,
        )

        val textLabelFields = listOf(AssessmentDefinedParams.FOLIC_ACID_TABLETS, AssessmentDefinedParams.IFA_TABLETS, AssessmentDefinedParams.CALCIUM_TABLETS)

        fieldsToStore.forEach { fieldId ->
            // TextLabel fields use just 'id' as tag, others use 'id + titleSuffix'
            val tag = if (fieldId in textLabelFields) {
                fieldId
            } else {
                fieldId + formGenerator.titleSuffix
            }

            val titleView = formGenerator.getViewByTag(tag) as? TextView
            titleView?.let {
                val currentText = it.text.toString()
                // Remove only the last status pattern at the end: " (Status Text)"
                // Since status is always added last, this will remove only the status, keeping the rest of the title intact
                val statusPattern = "\\s+\\([^)]+\\)\\s*$".toRegex()
                val original = currentText.replace(statusPattern, "")
                originalTitles[fieldId] = original
            }
        }
    }

    /**
     * ANC : Evaluates status condition for a field and returns status text if condition matches
     * Returns Pair<statusText, statusTextCulture> or null if no condition matches
     */
    private fun evaluateAncFieldStatus(
        fieldId: String,
        value: Any?,
    ): Pair<String?, String?>? {
        val resultMap = formGenerator.getResultMap()

        return when (fieldId) {
            AssessmentDefinedParams.PREGNANT_WOMAN_ON_TREATMENT -> evaluatePregnantWomanOnTreatmentStatus(value, resultMap)
            AssessmentDefinedParams.SYSTOLIC -> evaluateSystolicStatus(value, resultMap)
            AssessmentDefinedParams.DIASTOLIC -> evaluateDiastolicStatus(value, resultMap)
            AssessmentDefinedParams.EDEMA -> evaluateEdemaStatus(value, resultMap)
            AssessmentDefinedParams.TEMPERATURE -> evaluateTemperatureStatus(value)
            AssessmentDefinedParams.PULSE -> evaluatePulseStatus(value)
            AssessmentDefinedParams.FUNDAL_HEIGHT -> evaluateFundalHeightStatus(value)
            AssessmentDefinedParams.HEMOGLOBIN -> evaluateHemoglobinStatus(value)
            AssessmentDefinedParams.URINARY_ALBUMIN -> evaluateUrinaryAlbuminStatus(value, resultMap)
            AssessmentDefinedParams.URINARY_SUGAR -> evaluateUrinarySugarStatus(value)
            AssessmentDefinedParams.URINARY_BILIRUBIN -> evaluateUrinaryBilirubinStatus(value)
            AssessmentDefinedParams.FOLIC_ACID_TABLETS -> evaluateFolicAcidStatus(resultMap)
            AssessmentDefinedParams.IFA_TABLETS -> evaluateIFAStatus(resultMap)
            AssessmentDefinedParams.CALCIUM_TABLETS -> evaluateCalciumStatus(resultMap)
            AssessmentDefinedParams.ANC_FROM_MEDICAL_DOCTOR -> evaluateANCFromMedicalDoctorStatus(value, resultMap)
            AssessmentDefinedParams.ULTRASOUND -> evaluateUltrasoundStatus(value, resultMap)
            AssessmentDefinedParams.BLOOD_SUGAR_FASTING -> evaluateBloodSugarFastingStatus(value, resultMap)
            AssessmentDefinedParams.BLOOD_SUGAR_RANDOM -> evaluateBloodSugarRandomStatus(value, resultMap)
            AssessmentDefinedParams.FACILITY_IDENTIFIED_FOR_DELIVERY -> evaluateFacilityIdentifiedForDeliveryStatus(value)
            Screening.Weight -> evaluateAncWeightStatus(value)
            else -> null
        }
    }

    /**
     * PNC : Evaluates status condition for a field and returns status text if condition matches
     * Returns Pair<statusText, statusTextCulture> or null if no condition matches
     */
    private fun evaluatePncFieldStatus(
        fieldId: String,
        value: Any?,
    ): Pair<String?, String?>? =
        when (fieldId) {
            RMNCH.ID_IFA_TABLETS -> {
                evaluateIfaTabletsStatus()
            }

            RMNCH.ID_CALCIUM_TABLETS -> {
                evaluateCalciumTabletStatus()
            }

            RMNCH.ID_VITAMIN_A_CONSUMED -> {
                if (isSelectionPresent(value, DefinedParams.Yes)) {
                    null
                } else {
                    AssessmentDefinedParams.STATUS_GAP to null
                }
            }

            RMNCH.ID_FAMILY_PLANNING_METHODS -> {
                if (isSelectionPresent(value, DefinedParams.None)) {
                    AssessmentDefinedParams.STATUS_GAP to null
                } else {
                    null
                }
            }

            RMNCH.ID_TEMPERATURE -> {
                evaluateTemperatureStatus(value)
            }

            RMNCH.ID_URINARY_BILIRUBIN -> {
                evaluateUrinaryBilirubinStatus(value)
            }

            RMNCH.ID_PULSE -> {
                val pulse = CommonUtils.getInteger(value)
                if (pulse !in 60..90) {
                    AssessmentDefinedParams.STATUS_HIGH_RISK to null
                } else {
                    null
                }
            }

            RMNCH.ID_POSTPARTUM_DANGER_SIGNS -> {
                evaluatePostpartumDangerSigns(value)
            }

            RMNCH.ID_HEMOGLOBIN -> {
                CommonUtils.getDouble(value).takeIf { it > 0 }?.let {
                    evaluateHemoglobinStatus(it)
                }
            }

            RMNCH.ID_ECLAMPSIA,
            RMNCH.ID_KNOWN_HTN,
            RMNCH.ID_GDM_PATIENT,
            RMNCH.ID_DM_PATIENT,
            -> {
                if (isSelectionPresent(value, DefinedParams.Yes)) {
                    AssessmentDefinedParams.STATUS_HIGH_RISK to null
                } else {
                    null
                }
            }

            else -> null
        }

    /**
     * Evaluates Postpartum Danger Signs, if danger signs are there than high risk
     */
    private fun evaluatePostpartumDangerSigns(value: Any?): Pair<String, String?>? {
        val dangerSigns = value as? List<*>
        val selectedSigns = dangerSigns?.filterIsInstance<Map<String, Any>>()?.mapNotNull { it[DefinedParams.Value] as? String }?.toSet()
        return if (isSelectionPresent(selectedSigns.toString(), DefinedParams.None)) {
            null
        } else {
            AssessmentDefinedParams.STATUS_HIGH_RISK to null
        }
    }

    /**
     * Evaluates calcium tablets status, if consumption is less than (days since delviery + 1) then gap
     */
    private fun evaluateCalciumTabletStatus(): Pair<String, Nothing?>? {
        val daysSinceDelivery = getDaysSinceDelivery()
        return daysSinceDelivery?.let {
            val expectedTablets = daysSinceDelivery + 1
            val resultMap = formGenerator.getResultMap()
            val calciumConsumed = CommonUtils.getInteger(resultMap[RMNCH.ID_CALCIUM_TABLETS_CONSUMED])
            if (calciumConsumed < expectedTablets) {
                AssessmentDefinedParams.STATUS_GAP to null
            } else {
                null
            }
        } ?: run {
            null
        }
    }

    /**
     * Evaluates ifa tablets status, if consumption is less than (days since delviery + 1) then gap
     */
    private fun evaluateIfaTabletsStatus(): Pair<String, Nothing?>? {
        val daysSinceDelivery = getDaysSinceDelivery()
        return daysSinceDelivery?.let {
            val expectedTablets = daysSinceDelivery + 1
            val resultMap = formGenerator.getResultMap()
            val ifaConsumed = CommonUtils.getInteger(resultMap[RMNCH.ID_IFA_TABLETS_CONSUMED])
            if (ifaConsumed < expectedTablets) {
                AssessmentDefinedParams.STATUS_GAP to null
            } else {
                null
            }
        } ?: run {
            null
        }
    }

    /**
     * 1. Pregnant woman on treatment for any existing illness - if count of selected options < total available options
     */
    private fun evaluatePregnantWomanOnTreatmentStatus(
        value: Any?,
        resultMap: HashMap<String, Any>,
    ): Pair<String?, String?>? {
        // If user hasn't selected any treatment already, then ignore highlighting
        if (!ancSelectedTreatment) return null
        val selectedOptions = value as? ArrayList<HashMap<String, Any>> ?: ArrayList<HashMap<String, Any>>()
        val existingIllnessOptions = resultMap[AssessmentDefinedParams.PREGNANT_WOMAN_EXISTING_ILLNESS] as? ArrayList<HashMap<String, Any>> ?: return null

        // Filter out "none" option from existing illness options for count calculation
        val filteredExistingIllnessOptions = existingIllnessOptions.filter { illnessItem ->
            val id = illnessItem[DefinedParams.ID]?.toString()?.lowercase() ?: ""
            val name = illnessItem[DefinedParams.NAME]?.toString()?.lowercase() ?: ""
            id != AssessmentDefinedParams.ILLNESS_NONE_ID &&
                name != DefinedParams.None.lowercase()
        }

        // Get total available options from filtered existing illness (excluding "none")
        val totalAvailableOptions = filteredExistingIllnessOptions.size
        val selectedCount = selectedOptions.size

        return if (totalAvailableOptions > 0 && selectedCount < totalAvailableOptions) {
            Pair(AssessmentDefinedParams.STATUS_HIGH_RISK, null)
        } else {
            null
        }
    }

    /**
     * 2. Systolic - High Risk if >= 140 OR if existing HTN illness is present
     */
    private fun evaluateSystolicStatus(
        value: Any?,
        resultMap: HashMap<String, Any>,
    ): Pair<String?, String?>? {
        // Check if there's existing HTN illness first
        val hasHTN = RMNCHAssessmentEvaluator.hasExistingHTNIllness(resultMap)
        val systolic = (value as? String)?.toDouble() ?: return null
        if (hasHTN) {
            return Pair(AssessmentDefinedParams.STATUS_HIGH_RISK, null)
        }

        // If no HTN, check value threshold

        return if (systolic >= AssessmentDefinedParams.BP_SYSTOLIC_THRESHOLD) {
            Pair(AssessmentDefinedParams.STATUS_HIGH_RISK, null)
        } else {
            null
        }
    }

    /**
     * 2. Diastolic - High Risk if >= 90 OR if existing HTN illness is present
     */
    private fun evaluateDiastolicStatus(
        value: Any?,
        resultMap: HashMap<String, Any>,
    ): Pair<String?, String?>? {
        // Check if there's existing HTN illness first
        val diastolic = (value as? String)?.toDouble() ?: return null
        val hasHTN = RMNCHAssessmentEvaluator.hasExistingHTNIllness(resultMap)
        if (hasHTN) {
            return Pair(AssessmentDefinedParams.STATUS_HIGH_RISK, null)
        }

        // If no HTN, check value threshold

        return if (diastolic >= AssessmentDefinedParams.BP_DIASTOLIC_THRESHOLD) {
            Pair(AssessmentDefinedParams.STATUS_HIGH_RISK, null)
        } else {
            null
        }
    }

    /**
     * 3. Edema - (edema=present AND BP >= 140/90) OR (existing HTN illness with normal bp <140/90) OR (urine albumin = present)
     */
    private fun evaluateEdemaStatus(
        value: Any?,
        resultMap: HashMap<String, Any>,
    ): Pair<String?, String?>? {
        val edemaValue = value as? String
        if (edemaValue != AssessmentDefinedParams.VALUE_PRESENT) return null

        // Check BP from individual systolic and diastolic fields
        val systolic = (resultMap[AssessmentDefinedParams.SYSTOLIC] as? String)?.toDouble() ?: 0.0
        val diastolic = (resultMap[AssessmentDefinedParams.DIASTOLIC] as? String)?.toDouble() ?: 0.0
        val isHighBP = systolic >= AssessmentDefinedParams.BP_SYSTOLIC_THRESHOLD || diastolic >= AssessmentDefinedParams.BP_DIASTOLIC_THRESHOLD

        // Check existing HTN
        val hasHTN = RMNCHAssessmentEvaluator.hasExistingHTNIllness(resultMap)
        val isNormalBP = !isHighBP

        // Check urinary albumin
        val urinaryAlbumin = resultMap[AssessmentDefinedParams.URINARY_ALBUMIN] as? String

        val condition1 = edemaValue == AssessmentDefinedParams.VALUE_PRESENT && isHighBP
        val condition2 = hasHTN && isNormalBP
        val condition3 = urinaryAlbumin == AssessmentDefinedParams.VALUE_PRESENT

        return if (condition1 || condition2 || condition3) {
            Pair(AssessmentDefinedParams.STATUS_HIGH_RISK, null)
        } else {
            null
        }
    }

    /**
     * 4. Temperature - "High Risk - Fever" 100-101.9 F, "High Risk - High Fever" >= 102F
     */
    private fun evaluateTemperatureStatus(value: Any?): Pair<String?, String?>? {
        val temp = (value as? Number)?.toDouble() ?: return null

        return when {
            temp >= AssessmentDefinedParams.TEMP_HIGH_FEVER_THRESHOLD -> Pair(AssessmentDefinedParams.STATUS_HIGH_FEVER, null)
            temp >= AssessmentDefinedParams.TEMP_FEVER_MIN_THRESHOLD && temp <= AssessmentDefinedParams.TEMP_FEVER_MAX_THRESHOLD -> Pair(
                AssessmentDefinedParams.STATUS_FEVER,
                null,
            )

            else -> null
        }
    }

    /**
     * 5. Pulse - "abnormal" >90 or <60
     */
    private fun evaluatePulseStatus(value: Any?): Pair<String?, String?>? {
        val pulse = (value as? Number)?.toDouble() ?: return null

        return if (pulse > AssessmentDefinedParams.PULSE_HIGH_THRESHOLD || pulse < AssessmentDefinedParams.PULSE_LOW_THRESHOLD) {
            Pair(AssessmentDefinedParams.STATUS_ABNORMAL, null)
        } else {
            null
        }
    }

    /**
     * 6. Fundal Height - High risk if fundal height is not within gestational age ± 2 cm
     */
    private fun evaluateFundalHeightStatus(value: Any?): Pair<String?, String?>? {
        val fundalHeight = (value as? Number)?.toDouble() ?: return null
        val gestationalAgeWeeks = calculateGestationalAgeInWeeks() ?: return null

        // Expected fundal height = gestational age in weeks ± 2 cm
        val expectedMin = gestationalAgeWeeks - AssessmentDefinedParams.FUNDAL_HEIGHT_TOLERANCE_CM
        val expectedMax = gestationalAgeWeeks + AssessmentDefinedParams.FUNDAL_HEIGHT_TOLERANCE_CM

        return if (fundalHeight !in expectedMin..expectedMax) {
            Pair(AssessmentDefinedParams.STATUS_HIGH_RISK, null)
        } else {
            null
        }
    }

    /**
     * 7. Hemoglobin - "Moderate Anemia" if Hb<10, "Severe Anemia" if Hb <8
     */
    private fun evaluateHemoglobinStatus(value: Any?): Pair<String?, String?>? {
        val hb = (value as? Number)?.toDouble() ?: return null

        return when {
            hb < AssessmentDefinedParams.HEMOGLOBIN_SEVERE_ANEMIA_THRESHOLD -> Pair(AssessmentDefinedParams.STATUS_SEVERE_ANEMIA, null)
            hb < AssessmentDefinedParams.HEMOGLOBIN_MODERATE_ANEMIA_THRESHOLD -> Pair(AssessmentDefinedParams.STATUS_MODERATE_ANEMIA, null)
            hb < AssessmentDefinedParams.HEMOGLOBIN_MILD_ANEMIA_THRESHOLD -> Pair(AssessmentDefinedParams.STATUS_MILD_ANEMIA, null)
            else -> null
        }
    }

    /**
     * 8. Urinary Albumin - Positive AND (BP≥140/90 OR existing HTN patient even with normal values <140/90 OR edema=present)
     */
    private fun evaluateUrinaryAlbuminStatus(
        value: Any?,
        resultMap: HashMap<String, Any>,
    ): Pair<String?, String?>? {
        val urinaryAlbumin = value as? String
        if (urinaryAlbumin != AssessmentDefinedParams.VALUE_PRESENT) return null

        // Check BP from individual systolic and diastolic fields
        val systolic = (resultMap[AssessmentDefinedParams.SYSTOLIC] as? String)?.toDouble() ?: 0.0
        val diastolic = (resultMap[AssessmentDefinedParams.DIASTOLIC] as? String)?.toDouble() ?: 0.0
        val isHighBP = systolic >= AssessmentDefinedParams.BP_SYSTOLIC_THRESHOLD || diastolic >= AssessmentDefinedParams.BP_DIASTOLIC_THRESHOLD

        // Check existing HTN
        val hasHTN = RMNCHAssessmentEvaluator.hasExistingHTNIllness(resultMap)
        val isNormalBP = !isHighBP

        // Check edema
        val edema = resultMap[AssessmentDefinedParams.EDEMA] as? String

        val condition1 = isHighBP
        val condition2 = hasHTN && isNormalBP
        val condition3 = edema == AssessmentDefinedParams.VALUE_PRESENT

        return if (condition1 || condition2 || condition3) {
            Pair(AssessmentDefinedParams.STATUS_HIGH_RISK, null)
        } else {
            null
        }
    }

    /**
     * 9. Urinary Sugar - High risk if present
     */
    private fun evaluateUrinarySugarStatus(value: Any?): Pair<String?, String?>? {
        val urinarySugar = value as? String
        return if (urinarySugar == AssessmentDefinedParams.VALUE_PRESENT) {
            Pair(AssessmentDefinedParams.STATUS_HIGH_RISK, null)
        } else {
            null
        }
    }

    /**
     * 10. Urinary Bilirubin - High risk if present
     */
    private fun evaluateUrinaryBilirubinStatus(value: Any?): Pair<String?, String?>? {
        val urinaryBilirubin = value as? String
        return if (urinaryBilirubin == AssessmentDefinedParams.VALUE_PRESENT) {
            Pair(AssessmentDefinedParams.STATUS_HIGH_RISK, null)
        } else {
            null
        }
    }

    /**
     * 11. Folic Acid Tablets - "gap" if <30 tabs consumed
     */
    private fun evaluateFolicAcidStatus(resultMap: HashMap<String, Any>): Pair<String?, String?>? {
        val consumed = (resultMap[AssessmentDefinedParams.FOLIC_ACID_TOTAL_CONSUMED] as String?)?.toIntOrNull()

        return if (consumed != null && consumed < AssessmentDefinedParams.TABLET_CONSUMPTION_THRESHOLD) {
            Pair(AssessmentDefinedParams.STATUS_GAP, null)
        } else {
            null
        }
    }

    /**
     * 12. IFA Tablets - "gap" if <30 tabs consumed
     */
    private fun evaluateIFAStatus(resultMap: HashMap<String, Any>): Pair<String?, String?>? {
        val consumed = (resultMap[AssessmentDefinedParams.IFA_TOTAL_CONSUMED] as String?)?.toIntOrNull()

        return if (consumed != null && consumed < AssessmentDefinedParams.TABLET_CONSUMPTION_THRESHOLD) {
            Pair(AssessmentDefinedParams.STATUS_GAP, null)
        } else {
            null
        }
    }

    /**
     * 13. Calcium Tablets - "gap" if <30 tabs consumed
     */
    private fun evaluateCalciumStatus(resultMap: HashMap<String, Any>): Pair<String?, String?>? {
        val consumed = (resultMap[AssessmentDefinedParams.CALCIUM_TOTAL_CONSUMED] as String?)?.toIntOrNull()
        return if (consumed != null && consumed < AssessmentDefinedParams.TABLET_CONSUMPTION_THRESHOLD) {
            Pair(AssessmentDefinedParams.STATUS_GAP, null)
        } else {
            null
        }
    }

    /**
     * 14. ANC from Medical Doctor - "gap" if value is "no" AND gestational age > 36 weeks
     */
    private fun evaluateANCFromMedicalDoctorStatus(
        value: Any?,
        resultMap: HashMap<String, Any>,
    ): Pair<String?, String?>? {
        val ancFromDoctor = value as? String ?: return null
        val gestationalAgeWeeks = calculateGestationalAgeInWeeks()

        return if (ancFromDoctor.equals(AssessmentDefinedParams.VALUE_NO, ignoreCase = true) &&
            gestationalAgeWeeks != null &&
            gestationalAgeWeeks > AssessmentDefinedParams.GESTATIONAL_AGE_WEEK_36
        ) {
            Pair(AssessmentDefinedParams.STATUS_GAP, null)
        } else {
            null
        }
    }

    /**
     * 15. Ultrasound - "gap" if value is "notDone" AND gestational age > 36 weeks
     */
    private fun evaluateUltrasoundStatus(
        value: Any?,
        resultMap: HashMap<String, Any>,
    ): Pair<String?, String?>? {
        val ultrasound = value as? String ?: return null
        val gestationalAgeWeeks = calculateGestationalAgeInWeeks()

        return if (ultrasound.equals(AssessmentDefinedParams.VALUE_NOT_DONE, ignoreCase = true) &&
            gestationalAgeWeeks != null &&
            gestationalAgeWeeks > AssessmentDefinedParams.GESTATIONAL_AGE_WEEK_36
        ) {
            Pair(AssessmentDefinedParams.STATUS_GAP, null)
        } else {
            null
        }
    }

    /**
     * Blood Sugar Fasting - High Risk if >= 5.1 OR if existing DM illness is present
     */
    private fun evaluateBloodSugarFastingStatus(
        value: Any?,
        resultMap: HashMap<String, Any>,
    ): Pair<String?, String?>? {
        // Check if there's existing DM illness first
        val hasDM = RMNCHAssessmentEvaluator.hasExistingDMIllness(resultMap)
        val fastingValue = value as? Double
        if (fastingValue != null && hasDM) {
            return Pair(AssessmentDefinedParams.STATUS_HIGH_RISK, null)
        }
        // If no DM, check value threshold

        return if (fastingValue != null && fastingValue >= AssessmentDefinedParams.BLOOD_SUGAR_FASTING_THRESHOLD) {
            Pair(AssessmentDefinedParams.STATUS_HIGH_RISK, null)
        } else {
            null
        }
    }

    /**
     * Blood Sugar Random - High Risk if >= 8.5 OR if existing DM illness is present
     */
    private fun evaluateBloodSugarRandomStatus(
        value: Any?,
        resultMap: HashMap<String, Any>,
    ): Pair<String?, String?>? {
        // Check if there's existing DM illness first
        val hasDM = RMNCHAssessmentEvaluator.hasExistingDMIllness(resultMap)
        val randomValue = value as? Double
        if (randomValue != null && hasDM) {
            return Pair(AssessmentDefinedParams.STATUS_HIGH_RISK, null)
        }
        // If no DM, check value threshold

        return if (randomValue != null && randomValue >= AssessmentDefinedParams.BLOOD_SUGAR_RANDOM_THRESHOLD) {
            Pair(AssessmentDefinedParams.STATUS_HIGH_RISK, null)
        } else {
            null
        }
    }

    /**
     * Facility Identified For Delivery - High Risk if "Not identified yet" or "Planned for home delivery"
     */
    private fun evaluateFacilityIdentifiedForDeliveryStatus(value: Any?): Pair<String?, String?>? {
        val selectedId = value as? String
        return if (selectedId == AssessmentDefinedParams.FACILITY_NOT_IDENTIFIED ||
            selectedId == AssessmentDefinedParams.FACILITY_HOME_DELIVERY
        ) {
            Pair(AssessmentDefinedParams.STATUS_HIGH_RISK, null)
        } else {
            null
        }
    }

    /**
     * Evaluate : Auto-calculate difference in weight from previous ANC visit and highlight abnormal weight gain between ANC visits
     * i.e, less or more than expected weight gain of 1 kg per 15 days
     */
    private fun evaluateAncWeightStatus(value: Any?): Pair<String?, String?>? {
        val ancVisitNumber = getANCVisitNumber()
        return if (ancVisitNumber < 2) {
            // If visit is less than 2 then no need to calculate risk
            null
        } else {
            val weight = CommonUtils.getDouble(value).takeIf { it > 0 }
            val daysSinceLastVisit = viewModel.pregnancyDetailLiveData.value
                ?.ancVisitDate
                ?.let { DateUtils.parseDate(it) }
                ?.getLongTime()
                ?.let { DateUtils.getDaysDifference(it) }
            val previousWeight = viewModel.pregnancyDetailLiveData.value?.ancWeight
            if (weight == null || daysSinceLastVisit == null || previousWeight == null) {
                null
            } else {
                val weightDiff = abs(weight - previousWeight)
                val daysForCalculate = daysSinceLastVisit / 15
                if (weightDiff != daysForCalculate.toDouble()) {
                    AssessmentDefinedParams.STATUS_ABNORMAL to null
                } else {
                    null
                }
            }
        }
    }

    /**
     * Updates status for all fields that need evaluation
     */
    private fun updateAllFieldStatuses() {
        val resultMap = formGenerator.getResultMap()
        // Fields that always need evaluation (depend on other fields)
        val fieldsToAlwaysCheck = listOf(
            AssessmentDefinedParams.EDEMA,
            AssessmentDefinedParams.URINARY_ALBUMIN,
            AssessmentDefinedParams.FOLIC_ACID_TABLETS,
            AssessmentDefinedParams.IFA_TABLETS,
            AssessmentDefinedParams.CALCIUM_TABLETS,
            AssessmentDefinedParams.ANC_FROM_MEDICAL_DOCTOR,
            AssessmentDefinedParams.ULTRASOUND,
            AssessmentDefinedParams.SYSTOLIC, // Depends on HTN illness status
            AssessmentDefinedParams.DIASTOLIC, // Depends on HTN illness status
            AssessmentDefinedParams.BLOOD_SUGAR_FASTING, // Depends on DM illness status
            AssessmentDefinedParams.BLOOD_SUGAR_RANDOM, // Depends on DM illness status
            AssessmentDefinedParams.PREGNANT_WOMAN_ON_TREATMENT, // Depends on existing illness
        )

        // Fields that only need evaluation if they have a value
        val fieldsToCheckIfValueExists = listOf(
            AssessmentDefinedParams.TEMPERATURE,
            AssessmentDefinedParams.PULSE,
            AssessmentDefinedParams.FUNDAL_HEIGHT,
            AssessmentDefinedParams.HEMOGLOBIN,
            AssessmentDefinedParams.URINARY_SUGAR,
            AssessmentDefinedParams.URINARY_BILIRUBIN,
            AssessmentDefinedParams.FACILITY_IDENTIFIED_FOR_DELIVERY,
        )

        // Always evaluate fields that depend on other fields
        fieldsToAlwaysCheck.forEach { fieldId ->
            handleAncFieldStatusUpdate(fieldId)
        }

        // Evaluate fields only if they have values
        fieldsToCheckIfValueExists.forEach { fieldId ->
            val value = resultMap[fieldId]
            if (value != null) {
                handleAncFieldStatusUpdate(fieldId)
            }
        }
    }

    /**
     * Updates field title with status text in red color
     * If statusText is null, restores original title
     */
    private fun updateFieldTitleWithStatus(
        fieldId: String,
        textLabelFieldIds: List<String>,
        statusText: String?,
        statusTextCulture: String?,
    ) {
        val tag = if (fieldId in textLabelFieldIds) {
            // For TextLabel, the title TextView is tagged with just the fieldId
            fieldId
        } else {
            // For other fields, use fieldId + titleSuffix
            fieldId + formGenerator.titleSuffix
        }

        val titleView = formGenerator.getViewByTag(tag) as? TextView
        titleView?.let { tv ->
            // Get original title (store it first time if not stored)2
            val originalTitle = originalTitles[fieldId] ?: run {
                // Since status is always added last, this will only remove the status, preserving unit measurements
                val currentText = tv.text.toString()
                // Remove only the last status pattern at the end: " (Status Text)"
                // Since status is always added last, this will remove only the status, keeping the rest of the title intact
                val statusPattern = "\\s+\\([^)]+\\)\\s*$".toRegex()
                val original = currentText.replace(statusPattern, "")
                originalTitles[fieldId] = original
                original
            }

            // Build title with status
            if (statusText != null && statusText.isNotEmpty()) {
                val translate = SecuredPreference.getIsTranslationEnabled()
                val displayStatus = if (translate && !statusTextCulture.isNullOrBlank()) {
                    statusTextCulture
                } else {
                    statusText
                }

                tv.text = buildSpannedString {
                    append(originalTitle)
                    append(" ")
                    color(Color.RED) {
                        append("($displayStatus)")
                    }
                }
            } else {
                // No status, show original title
                tv.text = originalTitle
            }
        }
    }

    /**
     * Handles status evaluation and title update for ANC field
     */
    private fun handleAncFieldStatusUpdate(fieldId: String) {
        // TextLabel fields (folicAcidTablets, ifaTablets, calciumTablets) use just 'id' as tag, not 'id + titleSuffix'
        val textLabelFields = listOf(AssessmentDefinedParams.FOLIC_ACID_TABLETS, AssessmentDefinedParams.IFA_TABLETS, AssessmentDefinedParams.CALCIUM_TABLETS)
        handleFieldStatusUpdate(fieldId, textLabelFields, ::evaluateAncFieldStatus)
    }

    /**
     * Handles status evaluation and title update for PNC field
     */
    private fun handlePncFieldStatusUpdate(fieldId: String) {
        val textLabelFields = listOf(RMNCH.ID_IFA_TABLETS, RMNCH.ID_CALCIUM_TABLETS)
        handleFieldStatusUpdate(fieldId, textLabelFields, ::evaluatePncFieldStatus)
    }

    /**
     * Handles status evaluation and title update for PNC BP fields
     */
    private fun handlePncBpHighRiskHighlight() {
        val resultMap = formGenerator.getResultMap()
        val systolic = CommonUtils.getInteger(resultMap[RMNCH.ID_SYSTOLIC])
        val diastolic = CommonUtils.getInteger(resultMap[RMNCH.ID_DIASTOLIC])
        val isKnownHtn = isSelectionPresent(resultMap[RMNCH.ID_KNOWN_HTN], DefinedParams.Yes)
        val edema = resultMap[RMNCH.ID_EDEMA] as? String
        val urinaryAlbumin = resultMap[RMNCH.ID_URINARY_ALBUMIN] as? String

        val isHighBp = PNCAssessmentEvaluator.isHighBp(systolic, diastolic)
        val isEdemaPresent = isSelectionPresent(edema)
        val isAlbuminPositive = isSelectionPresent(urinaryAlbumin)

        // Common high risk triggers for BP (Point 1, 2, 3)
        val bpHighRiskTrigger = isHighBp || isKnownHtn || (isEdemaPresent && isAlbuminPositive)

        // Systolic
        if (systolic > 0 && bpHighRiskTrigger) {
            updateFieldTitleWithStatus(RMNCH.ID_SYSTOLIC, emptyList(), AssessmentDefinedParams.STATUS_HIGH_RISK, null)
        } else {
            updateFieldTitleWithStatus(RMNCH.ID_SYSTOLIC, emptyList(), null, null)
        }

        // Diastolic
        if (diastolic > 0 && bpHighRiskTrigger) {
            updateFieldTitleWithStatus(RMNCH.ID_DIASTOLIC, emptyList(), AssessmentDefinedParams.STATUS_HIGH_RISK, null)
        } else {
            updateFieldTitleWithStatus(RMNCH.ID_DIASTOLIC, emptyList(), null, null)
        }

        // Edema (Point 2)
        if (isEdemaPresent && (isHighBp || isKnownHtn || isAlbuminPositive)) {
            updateFieldTitleWithStatus(RMNCH.ID_EDEMA, emptyList(), AssessmentDefinedParams.STATUS_HIGH_RISK, null)
        } else {
            updateFieldTitleWithStatus(RMNCH.ID_EDEMA, emptyList(), null, null)
        }

        // Urinary Albumin (Point 3)
        if (isAlbuminPositive && (isHighBp || isKnownHtn || isEdemaPresent)) {
            updateFieldTitleWithStatus(RMNCH.ID_URINARY_ALBUMIN, emptyList(), AssessmentDefinedParams.STATUS_HIGH_RISK, null)
        } else {
            updateFieldTitleWithStatus(RMNCH.ID_URINARY_ALBUMIN, emptyList(), null, null)
        }
    }

    /**
     * Handles status evaluation and title update for PNC Blood Sugar fields
     */
    private fun handlePncBloodSugarHighRiskHighlight() {
        val resultMap = formGenerator.getResultMap()
        val fastingSugar = CommonUtils.getDouble(resultMap[RMNCH.ID_FASTING_BLOOD_SUGAR])
        val randomSugar = CommonUtils.getDouble(resultMap[RMNCH.ID_RANDOM_BLOOD_SUGAR])
        val isDmPatient = isSelectionPresent(resultMap[RMNCH.ID_DM_PATIENT], DefinedParams.Yes)
        val isGdmPatient = isSelectionPresent(resultMap[RMNCH.ID_GDM_PATIENT], DefinedParams.Yes)
        val isDmOrGdm = isDmPatient || isGdmPatient

        // Fasting (Point 1)
        if (fastingSugar > 0 && (fastingSugar >= 7.0 || isDmOrGdm)) {
            updateFieldTitleWithStatus(RMNCH.ID_FASTING_BLOOD_SUGAR, emptyList(), AssessmentDefinedParams.STATUS_HIGH_RISK, null)
        } else {
            updateFieldTitleWithStatus(RMNCH.ID_FASTING_BLOOD_SUGAR, emptyList(), null, null)
        }

        // Random (Point 2)
        if (randomSugar > 0 && (randomSugar >= 11.1 || isDmOrGdm)) {
            updateFieldTitleWithStatus(RMNCH.ID_RANDOM_BLOOD_SUGAR, emptyList(), AssessmentDefinedParams.STATUS_HIGH_RISK, null)
        } else {
            updateFieldTitleWithStatus(RMNCH.ID_RANDOM_BLOOD_SUGAR, emptyList(), null, null)
        }
    }

    /**
     * Handles status evaluation and title update for a field
     */
    private fun handleFieldStatusUpdate(
        fieldId: String,
        textLabelFieldIds: List<String>,
        evaluateStatus: (
            fieldId: String,
            value: Any?,
        ) -> Pair<String?, String?>?,
    ) {
        val resultMap = formGenerator.getResultMap()
        val value = resultMap[fieldId]

        val status = evaluateStatus(fieldId, value)
        if (status != null) {
            updateFieldTitleWithStatus(fieldId, textLabelFieldIds, status.first, status.second)
        } else {
            // No status condition matched, remove status from title
            updateFieldTitleWithStatus(fieldId, textLabelFieldIds, null, null)
        }
    }

    /**
     * Evaluate highRiskPregnantWoman and gapsInAnc and add to result map
     */
    private fun evaluateAndAddAncSummaryData(resultMap: HashMap<String, Any>) {
        val ancMap = resultMap[RMNCH.ANC] as? Map<*, *> ?: return

        // Convert ancMap to HashMap for processing
        val ancResultMap = ancMap
            .filterKeys { it is String }
            .mapKeys { it.key as String }
            .let { HashMap(it as Map<String, Any>) }

        // Get or create summary group in the ANC map
        val ancHashMap = resultMap[RMNCH.ANC] as? HashMap<String, Any>
            ?: return // If not mutable, return early

        val summaryGroup = (ancHashMap[AssessmentDefinedParams.GROUP_SUMMARY] as? HashMap<String, Any>)
            ?: HashMap<String, Any>().also {
                ancHashMap[AssessmentDefinedParams.GROUP_SUMMARY] = it
            }

        // Evaluate highRiskPregnantWoman
        val (dangerSignsList, hasOtherSelected) = getDangerSignsValues(ancHashMap, AssessmentDefinedParams.GROUP_DANGER_SIGNS_RISK_IDENTIFICATION)
        val emergencyConditions = evaluateEmergencyReferralConditions(ancResultMap)
        val nonEmergencyConditions = evaluateNonEmergencyReferralConditions(
            ancResultMap,
            viewModel.memberDetailsLiveData.value
                ?.data
                ?.dateOfBirth,
            hasOtherSelected,
        )

        // Combine all high risk conditions
        val combinedHighRiskList = mutableListOf<String>()
        dangerSignsList.forEach { dangerSign ->
            // Split by comma, trim, and filter out "None" (defensive check)
            val individualSigns = dangerSign
                .split(",")
                .map { it.trim() }
                .filter {
                    it.isNotEmpty() &&
                        !it.equals(DefinedParams.None, ignoreCase = true)
                }
            combinedHighRiskList.addAll(individualSigns)
        }
        combinedHighRiskList.addAll(emergencyConditions)
        combinedHighRiskList.addAll(nonEmergencyConditions)

        // Add to summary group
        summaryGroup[AssessmentDefinedParams.HIGH_RISK_PREGNANT_WOMAN] = combinedHighRiskList

        // Evaluate gapsInAnc
        val gapsList = evaluateGapsInANC(ancResultMap, resultMap)
        summaryGroup[AssessmentDefinedParams.GAPS_IN_ANC] = gapsList
    }

    /**
     * Helper method to get value from nested structure
     * Checks all ANC form groups first, then top level
     */
    private fun getValueFromNestedMap(
        resultMap: HashMap<String, Any>,
        key: String,
    ): Any? {
        // Check all ANC form groups for the field
        for (groupId in AssessmentDefinedParams.ANC_FORM_GROUPS) {
            val groupMap = resultMap[groupId] as? Map<*, *>
            groupMap?.get(key)?.let { return it }
        }
        // If not found in any group, check top level
        return resultMap[key]
    }

    /**
     * Evaluate Emergency Referral conditions
     * Returns list of condition texts that match
     */
    private fun evaluateEmergencyReferralConditions(resultMap: HashMap<String, Any>): List<String> {
        val conditions = mutableListOf<String>()

        // 1. Suspected Pre-eclampsia
        if (RMNCHAssessmentEvaluator.isSuspectedPreEclampsia(resultMap)) {
            conditions.add(AssessmentDefinedParams.CONDITION_SUSPECTED_PRE_ECLAMPSIA)
        }

        // 2. High Fever - >=102F
        val temperature = (getValueFromNestedMap(resultMap, AssessmentDefinedParams.TEMPERATURE) as? Number)?.toDouble()
        if (temperature != null && temperature >= AssessmentDefinedParams.TEMP_HIGH_FEVER_THRESHOLD) {
            conditions.add(AssessmentDefinedParams.CONDITION_HIGH_FEVER)
        }

        // 3. Abnormal fundal height
        val fundalHeightValue = getValueFromNestedMap(resultMap, AssessmentDefinedParams.FUNDAL_HEIGHT)
        if (evaluateFundalHeightStatus(fundalHeightValue)?.first == AssessmentDefinedParams.STATUS_HIGH_RISK) {
            conditions.add(AssessmentDefinedParams.CONDITION_ABNORMAL_FUNDAL_HEIGHT)
        }

        // 4. Abnormal weight gain
        val weightValue = getValueFromNestedMap(resultMap, AssessmentDefinedParams.WEIGHT)
        if (evaluateAncWeightStatus(weightValue)?.first == AssessmentDefinedParams.STATUS_ABNORMAL) {
            conditions.add(AssessmentDefinedParams.CONDITION_ABNORMAL_WEIGHT_GAIN)
        }

        // 5. Pulse - >90 or <60
        val pulse = (getValueFromNestedMap(resultMap, AssessmentDefinedParams.PULSE) as? Number)?.toDouble()
        if (pulse != null && (pulse > AssessmentDefinedParams.PULSE_HIGH_THRESHOLD || pulse < AssessmentDefinedParams.PULSE_LOW_THRESHOLD)) {
            conditions.add(AssessmentDefinedParams.CONDITION_ABNORMAL_PULSE)
        }

        // 6. Severe Anemia <8g/dl
        val hemoglobin = (getValueFromNestedMap(resultMap, AssessmentDefinedParams.HEMOGLOBIN) as? Number)?.toDouble()
        if (hemoglobin != null && hemoglobin < AssessmentDefinedParams.HEMOGLOBIN_SEVERE_ANEMIA_THRESHOLD) {
            conditions.add(AssessmentDefinedParams.CONDITION_SEVERE_ANEMIA)
        }

        // 7. Urinary Bilirubin present
        val urinaryBilirubin = getValueFromNestedMap(resultMap, AssessmentDefinedParams.URINARY_BILIRUBIN) as? String
        if (urinaryBilirubin == AssessmentDefinedParams.VALUE_PRESENT) {
            conditions.add(AssessmentDefinedParams.CONDITION_URINARY_BILIRUBIN)
        }

        // 8. PW with existing chronic illnesses and not on treatment
        if (RMNCHAssessmentEvaluator.hasChronicIllnessNotOnTreatment(resultMap)) {
            conditions.add(AssessmentDefinedParams.CONDITION_CHRONIC_ILLNESS_NOT_ON_TREATMENT)
        }

        return conditions
    }

    /**
     * Evaluate Non-Emergency Referral conditions
     * Returns list of condition texts that match
     * @param hasOtherSelected If true, adds "Any Other" to the conditions (when "other" option is selected in danger signs)
     */
    private fun evaluateNonEmergencyReferralConditions(
        resultMap: HashMap<String, Any>,
        dateOfBirth: String?,
        hasOtherSelected: Boolean = false,
    ): List<String> {
        val conditions = mutableListOf<String>()

        // 1. High risk pregnancy (short birth spacing <2 years/Age <18 years or >35 years/Multipara>3)
        if (RMNCHAssessmentEvaluator.isHighRiskPregnancy(dateOfBirth, viewModel.pregnancyDetailLiveData.value)) {
            conditions.add(AssessmentDefinedParams.CONDITION_HIGH_RISK_PREGNANCY)
        }

        // 2. Moderate Anemia (Hb-<10)
        val hemoglobin = (getValueFromNestedMap(resultMap, AssessmentDefinedParams.HEMOGLOBIN) as? Number)?.toDouble()
        if (hemoglobin != null &&
            hemoglobin < AssessmentDefinedParams.HEMOGLOBIN_MODERATE_ANEMIA_THRESHOLD &&
            hemoglobin >= AssessmentDefinedParams.HEMOGLOBIN_SEVERE_ANEMIA_THRESHOLD
        ) {
            conditions.add(AssessmentDefinedParams.CONDITION_MODERATE_ANEMIA)
        }

        // 3. Mild Anemia (Hb < 11)
        if (hemoglobin != null &&
            hemoglobin >= AssessmentDefinedParams.HEMOGLOBIN_MODERATE_ANEMIA_THRESHOLD &&
            hemoglobin < AssessmentDefinedParams.HEMOGLOBIN_MILD_ANEMIA_THRESHOLD
        ) {
            conditions.add(AssessmentDefinedParams.CONDITION_MILD_ANEMIA)
        }

        // 4. Suspected/Existing Case of Diabetes
        if (RMNCHAssessmentEvaluator.isSuspectedDiabetes(resultMap)) {
            conditions.add(AssessmentDefinedParams.CONDITION_SUSPECTED_DIABETES)
        }

        // 5. PW with existing chronic illnesses with treatment
        if (RMNCHAssessmentEvaluator.hasChronicIllnessWithTreatment(resultMap)) {
            conditions.add(AssessmentDefinedParams.CONDITION_CHRONIC_ILLNESS_WITH_TREATMENT)
        }

        // 6. Mild Fever - 100-101.9
        val temperature = (getValueFromNestedMap(resultMap, AssessmentDefinedParams.TEMPERATURE) as? Number)?.toDouble()
        if (temperature != null &&
            temperature >= AssessmentDefinedParams.TEMP_FEVER_MIN_THRESHOLD &&
            temperature <= AssessmentDefinedParams.TEMP_FEVER_MAX_THRESHOLD
        ) {
            conditions.add(AssessmentDefinedParams.CONDITION_MILD_FEVER)
        }

        // 7. H/O Preg related medical complications (H/O Convulsions/ H/O Postpartum hemorrhage/H/O Severe Anemia /H/O GDM)
        if (RMNCHAssessmentEvaluator.hasPregnancyRelatedMedicalComplications(resultMap)) {
            conditions.add(AssessmentDefinedParams.CONDITION_PREGNANCY_RELATED_MEDICAL_COMPLICATIONS)
        }

        // 8. Any Other - if "other" option is selected in danger signs
        if (hasOtherSelected) {
            conditions.add(AssessmentDefinedParams.CONDITION_ANY_OTHER)
        }

        return conditions
    }

    /**
     * Filters out "None" from a comma-separated danger signs string
     * @param dangerSignString Comma-separated string of danger signs
     * @return Filtered string with "None" removed, or empty string if only "None" was present
     */
    private fun filterNoneFromDangerSignsString(dangerSignString: String): String {
        val signs = dangerSignString
            .split(",")
            .map { it.trim() }
            .filter {
                it.isNotEmpty() &&
                    !it.equals(DefinedParams.None, ignoreCase = true)
            }
        return signs.joinToString(", ")
    }

    /**
     * Get danger signs values from all three danger signs fields
     * Filters out "None" option from the results
     * @return Pair of (filtered danger signs list, boolean indicating if "other" option is selected)
     */
    private fun getDangerSignsValues(
        map: HashMap<String, Any>,
        section: String?,
    ): Pair<List<String>, Boolean> {
        val dangerSignsList = mutableListOf<String>()
        var hasOtherSelected = false
        val ancMap = map[section] as? Map<*, *>
        ancMap?.let { anc ->
            // Check all three danger signs fields
            listOf(
                AssessmentDefinedParams.DANGER_SIGNS_EXPERIENCED_12,
                AssessmentDefinedParams.DANGER_SIGNS_EXPERIENCED_13_27,
                AssessmentDefinedParams.DANGER_SIGNS_EXPERIENCED_28_40,
            ).forEach { dangerSignId ->
                val dangerSignValue = anc[dangerSignId]
                if (dangerSignValue != null) {
                    // Check raw value for "other" option before formatting
                    if (dangerSignValue is ArrayList<*>) {
                        dangerSignValue.forEach { item ->
                            if (item is Map<*, *>) {
                                val id = item[DefinedParams.ID]?.toString()?.lowercase() ?: ""
                                val name = item[DefinedParams.NAME]?.toString()?.lowercase() ?: ""
                                // Check if "other" option is selected
                                if (id == "other" || name == DefinedParams.Other.lowercase()) {
                                    hasOtherSelected = true
                                }
                            }
                        }
                    }

                    val formattedValue = RMNCH.getValueFromMap(
                        map,
                        dangerSignId,
                        ViewType.VIEW_TYPE_DIALOG_CHECKBOX,
                        section,
                        false,
                        Triple(
                            getString(R.string.yes),
                            getString(R.string.no),
                            getString(R.string.hyphen_symbol),
                        ),
                        requireContext(),
                    )
                    if (formattedValue != getString(R.string.hyphen_symbol)) {
                        // Filter out "None" from the formatted value
                        val filteredValue = filterNoneFromDangerSignsString(formattedValue)
                        // Only add if there are signs remaining after filtering "None"
                        if (filteredValue.isNotEmpty()) {
                            dangerSignsList.add(filteredValue)
                        }
                    }
                }
            }
        }
        return Pair(dangerSignsList, hasOtherSelected)
    }

    /**
     * Evaluate all gap conditions in ANC
     * Returns list of gap condition texts that match
     */
    private fun evaluateGapsInANC(
        resultMap: HashMap<String, Any>,
        fullMap: HashMap<String, Any>,
    ): List<String> {
        val gaps = mutableListOf<String>()

        // 1. TT vaccination incomplete
        val ttTdCompleted = getValueFromNestedMap(resultMap, AssessmentDefinedParams.TT_TD_COMPLETED) as? String
        if (ttTdCompleted.isNullOrBlank() || ttTdCompleted.equals(AssessmentDefinedParams.NO, ignoreCase = true)) {
            gaps.add(AssessmentDefinedParams.GAP_TT_VACCINATION_INCOMPLETE)
        }

        // Calculate gestational age for conditions that need it
        val gestationalAgeWeeks = calculateGestationalAgeInWeeks(fullMap)

        // 2. USG not done >36 weeks
        val ultrasound = getValueFromNestedMap(resultMap, AssessmentDefinedParams.ULTRASOUND) as? String
        if (ultrasound != null &&
            ultrasound.equals(AssessmentDefinedParams.VALUE_NOT_DONE, ignoreCase = true) &&
            gestationalAgeWeeks != null &&
            gestationalAgeWeeks > AssessmentDefinedParams.GESTATIONAL_AGE_WEEK_36
        ) {
            gaps.add(AssessmentDefinedParams.GAP_USG_NOT_DONE)
        }

        // 3. ANC with Doctor not done >36 weeks
        val ancFromDoctor = getValueFromNestedMap(resultMap, AssessmentDefinedParams.ANC_FROM_MEDICAL_DOCTOR) as? String
        if (ancFromDoctor != null &&
            ancFromDoctor.equals(AssessmentDefinedParams.VALUE_NO, ignoreCase = true) &&
            gestationalAgeWeeks != null &&
            gestationalAgeWeeks > AssessmentDefinedParams.GESTATIONAL_AGE_WEEK_36
        ) {
            gaps.add(AssessmentDefinedParams.GAP_ANC_WITH_DOCTOR_NOT_DONE)
        }

        // 4. Less than 3 ANCs completed at end of 36 weeks
        val ancVisitNo = viewModel.pregnancyDetailLiveData.value?.ancVisitNo ?: 0L
        if (ancVisitNo < AssessmentDefinedParams.MIN_ANC_VISITS_REQUIRED &&
            gestationalAgeWeeks != null &&
            gestationalAgeWeeks >= AssessmentDefinedParams.GESTATIONAL_AGE_WEEK_36
        ) {
            gaps.add(AssessmentDefinedParams.GAP_LESS_THAN_3_ANCS)
        }

        // 5. Inadequate /Non consumption IFA
        val ifaConsumed = (getValueFromNestedMap(resultMap, AssessmentDefinedParams.IFA_TOTAL_CONSUMED) as? String)?.toIntOrNull()
        if (ifaConsumed == null || ifaConsumed < AssessmentDefinedParams.TABLET_CONSUMPTION_THRESHOLD) {
            gaps.add(AssessmentDefinedParams.GAP_INADEQUATE_IFA)
        }

        // 6. Inadequate /Non consumption Calcium
        val calciumConsumed = (getValueFromNestedMap(resultMap, AssessmentDefinedParams.CALCIUM_TOTAL_CONSUMED) as? String)?.toIntOrNull()
        if (calciumConsumed == null || calciumConsumed < AssessmentDefinedParams.TABLET_CONSUMPTION_THRESHOLD) {
            gaps.add(AssessmentDefinedParams.GAP_INADEQUATE_CALCIUM)
        }

        // 7. Facility not identified for institutional delivery
        val facilityIdentified = getValueFromNestedMap(resultMap, AssessmentDefinedParams.FACILITY_IDENTIFIED_FOR_DELIVERY) as? String
        if (facilityIdentified == AssessmentDefinedParams.FACILITY_NOT_IDENTIFIED) {
            gaps.add(AssessmentDefinedParams.GAP_FACILITY_NOT_IDENTIFIED)
        }

        // 8. Planned for Home Delivery
        if (facilityIdentified == AssessmentDefinedParams.FACILITY_HOME_DELIVERY) {
            gaps.add(AssessmentDefinedParams.GAP_PLANNED_HOME_DELIVERY)
        }

        return gaps
    }

    /**
     * Gets the current ANC visit number.
     * Visit number is calculated as visitCount + 1 (visitCount is 0-based)
     */
    private fun getANCVisitNumber(): Int {
        val visitCount = viewModel.pregnancyDetailLiveData.value?.ancVisitNo ?: 0L
        return (visitCount + 1).toInt()
    }

    /**
     * Returns pnc visit count from the DB plus 1 to count current visit
     */
    private fun getPncVisitNumber(): Int = (viewModel.pregnancyDetailLiveData.value?.pncVisitNo ?: 0L).toInt() + 1

    /**
     * Returns child visit number from the DB + 1 to count current visit
     */
    private fun getChildVisitNumber(): Int = (viewModel.pregnancyDetailLiveData.value?.childVisitNo ?: 0L).toInt() + 1

    /**
     * Returns anc visit count
     */
    private fun getAncVisitNumberForPnc(): Int = (viewModel.pregnancyDetailLiveData.value?.ancVisitNo ?: 0L).toInt()

    /**
     * Calculates days since delivery from the delivery date
     */
    private fun getDaysSinceDelivery(): Int? =
        viewModel.pregnancyDetailLiveData.value?.dateOfDelivery?.let { clinicalDate ->
            DateUtils.parseDate(clinicalDate)?.let { date ->
                DateUtils.getDaysDifference(date.getLongTime())
            }
        }
}
