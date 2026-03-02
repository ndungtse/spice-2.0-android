package com.medtroniclabs.spice.ui.assessment.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import com.medtroniclabs.spice.R
import com.medtroniclabs.spice.app.analytics.utils.AnalyticsDefinedParams
import com.medtroniclabs.spice.common.DefinedParams
import com.medtroniclabs.spice.common.SecuredPreference
import com.medtroniclabs.spice.data.model.RecommendedDosageListModel
import com.medtroniclabs.spice.databinding.FragmentAssessmentBinding
import com.medtroniclabs.spice.formgeneration.FormGenerator
import com.medtroniclabs.spice.formgeneration.extension.safeClickListener
import com.medtroniclabs.spice.formgeneration.listener.FormEventListener
import com.medtroniclabs.spice.formgeneration.model.FormLayout
import com.medtroniclabs.spice.formgeneration.ui.FormResultComposer
import com.medtroniclabs.spice.formgeneration.utility.CheckBoxDialog
import com.medtroniclabs.spice.formgeneration.utility.InformationLayoutFragment
import com.medtroniclabs.spice.network.resource.ResourceState
import com.medtroniclabs.spice.ui.BaseFragment
import com.medtroniclabs.spice.ui.assessment.AssessmentDefinedParams
import com.medtroniclabs.spice.ui.assessment.AssessmentDefinedParams.Contraceptive
import com.medtroniclabs.spice.ui.assessment.referrallogic.ReferralResultGenerator
import com.medtroniclabs.spice.ui.assessment.viewmodel.AssessmentViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AssessmentFamilyPlanningFragment : BaseFragment(), FormEventListener, View.OnClickListener {
    private lateinit var binding: FragmentAssessmentBinding
    private lateinit var formGenerator: FormGenerator
    private val viewModel: AssessmentViewModel by activityViewModels()

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
        getFormDataForWorkflow()
        setListeners()
        attachObservers()
        viewModel.setUserJourney(AnalyticsDefinedParams.FAMILYPLANNING)
    }

    private fun initView() {
        replaceFragmentInId<BioDataFragment>(
            binding.bioDataFragmentContainer.id,
            tag = BioDataFragment.TAG,
        )
        formGenerator = FormGenerator(
            requireContext(),
            binding.llForm,
            this,
            binding.scrollView,
            translate = SecuredPreference.getIsTranslationEnabled(),
            callback = { resultMap, fieldId ->
                if (fieldId == AssessmentDefinedParams.NumberOfLivingChildren || fieldId == AssessmentDefinedParams.DesireForChildrenInFuture) {
                    updateCounsellingMessages(resultMap)
                }
            },
        )
    }

    private fun setListeners() {
        binding.btnSubmit.safeClickListener(this)
    }

    private fun attachObservers() {
        viewModel.formLayoutsLiveData.observe(viewLifecycleOwner) { resourceState ->
            when (resourceState.state) {
                ResourceState.LOADING -> {
                    showProgress()
                }

                ResourceState.SUCCESS -> {
                    hideProgress()
                    resourceState.data?.let { data ->
                        val filteredFormLayout = data.formLayout.filterNot {
                            it.id == AssessmentDefinedParams.ReferralFacilityType
                        }
                        formGenerator.populateViews(filteredFormLayout)
                    }
                }

                ResourceState.ERROR -> {
                    hideProgress()
                }
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
        CheckBoxDialog
            .newInstance(id, resultMap, title = getString(R.string.methods)) { resultMap ->
                formGenerator.validateCheckboxDialogue(id, formLayout, resultMap)
            }.show(childFragmentManager, CheckBoxDialog.TAG)
    }

    override fun onInstructionClicked(
        id: String,
        title: String,
        informationList: ArrayList<String>?,
        description: String?,
        dosageListModel: ArrayList<RecommendedDosageListModel>?,
    ) {
        showInstructionDialog(id)
    }

    override fun onFormSubmit(
        resultMap: HashMap<String, Any>?,
        serverData: List<FormLayout>?,
    ) {
        resultMap?.let { details ->
            val referralResult = ReferralResultGenerator().calculateFamilyPlanningStatus(details)
            val result = serverData?.let {
                FormResultComposer().groupValues(
                    serverData = it,
                    details,
                    DefinedParams.familyPlanning.lowercase(),
                )
            }
            result?.second?.let {
                viewModel.setUserJourney(AnalyticsDefinedParams.SUBMITBUTTONTRIGGERED)
                viewModel.saveAssessment(serverData, it, referralResult, viewModel.menuId)
            }
        }
    }

    override fun onRenderingComplete() {
        formGenerator.getResultMap().let { resultMap ->
            updateCounsellingMessages(resultMap)
        }
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

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.btnSubmit -> {
                withLocationCheck({
                    viewModel.fetchCurrentLocation(requireContext())
                    formGenerator.formSubmitAction(view)
                })
            }
        }
    }

    private fun getFormDataForWorkflow() {
        viewModel.getFormData(AssessmentDefinedParams.Family_Planning)
        viewModel.getNearestHealthFacility()
    }

    companion object {
        const val TAG = "AssessmentFamilyPlanningFragment"
    }

    private fun showInstructionDialog(id: String) {
        val titleById = getTitleById(id)
        when (id) {
            Contraceptive -> {
                InformationLayoutFragment
                    .newInstance(id, titleById)
                    .show(childFragmentManager, InformationLayoutFragment.TAG)
            }
        }
    }

    private fun getTitleById(id: String): String {
        return when (id) {
            Contraceptive -> return getString(R.string.job_aid_contraceptive)
            else -> ""
        }
    }

    fun getCurrentAnsweredStatus(): Boolean = formGenerator.getResultMap().isNotEmpty()

    /**
     * Updates counselling messages visibility based on number of living children
     * and desire for children in future.
     *
     * Business Rules:
     * 1. SARCs: Show if (Children = 0) OR (Desire = "yes_within_2_yrs")
     * 2. LARCs: Show if (Children ≥ 1) OR (Desire = "yes_after_2_yrs")
     * 3. Permanent: Show if (Children ≥ 2) OR (Desire = "no_more_children")
     * 4. Special: If Desire = "unsure", ignore desire and use only children logic
     */
    private fun updateCounsellingMessages(resultMap: HashMap<String, Any>) {
        // Extract number of living children
        val numChildren = when (val childrenValue = resultMap[AssessmentDefinedParams.NumberOfLivingChildren]) {
            is Number -> childrenValue.toInt()
            is String -> childrenValue.toIntOrNull() ?: 0
            else -> 0
        }

        // Extract desire for children in future
        val desire = resultMap[AssessmentDefinedParams.DesireForChildrenInFuture] as? String

        // Handle unsure special case - ignore desire field
        val isUnsure = desire == AssessmentDefinedParams.DesireUnsure

        // Evaluate conditions with OR logic
        val showSARCs = if (isUnsure) {
            // Unsure case: use only children logic
            numChildren == 0
        } else {
            // Normal case: (Children = 0) OR (Desire = "yes_within_2_yrs")
            numChildren == 0 || desire == AssessmentDefinedParams.DesireYesWithin2Yrs
        }

        val showLARCs = if (isUnsure) {
            // Unsure case: use only children logic
            numChildren >= 1
        } else {
            // Normal case: (Children ≥ 1) OR (Desire = "yes_after_2_yrs")
            numChildren >= 1 || desire == AssessmentDefinedParams.DesireYesAfter2Yrs
        }

        val showPermanent = if (isUnsure) {
            // Unsure case: use only children logic
            numChildren >= 2
        } else {
            // Normal case: (Children ≥ 2) OR (Desire = "no_more_children")
            numChildren >= 2 || desire == AssessmentDefinedParams.DesireNoMore
        }

        // Update visibility of counselling message views
        updateCounsellingMessageVisibility(AssessmentDefinedParams.CounsellingMessageSarcs, showSARCs)
        updateCounsellingMessageVisibility(AssessmentDefinedParams.CounsellingMessageLarcs, showLARCs)
        updateCounsellingMessageVisibility(AssessmentDefinedParams.CounsellingMessagePermanent, showPermanent)
    }

    /**
     * Updates the visibility of a counselling message view.
     *
     * @param messageId The ID of the counselling message field
     * @param show True to show (VISIBLE), false to hide (GONE)
     */
    private fun updateCounsellingMessageVisibility(
        messageId: String,
        show: Boolean,
    ) {
        val viewTag = messageId + AssessmentDefinedParams.rootSuffix
        val view = formGenerator.getViewByTag(viewTag)
        view?.visibility = if (show) View.VISIBLE else View.GONE
    }
}
