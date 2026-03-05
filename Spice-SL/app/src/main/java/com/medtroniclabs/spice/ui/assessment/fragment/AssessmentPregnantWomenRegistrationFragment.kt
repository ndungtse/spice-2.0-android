package com.medtroniclabs.spice.ui.assessment.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.view.children
import androidx.fragment.app.activityViewModels
import com.medtroniclabs.spice.app.analytics.model.UserDetail
import com.medtroniclabs.spice.app.analytics.utils.AnalyticsDefinedParams
import com.medtroniclabs.spice.app.analytics.utils.AnalyticsUtils
import com.medtroniclabs.spice.appextensions.gone
import com.medtroniclabs.spice.appextensions.visible
import com.medtroniclabs.spice.common.DateUtils
import com.medtroniclabs.spice.common.DateUtils.formatGestationalAge
import com.medtroniclabs.spice.common.SecuredPreference
import com.medtroniclabs.spice.data.model.RecommendedDosageListModel
import com.medtroniclabs.spice.databinding.FragmentAssessmentBinding
import com.medtroniclabs.spice.db.entity.SignsAndSymptomsEntity
import com.medtroniclabs.spice.formgeneration.FormGenerator
import com.medtroniclabs.spice.formgeneration.config.DefinedParams
import com.medtroniclabs.spice.formgeneration.extension.safeClickListener
import com.medtroniclabs.spice.formgeneration.extension.textSizeSsp
import com.medtroniclabs.spice.formgeneration.listener.FormEventListener
import com.medtroniclabs.spice.formgeneration.model.FormLayout
import com.medtroniclabs.spice.formgeneration.ui.FormResultComposer
import com.medtroniclabs.spice.formgeneration.utility.CheckBoxDialog
import com.medtroniclabs.spice.mappingkey.PregnantWomen
import com.medtroniclabs.spice.network.resource.ResourceState
import com.medtroniclabs.spice.ui.BaseFragment
import com.medtroniclabs.spice.ui.MenuConstants
import com.medtroniclabs.spice.ui.assessment.AssessmentDefinedParams
import com.medtroniclabs.spice.ui.assessment.viewmodel.AssessmentViewModel
import java.util.Calendar
import java.util.Locale

/**
 * Fragment responsible for registering pregnant women details
 *
 * Ticket : https://mdtlabs.atlassian.net/browse/UHIS-118
 * UI : https://claude.ai/public/artifacts/b504e171-ab92-4553-8cb4-2ba36bb3a9ed
 */
class AssessmentPregnantWomenRegistrationFragment :
    BaseFragment(),
    FormEventListener,
    View.OnClickListener {
    private lateinit var binding: FragmentAssessmentBinding

    private val viewModel: AssessmentViewModel by activityViewModels()

    private lateinit var formGenerator: FormGenerator

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentAssessmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        attachObservers()
        setListener()
        UserDetail.startDateTime = AnalyticsUtils.getCurrentDateTimeInLocalTime()
        viewModel.setUserJourney(AnalyticsDefinedParams.PREGNANT_WOMEN_PROFILE)
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
    }

    private fun setListener() {
        binding.btnSubmit.safeClickListener(this)
    }

    fun initView() {
        replaceFragmentInId<BioDataFragment>(
            binding.bioDataFragmentContainer.id,
            tag = BioDataFragment.TAG,
        )
        childFragmentManager.executePendingTransactions() // Ensures transaction is complete
        formGenerator = FormGenerator(
            requireContext(),
            binding.llForm,
            this,
            binding.scrollView,
            translate = SecuredPreference.getIsTranslationEnabled(),
            callback = { resultHashMap, id ->
                when (id.lowercase(Locale.ENGLISH)) {
                    PregnantWomen.ID_LMP -> {
                        val lastMenstrualDateString =
                            resultHashMap[id] as? String ?: return@FormGenerator
                        val lastMenstrualDate =
                            DateUtils.getLastMenstrualDate(lastMenstrualDateString)
                        val daysDifference =
                            DateUtils.getDaysDifference(lastMenstrualDate.timeInMillis) ?: 0
                        if (daysDifference < PregnantWomen.LMP_THRESHOLD_DAYS) {
                            viewModel.isPregnancyTooEarlyToAccess = true
                            // Hide health risk & reset
                            formGenerator
                                .getViewByTag(
                                    PregnantWomen.ID_HEALTH_RISK_SCREENING + formGenerator.rootSuffix,
                                )?.gone()
                            (
                                formGenerator.getViewByTag(
                                    PregnantWomen.ID_HEALTH_RISK_SCREENING,
                                ) as? ViewGroup
                            )?.run {
                                children.forEach { healthRiskFields ->
                                    formGenerator.resetChildViews(healthRiskFields)
                                    healthRiskFields.gone()
                                }
                            }
                            // Hide rest of the form fields other than lmp and too early messages
                            (
                                formGenerator.getViewByTag(
                                    PregnantWomen.ID_PREGNANCY_DETAILS_AND_HISTORY,
                                ) as? ViewGroup
                            )?.run {
                                children.forEach { pregnancyField ->
                                    if (pregnancyField.tag !in listOf(
                                            PregnantWomen.ID_LMP + formGenerator.rootSuffix,
                                            PregnantWomen.ID_TOO_EARLY_TITLE + formGenerator.rootSuffix,
                                            PregnantWomen.ID_TOO_EARLY_DESC1 + formGenerator.rootSuffix,
                                            PregnantWomen.ID_TOO_EARLY_DESC2 + formGenerator.rootSuffix,
                                        )
                                    ) {
                                        formGenerator.resetChildViews(pregnancyField)
                                        pregnancyField.gone()
                                    }
                                }
                            }
                            // Show too early details
                            formGenerator
                                .getViewByTag(
                                    PregnantWomen.ID_TOO_EARLY_TITLE + formGenerator.rootSuffix,
                                )?.visible()
                            (
                                formGenerator.getViewByTag(
                                    PregnantWomen.ID_TOO_EARLY_TITLE,
                                ) as? TextView
                            )?.textSizeSsp = PregnantWomen.SSP_18
                            formGenerator
                                .getViewByTag(
                                    PregnantWomen.ID_TOO_EARLY_DESC1 + AssessmentDefinedParams.rootSuffix,
                                )?.visible()
                            formGenerator
                                .getViewByTag(
                                    PregnantWomen.ID_TOO_EARLY_DESC2 + AssessmentDefinedParams.rootSuffix,
                                )?.visible()
                        } else {
                            viewModel.isPregnancyTooEarlyToAccess = false
                            // Hide too early messages
                            listOf(
                                PregnantWomen.ID_TOO_EARLY_TITLE + formGenerator.rootSuffix,
                                PregnantWomen.ID_TOO_EARLY_DESC1 + formGenerator.rootSuffix,
                                PregnantWomen.ID_TOO_EARLY_DESC2 + formGenerator.rootSuffix,
                            ).forEach {
                                formGenerator.getViewByTag(it)?.gone()
                            }

                            // EDD
                            updateEDD(lastMenstrualDate)

                            // Gestational Week
                            updateGestationalWeek(lastMenstrualDate)

                            // Pregnancy test
                            formGenerator
                                .getViewByTag(
                                    PregnantWomen.ID_PREGNANCY_TEST + formGenerator.rootSuffix,
                                )?.visible()

                            // Gravida
                            formGenerator
                                .getViewByTag(
                                    PregnantWomen.ID_GRAVIDA + formGenerator.rootSuffix,
                                )?.visible()

                            // Show health risk
                            formGenerator.showHideCardFamily(
                                true,
                                PregnantWomen.ID_HEALTH_RISK_SCREENING,
                            )

                            // Show medical conditions
                            formGenerator
                                .getViewByTag(
                                    PregnantWomen.ID_CURRENT_MEDICAL_CONDITIONS + formGenerator.rootSuffix,
                                )?.visible()
                        }
                    }

                    PregnantWomen.ID_GRAVIDA -> {
                        fixObstetricComplications()
                        val gravida = resultHashMap[PregnantWomen.ID_GRAVIDA] as? Double
                            ?: return@FormGenerator
                        // Parity should be less than gravida (as gravida includes parity + current birth)
                        formGenerator.getFormLayout(PregnantWomen.ID_PARITY)?.maxValue = gravida - 1
                    }

                    PregnantWomen.ID_PARITY -> {
                        fixObstetricComplications()
                        val parity = resultHashMap[PregnantWomen.ID_PARITY] as? Double
                            ?: return@FormGenerator
                        // Living children can be less than or equal to parity
                        formGenerator
                            .getFormLayout(PregnantWomen.ID_LIVING_CHILDREN)
                            ?.maxValue = parity
                    }
                }
            },
        )
        viewModel.getFormData(MenuConstants.PREGNANT_WOMEN_PROFILE)
    }

    /**
     * Calculates gestational week and updates UI
     */
    private fun updateGestationalWeek(lastMenstrualDate: Calendar) {
        val gestationalAgeWeekString = formatGestationalAge(
            DateUtils.calculateGestationalAge(
                lastMenstrualDate,
            ),
            requireContext(),
        )
        formGenerator
            .getViewByTag(
                PregnantWomen.ID_GESTATIONAL_WEEK_TITLE + formGenerator.rootSuffix,
            )?.visible()
        formGenerator
            .getViewByTag(
                PregnantWomen.ID_GESTATIONAL_WEEK + formGenerator.rootSuffix,
            )?.visible()
        (
            formGenerator.getViewByTag(
                PregnantWomen.ID_GESTATIONAL_WEEK,
            ) as? TextView
        )?.text = gestationalAgeWeekString
    }

    /**
     * Calculates EDD and updates the map and UI
     */
    private fun updateEDD(lastMenstrualDate: Calendar) {
        val estimatedDeliveryDate =
            DateUtils.calculateEstimatedDeliveryDate(lastMenstrualDate)
        val formattedEstimatedDeliveryDate =
            DateUtils.getDateFormat().format(estimatedDeliveryDate.time)
        formGenerator
            .getViewByTag(
                PregnantWomen.ID_EDD_TITLE + formGenerator.rootSuffix,
            )?.visible()
        formGenerator
            .getViewByTag(
                PregnantWomen.ID_EDD + formGenerator.rootSuffix,
            )?.visible()
        (
            formGenerator.getViewByTag(
                PregnantWomen.ID_EDD,
            ) as? TextView
        )?.text = formattedEstimatedDeliveryDate
    }

    /**
     * Returns difference between gravida and parity
     */
    private fun getGravidaParityDiff(): Double {
        val resultHashMap = formGenerator.getResultMap()
        val gravida = resultHashMap[PregnantWomen.ID_GRAVIDA] as? Double ?: 0.0
        val parity = resultHashMap[PregnantWomen.ID_PARITY] as? Double ?: 0.0
        return gravida - parity
    }

    /**
     * If selected obstetric complications contains complications to ignore
     * when gravida parity difference is <= 1,
     * then it removes those selected ignored complications from the result and updates the UI
     */
    private fun fixObstetricComplications() {
        val id = PregnantWomen.ID_OBSTETRIC_COMPLICATIONS
        val formLayout = formGenerator.getFormLayout(id) ?: return

        @Suppress("UNCHECKED_CAST")
        val resultMap = formGenerator.getResult(id) as? ArrayList<HashMap<String, Any>> ?: return
        val gravidaParityDiff = getGravidaParityDiff()
        if (gravidaParityDiff <= PregnantWomen.GRAVIDA_PARITY_IGNORE_DIFF) {
            resultMap.removeIf {
                PregnantWomen.OBSTETRIC_COMPLICATIONS_TO_IGNORE.contains(it[DefinedParams.Value])
            }
        }
        formGenerator.validateCheckboxDialogue(id, formLayout, resultMap, false)
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
        val gravidaParityDiff = getGravidaParityDiff()
        val inputData = arrayListOf<SignsAndSymptomsEntity>()
        formLayout.optionsList?.forEachIndexed { index, it ->
            val name = it[DefinedParams.NAME] as String
            val value = it[DefinedParams.Value] as String
            if (id == PregnantWomen.ID_OBSTETRIC_COMPLICATIONS &&
                gravidaParityDiff < PregnantWomen.GRAVIDA_PARITY_IGNORE_DIFF &&
                PregnantWomen.OBSTETRIC_COMPLICATIONS_TO_IGNORE.contains(value)
            ) {
                return@forEachIndexed
            }
            inputData.add(
                SignsAndSymptomsEntity(
                    _id = it[DefinedParams.ID] as? Long ?: index.toLong(),
                    symptom = name,
                    type = it["type"] as String,
                    value = value,
                    displayOrder = it["displayOrder"] as? Int,
                    displayValue = it[DefinedParams.cultureValue] as? String,
                ),
            )
        }
        CheckBoxDialog
            .newInstance(
                id,
                resultMap,
                title = formLayout.hint,
                inputData = inputData,
            ) { map ->
                formGenerator.validateCheckboxDialogue(id, formLayout, map)
            }.show(childFragmentManager, CheckBoxDialog.TAG)
    }

    override fun onInstructionClicked(
        id: String,
        title: String,
        informationList: ArrayList<String>?,
        description: String?,
        dosageListModel: ArrayList<RecommendedDosageListModel>?,
    ) {
    }

    override fun onFormSubmit(
        resultMap: HashMap<String, Any>?,
        serverData: List<FormLayout>?,
    ) {
        resultMap?.let {
            val result = serverData?.let {
                FormResultComposer().groupValues(
                    serverData = it,
                    resultMap,
                    MenuConstants.PREGNANT_WOMEN_PROFILE,
                )
            }
            result?.second?.let {
                viewModel.saveAssessment(serverData, it, null, viewModel.menuId)
            }
        }
        viewModel.setAnalyticsData(
            UserDetail.startDateTime,
            eventType = AnalyticsDefinedParams.PREGNANT_WOMEN_PROFILE,
            eventName = AnalyticsDefinedParams.AssessmentCreation,
        )
    }

    override fun onRenderingComplete() {
    }

    override fun onUpdateInstruction(
        id: String,
        selectedId: Any?,
    ) {
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
    }

    override fun onClick(view: View) {
        when (view.id) {
            binding.btnSubmit.id -> {
                withLocationCheck({
                    viewModel.setUserJourney(AnalyticsDefinedParams.SUBMITBUTTONTRIGGERED)
                    // Since it is early pregnancy no need to access anything. Just stop the flow
                    if (viewModel.isPregnancyTooEarlyToAccess) {
                        viewModel.updatePregnantWomanAssessmentDetails()
                    } else {
                        viewModel.fetchCurrentLocation(requireContext())
                        formGenerator.formSubmitAction(view)
                    }
                })
            }
        }
    }

    /**
     * Returns whether user has filled any details already or not.
     *
     * Based on this check, when user pressing back, we show alert for confirmation
     */
    fun getCurrentAnsweredStatus(): Boolean = formGenerator.getResultMap().isNotEmpty()

    companion object {
        const val TAG = "AssessmentPregnantWomenRegistrationFragment"
    }
}
