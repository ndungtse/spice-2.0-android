package com.medtroniclabs.spice.ui.assessment.fragment

import android.content.res.Configuration
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import com.medtroniclabs.spice.R
import com.medtroniclabs.spice.app.analytics.model.UserDetail
import com.medtroniclabs.spice.app.analytics.utils.AnalyticsDefinedParams
import com.medtroniclabs.spice.appextensions.gone
import com.medtroniclabs.spice.common.CommonUtils.extractNumber
import com.medtroniclabs.spice.common.CommonUtils.isMandateOrNot
import com.medtroniclabs.spice.common.DateUtils
import com.medtroniclabs.spice.common.DateUtils.formatGestationalAge
import com.medtroniclabs.spice.common.DefinedParams
import com.medtroniclabs.spice.common.EntityMapper
import com.medtroniclabs.spice.common.SecuredPreference
import com.medtroniclabs.spice.data.model.RecommendedDosageListModel
import com.medtroniclabs.spice.databinding.FragmentAssessmentRmnchBinding
import com.medtroniclabs.spice.formgeneration.FormGenerator
import com.medtroniclabs.spice.formgeneration.config.DefinedParams.VISIBLE
import com.medtroniclabs.spice.formgeneration.extension.safeClickListener
import com.medtroniclabs.spice.formgeneration.listener.FormEventListener
import com.medtroniclabs.spice.formgeneration.model.FormLayout
import com.medtroniclabs.spice.formgeneration.ui.FormResultComposer
import com.medtroniclabs.spice.formgeneration.utility.CheckBoxDialog
import com.medtroniclabs.spice.formgeneration.utility.InformationLayoutFragment
import com.medtroniclabs.spice.network.resource.ResourceState
import com.medtroniclabs.spice.ui.BaseFragment
import com.medtroniclabs.spice.ui.assessment.AssessmentActivity
import com.medtroniclabs.spice.ui.assessment.AssessmentCommonUtils.getMuacColorCode
import com.medtroniclabs.spice.ui.assessment.AssessmentCommonUtils.getNutritionStatus
import com.medtroniclabs.spice.ui.assessment.AssessmentDefinedParams
import com.medtroniclabs.spice.ui.assessment.AssessmentDefinedParams.ExclusivelyBreastfeeding
import com.medtroniclabs.spice.ui.assessment.AssessmentDefinedParams.FedFrom4FoodGroups
import com.medtroniclabs.spice.ui.assessment.AssessmentDefinedParams.MUAC
import com.medtroniclabs.spice.ui.assessment.AssessmentDefinedParams.TakingMinimumMealsPerDay
import com.medtroniclabs.spice.ui.assessment.AssessmentDefinedParams.muacStatus
import com.medtroniclabs.spice.ui.assessment.AssessmentDefinedParams.rootSuffix
import com.medtroniclabs.spice.ui.assessment.AssessmentDefinedParams.summaryKey
import com.medtroniclabs.spice.ui.assessment.referrallogic.ReferralResultGenerator
import com.medtroniclabs.spice.ui.assessment.rmnch.RMNCH
import com.medtroniclabs.spice.ui.assessment.rmnch.RMNCH.DeathOfMother
import com.medtroniclabs.spice.ui.assessment.rmnch.RMNCH.Miscarriage
import com.medtroniclabs.spice.ui.assessment.rmnch.RMNCH.PlaceOfDelivery
import com.medtroniclabs.spice.ui.assessment.rmnch.RMNCH.deathOfBaby
import com.medtroniclabs.spice.ui.assessment.viewmodel.AssessmentViewModel
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@AndroidEntryPoint
class AssessmentRMNCHFragment : BaseFragment(), View.OnClickListener,
    FormEventListener {

    private lateinit var binding: FragmentAssessmentRmnchBinding
    private val viewModel: AssessmentViewModel by activityViewModels()
    private lateinit var formGenerator: FormGenerator

    companion object {
        const val TAG = "AssessmentRMNCHFragment"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAssessmentRmnchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        viewModel.getNearestHealthFacility()
        attachObservers()
        setListener()
        UserDetail.startDateTime =
            com.medtroniclabs.spice.app.analytics.utils.CommonUtils.getCurrentDateTimeInLocalTime()
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
                        formGenerator.populateViews(data.formLayout)
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
        viewModel.facilitySpinnerLiveData.observe(viewLifecycleOwner) { resourceState ->
            when (resourceState.state) {
                ResourceState.SUCCESS -> {
                    hideProgress()
                    resourceState.data?.let { data ->
                        val facilityList = EntityMapper.getResultSpinnerMapList(data)
                        if (viewModel.workflowName == RMNCH.ANC) {
                            facilityList.add(
                                mapOf(
                                    DefinedParams.name to DefinedParams.Others,
                                    DefinedParams.ID to DefinedParams.Others
                                )
                            )
                        }
                        formGenerator.spinnerDataInjection(
                            data,
                            facilityList
                        )
                    }
                }

                ResourceState.LOADING -> {
                    showProgress()
                }

                ResourceState.ERROR -> {
                    hideProgress()
                }
            }
        }

        viewModel.memberClinicalLiveData.observe(viewLifecycleOwner) { data ->
            data?.clinicalDate?.let { date ->
                if (date.isNotEmpty()) {
                    formGenerator.getViewByTag(RMNCH.lastMenstrualPeriod + formGenerator.rootSuffix)
                        ?.gone()
                    formGenerator.getViewByTag(RMNCH.DateOfDelivery + formGenerator.rootSuffix)
                        ?.gone()
                    formGenerator.getViewByTag(RMNCH.NoOfNeonate + formGenerator.rootSuffix)?.gone()
                }
            }
        }
        viewModel.ageInMonth.observe(viewLifecycleOwner) {
            updateAgeInMonths(it)
        }
    }


    private fun initView() {
        replaceFragmentInId<BioDataFragment>(
            binding.bioDataFragmentContainer.id,
            tag = BioDataFragment.TAG
        )
        formGenerator = FormGenerator(
            requireContext(), binding.llForm, null, this, binding.scrollView,
            translate = SecuredPreference.getIsTranslationEnabled()
        )
        showRespectiveWorkflow()
    }

    private fun showRespectiveWorkflow() {
        when (viewModel.workflowName) {
            RMNCH.ANC -> {
                binding.btnSubmit.text = getString(R.string.submit)
            }

            RMNCH.ChildHoodVisit -> {
                binding.btnSubmit.text = getString(R.string.submit)
            }

            RMNCH.PNC -> {
                binding.btnSubmit.text = getString(R.string.next)

            }
        }
        viewModel.workflowName?.let { name ->
            viewModel.getFormData(name)
        }
    }

    override fun onClick(v: View) {
        when (v.id) {
            binding.btnSubmit.id -> {
                formGenerator.formSubmitAction(v)
            }
        }
    }

    override fun loadLocalCache(id: String, localDataCache: Any, selectedParent: Long?) {
        when (id) {
            PlaceOfDelivery -> {
                if (localDataCache is String) {
                    viewModel.loadDataCacheByType(id, localDataCache)
                }
            }
        }
    }

    override fun onPopulate(targetId: String) {
    }

    override fun onCheckBoxDialogueClicked(
        id: String,
        serverViewModel: FormLayout,
        resultMap: Any?
    ) {
        CheckBoxDialog.newInstance(id, resultMap) { map ->
            formGenerator.validateCheckboxDialogue(id, serverViewModel, map)
        }.show(childFragmentManager, CheckBoxDialog.TAG)
    }

    override fun onInstructionClicked(
        id: String,
        title: String,
        informationList: ArrayList<String>?,
        description: String?,
        dosageListModel: ArrayList<RecommendedDosageListModel>?
    ) {
        viewModel.instructionId = id
        showInstructionDialog(id)
    }

    override fun onFormSubmit(resultMap: HashMap<String, Any>?, serverData: List<FormLayout?>?) {
        resultMap?.let { details ->
            val result = serverData?.let {
                FormResultComposer().groupValues(
                    context = requireContext(),
                    serverData = it,
                    details
                )
            }
            result?.second?.let { second ->
                handleNextPregnancyFlow(second)
            }
        }
        viewModel.setAnalyticsData(
            UserDetail.startDateTime,
            eventType = viewModel.workflowName.plus(AnalyticsDefinedParams.RMNCHAssessment),
            eventName = AnalyticsDefinedParams.AssessmentCreation
        )
    }

    private fun handleNextPregnancyFlow(second: HashMap<String, Any>) {
        viewModel.workflowName?.let { name ->
            when (name) {
                RMNCH.PNC -> {
                    viewModel.pncMotherDetailMap = second
                    (requireActivity() as AssessmentActivity).replaceAssessmentRMNCHNeonateFragment()
                }

                else -> {
                    if (!checkForOtherMetrics(second, name)) {
                        viewModel.memberDetailsLiveData.value?.data?.let { memberDetail ->
                            viewModel.handlePregnancy(
                                second,
                                workflowName = name,
                                memberDetail,
                                viewModel.memberClinicalLiveData.value
                            )
                        }
                    } else {
                        val visitCount = viewModel.memberClinicalLiveData.value?.visitCount ?: 0
                        if (second.containsKey(name) && second[name] is Map<*, *>) {
                            val clinicalMap = second[name] as HashMap<String, Any>
                            clinicalMap[RMNCH.visitNo] = visitCount + 1
                        }
                    }
                    calculateGestationalAge(second, name)
                    val resultGenerator = ReferralResultGenerator()
                    val referralResult = resultGenerator.calculateRMNCHReferralResult(second, false)
                    viewModel.saveAssessment(
                        second,
                        referralResult,
                        RMNCH.getMenuName(viewModel.workflowName)
                    )
                }
            }
        }
    }

    private fun checkForOtherMetrics(details: HashMap<String, Any>, name: String): Boolean {
        var status = false
        var miscarriageReset = false
        var deathOfMotherReset = false

        if (details.containsKey(name) && details[name] is Map<*, *>) {
            val second = details[name] as HashMap<String, Any>
            if (second.containsKey(Miscarriage)) {
                val miscarriage = second[Miscarriage]
                if (miscarriage is Boolean && miscarriage) {
                    status = true
                    miscarriageReset = true
                }
            }

            if (second.containsKey(DeathOfMother)) {
                val deathOfMother = second[DeathOfMother]
                if (deathOfMother is Boolean && deathOfMother) {
                    viewModel.memberDetailsLiveData.value?.data?.let {
                        viewModel.updateMemberDeceasedStatus(it.id, false)
                        deathOfMotherReset = true
                        status = true
                    }
                }
            }

            if (second.containsKey(deathOfBaby)) {
                val deathOfBaby = second[deathOfBaby]
                if (deathOfBaby is Boolean && deathOfBaby) {
                    viewModel.memberDetailsLiveData.value?.data?.let {
                        viewModel.updateMemberDeceasedStatus(it.id, false)
                        status = true
                    }
                }
            }

            if (miscarriageReset && !deathOfMotherReset) {
                viewModel.memberDetailsLiveData.value?.data?.let {
                    viewModel.updateMemberClinicalData(it.id, 0L, null)
                }
            }
        }
        return status
    }


    private fun calculateGestationalAge(details: HashMap<String, Any>, name: String) {
        if (details.containsKey(name) && details[name] is Map<*, *>) {
            val second = details[name] as HashMap<String, Any>
            if (second.containsKey(RMNCH.lastMenstrualPeriod)) {
                val lastMenstrualDate = second[RMNCH.lastMenstrualPeriod]
                if (lastMenstrualDate is String) {
                    val calendar = getLastMenstrualDate(lastMenstrualDate)
                    second[RMNCH.gestationalAge] = extractNumber(
                        formatGestationalAge(
                            DateUtils.calculateGestationalAge(
                                calendar
                            ).first, requireContext()
                        )
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
            DateUtils.DATE_ddMMyyyy
        )
        return Calendar.getInstance().apply {
            time = getDateFormat().parse(lastMenstrualDateString)
        }
    }

    private fun getDateFormat(): SimpleDateFormat {
        return SimpleDateFormat(
            DateUtils.DATE_ddMMyyyy,
            Locale.getDefault()
        )
    }

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
                InformationLayoutFragment.newInstance(id, titleById)
                    .show(childFragmentManager, InformationLayoutFragment.TAG)
            }
        }
    }

    private fun getTitleById(id: String): String {
        return when (id) {
            MUAC -> getString(R.string.measuring_muac)
            else -> {
                id
            }
        }
    }


    override fun onRenderingComplete() {
        viewModel.memberClinicalLiveData.value?.clinicalDate?.let {
            formGenerator.getViewByTag(RMNCH.lastMenstrualPeriod + formGenerator.rootSuffix)?.gone()
        }
    }

    override fun onUpdateInstruction(id: String, selectedId: Any?) {
        Timber.d("onUpdateInstruction $id $selectedId")
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
                            selectedId
                        )

                    (formGenerator.getViewByTag(muacStatusTag) as? TextView)?.text =
                        getNutritionStatus(selectedId, requireContext())
                    formGenerator.getViewByTag(MUAC)?.apply {
                        val background = background as? GradientDrawable
                        background?.setStroke(
                            resources.getDimensionPixelSize(R.dimen._4sdp),
                            getMuacColorCode(selectedId as String, requireContext())
                        )
                    }
                } else {
                    formGenerator.getViewByTag(rootSuffixTag)?.visibility = View.GONE
                    formGenerator.getViewByTag(MUAC)?.apply {
                        val background = background as? GradientDrawable
                        background?.setStroke(
                            resources.getDimensionPixelSize(R.dimen._1sdp),
                            getMuacColorCode(selectedId as String, requireContext())
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
        resultMap: HashMap<String, Any>?
    ) {
    }


    override fun onAgeCheckForPregnancy() {
    }

    override fun handleMandatoryCondition(serverData: FormLayout?) {
        if (serverData?.id == MUAC) {
            viewModel.selectedMemberDob?.let { dateOfBirth ->
                val visibility = isMandateOrNot(dateOfBirth)
                serverData.visibility = visibility
                serverData.isMandatory = (visibility == VISIBLE)
                serverData.isSummary = (visibility == VISIBLE)
            }
        }

        if (serverData?.id == muacStatus) {
            viewModel.selectedMemberDob?.let { dateOfBirth ->
                serverData.visibility = isMandateOrNot(dateOfBirth)
            }
        }
    }

    override fun onAgeUpdateListener(
        age: Int,
        serverData: List<FormLayout?>?,
        resultHashMap: HashMap<String, Any>
    ) {
        /*
       Never used
        */
    }

    fun getCurrentAnsweredStatus(): Boolean {
        return formGenerator.getResultMap().isNotEmpty()
    }

    private fun updateAgeInMonths(age: String) {
        if (age.contains(getString(R.string.weeks))||age.lowercase().contains(getString(R.string.week))){
            hideUnder5Months()
        }else {
            when (age.replace(getString(R.string.months), "").replace(getString(R.string.month), "").trim().toInt()) {
                in 0..5 -> {
                    hideUnder5Months()
                }
                in 6..15 -> {

                    formGenerator.getViewByTag(ExclusivelyBreastfeeding + rootSuffix)?.apply {
                        visibility = View.GONE
                    }
                }

                else -> {
                }
            }
        }
    }
    private fun hideUnder5Months(){
        formGenerator.getViewByTag(TakingMinimumMealsPerDay + rootSuffix)?.apply {
            visibility = View.GONE
        }

        formGenerator.getViewByTag(FedFrom4FoodGroups + rootSuffix)?.apply {
            visibility = View.GONE
        }
    }

}
